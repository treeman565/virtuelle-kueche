package de.virtuellekueche.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class HaltbarkeitTest {
    @Test fun milchImKuehlschrankEineWoche() =
        assertEquals(7, Haltbarkeit.schaetzeTage("Frische Vollmilch 3,5%", LagerortArt.KUEHLSCHRANK))

    @Test fun hackfleischVorFleisch() =
        assertEquals(1, Haltbarkeit.schaetzeTage("Rinderhackfleisch", LagerortArt.KUEHLSCHRANK))

    @Test fun tiefkuehlerHaeltLaenger() =
        assertEquals(90, Haltbarkeit.schaetzeTage("Rinderhackfleisch", LagerortArt.TIEFKUEHLER))

    @Test fun eiNurAlsGanzesWort() {
        assertEquals(21, Haltbarkeit.schaetzeTage("Eier Freiland", LagerortArt.KUEHLSCHRANK))
        // "Weizenmehl" enthält "ei", darf aber nicht als Ei erkannt werden.
        assertEquals(365, Haltbarkeit.schaetzeTage("Weizenmehl", LagerortArt.VORRATSSCHRANK))
    }

    @Test fun unbekanntNimmtStandard() =
        assertEquals(180, Haltbarkeit.schaetzeTage("Gewürzmischung", LagerortArt.VORRATSSCHRANK))

    @Test fun regelOhneWertFuerLagerortFaelltAufStandard() =
        assertEquals(30, Haltbarkeit.schaetzeTage("Milch", LagerortArt.SONSTIGE))

    @Test fun datumWirdAddiert() {
        val tag = 24L * 60 * 60 * 1000
        assertEquals(1000 + 7 * tag, Haltbarkeit.schaetzeVerfallsdatum("Milch", LagerortArt.KUEHLSCHRANK, 1000))
    }

    @Test fun haushaltOhneVerfallsdatum() {
        org.junit.Assert.assertNull(Haltbarkeit.schaetzeTage("Klopapier", LagerortArt.HAUSHALT))
        org.junit.Assert.assertNull(Haltbarkeit.schaetzeVerfallsdatum("Zewa", LagerortArt.HAUSHALT, 1000))
    }

    @Test fun suessigkeitenHaltenLange() =
        assertEquals(180, Haltbarkeit.schaetzeTage("Gummibärchen", LagerortArt.SUESSIGKEITEN))
}
