package com.sports.assessment.screenshots

import android.app.Application
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import coil.Coil
import coil.ImageLoader
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.sports.assessment.presentation.schedule.ScheduleScreen
import com.sports.assessment.presentation.schedule.ScheduleViewModel
import com.sports.assessment.testing.FakeScheduleRepository
import com.sports.assessment.testing.MutableClock
import com.sports.assessment.testing.TestData
import com.sports.assessment.ui.theme.AssessmentTheme
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.IOException
import java.time.Instant
import java.util.TimeZone

/**
 * Renders the real [ScheduleScreen] + [ScheduleViewModel] on the JVM (Robolectric native graphics)
 * with the recorded 2020 Green Bay feed and a fixed clock, and records README screenshots with
 * Roborazzi.
 *
 * `./gradlew recordRoborazziDebug` writes the PNGs to `docs/screenshots/`. A plain
 * `testDebugUnitTest` still runs every scenario (catching crashes) but skips image capture.
 *
 * Network is disabled for Coil, so team logos fall back to their tricode badges, exactly as they do
 * on a device without connectivity.
 */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [34], qualifiers = PHONE)
class ScheduleScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val defaultTimeZone: TimeZone = TimeZone.getDefault()

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone(TestData.CENTRAL))
        val context = ApplicationProvider.getApplicationContext<Application>()
        Coil.setImageLoader(
            ImageLoader.Builder(context)
                .okHttpClient(
                    OkHttpClient.Builder()
                        .addInterceptor { throw IOException("Network disabled in screenshot tests") }
                        .build()
                )
                .build()
        )
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(defaultTimeZone)
        Coil.reset()
    }

    @Test
    fun schedule() {
        show(now = TestData.FEED_GENERATED_AT)
        composeRule.onNodeWithText("SEASON AT A GLANCE").assertExists()
        composeRule.onNodeWithText("Kickoff in 2d 2h").assertExists()
        capture("01_schedule")
    }

    @Test
    fun resultsFilter() {
        show(now = TestData.FEED_GENERATED_AT)
        composeRule.onNodeWithText("Results").performClick()
        composeRule.onNode(verticalList).performScrollToIndex(FILTER_ROW_INDEX)
        composeRule.waitForIdle()
        // Week 1 at Minnesota is an away game: "GB @ MIN" with the Packers' 43 on the left.
        composeRule.onAllNodesWithText("@").onFirst().assertExists()
        capture("02_filter_results")
    }

    @Test
    fun upcomingFilterWithNextGame() {
        show(now = TestData.FEED_GENERATED_AT)
        composeRule.onNodeWithText("Upcoming").performClick()
        composeRule.waitForIdle()
        // Only the playoff game is left, so stats, countdown and the game card fit on one screen.
        composeRule.onNodeWithText("POSTSEASON").assertExists()
        composeRule.onNodeWithText("REGULAR SEASON").assertDoesNotExist()
        // The playoff game is at Lambeau Field: the card reads "GB vs LAR" and shows no 0-0 score.
        // (Resource whitespace is trimmed, so the separators render as "vs" / "@".)
        composeRule.onAllNodesWithText("vs").assertCountEquals(2) // Next game card + game card
        composeRule.onAllNodesWithText("@").assertCountEquals(0)
        composeRule.onAllNodesWithText("0").assertCountEquals(0)
        capture("03_filter_upcoming_next_game")
    }

    @Test
    fun seasonComplete() {
        show(now = TODAY)
        composeRule.onNodeWithText("SEASON COMPLETE").assertExists()
        capture("04_season_complete")
    }

    @Test
    fun upcomingFilterAfterSeason() {
        show(now = TODAY)
        composeRule.onNodeWithText("Upcoming").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("No upcoming games").assertExists()
        capture("05_upcoming_empty")
    }

    @Test
    fun errorState() {
        show(now = TODAY, repository = FakeScheduleRepository(Result.failure(IOException("Unable to load the schedule. Check your connection and try again."))))
        composeRule.onNodeWithText("Retry").assertExists()
        capture("06_error")
    }

    @Test
    @Config(qualifiers = "+night")
    fun scheduleDark() {
        show(now = TestData.FEED_GENERATED_AT)
        capture("07_schedule_dark")
    }

    private fun show(now: Instant, repository: FakeScheduleRepository = FakeScheduleRepository()) {
        val viewModel = ScheduleViewModel(repository, MutableClock(now), SavedStateHandle())
        composeRule.setContent {
            AssessmentTheme {
                ScheduleScreen(viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    private val verticalList = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)

    private fun capture(name: String) {
        composeRule.onRoot().captureRoboImage("$OUTPUT_DIR/$name.png", roborazziOptions = OPTIONS)
    }

    private companion object {
        const val OUTPUT_DIR = "../docs/screenshots"
        const val FILTER_ROW_INDEX = 2
        val TODAY: Instant = Instant.parse("2026-10-02T17:00:00Z")
        val OPTIONS = RoborazziOptions(recordOptions = RoborazziOptions.RecordOptions(resizeScale = 0.5))
    }
}

/** Pixel 7-sized phone. */
private const val PHONE = "w411dp-h891dp-normal-long-notround-xxhdpi"
