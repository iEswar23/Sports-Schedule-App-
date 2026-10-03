package com.sports.assessment.testing

import com.sports.assessment.domain.model.ScheduleDomain
import com.sports.assessment.domain.repository.ScheduleRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** Repository double: returns [result] on every call, optionally waiting on [gate] first. */
class FakeScheduleRepository(
    var result: Result<ScheduleDomain> = Result.success(TestData.fixtureSchedule()),
) : ScheduleRepository {
    var calls = 0
        private set

    /** When set, each request suspends until the deferred completes (to observe loading states). */
    var gate: CompletableDeferred<Unit>? = null

    /** When set, the flow throws instead of emitting a Result. */
    var throwable: Throwable? = null

    override fun getSchedule(): Flow<Result<ScheduleDomain>> = flow {
        calls++
        gate?.await()
        throwable?.let { throw it }
        emit(result)
    }
}

/** A [Clock] whose time only moves when a test sets [instant]. */
class MutableClock(
    var instant: Instant,
    private val zone: ZoneId = ZoneOffset.UTC,
) : Clock() {
    override fun instant(): Instant = instant
    override fun getZone(): ZoneId = zone
    override fun withZone(zone: ZoneId): Clock = MutableClock(instant, zone)
}

/** Swaps Dispatchers.Main for a test dispatcher so viewModelScope runs on virtual time. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
