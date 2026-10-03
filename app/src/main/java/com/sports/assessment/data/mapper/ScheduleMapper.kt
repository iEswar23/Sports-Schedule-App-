package com.sports.assessment.data.mapper

import com.sports.assessment.data.remote.dto.GameDto
import com.sports.assessment.data.remote.dto.GameSectionDto
import com.sports.assessment.data.remote.dto.OpponentDto
import com.sports.assessment.data.remote.dto.ScheduleResponseDto
import com.sports.assessment.data.remote.dto.TeamInfoDto
import com.sports.assessment.domain.model.GameDomain
import com.sports.assessment.domain.model.GameType
import com.sports.assessment.domain.model.ScheduleDomain
import com.sports.assessment.domain.model.ScheduleSectionDomain
import com.sports.assessment.domain.model.SeasonPhase
import com.sports.assessment.domain.model.TeamDomain
import com.sports.assessment.util.DateTimeUtils
import java.time.ZoneId

/**
 * Maps the feed into domain models. Date and time labels are rendered in [zone] (the device zone
 * in the app); the raw kickoff is kept as an [java.time.Instant] for countdowns and filtering.
 */
fun ScheduleResponseDto.toDomain(zone: ZoneId = ZoneId.systemDefault()): ScheduleDomain {
    return ScheduleDomain(
        team = team?.toDomain(),
        sections = gameSections?.map { it.toDomain(zone) } ?: emptyList()
    )
}

fun TeamInfoDto.toDomain(): TeamDomain = TeamDomain(
    name = name ?: "",
    city = city ?: "",
    triCode = triCode ?: "",
    record = record,
    logoUrl = logoUrlFor(triCode)
)

fun GameSectionDto.toDomain(zone: ZoneId = ZoneId.systemDefault()): ScheduleSectionDomain {
    val sectionHeading = heading ?: ""
    val phase = SeasonPhase.fromHeading(sectionHeading)
    return ScheduleSectionDomain(
        heading = sectionHeading,
        games = games?.mapIndexed { index, game ->
            game.toDomain(
                phase = phase,
                fallbackId = "$sectionHeading#$index",
                zone = zone
            )
        } ?: emptyList()
    )
}

/**
 * @param fallbackId stable id used when the feed has no usable id (bye weeks carry `Id: 0`), so
 * list keys stay unique and survive refreshes.
 */
fun GameDto.toDomain(
    phase: SeasonPhase = SeasonPhase.REGULAR_SEASON,
    fallbackId: String = "${type.orEmpty()}-${week.orEmpty()}",
    zone: ZoneId = ZoneId.systemDefault()
): GameDomain {
    val gameType = when (type) {
        "S" -> GameType.SCHEDULED
        "F" -> GameType.FINAL
        "B" -> GameType.BYE
        else -> GameType.UNKNOWN
    }

    val isHomeVal = isHome ?: false
    val kickoff = DateTimeUtils.parseInstant(date?.timestamp)

    return GameDomain(
        id = id?.takeIf { it > 0 }?.toString() ?: fallbackId,
        type = gameType,
        week = week ?: "",
        // Prefer the instant rendered in the local zone; fall back to the feed's own labels.
        date = kickoff?.let { DateTimeUtils.formatDate(it, zone) } ?: date?.text ?: "",
        time = kickoff?.let { DateTimeUtils.formatTime(it, zone) } ?: date?.time ?: "",
        opponent = opponent?.toDomain(),
        isHome = isHomeVal,
        myScore = if (isHomeVal) homeScore else awayScore,
        opponentScore = if (isHomeVal) awayScore else homeScore,
        tvStation = tv,
        gameState = gameState ?: "",
        venue = venue ?: "",
        kickoff = kickoff,
        phase = phase,
    )
}

fun OpponentDto.toDomain(): TeamDomain = TeamDomain(
    name = name ?: "",
    city = city ?: "",
    triCode = triCode ?: "",
    record = record,
    logoUrl = logoUrlFor(triCode)
)

/** Empty when the tricode is unknown, so the UI shows its placeholder instead of a broken URL. */
private fun logoUrlFor(triCode: String?): String {
    if (triCode.isNullOrBlank()) return ""
    return "https://s3.amazonaws.com/yc-app-resources/nfl/logos/nfl_${triCode.trim().lowercase()}_light.png"
}
