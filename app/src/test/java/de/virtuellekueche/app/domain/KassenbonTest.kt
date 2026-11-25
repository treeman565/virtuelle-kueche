package de.virtuellekueche.app.domain

import de.virtuellekueche.app.data.model.Einheit
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class KassenbonTest {

    private val antwort = """
        Hier ist das Ergebnis:
        ```json
        {
          "supermarkt": "REWE",
          "datum": "2026-11-24",
          "summe": 7.47,
          "positionen": [
            {"bonText": "VOLLMILCH 3,5% 1L", "name": "Vollmilch 3,5%", "menge": 2, "einheit": "PACKUNG", "preis": 2.38, "lebensmittel": true},
            {"bonText": "BANANEN", "name": "Bananen", "menge": 0.812, "einheit": "kilogramm", "preis": 1.38, "lebensmittel": true},
            {"bonText": "SPUELMITTEL", "name": "Spülmittel", "menge": 1, "einheit": "STUECK", "preis": "1,49", "lebensmittel": false},
            {"bonText": "UNLESBAR", "name": null, "menge": null, "einheit": "UNBEKANNT", "preis": null}
          ]
        }
        ```
    """.trimIndent()

    @Test fun parstAntwortMitCodeblock() {
        val e = BonParser.parse(antwort)
        assertEquals("REWE", e.supermarkt)
        assertEquals(Datum.ausTag(LocalDate.of(2026, 11, 24)), e.datum)
        assertEquals(747L, e.summeCent)
        assertEquals(4, e.positionen.size)

        val milch = e.positionen[0]
        assertEquals("Vollmilch 3,5%", milch.name)
        assertEquals(2.0, milch.menge, 0.0)
        assertEquals(Einheit.PACKUNG, milch.einheit)
        assertEquals(238L, milch.preisCent)

        assertEquals(Einheit.KILOGRAMM, e.positionen[1].einheit)
        assertEquals(149L, e.positionen[2].preisCent)
        assertFalse(e.positionen[2].lebensmittel)
    }

    @Test fun fehlendeWerteBekommenStandard() {
        val p = BonParser.parse(antwort).positionen[3]
        assertEquals("UNLESBAR", p.name)
        assertEquals(1.0, p.menge, 0.0)
        assertEquals(Einheit.STUECK, p.einheit)
        assertNull(p.preisCent)
    }

    @Test fun nullWerteImKopf() {
        val e = BonParser.parse("""{"supermarkt": null, "datum": "kaputt", "positionen": []}""")
        assertNull(e.supermarkt)
        assertNull(e.datum)
        assertNull(e.summeCent)
        assertEquals(0, e.positionen.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun keinJsonWirftFehler() {
        BonParser.parse("Ich kann den Bon leider nicht lesen.")
    }

    @Test fun lagerortVorschlaege() {
        assertEquals(LagerortArt.KUEHLSCHRANK, LagerortVorschlag.vorschlagen("Vollmilch 3,5%"))
        assertEquals(LagerortArt.TIEFKUEHLER, LagerortVorschlag.vorschlagen("TK Rahmspinat"))
        assertEquals(LagerortArt.TIEFKUEHLER, LagerortVorschlag.vorschlagen("Tiefkühl-Pizza Salami"))
        assertEquals(LagerortArt.VORRATSSCHRANK, LagerortVorschlag.vorschlagen("Spaghetti"))
        assertEquals(LagerortArt.KUEHLSCHRANK, LagerortVorschlag.vorschlagen("Gouda Käse"))
    }

    @Test fun suessesUndHaushalt() {
        assertEquals(LagerortArt.SUESSIGKEITEN, LagerortVorschlag.vorschlagen("Milka Alpenmilch Schokolade"))
        assertEquals(LagerortArt.SUESSIGKEITEN, LagerortVorschlag.vorschlagen("Haribo Goldbären"))
        assertEquals(LagerortArt.HAUSHALT, LagerortVorschlag.vorschlagen("Zewa Küchenrolle"))
        assertEquals(LagerortArt.HAUSHALT, LagerortVorschlag.vorschlagen("Toilettenpapier 3-lagig"))
        // Von der KI als Nicht-Lebensmittel markiert, auch ohne bekanntes Stichwort.
        assertEquals(LagerortArt.HAUSHALT, LagerortVorschlag.vorschlagen("Grillanzünder", lebensmittel = false))
    }
}
