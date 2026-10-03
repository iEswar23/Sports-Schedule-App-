package com.sports.assessment.domain.model

import java.time.Instant

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
    /** Exact kickoff instant parsed from the feed's ISO-8601 timestamp; null when absent or malformed. */
    val kickoff: Instant? = null,
    /** Part of the season this game belongs to, derived from its section heading. */
    val phase: SeasonPhase = SeasonPhase.REGULAR_SEASON,
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

enum class SeasonPhase {
    PRESEASON, REGULAR_SEASON, POSTSEASON;

    companion object {
        /**
         * Maps a feed section heading ("REGULAR SEASON", "POSTSEASON", "PRESEASON", "PLAYOFFS", ...)
         * to a phase. Anything unrecognised is treated as regular season.
         */
        fun fromHeading(heading: String?): SeasonPhase {
            val normalized = heading.orEmpty().uppercase().replace("-", "").replace(" ", "")
            return when {
                normalized.contains("PRESEASON") -> PRESEASON
                normalized.contains("POSTSEASON") || normalized.contains("PLAYOFF") -> POSTSEASON
                else -> REGULAR_SEASON
            }
        }
    }
}
