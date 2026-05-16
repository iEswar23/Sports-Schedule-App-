package com.sports.assessment.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleResponseDto(
    @SerialName("Team") val team: TeamInfoDto? = null,
    @SerialName("GameSection") val gameSections: List<GameSectionDto>? = null
)

@Serializable
data class TeamInfoDto(
    @SerialName("TriCode") val triCode: String? = null,
    @SerialName("FullName") val fullName: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("City") val city: String? = null,
    @SerialName("Record") val record: String? = null
)

@Serializable
data class GameSectionDto(
    @SerialName("Heading") val heading: String? = null,
    @SerialName("Game") val games: List<GameDto>? = null
)

@Serializable
data class GameDto(
    @SerialName("Id") val id: Long? = null,
    @SerialName("Type") val type: String? = null, // S, F, B
    @SerialName("Week") val week: String? = null,
    @SerialName("GameState") val gameState: String? = null,
    @SerialName("IsHome") val isHome: Boolean? = null,
    @SerialName("TV") val tv: String? = null,
    @SerialName("AwayScore") val awayScore: String? = null,
    @SerialName("HomeScore") val homeScore: String? = null,
    @SerialName("Date") val date: DateDto? = null,
    @SerialName("Opponent") val opponent: OpponentDto? = null,
    @SerialName("Venue") val venue: String? = null,
)

@Serializable
data class DateDto(
    @SerialName("Numeric") val numeric: String? = null,
    @SerialName("Text") val text: String? = null,
    @SerialName("Time") val time: String? = null,
    @SerialName("Timestamp") val timestamp: String? = null
)

@Serializable
data class OpponentDto(
    @SerialName("TriCode") val triCode: String? = null,
    @SerialName("FullName") val fullName: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("City") val city: String? = null,
    @SerialName("Record") val record: String? = null
)
