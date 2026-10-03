package com.sports.assessment.domain.schedule

import com.sports.assessment.domain.model.GameType
import com.sports.assessment.testing.TestData
import com.sports.assessment.testing.TestData.bye
import com.sports.assessment.testing.TestData.game
import com.sports.assessment.testing.TestData.scheduled
import com.sports.assessment.testing.TestData.schedule
import com.sports.assessment.testing.TestData.win
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant

class NextGameFinderTest {

    private val kickoff = Instant.parse("2021-01-16T21:35:00Z")

    @Test
    fun `recorded feed before kickoff - the divisional playoff game is next`() {
        val state = NextGameFinder.find(TestData.fixtureSchedule(), TestData.FEED_GENERATED_AT)

        state as NextGameState.Upcoming
        assertEquals("Divisional Playoffs", state.game.week)
        assertEquals("LAR", state.game.opponent?.triCode)
        assertEquals(Duration.ofDays(2).plusHours(2), state.timeToKickoff)
    }

    @Test
    fun `recorded feed with today's clock - season is complete`() {
        val state = NextGameFinder.find(TestData.fixtureSchedule(), Instant.parse("2026-10-02T12:00:00Z"))

        assertEquals(NextGameState.SeasonComplete, state)
    }

    @Test
    fun `game in progress is live until the live window closes`() {
        val schedule = schedule("REGULAR SEASON" to listOf(scheduled("1", kickoff)))

        assertEquals(NextGameState.Live(schedule.sections[0].games[0]), NextGameFinder.find(schedule, kickoff))
        assertEquals(
            NextGameState.Live(schedule.sections[0].games[0]),
            NextGameFinder.find(schedule, kickoff.plus(Duration.ofHours(3).plusMinutes(59)))
        )
        assertEquals(NextGameState.SeasonComplete, NextGameFinder.find(schedule, kickoff.plus(GameTiming.LIVE_WINDOW)))
    }

    @Test
    fun `earliest remaining kickoff wins regardless of feed order`() {
        val later = scheduled("later", kickoff.plus(Duration.ofDays(7)))
        val sooner = scheduled("sooner", kickoff)
        val state = NextGameFinder.find(schedule("REGULAR SEASON" to listOf(later, sooner)), kickoff.minusSeconds(60))

        assertEquals(NextGameState.Upcoming(sooner, Duration.ofSeconds(60)), state)
    }

    @Test
    fun `final games are never next even if their kickoff is in the future`() {
        val final = game("1", type = GameType.FINAL, kickoff = kickoff)
        val next = scheduled("2", kickoff.plus(Duration.ofDays(7)))
        val state = NextGameFinder.find(schedule("REGULAR SEASON" to listOf(final, next)), kickoff.minusSeconds(3600))

        assertEquals("2", (state as NextGameState.Upcoming).game.id)
    }

    @Test
    fun `remaining games without a kickoff time make the card unavailable rather than complete`() {
        val state = NextGameFinder.find(
            schedule("REGULAR SEASON" to listOf(win("1"), scheduled("2", kickoff = null))),
            kickoff
        )

        assertEquals(NextGameState.Unavailable, state)
    }

    @Test
    fun `schedules without games are unavailable`() {
        assertEquals(NextGameState.Unavailable, NextGameFinder.find(schedule(), kickoff))
        assertEquals(NextGameState.Unavailable, NextGameFinder.find(schedule("REGULAR SEASON" to listOf(bye("1"))), kickoff))
    }

    @Test
    fun `all games final means season complete`() {
        val state = NextGameFinder.find(schedule("REGULAR SEASON" to listOf(win("1"), bye("2"), win("3"))), kickoff)

        assertEquals(NextGameState.SeasonComplete, state)
    }
}
