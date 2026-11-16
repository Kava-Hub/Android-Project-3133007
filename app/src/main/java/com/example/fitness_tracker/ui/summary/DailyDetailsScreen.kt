package com.example.fitness_tracker.ui.summary

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness_tracker.data.*
import com.example.fitness_tracker.domain.*
import com.example.fitness_tracker.ui.components.*
import com.example.fitness_tracker.ui.theme.*
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun ActivityDetails(
    week: List<ActivityDay>,
    goals: Goals,
    title: String = "Activity",
    highlight: String? = null,
    sample: Boolean = true,
    goalsByDate: Map<java.time.LocalDate, Goals> = emptyMap(),
    sessions: List<WorkoutLog> = emptyList(),
    onSession: (WorkoutLog) -> Unit = {}
) {
    if (week.isEmpty()) {
        ScreenColumn {
            PageTitle(title); Text("No activity recorded yet")
        };
        return
    }
    var selected by rememberSaveable { mutableIntStateOf(week.lastIndex) }
    val day = week[selected.coerceIn(week.indices)]
    val dayGoals = goalsByDate[day.date] ?: goals
    val scroll = rememberScrollState()
    val layer = rememberGraphicsLayer()
    val shade by animateFloatAsState(if (scroll.value > 20) 0.85f else 1f, label = "Header shade")
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = 560.dp).fillMaxSize()) {
            Column(Modifier.fillMaxSize().drawWithContent {
                layer.record { this@drawWithContent.drawContent() }; drawLayer(layer)
            }.verticalScroll(scroll).padding(horizontal = 20.dp).padding(top = 148.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)) {
                ActivityRings(day.progress(dayGoals), Modifier.fillMaxWidth().height(280.dp),
                    "Move ${day.move} of ${dayGoals.move}, Exercise ${day.exercise} of ${dayGoals.exercise}, Stand ${day.stand} of ${dayGoals.stand}", arrows = true)
                Text(if (sample) "Sample activity · hourly breakdown" else "Recorded totals · hourly breakdown unavailable", color = TextMuted, fontSize = 12.sp)
                AnimatedContent(day.date, label = "Selected day") { date ->
                    val chosen = week.first { it.date == date }
                    Column(verticalArrangement = Arrangement.spacedBy(26.dp)) {
                        HourChart("Move", "${chosen.move}/${dayGoals.move} kcal", if (sample) hourlyDistribution(chosen.move, date.dayOfMonth) else List(24) { 0 }, MovePink, "TOTAL ${chosen.move} ACTIVE KCAL", available = sample)
                        HourChart("Exercise", "${chosen.exercise}/${dayGoals.exercise} min", if (sample) hourlyDistribution(chosen.exercise, date.dayOfMonth + 2) else List(24) { 0 }, ExerciseGreen, "TOTAL ${chosen.exercise} MIN", available = sample)
                        HourChart("Stand", "${chosen.stand}/${dayGoals.stand} hrs", if (sample) standHours(chosen) else List(24) { 0 }, StandCyan, "${chosen.stand} STAND HOURS", true, available = sample)
                        Row(Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) { Text("Steps", color = TextMuted); Text(chosen.steps.grouped(), fontSize = 30.sp) }
                            Column(Modifier.weight(1f)) { Text("Distance", color = TextMuted); Text(chosen.distance.kilometres(), fontSize = 30.sp) }
                        }
                        SectionTitle("Workouts", chosen.date.shortDate())
                        SessionHistory(sessions.filter { it.date == chosen.date }, onSession)
                        highlight?.let { Text(it, color = TextMuted) }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(140.dp).clipToBounds()) {
                Canvas(Modifier.matchParentSize().blur(18.dp)) { drawLayer(layer) }
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = shade), Color.Black.copy(alpha = shade), Color.Black.copy(alpha = 0.7f)))))
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(day.date.displayDate(), style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth()) {
                        week.forEachIndexed { i, item ->
                            Column(Modifier.weight(1f).press(Role.Tab, i == selected) { selected = i }
                                .semantics { contentDescription = item.date.displayDate() }.padding(vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.ENGLISH), color = if (i == selected) FocusGreen else TextMuted)
                                ActivityRings(item.progress(goalsByDate[item.date] ?: goals), Modifier.size(32.dp), animate = false)
                                Text(item.date.dayOfMonth.toString(), color = if (i == selected) FocusGreen else TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun HourChart(
    label: String,
    value: String,
    bins: List<Int>,
    tint: Color,
    total: String,
    stand: Boolean = false,
    available: Boolean = true
) {
    var selected by rememberSaveable(label) { mutableIntStateOf(-1) }
    Column {
        Text(label, fontSize = 21.sp)
        Text(value, color = tint, fontSize = 30.sp)
        if (!available) {
            Text("Hourly data was not recorded for these daily totals.", color = TextMuted, fontSize = 12.sp)
            Text(total, color = tint, fontSize = 13.sp)
            return@Column
        }
        Text(
            if (selected >= 0) "%02d:00 · %d %s".format(selected, bins[selected],
            if (stand) "stand hour" else if (label == "Move") "kcal" else "min")
            else "Tap a bar for hourly details", color = TextMuted, fontSize = 12.sp
        )
        Row(
            Modifier.fillMaxWidth().height(100.dp).padding(top = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val max = bins.maxOrNull()?.coerceAtLeast(1) ?: 1
            bins.forEachIndexed { hour, count ->
                val fraction by animateFloatAsState(count.toFloat() / max, label = "Hourly bar")
                Box(
                    Modifier.weight(1f).fillMaxHeight().press { selected = hour }.semantics { contentDescription = "$label at $hour:00, $count" },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        Modifier.fillMaxWidth().fillMaxHeight(fraction.coerceAtLeast(0.025f))
                            .then(if (stand) Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) else Modifier)
                            .background(tint.copy(alpha = if (selected < 0 || selected == hour) 1f else 0.4f))
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            listOf("00:00", "06:00", "12:00", "18:00").forEach{
                Text(it, Modifier.weight(1f), color = TextMuted, fontSize = 12.sp)
            }
        }
        Text(total, color = tint, fontSize = 13.sp)
    }
}
