package com.example.fitness_tracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    // keep activity palette consistent across the app
    MaterialTheme(colorScheme = FitnessColors, typography = Typography, content = content)
}