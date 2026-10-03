package com.sports.assessment.testing

import com.sports.assessment.data.mapper.toDomain
import com.sports.assessment.data.remote.ScheduleJson
import com.sports.assessment.data.remote.dto.ScheduleResponseDto
import com.sports.assessment.domain.model.GameDomain
import com.sports.assessment.domain.model.GameType
import com.sports.assessment.domain.model.ScheduleDomain
import com.sports.assessment.domain.model.ScheduleSectionDomain
import com.sports.assessment.domain.model.SeasonPhase
import com.sports.assessment.domain.model.TeamDomain
import java.time.Instant
import java.time.ZoneId

/** Builders for domain objects plus access to the recorded feed in `src/test/resources/schedule.json`. */
object TestData {

    /** Green Bay's local zone, used wherever a test needs deterministic date/time labels. */
    val CENTRAL: ZoneId = ZoneId.of("America/Chicago")

    /** When the recorded feed was generated ("1/14/2021 1:35:41 PM" Central). */
    val FEED_GENERATED_AT: Instant = Instant.parse("2021-01-14T19:35:00Z")

    /** Kickoff of the only scheduled game in the feed (Divisional Playoffs vs LAR). */
    val PLAYOFF_KICKOFF: Instant = Instant.parse("2021-01-16T21:35:00Z")

    fun fixtureJson(): String =
        requireNotNull(javaClass.classLoader?.getResource("schedule.json")) { "schedule.json fixture missing" }
            .readText()

    fun fixtureDto(): ScheduleResponseDto = ScheduleJson.decodeFromString(fixtureJson())

    fun fixtureSchedule(zone: ZoneId = CENTRAL): ScheduleDomain = fixtureDto().toDomain(zone)

    fun team(triCode: String = "OPP", name: String = "OPPONENTS") = TeamDomain(
        name = name,
        city = "City",
        triCode = triCode,
        record = "0-0",
        logoUrl = ""
    )

    fun game(
        id: String,
        type: GameType = GameType.FINAL,
        isHome: Boolean = true,
        myScore: String? = "21",
        opponentScore: String? = "14",
        phase: SeasonPhase = SeasonPhase.REGULAR_SEASON,
        kickoff: Instant? = null,
        week: String = "Week $id",
        opponent: TeamDomain? = team(),
    ) = GameDomain(
        id = id,
        type = type,
        week = week,
        date = "",
        time = "",
        opponent = opponent,
        isHome = isHome,
        myScore = myScore,
        opponentScore = opponentScore,
        tvStation = null,
        gameState = "",
        venue = "",
        kickoff = kickoff,
        phase = phase,
    )

    fun win(id: String, isHome: Boolean = true, score: Pair<Int, Int> = 24 to 10, phase: SeasonPhase = SeasonPhase.REGULAR_SEASON) =
        game(id, isHome = isHome, myScore = "${score.first}", opponentScore = "${score.second}", phase = phase)

    fun loss(id: String, isHome: Boolean = true, score: Pair<Int, Int> = 10 to 24, phase: SeasonPhase = SeasonPhase.REGULAR_SEASON) =
        game(id, isHome = isHome, myScore = "${score.first}", opponentScore = "${score.second}", phase = phase)

    fun tie(id: String, isHome: Boolean = true, score: Int = 17, phase: SeasonPhase = SeasonPhase.REGULAR_SEASON) =
        game(id, isHome = isHome, myScore = "$score", opponentScore = "$score", phase = phase)

    fun scheduled(id: String, kickoff: Instant?, isHome: Boolean = true, phase: SeasonPhase = SeasonPhase.REGULAR_SEASON) =
        game(id, type = GameType.SCHEDULED, isHome = isHome, myScore = "", opponentScore = "", kickoff = kickoff, phase = phase)

    fun bye(id: String) = game(id, type = GameType.BYE, myScore = null, opponentScore = null, opponent = null)

    fun schedule(vararg sections: Pair<String, List<GameDomain>>) = ScheduleDomain(
        team = team(triCode = "GB", name = "PACKERS"),
        sections = sections.map { (heading, games) -> ScheduleSectionDomain(heading, games) }
    )
}
