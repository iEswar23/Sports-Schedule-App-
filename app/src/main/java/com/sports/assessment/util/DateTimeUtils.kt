package com.sports.assessment.util

import android.os.Build
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {
    private const val ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    /**
     * Converts an ISO-8601 timestamp (e.g., 2020-09-13T17:00:00Z)
     * to a local timezone display time string.
     */
    fun formatToLocalTime(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return ""
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatToLocalTimeApi26(timestamp)
        } else {
            formatToLocalTimeLegacy(timestamp)
        }
    }

    /**
     * Converts an ISO-8601 timestamp (e.g., 2020-09-13T17:00:00Z)
     * to a local timezone display date string.
     */
    fun formatToLocalDate(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return ""
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatToLocalDateApi26(timestamp)
        } else {
            formatToLocalDateLegacy(timestamp)
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    private fun formatToLocalTimeApi26(timestamp: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
            val zonedDateTime = ZonedDateTime.parse(timestamp)
            zonedDateTime.format(formatter)
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatToLocalTimeLegacy(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat(ISO_PATTERN, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date: Date? = inputFormat.parse(timestamp)
            val outputFormat = SimpleDateFormat("h:mm a", Locale.US).apply {
                timeZone = TimeZone.getDefault()
            }
            if (date != null) outputFormat.format(date) else ""
        } catch (e: Exception) {
            ""
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    private fun formatToLocalDateApi26(timestamp: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)
            val zonedDateTime = ZonedDateTime.parse(timestamp)
            zonedDateTime.format(formatter)
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatToLocalDateLegacy(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat(ISO_PATTERN, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date: Date? = inputFormat.parse(timestamp)
            val outputFormat = SimpleDateFormat("EEE, MMM d", Locale.US).apply {
                timeZone = TimeZone.getDefault()
            }
            if (date != null) outputFormat.format(date) else ""
        } catch (e: Exception) {
            ""
        }
    }
}
