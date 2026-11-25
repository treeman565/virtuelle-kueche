package de.virtuellekueche.app.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DatumTest {
    private val heute = Datum.ausTag(LocalDate.of(2026, 11, 25))

    @Test fun hinUndZurueck() {
        assertEquals(LocalDate.of(2026, 11, 25), Datum.zuTag(heute))
        assertEquals("25.11.2026", Datum.format(heute))
    }

    @Test fun ablaufTexte() {
        assertEquals("läuft heute ab", Datum.ablaufText(heute, heute))
        assertEquals("läuft morgen ab", Datum.ablaufText(Datum.ausTag(LocalDate.of(2026, 11, 26)), heute))
        assertEquals("noch 7 Tage", Datum.ablaufText(Datum.ausTag(LocalDate.of(2026, 12, 2)), heute))
        assertEquals("seit gestern abgelaufen", Datum.ablaufText(Datum.ausTag(LocalDate.of(2026, 11, 24)), heute))
        assertEquals("seit 3 Tagen abgelaufen", Datum.ablaufText(Datum.ausTag(LocalDate.of(2026, 11, 22)), heute))
    }

    @Test fun ablaufKurzform() {
        fun am(j: Int, m: Int, t: Int) = Datum.ablaufKurz(Datum.ausTag(LocalDate.of(j, m, t)), heute)
        assertEquals("0" to "Tage", am(2026, 11, 25))
        assertEquals("1" to "Tag", am(2026, 11, 26))
        assertEquals("9" to "Tage", am(2026, 12, 4))
        assertEquals("11" to "Mon.", am(2027, 10, 25))
        assertEquals("!" to "abgel.", am(2026, 11, 20))
    }
}
