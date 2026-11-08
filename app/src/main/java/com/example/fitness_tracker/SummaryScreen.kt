package com.example.fitness_tracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness_tracker.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun SummaryScreen(
    goals: Goals,
    goalsConfirmed: Boolean,
    visibleCards: Set<Category>,
    sessions: List<WorkoutLog>,
    open: (String) -> Unit,
) {
    val today = DemoWeek.last()
    ScreenColumn {
        PageTitle("Summary", today.date.displayDate()) {
            Avatar("JJ", Modifier.clip(CircleShape).press { open("profile") }
                .semantics { contentDescription = "Profile" }, palette = 3)
        }
        if (!goalsConfirmed) {
            Panel(Modifier.fillMaxWidth(), onClick = { open("goals") }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconDisc(Glyph.Bolt)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Eyebrow("FINISH SETUP", FocusGreen)
                        Text("Set your activity goals", style = MaterialTheme.typography.titleMedium)
                    }
                    FitnessIcon(Glyph.Chevron, FocusGreen, 17.dp)
                }
            }
        }
        ActivityCard(today, goals) { open("activity") }
        val grid = Category.entries.filter { it in visibleCards && it != Category.Trends }
        if (grid.isNotEmpty()) {
            // Equal columns and repeated card anatomy make the four categories easy to scan.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                grid.chunked(2).forEach { pair ->
                    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { category ->
                            SummaryTile(category, today, sessions) { open("category/${category.name}") }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        if (Category.Trends in visibleCards) TrendCard { open("category/Trends") }
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            ActionButton("Edit Summary", { open("edit") }, icon = Glyph.Edit)
            ActionButton("See All Categories", { open("categories") }, icon = Glyph.Grid)
        }
    }
}

@Composable
private fun ActivityCard(today: ActivityDay, goals: Goals, onClick: (() -> Unit)? = null) {
    Panel(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Activity Rings", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            if (onClick != null) {
                Text("Today", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                FitnessIcon(Glyph.Chevron, TextMuted, 14.dp)
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val ringSize = (maxWidth * 0.46f).coerceAtMost(154.dp)
            Row(
                Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                ActivityRings(today.progress(goals), Modifier.size(ringSize))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    RingMetric("Move", "${today.move}/${goals.move}", "kcal", MovePink)
                    RingMetric("Exercise", "${today.exercise}/${goals.exercise}", "min", ExerciseGreen)
                    RingMetric("Stand", "${today.stand}/${goals.stand}", "hr", StandCyan)
                }
            }
        }
    }
}

@Composable
private fun RingMetric(label: String, value: String, unit: String, tint: Color) {
    Column {
        Text(label, color = TextMuted, style = MaterialTheme.typography.labelMedium)
        Text("$value $unit", color = tint, fontSize = 20.sp, lineHeight = 23.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp)
    }
}

@Composable
private fun RowScope.SummaryTile(category: Category, today: ActivityDay, sessions: List<WorkoutLog>, onClick: () -> Unit) {
    val (headline, caption, tint) = when (category) {
        Category.Steps -> Triple(today.steps.grouped(), "Today", StepViolet)
        Category.Distance -> Triple(today.distance.kilometres(), "Today", DistanceBlue)
        Category.Sessions -> Triple("${sessions.size} sessions", "Latest workouts", FocusGreen)
        Category.Awards -> Triple("Perfect Week", "Latest award", AwardGold)
        Category.Trends -> error("Trends uses a wide card")
    }
    Panel(Modifier.weight(1f).fillMaxHeight(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FitnessIcon(category.icon, tint, 17.dp)
            Text(category.label, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            FitnessIcon(Glyph.Chevron, TextMuted, 12.dp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(headline, color = tint, fontSize = 21.sp, fontWeight = FontWeight.Bold,
                letterSpacing = (-0.45).sp, lineHeight = 24.sp)
            Text(caption, color = TextMuted, style = MaterialTheme.typography.labelMedium)
        }
        when (category) {
            Category.Steps -> Bars(DemoWeek.map { it.steps.toFloat() }, tint, Modifier.height(21.dp), selected = 6)
            Category.Distance -> Bars(DemoWeek.map { it.distance.toFloat() }, tint, Modifier.height(21.dp), selected = 6)
            else -> Unit
        }
    }
}

@Composable
private fun TrendCard(open: () -> Unit) {
    Panel(Modifier.fillMaxWidth(), onClick = open) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FitnessIcon(Glyph.Trend, FocusGreen, 20.dp)
            Text("Trends", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text("Last 7 days", color = TextMuted, style = MaterialTheme.typography.labelMedium)
            FitnessIcon(Glyph.Chevron, TextMuted, 14.dp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TrendValue("Move", "${DemoWeek.map { it.move }.average().toInt()} kcal", MovePink)
            TrendValue("Exercise", "${DemoWeek.map { it.exercise }.average().toInt()} min", ExerciseGreen)
        }
    }
}

@Composable
private fun RowScope.TrendValue(label: String, value: String, tint: Color) {
    Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(RaisedGrey).padding(12.dp)) {
        Text(label, color = TextMuted, style = MaterialTheme.typography.labelMedium)
        Text(value, color = tint, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
internal fun ActivityDetails(
    week: List<ActivityDay>,
    goals: Goals,
    title: String = "Activity",
    highlight: String? = null,
) {
    var selected by rememberSaveable { mutableIntStateOf(week.lastIndex) }
    val day = week[selected]
    ScreenColumn {
        PageTitle(title, "LAST 7 DAYS")
        WeekPicker(week, goals, selected) { selected = it }
        SectionTitle(day.date.displayDate(), if (selected == week.lastIndex) "Today" else null)
        ActivityCard(day, goals)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricChart("Move", "${day.move} kcal", week.map { it.move.toFloat() }, MovePink, selected, week)
            MetricChart("Exercise", "${day.exercise} min", week.map { it.exercise.toFloat() }, ExerciseGreen, selected, week)
            MetricChart("Stand", "${day.stand} hr", week.map { it.stand.toFloat() }, StandCyan, selected, week)
        }
        highlight?.let {
            Panel(Modifier.fillMaxWidth()) {
                Text("Latest highlight", style = MaterialTheme.typography.titleMedium)
                Text(it, color = TextMuted)
            }
        }
    }
}

@Composable
private fun WeekPicker(week: List<ActivityDay>, goals: Goals, selected: Int, onSelect: (Int) -> Unit) {
    Panel(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().selectableGroup()) {
            week.forEachIndexed { index, day ->
                val chosen = index == selected
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .press(role = Role.Tab, selected = chosen) { onSelect(index) }
                        .background(if (chosen) RaisedGrey else Color.Transparent)
                        .semantics { contentDescription = day.date.displayDate() }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(day.date.weekdayLabel(), color = if (chosen) FocusGreen else TextMuted, fontSize = 11.sp)
                    ActivityRings(day.progress(goals), Modifier.fillMaxWidth(0.78f).height(24.dp), animate = false)
                    Text(day.date.dayOfMonth.toString(), fontSize = 12.sp, color = if (chosen) FocusGreen else TextMuted)
                }
            }
        }
    }
}

@Composable
private fun Bars(values: List<Float>, tint: Color, modifier: Modifier, selected: Int? = null) {
    Canvas(modifier.fillMaxWidth()) {
        if (values.isEmpty()) return@Canvas
        val max = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val slot = size.width / values.size
        val barWidth = slot * 0.56f
        values.forEachIndexed { index, value ->
            if (value <= 0f) return@forEachIndexed
            val height = (size.height * value / max).coerceAtLeast(1f)
            drawRoundRect(
                color = tint.copy(alpha = if (selected == null || selected == index) 1f else 0.42f),
                topLeft = Offset(slot * index + (slot - barWidth) / 2, size.height - height),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(barWidth.coerceAtMost(10f) / 2)
            )
        }
    }
}

@Composable
private fun MetricChart(
    title: String,
    value: String,
    values: List<Float>,
    tint: Color,
    selected: Int? = null,
    week: List<ActivityDay> = DemoWeek,
) {
    Panel(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text(value, color = tint, style = MaterialTheme.typography.titleMedium)
        }
        Bars(values, tint, Modifier.height(62.dp), selected)
        Row(Modifier.fillMaxWidth()) {
            week.forEachIndexed { index, day ->
                Text(day.date.weekdayLabel(), Modifier.weight(1f),
                    color = if (index == selected) tint else TextMuted, fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

private fun LocalDate.weekdayLabel() = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

@Composable
internal fun CategoryDetails(category: Category, sessions: List<WorkoutLog>, onSession: (WorkoutLog) -> Unit) {
    ScreenColumn {
        PageTitle(category.label, "SUMMARY")
        when (category) {
            Category.Steps -> MetricChart("Today", DemoWeek.last().steps.grouped(), DemoWeek.map { it.steps.toFloat() }, StepViolet, 6)
            Category.Distance -> MetricChart("Today", DemoWeek.last().distance.kilometres(), DemoWeek.map { it.distance.toFloat() }, DistanceBlue, 6)
            Category.Sessions -> SessionHistory(sessions, onSession)
            Category.Awards -> {
                Panel(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.fillMaxWidth().height(132.dp).clip(CardShape)
                            .background(Brush.radialGradient(listOf(Color(0xFF584022), CardGrey))),
                        contentAlignment = Alignment.Center
                    ) { IconDisc(Glyph.Award, AwardGold, 90.dp) }
                    Text("Perfect Week", style = MaterialTheme.typography.headlineSmall)
                    Text("Reached the Move goal seven days in a row.", color = TextMuted)
                    Eyebrow("EARNED OCT 25", AwardGold)
                }
            }
            Category.Trends -> {
                SectionTitle("Weekly averages")
                MetricChart("Move", "${DemoWeek.map { it.move }.average().toInt()} kcal", DemoWeek.map { it.move.toFloat() }, MovePink)
                MetricChart("Exercise", "${DemoWeek.map { it.exercise }.average().toInt()} min", DemoWeek.map { it.exercise.toFloat() }, ExerciseGreen)
                MetricChart("Steps", DemoWeek.map { it.steps }.average().toInt().grouped(), DemoWeek.map { it.steps.toFloat() }, StepViolet)
            }
        }
    }
}

@Composable
internal fun CategoriesScreen(open: (String) -> Unit) {
    ScreenColumn {
        PageTitle("All Categories", "SUMMARY")
        Category.entries.forEach { category ->
            Panel(Modifier.fillMaxWidth(), onClick = { open("category/${category.name}") }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconDisc(category.icon)
                    Text(category.label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    FitnessIcon(Glyph.Chevron, TextMuted, 15.dp)
                }
            }
        }
    }
}

@Composable
internal fun EditSummaryScreen(cards: Set<Category>, save: (Set<Category>) -> Unit) {
    var selection by rememberSaveable { mutableStateOf(ArrayList(cards.map { it.name })) }
    ScreenColumn {
        PageTitle("Edit Summary")
        Text("Choose the cards you want to see below your activity rings.", color = TextMuted)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Category.entries.forEach { category ->
                val checked = category.name in selection
                Row(
                    Modifier.fillMaxWidth().clip(CardShape).press(role = Role.Checkbox, selected = checked) {
                        selection = ArrayList(if (checked) selection - category.name else selection + category.name)
                    }.background(CardGrey).heightIn(min = 58.dp).padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    IconDisc(category.icon, if (checked) FocusGreen else TextMuted, 36.dp)
                    Text(category.label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Box(
                        Modifier.size(25.dp).clip(CircleShape).background(if (checked) FocusGreen else RaisedGrey),
                        contentAlignment = Alignment.Center
                    ) { if (checked) FitnessIcon(Glyph.Check, ScreenBlack, 16.dp) }
                }
            }
        }
        ActionButton("Done", { save(Category.entries.filter { it.name in selection }.toSet()) }, primary = true, icon = Glyph.Check)
    }
}

@Composable
internal fun ProfileScreen(onGoals: () -> Unit) {
    ScreenColumn {
        PageTitle("Profile")
        Panel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Avatar("JJ", palette = 3)
                Column {
                    Text("John Jones", style = MaterialTheme.typography.titleMedium)
                    Text("Sample profile", color = TextMuted)
                }
            }
        }
        ActionButton("Activity Goals", onGoals, icon = Glyph.Rings)
    }
}

@Composable
internal fun GoalScreen(goals: Goals, save: (Goals) -> Unit) {
    var move by rememberSaveable { mutableIntStateOf(goals.move) }
    var exercise by rememberSaveable { mutableIntStateOf(goals.exercise) }
    var stand by rememberSaveable { mutableIntStateOf(goals.stand) }
    ScreenColumn {
        PageTitle("Activity Goals")
        Text("Pick daily goals that fit your routine. The suggested values are a starting point.", color = TextMuted)
        GoalChoices("Move", "kcal", listOf(400, 600, 800), 600, move, MovePink) { move = it }
        GoalChoices("Exercise", "min", listOf(15, 30, 45), 30, exercise, ExerciseGreen) { exercise = it }
        GoalChoices("Stand", "hr", listOf(8, 10, 12), 12, stand, StandCyan) { stand = it }
        ActionButton("Save Goals", { save(Goals(move, exercise, stand)) }, primary = true, icon = Glyph.Check)
    }
}

@Composable
private fun GoalChoices(
    title: String, unit: String, options: List<Int>, suggested: Int, selected: Int, tint: Color, onSelect: (Int) -> Unit,
) {
    Panel(Modifier.fillMaxWidth()) {
        Text(title, color = tint, style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { value ->
                ChoicePill("$value $unit", selected == value) { onSelect(value) }
            }
        }
        Text("Suggested: $suggested $unit", color = TextMuted, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
internal fun RowScope.ChoicePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).press(Role.RadioButton, selected, onClick)
            .background(if (selected) FocusGreen else RaisedGrey).heightIn(min = 48.dp).padding(horizontal = 4.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) ScreenBlack else Color.White, fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
internal fun SessionHistory(sessions: List<WorkoutLog>, onSelect: ((WorkoutLog) -> Unit)? = null) {
    Panel(Modifier.fillMaxWidth()) {
        if (sessions.isEmpty()) Text("No sessions yet", color = TextMuted)
        sessions.forEachIndexed { index, session ->
            if (index > 0) HorizontalDivider(color = RaisedGrey)
            val modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            Row(
                (if (onSelect != null) modifier.press { onSelect(session) } else modifier).padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconDisc(session.type.icon)
                Column(Modifier.weight(1f)) {
                    Text(session.type.label, style = MaterialTheme.typography.titleMedium)
                    Text(if (session.id < 0 && session.date == DemoToday) "Today" else session.date.shortDate(), color = TextMuted)
                }
                Text(formatTimer(session.seconds), color = FocusGreen, style = MaterialTheme.typography.labelMedium)
                if (onSelect != null) FitnessIcon(Glyph.Chevron, TextMuted, 14.dp)
            }
        }
    }
}