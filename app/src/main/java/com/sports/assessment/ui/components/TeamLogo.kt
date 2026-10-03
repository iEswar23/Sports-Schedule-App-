package com.sports.assessment.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.sports.assessment.domain.model.TeamDomain

/**
 * A team logo loaded with Coil. While the image is loading, or when it cannot be loaded (offline,
 * unknown team, missing URL), a round badge with the team's tricode is shown instead.
 */
@Composable
fun TeamLogo(
    team: TeamDomain?,
    size: Dp,
    modifier: Modifier = Modifier,
    badgeColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    badgeTextColor: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String? = null,
) {
    val painter = rememberAsyncImagePainter(model = team?.logoUrl?.takeIf { it.isNotBlank() })
    val loaded = painter.state is AsyncImagePainter.State.Success

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (!loaded) {
            TricodeBadge(
                triCode = team?.triCode.orEmpty(),
                size = size,
                color = badgeColor,
                textColor = badgeTextColor,
            )
        }
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun TricodeBadge(triCode: String, size: Dp, color: Color, textColor: Color) {
    val label = triCode.trim().uppercase().take(3).ifEmpty { "?" }
    // Sized from the badge in dp (ignoring the user's font scale) so the letters always fit.
    val fontSize = with(LocalDensity.current) { (size * if (label.length >= 3) 0.32f else 0.4f).toSp() }
    Box(
        modifier = Modifier
            .size(size)
            .background(color = color, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = fontSize,
            lineHeight = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )
    }
}
