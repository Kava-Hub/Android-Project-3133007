package com.example.fitness_tracker.ui.awards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness_tracker.ui.components.PageTitle
import com.example.fitness_tracker.ui.components.press
import com.example.fitness_tracker.ui.theme.AwardGold
import com.example.fitness_tracker.ui.theme.CardGrey
import com.example.fitness_tracker.ui.theme.ExerciseGreen
import com.example.fitness_tracker.ui.theme.MovePink
import com.example.fitness_tracker.ui.theme.ScreenBlack
import com.example.fitness_tracker.ui.theme.StandCyan
import com.example.fitness_tracker.ui.theme.StepViolet
import com.example.fitness_tracker.ui.theme.TextMuted
import com.example.fitness_tracker.domain.AwardStatus
import com.example.fitness_tracker.ui.components.*
import com.example.fitness_tracker.ui.theme.*
import kotlin.collections.filter
import kotlin.math.*

/** Vector medals remain crisp, have distinct shapes/colors, and need no external bitmap assets. */
@Composable
internal fun AwardBadge(award: AwardStatus, modifier: Modifier = Modifier) {
    val palette = when {
        "Exercise" in award.title -> ExerciseGreen
        "Stand" in award.title -> StandCyan
        award.id.startsWith("days") -> StepViolet
        "All Activity" in award.title -> AwardGold
        else -> MovePink
    }
    val tint by animateColorAsState(if (award.unlocked) palette else Color(0xFF44444B), label = "Earned medal")
    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val r = size.minDimension / 2
            val hexagon = award.id.startsWith("week") || award.id == "streak"
            val path = Path().apply {
                for (i in 0 until 6) {
                    val angle = (i * 60 - 90) * PI / 180
                    val x = center.x + r * cos(angle).toFloat(); val y = center.y + r * sin(angle).toFloat()
                    if (i == 0) moveTo(x,y) else lineTo(x,y)
                }; close()
            }
            val metallic = Brush.linearGradient(listOf(Color.White, tint, Color(0xFF30303A), tint, Color.White))
            if (hexagon) {
                drawPath(path, if (award.unlocked) metallic else Brush.linearGradient(listOf(ScreenBlack, CardGrey)))
                drawPath(path, tint, style = Stroke(3.dp.toPx()))
            } else {
                drawCircle(if (award.unlocked) metallic else Brush.linearGradient(listOf(ScreenBlack, CardGrey)), r)
                drawCircle(tint, r, style = Stroke(3.dp.toPx()))
            }
            drawCircle(ScreenBlack.copy(alpha = 0.65f), r * 0.72f)
            drawCircle(tint, r * 0.73f, style = Stroke(2.dp.toPx()))
            drawCircle(tint.copy(alpha = 0.5f), r * 0.9f, style = Stroke(1.dp.toPx()))
        }
        Text(award.symbol, color = if (award.unlocked) Color.White else TextMuted, fontSize = if (award.symbol.length > 3) 24.sp else 34.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun AwardsScreen(awards: List<AwardStatus>, logActivity: () -> Unit) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var filter by rememberSaveable { mutableStateOf("All") }
    val shown = awards.filter { when (filter) { "Earned" -> it.unlocked; "In Progress" -> !it.unlocked; else -> true } }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        PageTitle("Close Your Rings", "AWARDS")
        Text("${awards.count { it.unlocked }} of ${awards.size} earned · recorded activity only", color = TextMuted, modifier = Modifier.padding(vertical = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("All", "Earned", "In Progress").forEach { value -> FilterChip(filter == value, { filter = value }, { Text(value) }) } }
        TextButton(onClick = logActivity) { Text("Log Activity") }
        if (shown.isEmpty()) Text("No awards in this category yet. Log your activity to start.", color = TextMuted)
        LazyVerticalGrid(GridCells.Adaptive(104.dp), verticalArrangement = Arrangement.spacedBy(24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
            items(shown, key = { it.id }) { award ->
                Column(Modifier.animateItem().press { selectedId = award.id }.semantics { contentDescription = "${award.title}, ${if (award.unlocked) "earned" else "locked"}" }, horizontalAlignment = Alignment.CenterHorizontally) {
                    AwardBadge(award, Modifier.fillMaxWidth())
                    Text(award.title, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                    Text(if (award.id in listOf("move2", "move3", "move4")) "${award.count} ${if (award.count == 1) "time" else "times"}" else if (award.unlocked) "Earned ${award.earned}" else "${award.progress}/${award.target}", color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
    awards.firstOrNull { it.id == selectedId }?.let { award ->
        AlertDialog(onDismissRequest = { selectedId = null }, title = { Text(award.title) },
            text = { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AwardBadge(award, Modifier.size(160.dp)); Text(award.detail)
                val progress by animateFloatAsState((award.progress.toFloat()/award.target.coerceAtLeast(1)).coerceIn(0f,1f), label = "Award progress")
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Text(if (award.unlocked) "Earned ${award.earned}" else "${award.progress} of ${award.target}")
                if (award.count > 0) Text("Earned ${award.count} ${if (award.count == 1) "time" else "times"}")
            } }, confirmButton = { TextButton(onClick = { selectedId = null }) { Text("Done") } })
    }
}
