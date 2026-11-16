package com.example.fitness_tracker.ui.components

import com.example.fitness_tracker.data.*
import com.example.fitness_tracker.ui.workout.formatTimer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.fitness_tracker.ui.theme.*

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
