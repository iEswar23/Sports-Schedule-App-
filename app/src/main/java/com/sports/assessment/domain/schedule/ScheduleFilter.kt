package com.sports.assessment.domain.schedule

import com.sports.assessment.domain.model.GameDomain
import com.sports.assessment.domain.model.GameType
import com.sports.assessment.domain.model.ScheduleDomain
import java.time.Duration
import java.time.Instant

enum class ScheduleFilterType {
    ALL, HOME, AWAY, RESULTS, UPCOMING
}

/**
 * Narrows a schedule to the games matching a [ScheduleFilterType]. Pure Kotlin.
 *
 * - [ScheduleFilterType.ALL]: everything, including bye weeks.
 * - [ScheduleFilterType.HOME] / [ScheduleFilterType.AWAY]: games by venue, played or not.
 * - [ScheduleFilterType.RESULTS]: final games.
 * - [ScheduleFilterType.UPCOMING]: games that have not finished yet (see [GameTiming.isUpcoming]).
 *
 * Bye weeks only appear under ALL, and sections left without games are dropped.
 */
object ScheduleFilter {

    fun apply(schedule: ScheduleDomain, filter: ScheduleFilterType, now: Instant): ScheduleDomain {
        if (filter == ScheduleFilterType.ALL) return schedule
        val sections = schedule.sections
            .map { section -> section.copy(games = section.games.filter { matches(it, filter, now) }) }
            .filter { it.games.isNotEmpty() }
        return schedule.copy(sections = sections)
    }

    fun matches(game: GameDomain, filter: ScheduleFilterType, now: Instant): Boolean {
        if (filter == ScheduleFilterType.ALL) return true
        if (game.type == GameType.BYE) return false
        return when (filter) {
            ScheduleFilterType.ALL -> true
            ScheduleFilterType.HOME -> game.isHome
            ScheduleFilterType.AWAY -> !game.isHome
            ScheduleFilterType.RESULTS -> game.type == GameType.FINAL
            ScheduleFilterType.UPCOMING -> GameTiming.isUpcoming(game, now)
        }
    }
}

object GameTiming {
    /**
     * How long after kickoff a non-final game is still treated as in progress. NFL games last
     * roughly 3 to 3.5 hours; four hours leaves room for overtime and delays.
     */
    val LIVE_WINDOW: Duration = Duration.ofHours(4)

    /**
     * A game is upcoming while it is not final, not a bye, and either has no known kickoff yet
     * (TBD) or kicked off less than [LIVE_WINDOW] ago.
     */
    fun isUpcoming(game: GameDomain, now: Instant): Boolean {
        if (game.type == GameType.FINAL || game.type == GameType.BYE) return false
        val kickoff = game.kickoff ?: return true
        return now.isBefore(kickoff.plus(LIVE_WINDOW))
    }
}
