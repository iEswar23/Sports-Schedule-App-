package com.sports.assessment.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Kickoff timestamp parsing and formatting. Uses java.time throughout (desugared on API < 26),
 * and takes the [ZoneId] explicitly so results are deterministic in tests.
 */
object DateTimeUtils {
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)

    /** Parses an ISO-8601 instant such as `2020-09-13T17:00:00Z`; null when blank or malformed. */
    fun parseInstant(timestamp: String?): Instant? {
        if (timestamp.isNullOrBlank()) return null
        return try {
            Instant.parse(timestamp.trim())
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /** "1:00 PM" in [zone]. */
    fun formatTime(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        TIME_FORMATTER.format(instant.atZone(zone))

    /** "Sun, Sep 13" in [zone]. */
    fun formatDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        DATE_FORMATTER.format(instant.atZone(zone))
}
