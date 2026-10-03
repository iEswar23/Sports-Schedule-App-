package com.sports.assessment.presentation.schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sports.assessment.domain.model.ScheduleDomain
import com.sports.assessment.domain.repository.ScheduleRepository
import com.sports.assessment.domain.schedule.NextGameFinder
import com.sports.assessment.domain.schedule.ScheduleFilter
import com.sports.assessment.domain.schedule.ScheduleFilterType
import com.sports.assessment.domain.stats.SeasonStats
import com.sports.assessment.domain.stats.SeasonStatsCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant

class ScheduleViewModel(
    private val repository: ScheduleRepository,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val loadState = MutableStateFlow<LoadState>(LoadState.Loading)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /** Kept in [SavedStateHandle] so it survives configuration changes and process death. */
    private val selectedFilter: Flow<ScheduleFilterType> = savedStateHandle
        .getStateFlow(KEY_FILTER, ScheduleFilterType.ALL.name)
        .map { name -> ScheduleFilterType.entries.firstOrNull { it.name == name } ?: ScheduleFilterType.ALL }

    /** Re-reads the clock periodically so the countdown and the Upcoming filter stay current. */
    private val now: Flow<Instant> = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_INTERVAL_MS)
        }
    }

    val uiState: StateFlow<ScheduleUiState> = combine(loadState, selectedFilter, now) { load, filter, instant ->
        when (load) {
            LoadState.Loading -> ScheduleUiState.Loading
            LoadState.Empty -> ScheduleUiState.Empty
            is LoadState.Failed -> ScheduleUiState.Error(load.message)
            is LoadState.Loaded -> ScheduleUiState.Success(
                schedule = ScheduleFilter.apply(load.schedule, filter, instant),
                seasonStats = load.stats,
                nextGame = NextGameFinder.find(load.schedule, instant),
                selectedFilter = filter,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        // Stops the clock ticker shortly after the UI goes away; survives rotation.
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = ScheduleUiState.Loading,
    )

    private var fetchJob: Job? = null

    init {
        loadSchedule()
    }

    /** Full-screen load (initial load and Retry). */
    fun loadSchedule() = fetch(isRefresh = false)

    /** Pull to refresh: keeps the current content on screen while loading. */
    fun refresh() = fetch(isRefresh = true)

    fun selectFilter(filter: ScheduleFilterType) {
        savedStateHandle[KEY_FILTER] = filter.name
    }

    private fun fetch(isRefresh: Boolean) {
        val previous = fetchJob
        fetchJob = viewModelScope.launch {
            // Let a superseded request finish cancelling (and run its cleanup) before starting.
            previous?.cancelAndJoin()
            if (isRefresh) _isRefreshing.value = true else loadState.value = LoadState.Loading
            try {
                repository.getSchedule()
                    .catch { emit(Result.failure(it)) }
                    .collect { result ->
                        loadState.value = result.fold(
                            onSuccess = ::loadedOrEmpty,
                            onFailure = { LoadState.Failed(it.message ?: UNKNOWN_ERROR) },
                        )
                    }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun loadedOrEmpty(schedule: ScheduleDomain): LoadState =
        if (schedule.sections.all { it.games.isEmpty() }) {
            LoadState.Empty
        } else {
            LoadState.Loaded(schedule, SeasonStatsCalculator.calculate(schedule))
        }

    private sealed interface LoadState {
        data object Loading : LoadState
        data object Empty : LoadState
        data class Failed(val message: String) : LoadState
        /** Stats only depend on the data, so they are computed once per load, not per tick. */
        data class Loaded(val schedule: ScheduleDomain, val stats: SeasonStats) : LoadState
    }

    companion object {
        const val KEY_FILTER = "schedule_filter"
        const val TICK_INTERVAL_MS = 30_000L
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val UNKNOWN_ERROR = "Unknown error"
    }
}
