package com.sports.assessment.domain.stats

import com.sports.assessment.domain.model.GameDomain
import com.sports.assessment.domain.model.GameType
import com.sports.assessment.domain.model.ScheduleDomain
import com.sports.assessment.domain.model.SeasonPhase

/**
 * Builds the "Season at a glance" summary from a schedule. Pure Kotlin, no Android dependencies.
 *
 * Rules:
 * - Only [GameType.FINAL] games count. Scheduled games, bye weeks and unknown types are ignored.
 * - Preseason games are exhibitions and are ignored entirely.
 * - The outcome comes from the scores (my team vs opponent): higher wins, lower loses, equal ties.
 * - A final game with a missing, blank, negative or non-numeric score is skipped (counted in
 *   [SeasonStats.skippedGames]) rather than guessed.
 * - Regular-season games feed the record, home/away splits and points; postseason games feed
 *   [SeasonStats.postseasonRecord]. Streak and last result use both, in feed order (the feed lists
 *   sections and games chronologically).
 */
object SeasonStatsCalculator {

    fun calculate(schedule: ScheduleDomain): SeasonStats =
        calculate(schedule.sections.flatMap { it.games })

    fun calculate(games: List<GameDomain>): SeasonStats {
        var record = Record.EMPTY
        var home = Record.EMPTY
        var away = Record.EMPTY
        var postseason = Record.EMPTY
        var pointsFor = 0
        var pointsAgainst = 0
        var skipped = 0
        val completed = mutableListOf<CompletedGame>()

        for (game in games) {
            if (game.type != GameType.FINAL || game.phase == SeasonPhase.PRESEASON) continue

            val mine = parseScore(game.myScore)
            val theirs = parseScore(game.opponentScore)
            if (mine == null || theirs == null) {
                skipped++
                continue
            }

            val outcome = when {
                mine > theirs -> GameOutcome.WIN
                mine < theirs -> GameOutcome.LOSS
                else -> GameOutcome.TIE
            }
            completed += CompletedGame(game, outcome, pointsFor = mine, pointsAgainst = theirs)

            if (game.phase == SeasonPhase.POSTSEASON) {
                postseason += outcome
            } else {
                record += outcome
                if (game.isHome) home += outcome else away += outcome
                pointsFor += mine
                pointsAgainst += theirs
            }
        }

        return SeasonStats(
            record = record,
            homeRecord = home,
            awayRecord = away,
            pointsFor = pointsFor,
            pointsAgainst = pointsAgainst,
            postseasonRecord = postseason,
            streak = currentStreak(completed),
            lastResult = completed.lastOrNull(),
            skippedGames = skipped,
        )
    }

    internal fun parseScore(score: String?): Int? =
        score?.trim()?.toIntOrNull()?.takeIf { it >= 0 }

    private fun currentStreak(completed: List<CompletedGame>): Streak? {
        val latest = completed.lastOrNull() ?: return null
        val length = completed.asReversed().takeWhile { it.outcome == latest.outcome }.size
        return Streak(latest.outcome, length)
    }
}
