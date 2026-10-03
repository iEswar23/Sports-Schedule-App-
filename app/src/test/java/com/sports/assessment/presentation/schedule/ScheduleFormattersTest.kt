package com.sports.assessment.presentation.schedule

import com.sports.assessment.domain.model.GameType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class ScheduleFormattersTest {

    private fun compact(duration: Duration) = CountdownFormatter.compact(duration)

    @Test
    fun `days and hours`() {
        assertEquals("2d 4h", compact(Duration.ofDays(2).plusHours(4).plusMinutes(59)))
    }

    @Test
    fun `whole days drop the zero hours`() {
        assertEquals("3d", compact(Duration.ofDays(3).plusMinutes(30)))
    }

    @Test
    fun `hours and minutes under a day`() {
        assertEquals("4h 12m", compact(Duration.ofHours(4).plusMinutes(12).plusSeconds(40)))
        assertEquals("23h 59m", compact(Duration.ofDays(1).minusSeconds(1)))
    }

    @Test
    fun `whole hours drop the zero minutes`() {
        assertEquals("1h", compact(Duration.ofHours(1)))
    }

    @Test
    fun `minutes under an hour`() {
        assertEquals("45m", compact(Duration.ofMinutes(45).plusSeconds(59)))
        assertEquals("1m", compact(Duration.ofSeconds(60)))
    }

    @Test
    fun `under a minute, zero and negative durations`() {
        assertEquals("<1m", compact(Duration.ofSeconds(59)))
        assertEquals("<1m", compact(Duration.ZERO))
        assertEquals("<1m", compact(Duration.ofMinutes(-5)))
    }

    @Test
    fun `exactly one day`() {
        assertEquals("1d", compact(Duration.ofDays(1)))
    }

    @Test
    fun `long countdowns keep counting in days`() {
        assertEquals("120d 5h", compact(Duration.ofDays(120).plusHours(5)))
    }

    @Test
    fun `point differential carries an explicit sign when positive`() {
        assertEquals("+140", formatPointDifferential(140))
        assertEquals("-12", formatPointDifferential(-12))
        assertEquals("0", formatPointDifferential(0))
    }

    private fun cardScore(score: String?, type: GameType) =
        formatCardScore(score, type, missingFinalScore = "0", notPlayed = "–")

    @Test
    fun `final games show their score`() {
        assertEquals("43", cardScore("43", GameType.FINAL))
        assertEquals("0", cardScore("0", GameType.FINAL))
        assertEquals("7", cardScore(" 7 ", GameType.FINAL))
    }

    @Test
    fun `final game without a score keeps the previous 0 fallback`() {
        assertEquals("0", cardScore(null, GameType.FINAL))
        assertEquals("0", cardScore("", GameType.FINAL))
    }

    @Test
    fun `scheduled games without scores show a dash instead of 0`() {
        assertEquals("–", cardScore("", GameType.SCHEDULED))
        assertEquals("–", cardScore(null, GameType.SCHEDULED))
        assertEquals("–", cardScore("  ", GameType.UNKNOWN))
    }

    @Test
    fun `a score reported for a game in progress is shown`() {
        assertEquals("14", cardScore("14", GameType.SCHEDULED))
    }
}
