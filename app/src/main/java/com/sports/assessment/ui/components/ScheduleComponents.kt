package com.sports.assessment.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sports.assessment.R
import com.sports.assessment.domain.model.GameDomain
import com.sports.assessment.domain.model.GameType
import com.sports.assessment.domain.model.TeamDomain
import com.sports.assessment.ui.theme.AssessmentTheme

@Composable
fun SeasonHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.season_header_height))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun TeamName(name: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = name.uppercase(),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun ScoreText(score: String?, modifier: Modifier = Modifier) {
    val defaultValue = stringResource(id = R.string.default_score)
    Text(
        text = score.ifNullOrEmpty { defaultValue },
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
fun ScheduleItemCard(game: GameDomain, myTeam: TeamDomain?) {
    val opponent = game.opponent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.padding_extra_small))
            .defaultMinSize(minHeight = dimensionResource(id = R.dimen.schedule_card_min_height)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.padding_extra_small))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(dimensionResource(id = R.dimen.padding_medium))
        ) {

            // Team Names Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen.padding_medium)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TeamName(name = myTeam?.name ?: "", modifier = Modifier.weight(1f))
                TeamName(
                    name = opponent?.name ?: "",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            // Central Area (Scores, Logos Stacked with Week)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // My Team Info
                Column(horizontalAlignment = Alignment.Start) {
                    ScoreText(score = game.myScore)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                    Text(
                        text = myTeam?.record ?: stringResource(id = R.string.default_record),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Center Content: Logos Row and Week Text one below another
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = myTeam?.logoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.team_logo_size))
                                .aspectRatio(1f),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = stringResource(id = R.string.game_at),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                        )
                        AsyncImage(
                            model = opponent?.logoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(dimensionResource(id = R.dimen.team_logo_size)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Text(
                        text = game.week,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Opponent Info
                Column(horizontalAlignment = Alignment.End) {
                    ScoreText(score = game.opponentScore)
                    Text(
                        text = opponent?.record ?: stringResource(id = R.string.default_record),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Bottom Info Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = game.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = game.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (!game.tvStation.isNullOrBlank()) {
                Text(
                    text = game.tvStation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
                )
            } else {
                VenueTag(venue = game.venue ?: "")
            }
        }
    }
}

@Composable
fun ByeWeekCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.padding_extra_small))
            .height(dimensionResource(id = R.dimen.schedule_card_min_height)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.padding_extra_small))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(id = R.string.game_bye),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_medium)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        Button(onClick = onRetry) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

@Composable
fun VenueTag(venue: String) {
    if (venue.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = dimensionResource(id = R.dimen.padding_small))
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_small))
                )
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_small),
                    vertical = dimensionResource(id = R.dimen.padding_extra_small)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(id = R.dimen.padding_medium)),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
            Text(
                text = venue.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleComponentsPreview() {
    val mockTeam = TeamDomain(
        name = "Packers",
        city = "Green Bay",
        triCode = "GB",
        record = "13-3",
        logoUrl = ""
    )
    val mockOpponent = TeamDomain(
        name = "Vikings",
        city = "Minnesota",
        triCode = "MIN",
        record = "7-9",
        logoUrl = ""
    )
    val mockGame = GameDomain(
        id = "1",
        type = GameType.FINAL,
        week = "Week 1",
        date = "Sep 13",
        time = "1:00 PM",
        opponent = mockOpponent,
        isHome = false,
        myScore = "43",
        opponentScore = "34",
        tvStation = "FOX",
        gameState = "Final",
        venue = ""
    )

    AssessmentTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            SeasonHeader(title = "Regular Season")
            ScheduleItemCard(game = mockGame, myTeam = mockTeam)
            ByeWeekCard()
        }
    }
}

@Preview(showBackground = true, name = "Loading and Error")
@Composable
fun StateViewsPreview() {
    AssessmentTheme {
        Column {
            Box(modifier = Modifier.height(100.dp)) {
                LoadingView()
            }
            HorizontalDivider()
            Box(modifier = Modifier.height(200.dp)) {
                ErrorView(message = "Failed to load schedule", onRetry = {})
            }
        }
    }
}

fun String?.ifNullOrEmpty(defaultValue: () -> String): String {
    return if (this.isNullOrEmpty()) defaultValue() else this
}