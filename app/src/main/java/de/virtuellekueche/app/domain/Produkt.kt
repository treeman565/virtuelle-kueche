package de.virtuellekueche.app.domain

import de.virtuellekueche.app.data.model.Einheit
import java.time.LocalDate
import org.json.JSONObject

/** Was die KI auf einem Produktfoto erkannt hat. Alles kann fehlen. */
data class ProduktErgebnis(
    val name: String?,
    val menge: Double?,
    val einheit: Einheit?,
    /** Aufgedrucktes Mindesthaltbarkeits- oder Verbrauchsdatum, Mitternacht UTC (siehe [Datum]). */
    val mhd: Long?,
    val lebensmittel: Boolean,
)

object ProduktPrompt {
    val text = """
        Auf dem Foto ist ein einzelnes Produkt aus dem Supermarkt, meist eine Verpackung.
        Antworte ausschließlich mit JSON in genau diesem Format, ohne weiteren Text:
        {
          "name": "kurzer deutscher Produktname mit Marke, z. B. Weihenstephan Frische Vollmilch 3,5%",
          "menge": 1,
          "einheit": "STUECK | PACKUNG | GRAMM | KILOGRAMM | MILLILITER | LITER",
          "mhd": "JJJJ-MM-TT oder null",
          "lebensmittel": true
        }
        Regeln:
        - menge und einheit beschreiben den Inhalt der Packung, z. B. 500 und GRAMM oder 1 und LITER.
        - Bei mehreren gleichen Stück in der Packung (z. B. 10 Eier) menge 10 und einheit STUECK.
        - "mhd" nur angeben, wenn ein Datum wie "mindestens haltbar bis" oder "zu verbrauchen bis" auf dem Foto lesbar ist.
          Fehlt der Tag (z. B. "06.2027"), nimm den letzten Tag des Monats.
        - "lebensmittel" ist false für alles, was man nicht isst oder trinkt (z. B. Klopapier, Spülmittel).
        - Unlesbare Werte als null angeben, nichts erfinden.
    """.trimIndent()
}

object ProduktParser {

    /** Wirft IllegalArgumentException, wenn die Antwort kein JSON enthält. */
    fun parse(antwort: String): ProduktErgebnis {
        val start = antwort.indexOf('{')
        val ende = antwort.lastIndexOf('}')
        require(start >= 0 && ende > start) { "Keine JSON-Antwort" }
        val o = JSONObject(antwort.substring(start, ende + 1))

        fun text(key: String): String? =
            if (o.isNull(key)) null else o.optString(key).trim().takeIf { it.isNotEmpty() && it != "null" }

        return ProduktErgebnis(
            name = text("name"),
            menge = if (o.isNull("menge")) null else o.optDouble("menge", Double.NaN).takeIf { it.isFinite() && it > 0 },
            einheit = text("einheit")?.let { e -> Einheit.entries.firstOrNull { it.name.equals(e, ignoreCase = true) } },
            mhd = text("mhd")?.let { runCatching { Datum.ausTag(LocalDate.parse(it)) }.getOrNull() },
            lebensmittel = o.optBoolean("lebensmittel", true),
        )
    }
}
