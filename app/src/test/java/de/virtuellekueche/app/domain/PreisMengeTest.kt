package de.virtuellekueche.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PreisMengeTest {
    @Test fun preisEinlesen() {
        assertEquals(199L, Preis.parseCent("1,99"))
        assertEquals(199L, Preis.parseCent("1.99"))
        assertEquals(200L, Preis.parseCent("2"))
        assertEquals(50L, Preis.parseCent("0,5"))
        assertEquals(1249L, Preis.parseCent(" 12,49 € "))
        assertNull(Preis.parseCent(""))
        assertNull(Preis.parseCent("abc"))
        assertNull(Preis.parseCent("1,999"))
    }

    @Test fun preisFormatieren() {
        assertEquals("1,99 €", Preis.format(199))
        assertEquals("0,05 €", Preis.format(5))
        assertEquals("12,00", Preis.formatEingabe(1200))
    }

    @Test fun mengeEinlesenUndFormatieren() {
        assertEquals(0.5, Menge.parse("0,5")!!, 0.0)
        assertNull(Menge.parse("-1"))
        assertNull(Menge.parse("x"))
        assertEquals("1", Menge.format(1.0))
        assertEquals("0,5", Menge.format(0.5))
        assertEquals("1,25", Menge.format(1.25))
    }
}
