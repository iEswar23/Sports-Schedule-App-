package com.sports.assessment.presentation.schedule

import androidx.lifecycle.SavedStateHandle
import com.sports.assessment.domain.model.ScheduleDomain
import com.sports.assessment.domain.schedule.NextGameState
import com.sports.assessment.domain.schedule.ScheduleFilterType
import com.sports.assessment.domain.stats.Record
import com.sports.assessment.testing.FakeScheduleRepository
import com.sports.assessment.testing.MainDispatcherRule
import com.sports.assessment.testing.MutableClock
import com.sports.assessment.testing.TestData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeScheduleRepository()
    private val clock = MutableClock(TestData.FEED_GENERATED_AT)

    private fun TestScope.createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ): ScheduleViewModel {
        val viewModel = ScheduleViewModel(repository, clock, savedStateHandle)
        // uiState is WhileSubscribed: keep a collector alive like the screen would.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        runCurrent()
        return viewModel
    }

    private fun ScheduleViewModel.success(): ScheduleUiState.Success =
        uiState.value as? ScheduleUiState.Success ?: error("Expected Success but was ${uiState.value}")

    @Test
    fun `initial load shows the full schedule with season stats and the next game`() = runTest {
        val state = createViewModel().success()

        assertEquals(ScheduleFilterType.ALL, state.selectedFilter)
        assertEquals(listOf("REGULAR SEASON", "POSTSEASON"), state.schedule.sections.map { it.heading })
        assertEquals(Record(13, 3), state.seasonStats.record)
        assertEquals(140, state.seasonStats.pointDifferential)
        val next = state.nextGame as NextGameState.Upcoming
        assertEquals("LAR", next.game.opponent?.triCode)
        assertEquals(Duration.ofDays(2).plusHours(2), next.timeToKickoff)
        assertEquals(1, repository.calls)
    }

    @Test
    fun `shows loading until the repository responds`() = runTest {
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }
        val viewModel = createViewModel()
        assertEquals(ScheduleUiState.Loading, viewModel.uiState.value)

        gate.complete(Unit)
        runCurrent()

        viewModel.success()
    }

    @Test
    fun `schedule without games is empty`() = runTest {
        repository.result = Result.success(ScheduleDomain(team = null, sections = emptyList()))

        assertEquals(ScheduleUiState.Empty, createViewModel().uiState.value)
    }

    @Test
    fun `repository failure shows its message`() = runTest {
        repository.result = Result.failure(IOException("Unable to resolve host"))

        assertEquals(ScheduleUiState.Error("Unable to resolve host"), createViewModel().uiState.value)
    }

    @Test
    fun `failure without a message falls back to a generic one`() = runTest {
        repository.result = Result.failure(IllegalStateException())

        assertEquals(ScheduleUiState.Error("Unknown error"), createViewModel().uiState.value)
    }

    @Test
    fun `exception thrown by the flow is turned into an error state`() = runTest {
        repository.throwable = IOException("timeout")

        assertEquals(ScheduleUiState.Error("timeout"), createViewModel().uiState.value)
    }

    @Test
    fun `retry after an error loads the schedule`() = runTest {
        repository.result = Result.failure(IOException("offline"))
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value is ScheduleUiState.Error)

        repository.result = Result.success(TestData.fixtureSchedule())
        viewModel.loadSchedule()
        runCurrent()

        viewModel.success()
        assertEquals(2, repository.calls)
    }

    @Test
    fun `refresh keeps content on screen and toggles the refreshing flag`() = runTest {
        val viewModel = createViewModel()
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }

        viewModel.refresh()
        runCurrent()
        assertTrue(viewModel.isRefreshing.value)
        viewModel.success() // still showing the previous data

        repository.result = Result.success(ScheduleDomain(team = null, sections = emptyList()))
        gate.complete(Unit)
        runCurrent()

        assertFalse(viewModel.isRefreshing.value)
        assertEquals(ScheduleUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `refresh failure clears the refreshing flag and shows the error`() = runTest {
        val viewModel = createViewModel()
        repository.result = Result.failure(IOException("offline"))

        viewModel.refresh()
        runCurrent()

        assertFalse(viewModel.isRefreshing.value)
        assertEquals(ScheduleUiState.Error("offline"), viewModel.uiState.value)
    }

    @Test
    fun `refresh during the initial load supersedes it`() = runTest {
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }
        val viewModel = createViewModel()

        viewModel.refresh()
        runCurrent()
        assertTrue(viewModel.isRefreshing.value)

        gate.complete(Unit)
        runCurrent()

        assertFalse(viewModel.isRefreshing.value)
        viewModel.success()
        assertEquals(2, repository.calls)
    }

    @Test
    fun `selecting a filter narrows the list but not the stats or next game`() = runTest {
        val viewModel = createViewModel()

        viewModel.selectFilter(ScheduleFilterType.RESULTS)
        runCurrent()

        val state = viewModel.success()
        assertEquals(ScheduleFilterType.RESULTS, state.selectedFilter)
        assertEquals(listOf("REGULAR SEASON"), state.schedule.sections.map { it.heading })
        assertEquals(16, state.schedule.sections.single().games.size)
        assertEquals(Record(13, 3), state.seasonStats.record)
        assertTrue(state.nextGame is NextGameState.Upcoming)
    }

    @Test
    fun `selected filter is written to SavedStateHandle`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.selectFilter(ScheduleFilterType.AWAY)

        assertEquals("AWAY", handle.get<String>(ScheduleViewModel.KEY_FILTER))
    }

    @Test
    fun `selected filter is restored after recreation`() = runTest {
        val handle = SavedStateHandle(mapOf(ScheduleViewModel.KEY_FILTER to "UPCOMING"))

        val state = createViewModel(handle).success()

        assertEquals(ScheduleFilterType.UPCOMING, state.selectedFilter)
        assertEquals("Divisional Playoffs", state.schedule.sections.single().games.single().week)
    }

    @Test
    fun `unknown saved filter falls back to all`() = runTest {
        val handle = SavedStateHandle(mapOf(ScheduleViewModel.KEY_FILTER to "BOGUS"))

        assertEquals(ScheduleFilterType.ALL, createViewModel(handle).success().selectedFilter)
    }

    @Test
    fun `countdown ticks with the clock and turns live at kickoff`() = runTest {
        val viewModel = createViewModel()

        clock.instant = TestData.PLAYOFF_KICKOFF.minus(Duration.ofMinutes(45))
        advanceTimeBy(ScheduleViewModel.TICK_INTERVAL_MS + 1)
        val upcoming = viewModel.success().nextGame as NextGameState.Upcoming
        assertEquals(Duration.ofMinutes(45), upcoming.timeToKickoff)

        clock.instant = TestData.PLAYOFF_KICKOFF.plus(Duration.ofMinutes(10))
        advanceTimeBy(ScheduleViewModel.TICK_INTERVAL_MS)
        assertTrue(viewModel.success().nextGame is NextGameState.Live)
    }

    @Test
    fun `with today's clock the season is complete and nothing is upcoming`() = runTest {
        clock.instant = Instant.parse("2026-10-02T12:00:00Z")
        val viewModel = createViewModel()
        assertEquals(NextGameState.SeasonComplete, viewModel.success().nextGame)

        viewModel.selectFilter(ScheduleFilterType.UPCOMING)
        runCurrent()

        assertTrue(viewModel.success().schedule.sections.isEmpty())
    }
}
