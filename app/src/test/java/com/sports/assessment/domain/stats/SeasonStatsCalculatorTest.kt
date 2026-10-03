package com.sports.assessment.domain.stats

import com.sports.assessment.domain.model.GameType
import com.sports.assessment.domain.model.SeasonPhase
import com.sports.assessment.testing.TestData
import com.sports.assessment.testing.TestData.bye
import com.sports.assessment.testing.TestData.game
import com.sports.assessment.testing.TestData.loss
import com.sports.assessment.testing.TestData.scheduled
import com.sports.assessment.testing.TestData.tie
import com.sports.assessment.testing.TestData.win
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SeasonStatsCalculatorTest {

    @Test
    fun `recorded 2020 Packers feed produces the real season summary`() {
        val stats = SeasonStatsCalculator.calculate(TestData.fixtureSchedule())

        assertEquals(Record(13, 3), stats.record)
        assertEquals("13-3", stats.record.formatted())
        assertEquals(Record(7, 1), stats.homeRecord)
        assertEquals(Record(6, 2), stats.awayRecord)
        assertEquals(509, stats.pointsFor)
        assertEquals(369, stats.pointsAgainst)
        assertEquals(140, stats.pointDifferential)
        assertEquals(Streak(GameOutcome.WIN, 6), stats.streak)
        assertEquals(Record.EMPTY, stats.postseasonRecord)
        assertEquals(0, stats.skippedGames)
    }

    @Test
    fun `last result in the feed is the week 17 win at Chicago`() {
        val last = SeasonStatsCalculator.calculate(TestData.fixtureSchedule()).lastResult!!

        assertEquals(GameOutcome.WIN, last.outcome)
        assertEquals(35, last.pointsFor)
        assertEquals(16, last.pointsAgainst)
        assertEquals("CHI", last.game.opponent?.triCode)
        assertFalse(last.game.isHome)
        assertEquals("Week 17", last.game.week)
    }

    @Test
    fun `empty schedule yields empty stats`() {
        val stats = SeasonStatsCalculator.calculate(emptyList())

        assertEquals(SeasonStats.EMPTY, stats)
        assertFalse(stats.hasResults)
        assertNull(stats.streak)
        assertEquals(0, stats.pointDifferential)
    }

    @Test
    fun `outcome comes from my score versus opponent score for home and away games`() {
        val stats = SeasonStatsCalculator.calculate(
            listOf(
                win("1", isHome = true, score = 30 to 20),
                loss("2", isHome = false, score = 3 to 7),
                win("3", isHome = false, score = 28 to 27),
            )
        )

        assertEquals(Record(2, 1), stats.record)
        assertEquals(Record(1, 0), stats.homeRecord)
        assertEquals(Record(1, 1), stats.awayRecord)
        assertEquals(61, stats.pointsFor)
        assertEquals(54, stats.pointsAgainst)
    }

    @Test
    fun `equal scores count as a tie and the record shows the tie column`() {
        val stats = SeasonStatsCalculator.calculate(
            listOf(win("1"), loss("2"), tie("3", isHome = false, score = 20))
        )

        assertEquals(Record(1, 1, 1), stats.record)
        assertEquals("1-1-1", stats.record.formatted())
        assertEquals(Record(0, 0, 1), stats.awayRecord)
        assertEquals(Streak(GameOutcome.TIE, 1), stats.streak)
    }

    @Test
    fun `scoreless tie is still a valid result`() {
        val stats = SeasonStatsCalculator.calculate(listOf(tie("1", score = 0)))

        assertEquals(Record(0, 0, 1), stats.record)
        assertEquals(0, stats.pointsFor)
        assertTrue(stats.hasResults)
    }

    @Test
    fun `final games with missing, blank or non numeric scores are skipped and counted`() {
        val stats = SeasonStatsCalculator.calculate(
            listOf(
                win("1"),
                game("2", myScore = null, opponentScore = "10"),
                game("3", myScore = "", opponentScore = "10"),
                game("4", myScore = "  ", opponentScore = "  "),
                game("5", myScore = "abc", opponentScore = "7"),
                game("6", myScore = "-3", opponentScore = "7"),
            )
        )

        assertEquals(Record(1, 0), stats.record)
        assertEquals(5, stats.skippedGames)
        assertEquals(Streak(GameOutcome.WIN, 1), stats.streak)
    }

    @Test
    fun `scores with surrounding whitespace are parsed`() {
        val stats = SeasonStatsCalculator.calculate(listOf(game("1", myScore = " 27 ", opponentScore = "13\n")))

        assertEquals(Record(1, 0), stats.record)
        assertEquals(27, stats.pointsFor)
        assertEquals(13, stats.pointsAgainst)
    }

    @Test
    fun `bye weeks, scheduled and unknown games are ignored`() {
        val stats = SeasonStatsCalculator.calculate(
            listOf(
                win("1"),
                bye("2"),
                scheduled("3", kickoff = Instant.parse("2030-01-01T00:00:00Z")),
                game("4", type = GameType.UNKNOWN, myScore = "50", opponentScore = "0"),
            )
        )

        assertEquals(Record(1, 0), stats.record)
        assertEquals(24, stats.pointsFor)
        assertEquals(0, stats.skippedGames)
    }

    @Test
    fun `scheduled games with scores are not counted until final`() {
        val stats = SeasonStatsCalculator.calculate(
            listOf(game("1", type = GameType.SCHEDULED, myScore = "14", opponentScore = "7"))
        )

        assertEquals(Record.EMPTY, stats.record)
        assertNull(stats.lastResult)
    }

    @Test
    fun `postseason games are tallied separately from the regular season record and points`() {
        val stats = SeasonStatsCalculator.calculate(
            listOf(
                win("1", isHome = true, score = 20 to 10),
                win("2", isHome = true, score = 32 to 18, phase = SeasonPhase.POSTSEASON),
                loss("3", isHome = true, score = 20 to 31, phase = SeasonPhase.POSTSEASON),
            )
        )

        assertEquals(Record(1, 0), stats.record)
        assertEquals(Record(1, 0), stats.homeRecord)
        assertEquals(20, stats.pointsFor)
        assertEquals(10, stats.pointsAgainst)
        assertEquals(Record(1, 1), stats.postseasonRecord)
    }

    @Test
    fun `streak and last result continue into the postseason`() {
        val stats = SeasonStatsCalculator.calculate(
            listOf(
                loss("1"),
                win("2"),
                win("3"),
                win("4", phase = SeasonPhase.POSTSEASON, score = 32 to 18),
            )
        )

        assertEquals(Streak(GameOutcome.WIN, 3), stats.streak)
        assertEquals("W3", stats.streak?.formatted())
        assertEquals("4", stats.lastResult?.game?.id)
        assertEquals(SeasonPhase.POSTSEASON, stats.lastResult?.game?.phase)
    }

    @Test
    fun `preseason games are ignored entirely`() {
        val stats = SeasonStatsCalculator.calculate(
            listOf(
                loss("1", phase = SeasonPhase.PRESEASON),
                game("2", myScore = "", opponentScore = "", phase = SeasonPhase.PRESEASON),
                win("3"),
            )
        )

        assertEquals(Record(1, 0), stats.record)
        assertEquals(Streak(GameOutcome.WIN, 1), stats.streak)
        assertEquals(0, stats.skippedGames)
    }

    @Test
    fun `streak counts consecutive identical outcomes ending with the latest game`() {
        val stats = SeasonStatsCalculator.calculate(
            listOf(win("1"), win("2"), loss("3"), loss("4"), loss("5"))
        )

        assertEquals(Streak(GameOutcome.LOSS, 3), stats.streak)
        assertEquals("L3", stats.streak?.formatted())
    }

    @Test
    fun `a skipped game does not break the streak`() {
        val stats = SeasonStatsCalculator.calculate(
            listOf(win("1"), game("2", myScore = null, opponentScore = null), win("3"))
        )

        assertEquals(Streak(GameOutcome.WIN, 2), stats.streak)
        assertEquals(1, stats.skippedGames)
    }

    @Test
    fun `negative point differential`() {
        val stats = SeasonStatsCalculator.calculate(listOf(loss("1", score = 3 to 38)))

        assertEquals(-35, stats.pointDifferential)
    }

    @Test
    fun `parseScore accepts only non negative integers`() {
        assertEquals(0, SeasonStatsCalculator.parseScore("0"))
        assertEquals(42, SeasonStatsCalculator.parseScore(" 42"))
        assertNull(SeasonStatsCalculator.parseScore(null))
        assertNull(SeasonStatsCalculator.parseScore(""))
        assertNull(SeasonStatsCalculator.parseScore("4.5"))
        assertNull(SeasonStatsCalculator.parseScore("-1"))
    }

    @Test
    fun `record formatting hides ties only when there are none`() {
        assertEquals("0-0", Record.EMPTY.formatted())
        assertEquals("10-6", Record(10, 6).formatted())
        assertEquals("8-7-1", Record(8, 7, 1).formatted())
        assertEquals(16, Record(8, 7, 1).gamesPlayed)
    }
}
