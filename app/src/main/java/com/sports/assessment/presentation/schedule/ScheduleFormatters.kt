package com.sports.assessment.presentation.schedule

import com.sports.assessment.domain.model.GameType
import java.time.Duration

/** Compact countdown text used in "Kickoff in …". Pure Kotlin so it can be unit tested. */
object CountdownFormatter {

    /**
     * Formats the time until kickoff with the two most significant units, truncating the rest:
     * "2d 4h", "2d", "4h 12m", "4h", "45m", and "<1m" for anything under a minute.
     */
    fun compact(remaining: Duration): String {
        val totalMinutes = remaining.toMinutes()
        if (totalMinutes < 1) return "<1m"

        val days = totalMinutes / MINUTES_PER_DAY
        val hours = (totalMinutes % MINUTES_PER_DAY) / MINUTES_PER_HOUR
        val minutes = totalMinutes % MINUTES_PER_HOUR

        return when {
            days > 0 -> if (hours > 0) "${days}d ${hours}h" else "${days}d"
            hours > 0 -> if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
            else -> "${minutes}m"
        }
    }

    private const val MINUTES_PER_HOUR = 60L
    private const val MINUTES_PER_DAY = 24L * MINUTES_PER_HOUR
}

/** "+140", "-12" or "0". */
fun formatPointDifferential(differential: Int): String =
    if (differential > 0) "+$differential" else differential.toString()

/**
 * Score shown on a game card. A score from the feed is always shown (trimmed); when it is missing,
 * final games fall back to [missingFinalScore] ("0") and games not played yet show [notPlayed] ("–").
 */
fun formatCardScore(score: String?, type: GameType, missingFinalScore: String, notPlayed: String): String = when {
    !score.isNullOrBlank() -> score.trim()
    type == GameType.FINAL -> missingFinalScore
    else -> notPlayed
}
