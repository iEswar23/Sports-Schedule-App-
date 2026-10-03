package com.sports.assessment.presentation.schedule

import com.sports.assessment.domain.model.ScheduleDomain
import com.sports.assessment.domain.schedule.NextGameState
import com.sports.assessment.domain.schedule.ScheduleFilterType
import com.sports.assessment.domain.stats.SeasonStats

sealed class ScheduleUiState {
    object Loading : ScheduleUiState()

    data class Success(
        /** The schedule after [selectedFilter] was applied; may have no sections. */
        val schedule: ScheduleDomain,
        /** Computed from the full, unfiltered schedule. */
        val seasonStats: SeasonStats,
        /** Computed from the full, unfiltered schedule at the current clock tick. */
        val nextGame: NextGameState,
        val selectedFilter: ScheduleFilterType,
    ) : ScheduleUiState()

    data class Error(val message: String) : ScheduleUiState()
    object Empty : ScheduleUiState()
}
