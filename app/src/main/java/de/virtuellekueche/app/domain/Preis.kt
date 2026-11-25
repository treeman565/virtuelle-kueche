package de.virtuellekueche.app.domain

/** Preise werden als Cent gespeichert, damit keine Rundungsfehler entstehen. */
object Preis {

    /** Liest "1,99", "1.99", "2", "2 €" oder "0,5" ein. Leer ergibt null, Unsinn ebenfalls null. */
    fun parseCent(text: String): Long? {
        val t = text.replace("€", "").replace(" ", "").trim()
        if (t.isEmpty()) return null
        val m = Regex("^(\\d{1,6})(?:[.,](\\d{1,2}))?$").matchEntire(t) ?: return null
        val euro = m.groupValues[1].toLong()
        val centText = m.groupValues[2]
        val cent = when (centText.length) {
            0 -> 0L
            1 -> centText.toLong() * 10
            else -> centText.toLong()
        }
        return euro * 100 + cent
    }

    /** 199 -> "1,99 €" */
    fun format(cent: Long): String = "%d,%02d €".format(cent / 100, cent % 100)

    /** 199 -> "1,99" (für Eingabefelder) */
    fun formatEingabe(cent: Long): String = "%d,%02d".format(cent / 100, cent % 100)
}
