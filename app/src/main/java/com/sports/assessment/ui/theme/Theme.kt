package com.sports.assessment.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AppTeal,
    onPrimary = Color.White,
    secondary = AppGrey,
    onSecondary = Color.White,
    background = AppBlack,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = AppGrey,
    error = Color(0xFFCF6679)
)

private val LightColorScheme = lightColorScheme(
    primary = AppTeal,
    onPrimary = Color.White,
    secondary = AppGrey,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = AppBlack,
    surface = Color.White,
    onSurface = AppBlack,
    surfaceVariant = AppLightGrey,
    onSurfaceVariant = AppGrey,
    error = Color(0xFFB00020)
)

@Composable
fun AssessmentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamicColor to ensure strict brand continuity for the assessment
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
