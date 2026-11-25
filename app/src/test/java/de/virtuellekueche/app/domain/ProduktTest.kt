package de.virtuellekueche.app.domain

import de.virtuellekueche.app.data.model.Einheit
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ProduktTest {
    @Test fun vollstaendigeAntwort() {
        val p = ProduktParser.parse(
            """```json
            {"name": "Weihenstephan Frische Vollmilch 3,5%", "menge": 1, "einheit": "LITER", "mhd": "2026-12-03", "lebensmittel": true}
            ```""",
        )
        assertEquals("Weihenstephan Frische Vollmilch 3,5%", p.name)
        assertEquals(1.0, p.menge!!, 0.0)
        assertEquals(Einheit.LITER, p.einheit)
        assertEquals(Datum.ausTag(LocalDate.of(2026, 12, 3)), p.mhd)
    }

    @Test fun fehlendeWerteBleibenLeer() {
        val p = ProduktParser.parse("""{"name": "Zewa Wisch und Weg", "menge": null, "einheit": "UNBEKANNT", "mhd": null, "lebensmittel": false}""")
        assertEquals("Zewa Wisch und Weg", p.name)
        assertNull(p.menge)
        assertNull(p.einheit)
        assertNull(p.mhd)
        assertFalse(p.lebensmittel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun keinJson() {
        ProduktParser.parse("Das kann ich nicht erkennen.")
    }
}
