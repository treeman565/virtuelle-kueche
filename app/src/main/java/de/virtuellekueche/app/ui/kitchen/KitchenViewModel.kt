package de.virtuellekueche.app.ui.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.virtuellekueche.app.data.VorratRepository
import de.virtuellekueche.app.data.model.Lagerort
import de.virtuellekueche.app.data.model.Lebensmittel
import de.virtuellekueche.app.data.model.LebensmittelStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

data class KitchenUiState(
    val laedt: Boolean = true,
    val lagerorte: List<Lagerort> = emptyList(),
    /** Anzahl vorrätiger Produkte je Lagerort-ID. */
    val anzahl: Map<String, Int> = emptyMap(),
    /** Gefilterte Liste, nach Verfallsdatum sortiert. */
    val liste: List<Lebensmittel> = emptyList(),
    val gesamt: Int = 0,
    val suche: String = "",
    val filterLagerortId: String? = null,
    val fehler: String? = null,
)

class KitchenViewModel(
    private val repo: VorratRepository = VorratRepository(),
) : ViewModel() {

    private val suche = MutableStateFlow("")
    private val filter = MutableStateFlow<String?>(null)
    private var standardGeprueft = false

    private val lagerorte = repo.lagerorteFlow().onEach { liste ->
        if (!standardGeprueft) {
            standardGeprueft = true
            repo.standardLagerorteErgaenzen(liste)
        }
    }

    val state: StateFlow<KitchenUiState> =
        combine(lagerorte, repo.vorratFlow(), suche, filter) { orte, vorrat, s, f ->
            val liste = vorrat
                .filter { f == null || it.lagerortId == f }
                .filter { s.isBlank() || it.name.contains(s.trim(), ignoreCase = true) }
            KitchenUiState(
                laedt = false,
                lagerorte = orte,
                anzahl = vorrat.groupingBy { it.lagerortId }.eachCount(),
                liste = liste,
                gesamt = vorrat.size,
                suche = s,
                filterLagerortId = f,
            )
        }
            .catch { emit(KitchenUiState(laedt = false, fehler = "Daten konnten nicht geladen werden.")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KitchenUiState())

    fun sucheAendern(text: String) { suche.value = text }

    fun filterSetzen(lagerortId: String?) { filter.value = lagerortId }

    fun speichern(lm: Lebensmittel) = repo.speichern(lm)

    fun verbraucht(lm: Lebensmittel) = repo.statusSetzen(lm.id, LebensmittelStatus.VERBRAUCHT)

    fun weggeworfen(lm: Lebensmittel) = repo.statusSetzen(lm.id, LebensmittelStatus.WEGGEWORFEN)

    fun loeschen(lm: Lebensmittel) = repo.loeschen(lm.id)

    fun lagerortAnlegen(name: String) = repo.lagerortAnlegen(name, state.value.lagerorte.size)
}
