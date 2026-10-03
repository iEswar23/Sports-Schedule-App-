package com.sports.assessment.presentation.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sports.assessment.R
import com.sports.assessment.domain.model.GameDomain
import com.sports.assessment.domain.model.GameType
import com.sports.assessment.domain.model.ScheduleDomain
import com.sports.assessment.domain.model.ScheduleSectionDomain
import com.sports.assessment.domain.model.TeamDomain
import com.sports.assessment.domain.schedule.NextGameState
import com.sports.assessment.domain.schedule.ScheduleFilterType
import com.sports.assessment.domain.stats.SeasonStatsCalculator
import com.sports.assessment.ui.components.ByeWeekCard
import com.sports.assessment.ui.components.ErrorView
import com.sports.assessment.ui.components.FilterEmptyView
import com.sports.assessment.ui.components.LoadingView
import com.sports.assessment.ui.components.NextGameCard
import com.sports.assessment.ui.components.ScheduleFilterRow
import com.sports.assessment.ui.components.ScheduleItemCard
import com.sports.assessment.ui.components.SeasonAtAGlanceCard
import com.sports.assessment.ui.components.SeasonHeader
import com.sports.assessment.ui.theme.AssessmentTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = koinViewModel()
) {
    // Lifecycle-aware: the countdown ticker stops while the app is in the background.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    ScheduleScreenContent(
        uiState = uiState,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::loadSchedule,
        onFilterSelected = viewModel::selectFilter
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreenContent(
    uiState: ScheduleUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onFilterSelected: (ScheduleFilterType) -> Unit = {}
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.schedule_screen_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = dimensionResource(R.dimen.schedule_title_letter_spacing).value.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.nav_menu_description),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is ScheduleUiState.Loading -> LoadingView()
                is ScheduleUiState.Success -> ScheduleList(
                    state = uiState,
                    onFilterSelected = onFilterSelected
                )
                is ScheduleUiState.Error -> ErrorView(
                    message = uiState.message,
                    onRetry = onRetry
                )
                is ScheduleUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_schedule_available),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleList(
    state: ScheduleUiState.Success,
    onFilterSelected: (ScheduleFilterType) -> Unit
) {
    val team = state.schedule.team
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = KEY_SEASON_STATS, contentType = KEY_SEASON_STATS) {
            SeasonAtAGlanceCard(stats = state.seasonStats)
        }
        item(key = KEY_NEXT_GAME, contentType = KEY_NEXT_GAME) {
            NextGameCard(state = state.nextGame, stats = state.seasonStats, myTeam = team)
        }
        item(key = KEY_FILTERS, contentType = KEY_FILTERS) {
            ScheduleFilterRow(selected = state.selectedFilter, onFilterSelected = onFilterSelected)
        }
        if (state.schedule.sections.isEmpty()) {
            item(key = KEY_FILTER_EMPTY, contentType = KEY_FILTER_EMPTY) {
                FilterEmptyView(filter = state.selectedFilter)
            }
        }
        state.schedule.sections.forEach { section ->
            item(key = "section:${section.heading}", contentType = "section") {
                SeasonHeader(title = section.heading)
            }
            items(
                items = section.games,
                key = { "game:${section.heading}:${it.id}" },
                contentType = { if (it.type == GameType.BYE) "bye" else "game" }
            ) { game ->
                if (game.type == GameType.BYE) {
                    ByeWeekCard()
                } else {
                    ScheduleItemCard(game = game, myTeam = team)
                }
            }
        }
    }
}

private const val KEY_SEASON_STATS = "season_stats"
private const val KEY_NEXT_GAME = "next_game"
private const val KEY_FILTERS = "filters"
private const val KEY_FILTER_EMPTY = "filter_empty"

@Preview(showBackground = true)
@Composable
fun ScheduleScreenPreview() {
    val sampleTeam = TeamDomain(
        name = "Eagles",
        city = "Philadelphia",
        triCode = "PHI",
        record = "10-1",
        logoUrl = ""
    )
    val sampleOpponent = TeamDomain(
        name = "Cowboys",
        city = "Dallas",
        triCode = "DAL",
        record = "8-3",
        logoUrl = ""
    )
    val sampleGames = listOf(
        GameDomain(
            id = "1",
            type = GameType.FINAL,
            week = "1",
            date = "Sun, Sep 10",
            time = "4:25 PM",
            opponent = sampleOpponent,
            isHome = true,
            myScore = "25",
            opponentScore = "20",
            tvStation = "FOX",
            gameState = "FINAL"
        ),
        GameDomain(
            id = "2",
            type = GameType.BYE,
            week = "2",
            date = "Sun, Sep 17",
            time = "",
            opponent = null,
            isHome = false,
            myScore = null,
            opponentScore = null,
            tvStation = null,
            gameState = "BYE"
        ),
        GameDomain(
            id = "3",
            type = GameType.SCHEDULED,
            week = "3",
            date = "Mon, Sep 25",
            time = "8:15 PM",
            opponent = sampleOpponent,
            isHome = false,
            myScore = null,
            opponentScore = null,
            tvStation = "ABC/ESPN",
            gameState = "SCHEDULED"
        )
    )
    val sampleSchedule = ScheduleDomain(
        team = sampleTeam,
        sections = listOf(
            ScheduleSectionDomain(
                heading = "REGULAR SEASON",
                games = sampleGames
            )
        )
    )

    AssessmentTheme {
        ScheduleScreenContent(
            uiState = ScheduleUiState.Success(
                schedule = sampleSchedule,
                seasonStats = SeasonStatsCalculator.calculate(sampleSchedule),
                nextGame = NextGameState.SeasonComplete,
                selectedFilter = ScheduleFilterType.ALL
            ),
            isRefreshing = false,
            onRefresh = {},
            onRetry = {}
        )
    }
}
