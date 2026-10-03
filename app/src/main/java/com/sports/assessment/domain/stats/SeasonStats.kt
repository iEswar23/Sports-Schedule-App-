package com.sports.assessment.domain.stats

import com.sports.assessment.domain.model.GameDomain

enum class GameOutcome(val code: Char) {
    WIN('W'), LOSS('L'), TIE('T')
}

/** A won-lost(-tied) record. Ties are only shown when there is at least one. */
data class Record(
    val wins: Int = 0,
    val losses: Int = 0,
    val ties: Int = 0,
) {
    val gamesPlayed: Int get() = wins + losses + ties

    operator fun plus(outcome: GameOutcome): Record = when (outcome) {
        GameOutcome.WIN -> copy(wins = wins + 1)
        GameOutcome.LOSS -> copy(losses = losses + 1)
        GameOutcome.TIE -> copy(ties = ties + 1)
    }

    /** "13-3", or "8-7-1" when the record contains ties. */
    fun formatted(): String = if (ties > 0) "$wins-$losses-$ties" else "$wins-$losses"

    companion object {
        val EMPTY = Record()
    }
}

/** Consecutive identical outcomes ending with the most recent completed game, e.g. "W3". */
data class Streak(
    val outcome: GameOutcome,
    val length: Int,
) {
    fun formatted(): String = "${outcome.code}$length"
}

/** A final game whose scores could be read. */
data class CompletedGame(
    val game: GameDomain,
    val outcome: GameOutcome,
    val pointsFor: Int,
    val pointsAgainst: Int,
)

/**
 * Season summary computed by [SeasonStatsCalculator].
 *
 * [record], [homeRecord], [awayRecord], [pointsFor] and [pointsAgainst] cover the regular season
 * only (the same convention as league standings). Postseason games are tallied separately in
 * [postseasonRecord]. [streak] and [lastResult] follow every counted game, regular season and
 * postseason, so they always describe the team's most recent form.
 */
data class SeasonStats(
    val record: Record,
    val homeRecord: Record,
    val awayRecord: Record,
    val pointsFor: Int,
    val pointsAgainst: Int,
    val postseasonRecord: Record,
    val streak: Streak?,
    val lastResult: CompletedGame?,
    /** Final games that were ignored because a score was missing, blank or not a number. */
    val skippedGames: Int,
) {
    val pointDifferential: Int get() = pointsFor - pointsAgainst

    val hasResults: Boolean get() = lastResult != null

    companion object {
        val EMPTY = SeasonStats(
            record = Record.EMPTY,
            homeRecord = Record.EMPTY,
            awayRecord = Record.EMPTY,
            pointsFor = 0,
            pointsAgainst = 0,
            postseasonRecord = Record.EMPTY,
            streak = null,
            lastResult = null,
            skippedGames = 0,
        )
    }
}
