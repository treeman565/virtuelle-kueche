package de.virtuellekueche.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Datumswerte werden als Mitternacht UTC des jeweiligen Kalendertags gespeichert.
 * Das passt zum Material-Datumswähler und vermeidet Zeitzonen-Verschiebungen.
 */
object Datum {
    private val format = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun ausTag(tag: LocalDate): Long = tag.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun zuTag(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    fun heute(zone: ZoneId = ZoneId.systemDefault()): Long = ausTag(LocalDate.now(zone))

    fun format(millis: Long): String = zuTag(millis).format(format)

    /** Tage von heute bis zum Datum. Negativ = schon abgelaufen. */
    fun tageBis(millis: Long, heute: Long = heute()): Long =
        ChronoUnit.DAYS.between(zuTag(heute), zuTag(millis))

    /** "heute", "morgen", "in 5 Tagen", "seit 2 Tagen abgelaufen" */
    fun ablaufText(millis: Long, heute: Long = heute()): String = when (val t = tageBis(millis, heute)) {
        0L -> "läuft heute ab"
        1L -> "läuft morgen ab"
        -1L -> "seit gestern abgelaufen"
        in 2L..Long.MAX_VALUE -> "noch $t Tage"
        else -> "seit ${-t} Tagen abgelaufen"
    }

    /**
     * Kurzform für das Ablauf-Kästchen: Zahl und Einheit, z. B. ("2", "Tage"),
     * ("1", "Tag"), ("11", "Mon.") oder ("!", "abgel.") wenn schon abgelaufen.
     */
    fun ablaufKurz(millis: Long, heute: Long = heute()): Pair<String, String> {
        val t = tageBis(millis, heute)
        return when {
            t < 0 -> "!" to "abgel."
            t == 1L -> "1" to "Tag"
            t <= 60 -> "$t" to "Tage"
            else -> "${Math.round(t / 30.4)}" to "Mon."
        }
    }
}
