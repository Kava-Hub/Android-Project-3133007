package com.example.fitness_tracker.ui.workout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import com.example.fitness_tracker.data.*
import com.example.fitness_tracker.ui.components.*
import com.example.fitness_tracker.ui.workout.formatTimer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness_tracker.ui.theme.*
import java.util.Locale

@Composable
internal fun WorkoutScreen(sessions: List<WorkoutLog>, open: (String) -> Unit) {
    val suggestion = sessions.firstOrNull()?.type ?: WorkoutType.Walk
    ScreenColumn {
        PageTitle("Workout")
        Box(
            Modifier.fillMaxWidth().clip(CardShape)
                .press { open("prepare/${suggestion.name}") }
                .background(Brush.linearGradient(listOf(Color(0xFF2D3817), Color(0xFF141B0D))))
                .padding(18.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Eyebrow("SUGGESTED WORKOUT", FocusGreen)
                    Text(suggestion.label, style = MaterialTheme.typography.headlineSmall)
                    Text("${suggestion.minutes} min · ${suggestion.detail}", color = Color(0xFFC4D1AB))
                }
                Box(Modifier.size(60.dp).clip(CircleShape).background(FocusGreen), contentAlignment = Alignment.Center) {
                    FitnessIcon(suggestion.icon, ScreenBlack, 32.dp)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Workouts")
            WorkoutType.entries.forEach { type ->
                Panel(Modifier.fillMaxWidth(), onClick = { open("prepare/${type.name}") }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconDisc(type.icon, size = 44.dp)
                        Column(Modifier.weight(1f)) {
                            Text(type.label, style = MaterialTheme.typography.titleMedium)
                            Text(type.detail, color = TextMuted, style = MaterialTheme.typography.labelMedium)
                        }
                        IconDisc(Glyph.Play, size = 32.dp)
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Recent Sessions")
            SessionHistory(sessions.take(3)) { open("session/${it.id}") }
            ActionButton("See All Sessions", { open("category/Sessions") })
        }
    }
}

@Composable
internal fun PrepareWorkout(type: WorkoutType, start: (Int) -> Unit) {
    var minutes by rememberSaveable { mutableIntStateOf(type.minutes) }
    ScreenColumn {
        PageTitle(type.label, "WORKOUT")
        WorkoutBadge(type)
        Panel(Modifier.fillMaxWidth()) {
            Text("Duration goal", style = MaterialTheme.typography.titleMedium)
            Text("Suggested: ${type.minutes} min", color = TextMuted)
            val times = (listOf(15, 30, 45) + type.minutes).distinct().sorted()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.selectableGroup()) {
                times.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { time -> ChoicePill("$time min", minutes == time) { minutes = time } }
                    }
                }
                Row(Modifier.fillMaxWidth()) { ChoicePill("Open Goal", minutes == 0) { minutes = 0 } }
            }
        }
        ActionButton("Start Workout", { start(minutes) }, primary = true, icon = Glyph.Play)
    }
}

@Composable
internal fun ActiveWorkout(
    type: WorkoutType, target: Int, state: WorkoutState,
    togglePause: () -> Unit, end: () -> Unit, dismissEnd: () -> Unit,
    onSave: () -> Unit, onDiscard: () -> Unit,
) {
    BackHandler(enabled = !state.ending) { end() }
    val seconds = state.seconds
    val targetSeconds = target * 60
    ScreenColumn {
        PageTitle(type.label, if (state.running) "WORKOUT IN PROGRESS" else "WORKOUT PAUSED")
        WorkoutBadge(type)
        Panel(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Eyebrow("ELAPSED TIME", FocusGreen)
                Text(
                    formatTimer(seconds), color = Color.White, fontSize = 48.sp, lineHeight = 58.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                )
                Text(
                    when {
                        target == 0 -> "Open Goal"
                        seconds >= targetSeconds -> "Duration goal reached"
                        else -> "$target min goal"
                    },
                    color = TextMuted
                )
            }
            if (target > 0) {
                val progress = (seconds.toFloat() / targetSeconds).coerceIn(0f, 1f)
                Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(RaisedGrey)
                    .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) }) {
                    Box(Modifier.fillMaxWidth(progress)
                        .height(6.dp).background(FocusGreen))
                }
            }
        }
        ActionButton(if (state.running) "Pause" else "Resume", togglePause, primary = true, icon = if (state.running) Glyph.Pause else Glyph.Play)
        ActionButton("End Workout", { end() }, icon = Glyph.Stop)
    }

    if (state.ending) {
        AlertDialog(
            onDismissRequest = dismissEnd,
            title = { Text("Workout Summary") },
            text = { Text("${type.label} · ${formatTimer(seconds)}") },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    if (seconds > 0) ActionButton("Save Session", onSave, primary = true, icon = Glyph.Check)
                    ActionButton("Discard", onDiscard)
                    ActionButton("Keep Going", dismissEnd)
                }
            }
        )
    }
}

@Composable
internal fun WorkoutSessionScreen(log: WorkoutLog) {
    ScreenColumn {
        PageTitle(log.type.label, log.date.displayDate())
        WorkoutBadge(log.type)
        Panel(Modifier.fillMaxWidth()) {
            Eyebrow("DURATION", FocusGreen)
            Text(formatTimer(log.seconds), style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Composable
private fun WorkoutBadge(type: WorkoutType) {
    Box(
        Modifier.fillMaxWidth().height(148.dp).clip(CardShape)
            .background(Brush.radialGradient(listOf(Color(0xFF35491A), Color(0xFF161E0D)))),
        contentAlignment = Alignment.Center
    ) { IconDisc(type.icon, size = 92.dp) }
}

internal fun formatTimer(seconds: Long): String {
    val minutes = seconds / 60
    return if (minutes < 60) String.format(Locale.US, "%02d:%02d", minutes, seconds % 60)
    else String.format(Locale.US, "%d:%02d:%02d", minutes / 60, minutes % 60, seconds % 60)
}