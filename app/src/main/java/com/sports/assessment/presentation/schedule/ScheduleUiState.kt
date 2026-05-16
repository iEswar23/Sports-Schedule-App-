package com.sports.assessment.presentation.schedule

import com.sports.assessment.domain.model.ScheduleDomain

sealed class ScheduleUiState {
    object Loading : ScheduleUiState()
    data class Success(val data: ScheduleDomain) : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
    object Empty : ScheduleUiState()
}
