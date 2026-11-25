package de.virtuellekueche.app.domain

object Menge {
    /** Liest "1", "0,5" oder "1.5" ein. */
    fun parse(text: String): Double? =
        text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 && it.isFinite() }

    /** 1.0 -> "1", 0.5 -> "0,5" */
    fun format(menge: Double): String =
        if (menge % 1.0 == 0.0) menge.toLong().toString()
        else "%.2f".format(java.util.Locale.GERMANY, menge).trimEnd('0').trimEnd(',')
}
