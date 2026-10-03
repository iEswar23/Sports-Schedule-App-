package com.sports.assessment.data.mapper

import com.sports.assessment.data.remote.dto.DateDto
import com.sports.assessment.data.remote.dto.GameDto
import com.sports.assessment.domain.model.GameType
import com.sports.assessment.domain.model.SeasonPhase
import com.sports.assessment.testing.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class ScheduleMapperTest {

    private val schedule = TestData.fixtureSchedule()
    private val allGames = schedule.sections.flatMap { it.games }

    @Test
    fun `team header is mapped from the feed`() {
        val team = schedule.team!!
        assertEquals("PACKERS", team.name)
        assertEquals("Green Bay", team.city)
        assertEquals("GB", team.triCode)
        assertEquals("13-3", team.record)
        assertEquals("https://s3.amazonaws.com/yc-app-resources/nfl/logos/nfl_gb_light.png", team.logoUrl)
    }

    @Test
    fun `sections keep feed order and get a season phase from their heading`() {
        assertEquals(listOf("REGULAR SEASON", "POSTSEASON"), schedule.sections.map { it.heading })
        assertEquals(17, schedule.sections[0].games.size)
        assertEquals(1, schedule.sections[1].games.size)
        assertTrue(schedule.sections[0].games.all { it.phase == SeasonPhase.REGULAR_SEASON })
        assertEquals(SeasonPhase.POSTSEASON, schedule.sections[1].games.single().phase)
    }

    @Test
    fun `game types map from S, F and B`() {
        assertEquals(16, allGames.count { it.type == GameType.FINAL })
        assertEquals(1, allGames.count { it.type == GameType.BYE })
        assertEquals(1, allGames.count { it.type == GameType.SCHEDULED })
    }

    @Test
    fun `away game scores are flipped so myScore is always my team`() {
        val week1 = allGames.first { it.week == "Week 1" }
        // Feed: AwayScore 43 (GB), HomeScore 34 (MIN) at U.S. Bank Stadium.
        assertFalse(week1.isHome)
        assertEquals("43", week1.myScore)
        assertEquals("34", week1.opponentScore)
        assertEquals("MIN", week1.opponent?.triCode)
        assertEquals("U.S. Bank Stadium", week1.venue)
    }

    @Test
    fun `home game scores map directly`() {
        val week2 = allGames.first { it.week == "Week 2" }
        assertTrue(week2.isHome)
        assertEquals("42", week2.myScore)
        assertEquals("21", week2.opponentScore)
    }

    @Test
    fun `kickoff is kept as an instant and labels are rendered in the requested zone`() {
        val week3 = allGames.first { it.week == "Week 3" }
        assertEquals(Instant.parse("2020-09-28T00:20:00Z"), week3.kickoff)
        // Sunday night game: still Sunday in Central time even though it is Monday in UTC.
        assertEquals("Sun, Sep 27", week3.date)
        assertEquals("7:20 PM", week3.time)

        val utc = TestData.fixtureSchedule(ZoneId.of("UTC")).sections[0].games.first { it.week == "Week 3" }
        assertEquals("Mon, Sep 28", utc.date)
        assertEquals("12:20 AM", utc.time)
    }

    @Test
    fun `bye week gets a stable unique id instead of the feed's zero id`() {
        val bye = allGames.single { it.type == GameType.BYE }
        assertEquals("REGULAR SEASON#4", bye.id)
        assertNull(bye.opponent)
        assertNull(bye.kickoff)
        assertEquals(allGames.size, allGames.map { it.id }.toSet().size)
    }

    @Test
    fun `postseason game keeps TV, venue and blank scores`() {
        val playoff = schedule.sections[1].games.single()
        assertEquals("2021011600", playoff.id)
        assertEquals("FOX", playoff.tvStation)
        assertEquals("Lambeau Field", playoff.venue)
        assertEquals("", playoff.myScore)
        assertEquals(TestData.PLAYOFF_KICKOFF, playoff.kickoff)
        assertEquals("Rams".uppercase(), playoff.opponent?.name)
    }

    @Test
    fun `missing or malformed timestamp falls back to the feed's text labels`() {
        val dto = GameDto(
            id = 7,
            type = "S",
            date = DateDto(text = "September 13, 2020", time = "TBD", timestamp = "not-a-date")
        )
        val game = dto.toDomain(zone = TestData.CENTRAL)

        assertNull(game.kickoff)
        assertEquals("September 13, 2020", game.date)
        assertEquals("TBD", game.time)
    }

    @Test
    fun `unknown type and missing fields map to safe defaults`() {
        val game = GameDto(type = "X").toDomain(fallbackId = "fallback")

        assertEquals(GameType.UNKNOWN, game.type)
        assertEquals("fallback", game.id)
        assertFalse(game.isHome)
        assertEquals("", game.week)
        assertEquals("", game.date)
        assertNull(game.opponent)
    }

    @Test
    fun `season phase is derived from common heading spellings`() {
        assertEquals(SeasonPhase.PRESEASON, SeasonPhase.fromHeading("PRESEASON"))
        assertEquals(SeasonPhase.PRESEASON, SeasonPhase.fromHeading("Pre-Season"))
        assertEquals(SeasonPhase.POSTSEASON, SeasonPhase.fromHeading("POSTSEASON"))
        assertEquals(SeasonPhase.POSTSEASON, SeasonPhase.fromHeading("Playoffs"))
        assertEquals(SeasonPhase.REGULAR_SEASON, SeasonPhase.fromHeading("REGULAR SEASON"))
        assertEquals(SeasonPhase.REGULAR_SEASON, SeasonPhase.fromHeading(null))
    }
}
