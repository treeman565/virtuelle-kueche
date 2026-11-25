package de.virtuellekueche.app.ui.bon

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import de.virtuellekueche.app.data.BonErkennung
import de.virtuellekueche.app.data.GeminiBonErkennung
import de.virtuellekueche.app.data.VorratRepository
import de.virtuellekueche.app.data.model.Kassenbon
import de.virtuellekueche.app.data.model.KassenbonPosition
import de.virtuellekueche.app.data.model.Lagerort
import de.virtuellekueche.app.data.model.Lebensmittel
import de.virtuellekueche.app.domain.BonErgebnis
import de.virtuellekueche.app.domain.Datum
import de.virtuellekueche.app.domain.Haltbarkeit
import de.virtuellekueche.app.domain.LagerortArt
import de.virtuellekueche.app.domain.LagerortVorschlag
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Eine Bonzeile im Prüfbildschirm. */
data class Entwurf(
    val bonText: String,
    val lebensmittel: Lebensmittel,
    val uebernehmen: Boolean,
)

sealed interface BonZustand {
    data object Start : BonZustand
    data class Erkennen(val foto: Bitmap) : BonZustand
    data class Pruefen(
        val supermarkt: String,
        val datum: Long,
        val summeCent: Long?,
        val entwuerfe: List<Entwurf>,
    ) : BonZustand
    data class Fehler(val text: String) : BonZustand
    data class Fertig(val anzahl: Int) : BonZustand
}

class BonViewModel(
    private val repo: VorratRepository = VorratRepository(),
    private val erkennung: BonErkennung = GeminiBonErkennung(),
) : ViewModel() {

    private val _zustand = MutableStateFlow<BonZustand>(BonZustand.Start)
    val zustand: StateFlow<BonZustand> = _zustand.asStateFlow()

    val lagerorte: StateFlow<List<Lagerort>> = repo.lagerorteFlow()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun fotoErkennen(foto: Bitmap) {
        _zustand.value = BonZustand.Erkennen(foto)
        viewModelScope.launch {
            _zustand.value = try {
                val ergebnis = erkennung.erkennen(foto)
                if (ergebnis.positionen.isEmpty()) {
                    BonZustand.Fehler("Auf dem Foto wurden keine Produkte erkannt. Versuch es mit einem schärferen Foto, auf dem der ganze Bon zu sehen ist.")
                } else {
                    pruefZustand(ergebnis)
                }
            } catch (e: Exception) {
                BonZustand.Fehler(fehlertext(e))
            }
        }
    }

    fun fotoFehler() {
        _zustand.value = BonZustand.Fehler("Das Foto konnte nicht geladen werden.")
    }

    private fun pruefZustand(ergebnis: BonErgebnis): BonZustand.Pruefen {
        val datum = ergebnis.datum ?: Datum.heute()
        val orte = lagerorte.value
        val entwuerfe = ergebnis.positionen.map { pos ->
            val art = LagerortVorschlag.vorschlagen(pos.name, pos.lebensmittel)
            val ort = orte.firstOrNull { it.art == art } ?: orte.firstOrNull()
            Entwurf(
                bonText = pos.bonText,
                lebensmittel = Lebensmittel(
                    name = pos.name,
                    menge = pos.menge,
                    einheit = pos.einheit,
                    lagerortId = ort?.id.orEmpty(),
                    kaufdatum = datum,
                    preisCent = pos.preisCent,
                    verfallsdatum = Haltbarkeit.schaetzeVerfallsdatum(pos.name, ort?.art ?: LagerortArt.SONSTIGE, datum),
                    verfallsdatumGeschaetzt = true,
                ),
                uebernehmen = true,
            )
        }
        return BonZustand.Pruefen(ergebnis.supermarkt.orEmpty(), datum, ergebnis.summeCent, entwuerfe)
    }

    fun supermarktAendern(text: String) = aendern { it.copy(supermarkt = text) }

    fun datumAendern(datum: Long) = aendern { p ->
        p.copy(
            datum = datum,
            entwuerfe = p.entwuerfe.map { e ->
                val lm = e.lebensmittel
                val art = lagerorte.value.firstOrNull { it.id == lm.lagerortId }?.art ?: LagerortArt.SONSTIGE
                e.copy(
                    lebensmittel = lm.copy(
                        kaufdatum = datum,
                        verfallsdatum = if (lm.verfallsdatumGeschaetzt) Haltbarkeit.schaetzeVerfallsdatum(lm.name, art, datum) else lm.verfallsdatum,
                    ),
                )
            },
        )
    }

    fun umschalten(index: Int) = aendern { p ->
        p.copy(entwuerfe = p.entwuerfe.mapIndexed { i, e -> if (i == index) e.copy(uebernehmen = !e.uebernehmen) else e })
    }

    fun entwurfAendern(index: Int, lm: Lebensmittel) = aendern { p ->
        p.copy(entwuerfe = p.entwuerfe.mapIndexed { i, e -> if (i == index) e.copy(lebensmittel = lm, uebernehmen = true) else e })
    }

    fun lagerortAnlegen(name: String) = repo.lagerortAnlegen(name, lagerorte.value.size)

    fun uebernehmen() {
        val p = _zustand.value as? BonZustand.Pruefen ?: return
        val bon = Kassenbon(
            supermarkt = p.supermarkt.trim().ifEmpty { null },
            datum = p.datum,
            summeCent = p.summeCent,
            positionen = p.entwuerfe.map { e ->
                KassenbonPosition(
                    bonText = e.bonText,
                    name = e.lebensmittel.name,
                    menge = e.lebensmittel.menge,
                    einheit = e.lebensmittel.einheit,
                    preisCent = e.lebensmittel.preisCent,
                )
            },
        )
        repo.kassenbonSpeichern(bon, p.entwuerfe.map { if (it.uebernehmen) it.lebensmittel else null })
        _zustand.value = BonZustand.Fertig(p.entwuerfe.count { it.uebernehmen })
    }

    fun neuStarten() { _zustand.value = BonZustand.Start }

    private fun aendern(block: (BonZustand.Pruefen) -> BonZustand.Pruefen) =
        _zustand.update { if (it is BonZustand.Pruefen) block(it) else it }

    private fun fehlertext(e: Exception): String = when {
        e is FirebaseNetworkException || e is IOException || e.cause is IOException ->
            "Zum Lesen des Bons brauchst du Internet. Versuch es noch einmal, sobald du Netz hast."
        e is IllegalArgumentException ->
            "Der Bon konnte nicht gelesen werden. Versuch es mit einem schärferen, gut beleuchteten Foto."
        else -> "Das Lesen hat nicht geklappt: ${e.message ?: e::class.simpleName}"
    }
}
