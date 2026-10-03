package com.sports.assessment.domain.schedule

import com.sports.assessment.domain.model.GameType
import com.sports.assessment.domain.model.GameDomain
import com.sports.assessment.domain.model.ScheduleDomain
import java.time.Duration
import java.time.Instant

sealed interface NextGameState {
    /** The next game kicks off in [timeToKickoff] (always positive). */
    data class Upcoming(val game: GameDomain, val timeToKickoff: Duration) : NextGameState

    /** Kickoff has passed but the game is not final and is still inside [GameTiming.LIVE_WINDOW]. */
    data class Live(val game: GameDomain) : NextGameState

    /** Every game on the schedule is in the past. */
    data object SeasonComplete : NextGameState

    /** Nothing to show: an empty schedule, or remaining games have no kickoff time yet. */
    data object Unavailable : NextGameState
}

/** Picks the game for the "Next game" card relative to [now]. Pure Kotlin. */
object NextGameFinder {

    fun find(schedule: ScheduleDomain, now: Instant): NextGameState {
        val games = schedule.sections.flatMap { it.games }.filter { it.type != GameType.BYE }
        if (games.isEmpty()) return NextGameState.Unavailable

        val next = games
            .filter { it.kickoff != null && GameTiming.isUpcoming(it, now) }
            .minByOrNull { it.kickoff!! }

        if (next == null) {
            // Remaining games without a kickoff time mean the season is not over, just unscheduled.
            val hasUnscheduled = games.any { GameTiming.isUpcoming(it, now) }
            return if (hasUnscheduled) NextGameState.Unavailable else NextGameState.SeasonComplete
        }

        val kickoff = next.kickoff!!
        return if (now.isBefore(kickoff)) {
            NextGameState.Upcoming(next, Duration.between(now, kickoff))
        } else {
            NextGameState.Live(next)
        }
    }
}
