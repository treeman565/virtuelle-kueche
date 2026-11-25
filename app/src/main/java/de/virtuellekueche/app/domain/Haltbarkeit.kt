package de.virtuellekueche.app.domain

import java.util.concurrent.TimeUnit

/** Art des Lagerorts, damit die Schätzung auch für selbst angelegte Orte funktioniert. */
enum class LagerortArt {
    KUEHLSCHRANK,
    TIEFKUEHLER,
    VORRATSSCHRANK,
    SUESSIGKEITEN,
    /** Haushaltsartikel wie Klopapier oder Spülmittel: kein Verfallsdatum. */
    HAUSHALT,
    /** Selbst angelegte Lagerorte. */
    SONSTIGE,
}

/**
 * Schätzt eine typische Haltbarkeit anhand von Produktname und Lagerort.
 * Das Ergebnis ist nur ein Vorschlag und bleibt immer von Hand änderbar.
 */
object Haltbarkeit {

    private data class Regel(val stichwoerter: List<String>, val tage: Map<LagerortArt, Int>)

    private fun r(vararg w: String, k: Int, t: Int = 90, v: Int? = null) = Regel(
        w.toList(),
        buildMap {
            put(LagerortArt.KUEHLSCHRANK, k)
            put(LagerortArt.TIEFKUEHLER, t)
            if (v != null) put(LagerortArt.VORRATSSCHRANK, v)
        },
    )

    // Reihenfolge zählt: spezifische Stichwörter vor allgemeinen.
    private val regeln = listOf(
        r("hackfleisch", "hack", "gehacktes", k = 1, t = 90),
        r("fisch", "lachs", "garnele", k = 2, t = 120),
        r("hähnchen", "huhn", "geflügel", "pute", k = 2, t = 180),
        r("fleisch", "steak", "schnitzel", "wurst", "schinken", "salami", k = 4, t = 180),
        r("frischmilch", "milch", k = 7, t = 90),
        r("sahne", "joghurt", "quark", "schmand", k = 7, t = 60),
        r("butter", k = 30, t = 180),
        r("käse", "gouda", "mozzarella", "feta", k = 14, t = 90),
        r("ei", "eier", k = 21, t = 0, v = 14),
        r("salat", "spinat", "rucola", k = 4, t = 180),
        r("beere", "erdbeer", "himbeer", k = 3, t = 240),
        r("tomate", "gurke", "paprika", "zucchini", k = 7, t = 180, v = 5),
        r("apfel", "äpfel", "birne", "zitrone", "orange", k = 21, t = 240, v = 10),
        r("banane", k = 5, t = 90, v = 5),
        r("kartoffel", "zwiebel", "knoblauch", k = 21, t = 180, v = 30),
        r("brot", "brötchen", "toast", k = 7, t = 90, v = 4),
        r("nudel", "pasta", "spaghetti", "reis", "mehl", "zucker", k = 365, t = 365, v = 365),
        r("konserve", "dose", k = 730, t = 730, v = 730),
    )

    private val standardTage = mapOf(
        LagerortArt.KUEHLSCHRANK to 7,
        LagerortArt.TIEFKUEHLER to 90,
        LagerortArt.VORRATSSCHRANK to 180,
        LagerortArt.SUESSIGKEITEN to 180,
        LagerortArt.SONSTIGE to 30,
    )

    /** Geschätzte Haltbarkeit in Tagen ab Kaufdatum, null = kein Verfallsdatum (Haushaltsartikel). */
    fun schaetzeTage(name: String, art: LagerortArt): Int? {
        if (art == LagerortArt.HAUSHALT) return null
        val woerter = name.lowercase().split(Regex("[^\\p{L}]+")).filter { it.isNotEmpty() }
        for (regel in regeln) {
            val passt = regel.stichwoerter.any { s ->
                // Kurze Stichwörter (z. B. "ei") nur als ganzes Wort, sonst auch als Wortteil.
                if (s.length <= 3) woerter.any { it == s } else woerter.any { it.contains(s) }
            }
            if (passt) {
                val tage = regel.tage[art]
                if (tage != null && tage > 0) return tage
            }
        }
        return standardTage.getValue(art)
    }

    /** Geschätztes Verfallsdatum (Epoch-Millis), null = kein Verfallsdatum. */
    fun schaetzeVerfallsdatum(name: String, art: LagerortArt, kaufdatum: Long): Long? =
        schaetzeTage(name, art)?.let { kaufdatum + TimeUnit.DAYS.toMillis(it.toLong()) }
}
