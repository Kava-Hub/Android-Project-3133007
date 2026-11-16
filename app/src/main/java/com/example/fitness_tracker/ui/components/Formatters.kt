package com.example.fitness_tracker.ui.components

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun LocalDate.displayDate() = format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH))
internal fun LocalDate.shortDate() = format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH))
internal fun Int.grouped() = String.format(Locale.US, "%,d", this)
internal val LocalMeasureUnits = androidx.compose.runtime.staticCompositionLocalOf { com.example.fitness_tracker.data.MeasureUnits.Metric }
@androidx.compose.runtime.Composable
internal fun Double.kilometres(): String = if (LocalMeasureUnits.current == com.example.fitness_tracker.data.MeasureUnits.Imperial)
    String.format(Locale.US, "%.2f mi", this / 1.609344) else String.format(Locale.US, "%.2f km", this)

