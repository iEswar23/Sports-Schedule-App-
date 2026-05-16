package com.sports.assessment.data.mapper

import com.sports.assessment.data.remote.dto.GameDto
import com.sports.assessment.data.remote.dto.GameSectionDto
import com.sports.assessment.data.remote.dto.OpponentDto
import com.sports.assessment.data.remote.dto.ScheduleResponseDto
import com.sports.assessment.data.remote.dto.TeamInfoDto
import com.sports.assessment.domain.model.*
import com.sports.assessment.util.DateTimeUtils

fun ScheduleResponseDto.toDomain(): ScheduleDomain {
    val teamDomain = team?.toDomain()
    val sections = gameSections?.map { it.toDomain() } ?: emptyList()
    return ScheduleDomain(
        team = teamDomain,
        sections = sections
    )
}

fun TeamInfoDto.toDomain(): TeamDomain {
    val triCodeLower = triCode?.lowercase() ?: ""
    return TeamDomain(
        name = name ?: "",
        city = city ?: "",
        triCode = triCode ?: "",
        record = record,
        logoUrl = "https://s3.amazonaws.com/yc-app-resources/nfl/logos/nfl_${triCodeLower}_light.png"
    )
}

fun GameSectionDto.toDomain(): ScheduleSectionDomain {
    return ScheduleSectionDomain(
        heading = heading ?: "",
        games = games?.map { it.toDomain() } ?: emptyList()
    )
}

fun GameDto.toDomain(): GameDomain {
    val gameType = when (type) {
        "S" -> GameType.SCHEDULED
        "F" -> GameType.FINAL
        "B" -> GameType.BYE
        else -> GameType.UNKNOWN
    }

    val isHomeVal = isHome ?: false
    val myScoreVal = if (isHomeVal) homeScore else awayScore
    val oppScoreVal = if (isHomeVal) awayScore else homeScore
    
    // Use timestamp for local timezone formatting
    val timestamp = date?.timestamp
    val formattedDate = if (timestamp != null) {
        DateTimeUtils.formatToLocalDate(timestamp)
    } else {
        date?.text ?: ""
    }
    
    val formattedTime = if (timestamp != null) {
        DateTimeUtils.formatToLocalTime(timestamp)
    } else {
        date?.time ?: ""
    }

    return GameDomain(
        id = id?.toString() ?: java.util.UUID.randomUUID().toString(),
        type = gameType,
        week = week ?: "",
        date = formattedDate,
        time = formattedTime,
        opponent = opponent?.toDomain(),
        isHome = isHomeVal,
        myScore = myScoreVal,
        opponentScore = oppScoreVal,
        tvStation = tv,
        gameState = gameState ?: "",
        venue = venue ?: "",
    )
}

fun OpponentDto.toDomain(): TeamDomain {
    val triCodeLower = triCode?.lowercase() ?: ""
    return TeamDomain(
        name = name ?: "",
        city = city ?: "",
        triCode = triCode ?: "",
        record = record,
        logoUrl = "https://s3.amazonaws.com/yc-app-resources/nfl/logos/nfl_${triCodeLower}_light.png"
    )
}
