package de.virtuellekueche.app.data.model

import de.virtuellekueche.app.domain.LagerortArt

/*
 * Datenmodell (Firestore). Alle Daten liegen unter users/{uid}/...,
 * damit jeder Benutzer nur seine eigenen Daten sieht (siehe firestore.rules).
 *
 *   users/{uid}/lebensmittel/{id}
 *   users/{uid}/lagerorte/{id}
 *   users/{uid}/kassenbons/{id}            (mit Positionen)
 *   users/{uid}/bonZuordnungen/{bonText}   (lernende Korrektur)
 *   users/{uid}/gerichte/{id}
 *   users/{uid}/einkaufsliste/{id}
 *
 * Firestore braucht Standardwerte und einen leeren Konstruktor, daher
 * haben alle Felder Default-Werte. Datumswerte sind Epoch-Millis.
 */

enum class Einheit(val kurz: String, val lang: String) {
    STUECK("Stk.", "Stück"),
    PACKUNG("Pkg.", "Packung"),
    GRAMM("g", "Gramm"),
    KILOGRAMM("kg", "Kilogramm"),
    MILLILITER("ml", "Milliliter"),
    LITER("l", "Liter"),
}

enum class LebensmittelStatus { VORRAETIG, VERBRAUCHT, WEGGEWORFEN }

data class Lagerort(
    val id: String = "",
    val name: String = "",
    /** Kühlschrank, Vorratsschrank, Tiefkühler sind Standard, weitere frei anlegbar. */
    val standard: Boolean = false,
    val art: LagerortArt = LagerortArt.SONSTIGE,
    val reihenfolge: Int = 0,
)

data class Lebensmittel(
    val id: String = "",
    val name: String = "",
    val menge: Double = 1.0,
    val einheit: Einheit = Einheit.STUECK,
    val lagerortId: String = "",
    val kaufdatum: Long = 0L,
    val preisCent: Long? = null,
    val verfallsdatum: Long? = null,
    /** true, wenn das Verfallsdatum geschätzt und nicht von Hand gesetzt wurde. */
    val verfallsdatumGeschaetzt: Boolean = true,
    val status: LebensmittelStatus = LebensmittelStatus.VORRAETIG,
    val statusGeaendert: Long? = null,
    val kassenbonId: String? = null,
)

data class Gericht(
    val id: String = "",
    val name: String = "",
    val zutaten: List<Zutat> = emptyList(),
    val schritte: List<String> = emptyList(),
    val sterne: Int? = null,
    val favorit: Boolean = false,
    val zuletztGekocht: Long? = null,
)

data class Zutat(
    val name: String = "",
    val menge: Double = 0.0,
    val einheit: Einheit = Einheit.STUECK,
)

data class EinkaufslistenEintrag(
    val id: String = "",
    val name: String = "",
    val menge: Double = 1.0,
    val einheit: Einheit = Einheit.STUECK,
    /** null = frei eingegeben, sonst das Gericht, für das die Zutat gebraucht wird. */
    val gerichtId: String? = null,
    val erledigt: Boolean = false,
)
