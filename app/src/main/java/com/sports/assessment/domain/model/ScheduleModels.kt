package com.sports.assessment.domain.model

data class ScheduleDomain(
    val team: TeamDomain?,
    val sections: List<ScheduleSectionDomain>
)

data class ScheduleSectionDomain(
    val heading: String,
    val games: List<GameDomain>
)

data class GameDomain(
    val id: String,
    val type: GameType,
    val week: String,
    val date: String,
    val time: String,
    val opponent: TeamDomain?,
    val isHome: Boolean,
    val myScore: String?,
    val opponentScore: String?,
    val tvStation: String?,
    val gameState: String,
    val venue: String? = null,
)

data class TeamDomain(
    val name: String,
    val city: String,
    val triCode: String,
    val record: String?,
    val logoUrl: String
)

enum class GameType {
    SCHEDULED, FINAL, BYE, UNKNOWN
}
