package com.sports.assessment.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sports.assessment.R
import com.sports.assessment.domain.model.GameDomain
import com.sports.assessment.domain.model.GameType
import com.sports.assessment.domain.model.SeasonPhase
import com.sports.assessment.domain.model.TeamDomain
import com.sports.assessment.domain.schedule.NextGameState
import com.sports.assessment.domain.schedule.ScheduleFilterType
import com.sports.assessment.domain.stats.CompletedGame
import com.sports.assessment.domain.stats.GameOutcome
import com.sports.assessment.domain.stats.Record
import com.sports.assessment.domain.stats.SeasonStats
import com.sports.assessment.domain.stats.Streak
import com.sports.assessment.presentation.schedule.CountdownFormatter
import com.sports.assessment.presentation.schedule.formatPointDifferential
import com.sports.assessment.ui.theme.AppTealLight
import com.sports.assessment.ui.theme.AssessmentTheme
import java.time.Duration

// region Season at a glance

@Composable
fun SeasonAtAGlanceCard(stats: SeasonStats, modifier: Modifier = Modifier) {
    val missing = stringResource(R.string.stat_missing)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_extra_small)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.padding_extra_small))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            CardEyebrow(text = stringResource(R.string.season_at_a_glance), color = MaterialTheme.colorScheme.secondary)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stats.record.formatted(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.regular_season_record),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                stats.streak?.let { StreakPill(streak = it) }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            StatRow(
                StatValue(
                    label = R.string.stat_points_for,
                    value = if (stats.hasResults) stats.pointsFor.toString() else missing
                ),
                StatValue(
                    label = R.string.stat_points_against,
                    value = if (stats.hasResults) stats.pointsAgainst.toString() else missing
                ),
                StatValue(
                    label = R.string.stat_point_differential,
                    value = if (stats.hasResults) formatPointDifferential(stats.pointDifferential) else missing,
                    color = differentialColor(stats.pointDifferential)
                ),
            )
            StatRow(
                StatValue(label = R.string.stat_home, value = stats.homeRecord.formatted()),
                StatValue(label = R.string.stat_away, value = stats.awayRecord.formatted()),
                // Postseason is kept out of the record above and reported on its own.
                StatValue(
                    label = R.string.stat_postseason,
                    value = stats.postseasonRecord.takeIf { it.gamesPlayed > 0 }?.formatted() ?: missing
                ),
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            val last = stats.lastResult
            if (last == null) {
                Text(
                    text = stringResource(R.string.no_results_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.last_result),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    ResultLine(result = last)
                }
            }

            if (stats.skippedGames > 0) {
                Text(
                    text = pluralStringResource(R.plurals.skipped_games, stats.skippedGames, stats.skippedGames),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private data class StatValue(
    @StringRes val label: Int,
    val value: String,
    val color: Color? = null,
)

@Composable
private fun StatRow(vararg stats: StatValue) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        stats.forEach { stat ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.titleLarge,
                    color = stat.color ?: MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = stringResource(stat.label),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StreakPill(streak: Streak) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .background(outcomeColor(streak.outcome), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = streak.formatted(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = stringResource(R.string.stat_streak),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** "W  35-16 @ CHI · Week 17" with the outcome letter coloured. */
@Composable
private fun ResultLine(result: CompletedGame, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(outcomeColor(result.outcome), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = result.outcome.code.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(
                R.string.result_score_line,
                result.pointsFor,
                result.pointsAgainst,
                opponentLabel(result.game),
                result.game.week
            ),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Fill colour for outcome badges (white text on top). */
@Composable
private fun outcomeColor(outcome: GameOutcome): Color = when (outcome) {
    GameOutcome.WIN -> MaterialTheme.colorScheme.primary
    GameOutcome.LOSS -> MaterialTheme.colorScheme.error
    GameOutcome.TIE -> MaterialTheme.colorScheme.secondary
}

/** Text colour for a signed value; the brand teal is lightened on dark surfaces for contrast. */
@Composable
private fun differentialColor(differential: Int): Color = when {
    differential > 0 -> if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) AppTealLight else MaterialTheme.colorScheme.primary
    differential < 0 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}

/** "vs CHI" for home games, "@ CHI" for away games. */
@Composable
private fun opponentLabel(game: GameDomain): String = stringResource(
    if (game.isHome) R.string.opponent_home else R.string.opponent_away,
    game.opponent?.triCode.orEmpty()
)

@Composable
private fun CardEyebrow(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp, fontSize = 12.sp),
        color = color,
        modifier = modifier.semantics { heading() }
    )
}

// endregion

// region Next game

/**
 * Highlighted card for the next game with a live countdown. Shows "Live now" while a game is in
 * progress and a "Season complete" summary with the final record once every game is in the past.
 * Renders nothing for [NextGameState.Unavailable].
 */
@Composable
fun NextGameCard(
    state: NextGameState,
    stats: SeasonStats,
    myTeam: TeamDomain?,
    modifier: Modifier = Modifier,
) {
    if (state == NextGameState.Unavailable) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_extra_small)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.padding_extra_small))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            when (state) {
                is NextGameState.Upcoming -> NextGameDetails(
                    game = state.game,
                    myTeam = myTeam,
                    status = stringResource(R.string.kickoff_in, CountdownFormatter.compact(state.timeToKickoff)),
                    isLive = false
                )
                is NextGameState.Live -> NextGameDetails(
                    game = state.game,
                    myTeam = myTeam,
                    status = stringResource(R.string.live_now),
                    isLive = true
                )
                NextGameState.SeasonComplete -> SeasonCompleteDetails(stats = stats)
                NextGameState.Unavailable -> Unit
            }
        }
    }
}

@Composable
private fun NextGameDetails(game: GameDomain, myTeam: TeamDomain?, status: String, isLive: Boolean) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val muted = onPrimary.copy(alpha = 0.8f)

    Row(verticalAlignment = Alignment.CenterVertically) {
        CardEyebrow(text = stringResource(R.string.next_game), color = muted, modifier = Modifier.weight(1f))
        Text(
            text = game.week.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            color = muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    Row(
        modifier = Modifier.padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamLogo(
            team = myTeam,
            size = 40.dp,
            badgeColor = onPrimary,
            badgeTextColor = MaterialTheme.colorScheme.primary,
            contentDescription = myTeam?.let { stringResource(R.string.team_logo_description, it.name) }
        )
        Text(
            text = stringResource(if (game.isHome) R.string.game_vs else R.string.game_at).trim(),
            style = MaterialTheme.typography.bodyMedium,
            color = muted,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        TeamLogo(
            team = game.opponent,
            size = 40.dp,
            badgeColor = onPrimary,
            badgeTextColor = MaterialTheme.colorScheme.primary,
            contentDescription = game.opponent?.let { stringResource(R.string.team_logo_description, it.name) }
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = game.opponent?.name.orEmpty(),
                style = MaterialTheme.typography.headlineMedium,
                color = onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val opponent = game.opponent
            if (opponent != null) {
                Text(
                    text = listOfNotNull(opponent.city.takeIf { it.isNotBlank() }, opponent.record).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = muted
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    if (game.date.isNotBlank() || game.time.isNotBlank()) {
        IconLine(
            icon = Icons.Default.DateRange,
            text = listOf(game.date, game.time).filter { it.isNotBlank() }.joinToString(" · ")
        )
    }
    val venueAndTv = listOfNotNull(
        game.venue?.takeIf { it.isNotBlank() },
        game.tvStation?.takeIf { it.isNotBlank() }?.let { stringResource(R.string.tv_station, it) }
    ).joinToString(" · ")
    if (venueAndTv.isNotEmpty()) {
        IconLine(icon = Icons.Default.Place, text = venueAndTv)
    }

    Spacer(modifier = Modifier.height(12.dp))
    StatusPill(text = status, isLive = isLive)
}

@Composable
private fun IconLine(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun StatusPill(text: String, isLive: Boolean) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLive) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (isLive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SeasonCompleteDetails(stats: SeasonStats) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val muted = onPrimary.copy(alpha = 0.8f)

    CardEyebrow(text = stringResource(R.string.season_complete), color = muted)
    Text(
        text = stringResource(R.string.final_record, stats.record.formatted()),
        style = MaterialTheme.typography.headlineMedium,
        color = onPrimary,
        modifier = Modifier.padding(top = 8.dp)
    )
    if (stats.postseasonRecord.gamesPlayed > 0) {
        Text(
            text = stringResource(R.string.postseason_record, stats.postseasonRecord.formatted()),
            style = MaterialTheme.typography.bodyMedium,
            color = muted
        )
    }
    stats.lastResult?.let { last ->
        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.last_game),
                style = MaterialTheme.typography.bodyMedium,
                color = muted,
                modifier = Modifier.padding(end = 12.dp)
            )
            Box(
                modifier = Modifier
                    .background(onPrimary, RoundedCornerShape(50))
                    .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
            ) {
                ResultLine(result = last, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// endregion

// region Filters

@Composable
fun ScheduleFilterRow(
    selected: ScheduleFilterType,
    onFilterSelected: (ScheduleFilterType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(R.dimen.padding_medium), vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ScheduleFilterType.entries.forEach { filter ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    // 13sp keeps all five chips on one line on a typical 411dp-wide phone.
                    Text(text = stringResource(filter.labelRes()), fontSize = 13.sp)
                },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@StringRes
private fun ScheduleFilterType.labelRes(): Int = when (this) {
    ScheduleFilterType.ALL -> R.string.filter_all
    ScheduleFilterType.HOME -> R.string.filter_home
    ScheduleFilterType.AWAY -> R.string.filter_away
    ScheduleFilterType.RESULTS -> R.string.filter_results
    ScheduleFilterType.UPCOMING -> R.string.filter_upcoming
}

/** Shown in place of the list when the selected filter matches no games. */
@Composable
fun FilterEmptyView(filter: ScheduleFilterType, modifier: Modifier = Modifier) {
    val message = when (filter) {
        ScheduleFilterType.UPCOMING -> R.string.filter_empty_upcoming
        ScheduleFilterType.RESULTS -> R.string.filter_empty_results
        else -> R.string.filter_empty_default
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium), vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
    }
}

// endregion

@Preview(showBackground = true, widthDp = 411)
@Composable
fun SeasonComponentsPreview() {
    val opponent = TeamDomain(name = "RAMS", city = "Los Angeles", triCode = "LAR", record = "10-6", logoUrl = "")
    val bears = TeamDomain(name = "BEARS", city = "Chicago", triCode = "CHI", record = "8-8", logoUrl = "")
    val packers = TeamDomain(name = "PACKERS", city = "Green Bay", triCode = "GB", record = "13-3", logoUrl = "")
    val lastGame = GameDomain(
        id = "1", type = GameType.FINAL, week = "Week 17", date = "Sun, Jan 3", time = "3:25 PM",
        opponent = bears, isHome = false, myScore = "35", opponentScore = "16", tvStation = "FOX",
        gameState = "Final", venue = "Soldier Field"
    )
    val nextGame = GameDomain(
        id = "2", type = GameType.SCHEDULED, week = "Divisional Playoffs", date = "Sat, Jan 16", time = "3:35 PM",
        opponent = opponent, isHome = true, myScore = "", opponentScore = "", tvStation = "FOX",
        gameState = "Pregame", venue = "Lambeau Field", phase = SeasonPhase.POSTSEASON
    )
    val stats = SeasonStats(
        record = Record(13, 3), homeRecord = Record(7, 1), awayRecord = Record(6, 2),
        pointsFor = 509, pointsAgainst = 369, postseasonRecord = Record.EMPTY,
        streak = Streak(GameOutcome.WIN, 6),
        lastResult = CompletedGame(lastGame, GameOutcome.WIN, 35, 16), skippedGames = 0
    )
    AssessmentTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            SeasonAtAGlanceCard(stats = stats)
            NextGameCard(
                state = NextGameState.Upcoming(nextGame, Duration.ofHours(52)),
                stats = stats,
                myTeam = packers
            )
            ScheduleFilterRow(selected = ScheduleFilterType.ALL, onFilterSelected = {})
            NextGameCard(state = NextGameState.SeasonComplete, stats = stats, myTeam = packers)
        }
    }
}
