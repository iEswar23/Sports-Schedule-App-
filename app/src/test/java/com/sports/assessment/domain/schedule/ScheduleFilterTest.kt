package com.sports.assessment.domain.schedule

import com.sports.assessment.domain.model.GameType
import com.sports.assessment.testing.TestData
import com.sports.assessment.testing.TestData.bye
import com.sports.assessment.testing.TestData.game
import com.sports.assessment.testing.TestData.loss
import com.sports.assessment.testing.TestData.scheduled
import com.sports.assessment.testing.TestData.schedule
import com.sports.assessment.testing.TestData.win
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class ScheduleFilterTest {

    private val now = Instant.parse("2021-01-14T19:35:00Z")

    private val sample = schedule(
        "REGULAR SEASON" to listOf(
            win("1", isHome = false),
            win("2", isHome = true),
            bye("3"),
            loss("4", isHome = true),
        ),
        "POSTSEASON" to listOf(
            scheduled("5", kickoff = now.plus(Duration.ofDays(2)), isHome = true),
        ),
    )

    private fun ids(filter: ScheduleFilterType, source: com.sports.assessment.domain.model.ScheduleDomain = sample) =
        ScheduleFilter.apply(source, filter, now).sections.associate { section -> section.heading to section.games.map { it.id } }

    @Test
    fun `all returns the schedule untouched including bye weeks`() {
        assertSame(sample, ScheduleFilter.apply(sample, ScheduleFilterType.ALL, now))
    }

    @Test
    fun `home keeps home games played or not and drops byes`() {
        assertEquals(
            mapOf("REGULAR SEASON" to listOf("2", "4"), "POSTSEASON" to listOf("5")),
            ids(ScheduleFilterType.HOME)
        )
    }

    @Test
    fun `away keeps away games and hides sections that become empty`() {
        assertEquals(mapOf("REGULAR SEASON" to listOf("1")), ids(ScheduleFilterType.AWAY))
    }

    @Test
    fun `results keeps only final games`() {
        assertEquals(mapOf("REGULAR SEASON" to listOf("1", "2", "4")), ids(ScheduleFilterType.RESULTS))
    }

    @Test
    fun `upcoming keeps only games that have not finished`() {
        assertEquals(mapOf("POSTSEASON" to listOf("5")), ids(ScheduleFilterType.UPCOMING))
    }

    @Test
    fun `upcoming is empty once every kickoff is long past`() {
        val later = now.plus(Duration.ofDays(30))
        assertTrue(ScheduleFilter.apply(sample, ScheduleFilterType.UPCOMING, later).sections.isEmpty())
    }

    @Test
    fun `filter keeps team and section order`() {
        val filtered = ScheduleFilter.apply(sample, ScheduleFilterType.HOME, now)
        assertEquals(sample.team, filtered.team)
        assertEquals(listOf("REGULAR SEASON", "POSTSEASON"), filtered.sections.map { it.heading })
    }

    @Test
    fun `bye weeks only match the all filter`() {
        val byeWeek = bye("9")
        ScheduleFilterType.entries.forEach { filter ->
            assertEquals(filter.name, filter == ScheduleFilterType.ALL, ScheduleFilter.matches(byeWeek, filter, now))
        }
    }

    @Test
    fun `recorded feed - results has 16 games and upcoming has the playoff game before kickoff`() {
        val fixture = TestData.fixtureSchedule()

        val results = ScheduleFilter.apply(fixture, ScheduleFilterType.RESULTS, TestData.FEED_GENERATED_AT)
        assertEquals(listOf("REGULAR SEASON"), results.sections.map { it.heading })
        assertEquals(16, results.sections.single().games.size)

        val upcoming = ScheduleFilter.apply(fixture, ScheduleFilterType.UPCOMING, TestData.FEED_GENERATED_AT)
        assertEquals("Divisional Playoffs", upcoming.sections.single().games.single().week)
    }

    @Test
    fun `recorded feed - home and away split the 16 games 8 and 8 plus the home playoff game`() {
        val fixture = TestData.fixtureSchedule()
        val home = ScheduleFilter.apply(fixture, ScheduleFilterType.HOME, TestData.FEED_GENERATED_AT)
        val away = ScheduleFilter.apply(fixture, ScheduleFilterType.AWAY, TestData.FEED_GENERATED_AT)

        assertEquals(9, home.sections.sumOf { it.games.size })
        assertEquals(8, away.sections.sumOf { it.games.size })
        assertTrue(away.sections.flatMap { it.games }.none { it.type == GameType.BYE })
    }

    @Test
    fun `isUpcoming covers TBD, in-progress and finished games`() {
        val kickoff = Instant.parse("2021-01-16T21:35:00Z")
        val game = scheduled("1", kickoff = kickoff)

        assertTrue(GameTiming.isUpcoming(scheduled("tbd", kickoff = null), now))
        assertTrue(GameTiming.isUpcoming(game, kickoff.minusSeconds(1)))
        assertTrue(GameTiming.isUpcoming(game, kickoff.plus(Duration.ofHours(3))))
        assertFalse(GameTiming.isUpcoming(game, kickoff.plus(GameTiming.LIVE_WINDOW)))
        assertFalse(GameTiming.isUpcoming(game(id = "f", kickoff = kickoff.plus(Duration.ofDays(1))), now))
    }
}
