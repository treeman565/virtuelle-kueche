package de.virtuellekueche.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.virtuellekueche.app.data.model.Lagerort
import de.virtuellekueche.app.data.model.Lebensmittel
import de.virtuellekueche.app.data.model.LebensmittelStatus
import de.virtuellekueche.app.domain.LagerortArt
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Lebensmittel und Lagerorte des angemeldeten Benutzers.
 *
 * Schreibvorgänge werden nicht abgewartet: Firestore speichert sie sofort im
 * lokalen Cache und synchronisiert, sobald Netz da ist. So funktioniert die
 * App auch im Supermarkt ohne Empfang.
 */
class VorratRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    private val uid: String
        get() = auth.currentUser?.uid ?: error("Nicht angemeldet")

    private fun user() = db.collection("users").document(uid)
    private fun lagerorte() = user().collection("lagerorte")
    private fun lebensmittel() = user().collection("lebensmittel")

    fun lagerorteFlow(): Flow<List<Lagerort>> = callbackFlow {
        val reg = lagerorte().orderBy("reihenfolge").addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val liste = snap?.documents.orEmpty().mapNotNull { d ->
                d.toObject(Lagerort::class.java)?.copy(id = d.id)
            }
            trySend(liste)
        }
        awaitClose { reg.remove() }
    }

    /** Nur vorrätige Lebensmittel, sortiert nach Verfallsdatum. */
    fun vorratFlow(): Flow<List<Lebensmittel>> = callbackFlow {
        val reg = lebensmittel()
            .whereEqualTo("status", LebensmittelStatus.VORRAETIG.name)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val liste = snap?.documents.orEmpty().mapNotNull { d ->
                    d.toObject(Lebensmittel::class.java)?.copy(id = d.id)
                }.sortedWith(compareBy(nullsLast()) { it.verfallsdatum })
                trySend(liste)
            }
        awaitClose { reg.remove() }
    }

    /** Legt beim ersten Start die drei Standard-Lagerorte an. */
    fun standardLagerorteAnlegen() {
        val standard = listOf(
            Triple("kuehlschrank", "Kühlschrank", LagerortArt.KUEHLSCHRANK),
            Triple("vorratsschrank", "Vorratsschrank", LagerortArt.VORRATSSCHRANK),
            Triple("tiefkuehler", "Tiefkühler", LagerortArt.TIEFKUEHLER),
        )
        val batch = db.batch()
        standard.forEachIndexed { i, (id, name, art) ->
            batch.set(lagerorte().document(id), Lagerort(id, name, standard = true, art = art, reihenfolge = i))
        }
        batch.commit()
    }

    fun lagerortAnlegen(name: String, reihenfolge: Int) {
        val ref = lagerorte().document()
        ref.set(Lagerort(ref.id, name.trim(), standard = false, art = LagerortArt.SONSTIGE, reihenfolge = reihenfolge))
    }

    fun speichern(lm: Lebensmittel) {
        val ref = if (lm.id.isEmpty()) lebensmittel().document() else lebensmittel().document(lm.id)
        ref.set(lm.copy(id = ref.id))
    }

    fun mengeSetzen(id: String, menge: Double) {
        if (menge <= 0.0) {
            statusSetzen(id, LebensmittelStatus.VERBRAUCHT)
        } else {
            lebensmittel().document(id).update("menge", menge)
        }
    }

    fun statusSetzen(id: String, status: LebensmittelStatus) {
        lebensmittel().document(id).update(
            mapOf("status" to status.name, "statusGeaendert" to System.currentTimeMillis()),
        )
    }

    fun loeschen(id: String) {
        lebensmittel().document(id).delete()
    }
}
