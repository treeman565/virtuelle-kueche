package de.virtuellekueche.app.domain

import de.virtuellekueche.app.data.model.Einheit
import java.time.LocalDate
import org.json.JSONObject

/** Eine erkannte Zeile vom Kassenbon. */
data class BonPosition(
    /** Text so, wie er auf dem Bon steht (für die lernende Korrektur in Schritt 6). */
    val bonText: String,
    /** Lesbarer Produktname, z. B. "Vollmilch" statt "VOLLM.3,5% 1L". */
    val name: String,
    val menge: Double,
    val einheit: Einheit,
    val preisCent: Long?,
    /** false für Pfand, Tüten, Drogerieartikel usw. */
    val lebensmittel: Boolean,
)

data class BonErgebnis(
    val supermarkt: String?,
    /** Mitternacht UTC (siehe [Datum]) oder null, wenn nicht lesbar. */
    val datum: Long?,
    val summeCent: Long?,
    val positionen: List<BonPosition>,
)

/** Anweisung an das KI-Modell. Für Gemini und Claude gleich. */
object BonPrompt {
    val text = """
        Du liest einen deutschen Kassenbon von einem Supermarkt auf dem Foto.
        Antworte ausschließlich mit JSON in genau diesem Format, ohne weiteren Text:
        {
          "supermarkt": "Name des Geschäfts oder null",
          "datum": "JJJJ-MM-TT oder null",
          "summe": 12.34,
          "positionen": [
            {
              "bonText": "Text genau wie auf dem Bon",
              "name": "verständlicher deutscher Produktname, z. B. Vollmilch 3,5%",
              "menge": 1,
              "einheit": "STUECK | PACKUNG | GRAMM | KILOGRAMM | MILLILITER | LITER",
              "preis": 1.99,
              "lebensmittel": true
            }
          ]
        }
        Regeln:
        - "preis" ist der Gesamtpreis der Zeile in Euro. Rabatte direkt vom Produkt darüber abziehen.
        - Steht "2 x 0,99", dann ist menge 2 und preis 1.98.
        - Bei Gewichtsware (z. B. "0,512 kg x 3,99 EUR/kg") ist menge 0.512 und einheit KILOGRAMM.
        - Sonst menge = Anzahl der Packungen und einheit = PACKUNG oder STUECK.
        - Pfand, Pfandrückgabe, Tüten und Zwischensummen weglassen.
        - "lebensmittel" ist false für alles, was man nicht isst oder trinkt (z. B. Spülmittel, Batterien).
        - Unlesbare Werte als null angeben, nichts erfinden.
    """.trimIndent()
}

object BonParser {

    /** Liest die JSON-Antwort des Modells. Wirft IllegalArgumentException bei unbrauchbarer Antwort. */
    fun parse(antwort: String): BonErgebnis {
        val start = antwort.indexOf('{')
        val ende = antwort.lastIndexOf('}')
        require(start >= 0 && ende > start) { "Keine JSON-Antwort" }
        val json = JSONObject(antwort.substring(start, ende + 1))

        val positionen = json.optJSONArray("positionen")?.let { arr ->
            (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let(::position) }
        }.orEmpty()

        return BonErgebnis(
            supermarkt = json.textOderNull("supermarkt"),
            datum = json.textOderNull("datum")?.let { runCatching { Datum.ausTag(LocalDate.parse(it)) }.getOrNull() },
            summeCent = json.euroAlsCent("summe"),
            positionen = positionen,
        )
    }

    private fun position(o: JSONObject): BonPosition? {
        val bonText = o.textOderNull("bonText")
        val name = o.textOderNull("name") ?: bonText ?: return null
        val einheit = o.textOderNull("einheit")
            ?.let { e -> Einheit.entries.firstOrNull { it.name.equals(e.trim(), ignoreCase = true) } }
            ?: Einheit.STUECK
        val menge = o.optDouble("menge", 1.0).takeIf { it.isFinite() && it > 0 } ?: 1.0
        return BonPosition(
            bonText = bonText ?: name,
            name = name,
            menge = menge,
            einheit = einheit,
            preisCent = o.euroAlsCent("preis"),
            lebensmittel = o.optBoolean("lebensmittel", true),
        )
    }

    private fun JSONObject.textOderNull(key: String): String? =
        if (isNull(key)) null else optString(key).trim().takeIf { it.isNotEmpty() && it != "null" }

    private fun JSONObject.euroAlsCent(key: String): Long? {
        if (isNull(key)) return null
        val wert = optDouble(key, Double.NaN)
        if (wert.isNaN()) return optString(key).let(Preis::parseCent)
        return Math.round(wert * 100)
    }
}

/** Schlägt vor, wo ein Produkt vom Bon eingeräumt wird. */
object LagerortVorschlag {
    private val tiefkuehl = listOf("tk ", "tk-", "tiefkühl", "tiefgefr", "gefrier", "eiscreme", "speiseeis", "rahmspinat", "fischstäbchen", "pommes")
    private val kuehl = listOf(
        "milch", "joghurt", "jogurt", "quark", "sahne", "schmand", "butter", "käse", "kaese", "mozzarella",
        "feta", "fleisch", "hack", "wurst", "schinken", "salami", "hähnchen", "lachs", "fisch", "aufschnitt",
        "salat", "rucola", "spinat", "beeren", "erdbeer", "tofu", "frischkäse", "eier", "margarine", "pudding",
    )

    fun vorschlagen(name: String): LagerortArt {
        val n = " ${name.lowercase()} "
        return when {
            tiefkuehl.any { n.contains(it) } || n.startsWith(" tk") -> LagerortArt.TIEFKUEHLER
            kuehl.any { n.contains(it) } -> LagerortArt.KUEHLSCHRANK
            else -> LagerortArt.VORRATSSCHRANK
        }
    }
}
