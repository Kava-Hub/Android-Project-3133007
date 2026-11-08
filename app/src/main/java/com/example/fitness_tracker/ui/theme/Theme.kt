package com.example.fitness_tracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val FitnessColors = darkColorScheme(
    primary = FocusGreen,
    onPrimary = ScreenBlack,
    primaryContainer = Color(0xFF2D3812),
    onPrimaryContainer = FocusGreen,
    background = ScreenBlack,
    onBackground = Color(0xFFF7F7F9),
    surface = CardGrey,
    onSurface = Color(0xFFF7F7F9),
    onSurfaceVariant = TextMuted,
    surfaceVariant = RaisedGrey,
    outline = Color(0xFF424247)
)

@Composable
fun Fitness_TrackerTheme(content: @Composable () -> Unit) {
    // set activity palette across the whole android app
    MaterialTheme(
        colorScheme = FitnessColors,
        typography = Typography,
        content = content)
}