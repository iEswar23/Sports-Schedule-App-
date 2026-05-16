package com.sports.assessment.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sports.assessment.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadSchedule()
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            repository.getSchedule().collect { result ->
                result.onSuccess { domain ->
                    if (domain.sections.isEmpty()) {
                        _uiState.value = ScheduleUiState.Empty
                    } else {
                        _uiState.value = ScheduleUiState.Success(domain)
                    }
                }.onFailure { error ->
                    _uiState.value = ScheduleUiState.Error(error.message ?: "Unknown error")
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.getSchedule().collect { result ->
                result.onSuccess { domain ->
                    if (domain.sections.isEmpty()) {
                        _uiState.value = ScheduleUiState.Empty
                    } else {
                        _uiState.value = ScheduleUiState.Success(domain)
                    }
                }.onFailure { error ->
                    _uiState.value = ScheduleUiState.Error(error.message ?: "Unknown error")
                }
                _isRefreshing.value = false
            }
        }
    }
}
