package com.example.fitness_tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.fitness_tracker.ui.theme.*
import kotlin.compareTo

@Composable
internal fun SharingScreen(friends: List<Friend>, open: (String) -> Unit) {
    ScreenColumn {
        PageTitle("Sharing")
        if (friends.isEmpty()) {
            Panel(Modifier.fillMaxWidth()) {
                IconDisc(Glyph.People)
                Text("No shared activity yet", style = MaterialTheme.typography.headlineSmall)
                Text("Activity rings and highlights from friends will appear here.", color = TextMuted)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Highlights", "Sample friends")
                // Let the next card peek into view to signal that the highlights scroll sideways.
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(friends) { index, friend ->
                        HighlightCard(friend, Modifier.fillParentMaxWidth(0.83f)) { open("friend/$index") }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                SectionTitle("Activity Rings", "Today")
                friends.forEachIndexed { index, friend ->
                    Panel(Modifier.fillMaxWidth(), onClick = { open("friend/$index") }) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val progress = friend.week.last().progress(Goals())
                            val closed = progress.count { it >= 1f }
                            Avatar(friend.initials, palette = friend.palette)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(friend.name, style = MaterialTheme.typography.titleMedium)
                                Text(if (closed == 3) "All rings closed" else "$closed of 3 rings closed", color = TextMuted)
                            }
                            ActivityRings(progress, Modifier.size(43.dp))
                            FitnessIcon(Glyph.Chevron, TextMuted, 13.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightCard(friend: Friend, modifier: Modifier, open: () -> Unit) {
    val tint = when (friend.icon) {
        Glyph.Rings -> MovePink
        Glyph.Award -> AwardGold
        else -> FocusGreen
    }
    val shade = when (friend.icon) {
        Glyph.Rings -> Color(0xFF321820)
        Glyph.Award -> Color(0xFF312617)
        else -> Color(0xFF232D16)
    }
    Panel(modifier, color = shade, onClick = open) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            IconDisc(friend.icon, tint)
            Text(friend.name, color = tint, style = MaterialTheme.typography.labelMedium)
        }
        Text(friend.headline, style = MaterialTheme.typography.headlineSmall)
        Text(friend.highlight, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
    }
}