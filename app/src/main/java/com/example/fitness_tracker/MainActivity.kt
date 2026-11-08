package com.example.fitness_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitness_tracker.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent { Fitness_TrackerTheme { FitnessApp() } }
    }
}

private enum class FitnessTab(val title: String, val glyph: Glyph) {
    Summary("Summary", Glyph.Rings),
    Workout("Workout", Glyph.Run),
    Sharing("Sharing", Glyph.People),
}

@Composable
private fun FitnessApp() {
    val context = LocalContext.current.applicationContext
    val store = remember(context) { FitnessStore(context) }
    var goals by remember { mutableStateOf(store.goals()) }
    var goalsConfirmed by remember { mutableStateOf(store.goalsConfirmed()) }
    var cards by remember { mutableStateOf(store.visibleCards()) }
    var savedSessions by remember { mutableStateOf(store.sessions()) }
    val sessions = savedSessions + DemoSessions

    var tab by rememberSaveable { mutableStateOf(FitnessTab.Summary) }
    var path by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    val pages = rememberSaveableStateHolder()
    val route = path.lastOrNull()
    val active = route?.startsWith("active/") == true

    fun open(destination: String) {
        if (path.lastOrNull() != destination) path = ArrayList(path + destination)
    }
    fun back() {
        path.lastOrNull()?.let(pages::removeState)
        path = ArrayList(path.dropLast(1))
    }
    fun finishWorkout() {
        path.forEach(pages::removeState)
        path = arrayListOf()
    }

    BackHandler(enabled = route != null && !active) { back() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ScreenBlack,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (route == null) TabBar(tab) { tab = it }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            if (route != null && !active) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(start = 20.dp, top = 5.dp)) {
                        IconButton(Glyph.Back, "Go back") { back() }
                    }
                }
            }
            // tabs and detail screens keep separate scroll positions and selection state.
            pages.SaveableStateProvider(route ?: "tab/${tab.name}") {
                when {
                    route == null -> when (tab) {
                        FitnessTab.Summary -> SummaryScreen(goals, goalsConfirmed, cards, sessions, ::open)
                        FitnessTab.Workout -> WorkoutScreen(sessions, ::open)
                        FitnessTab.Sharing -> SharingScreen(DemoFriends, ::open)
                    }
                    route == "activity" -> ActivityDetails(DemoWeek, goals)
                    route == "profile" -> ProfileScreen { open("goals") }
                    route == "goals" -> GoalScreen(goals) {
                        store.saveGoals(it)
                        goals = it
                        goalsConfirmed = true
                        back()
                    }
                    route == "edit" -> EditSummaryScreen(cards) {
                        store.saveCards(it)
                        cards = it
                        back()
                    }
                    route == "categories" -> CategoriesScreen(::open)
                    route.startsWith("category/") -> CategoryDetails(
                        Category.valueOf(route.substringAfter('/')), sessions
                    ) { open("session/${it.id}") }
                    route.startsWith("prepare/") -> {
                        val type = WorkoutType.valueOf(route.substringAfter('/'))
                        PrepareWorkout(type) { open("active/${type.name}/$it") }
                    }
                    route.startsWith("active/") -> {
                        val parts = route.split('/')
                        ActiveWorkout(
                            WorkoutType.valueOf(parts[1]), parts[2].toInt(),
                            onSave = { log ->
                                savedSessions = listOf(log) + savedSessions
                                store.saveSessions(savedSessions)
                                finishWorkout()
                                open("session/${log.id}")
                            },
                            onDiscard = ::finishWorkout,
                        )
                    }
                    route.startsWith("session/") -> {
                        sessions.firstOrNull { it.id.toString() == route.substringAfter('/') }?.let { WorkoutSessionScreen(it) }
                    }
                    route.startsWith("friend/") -> {
                        val friend = DemoFriends[route.substringAfter('/').toInt()]
                        ActivityDetails(friend.week, Goals(), friend.name, friend.highlight)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabBar(current: FitnessTab, select: (FitnessTab) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Color(0xFF151517))) {
        HorizontalDivider(color = RaisedGrey)
        Box(
            Modifier.fillMaxWidth().windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
            ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                Modifier.widthIn(max = 560.dp).fillMaxWidth().selectableGroup().padding(horizontal = 12.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FitnessTab.entries.forEach { tab ->
                    val selected = current == tab
                    val tint = if (selected) FocusGreen else TextMuted
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                            .press(Role.Tab, selected) { select(tab) }
                            .heightIn(min = 54.dp).padding(vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                    ) {
                        Box(
                            Modifier.clip(RoundedCornerShape(14.dp))
                                .background(if (selected) FocusGreen.copy(alpha = 0.12f) else Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) { FitnessIcon(tab.glyph, tint, 23.dp) }
                        Text(tab.title, color = tint, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852, backgroundColor = 0xFF000000)
@Composable
private fun FitnessPreview() {
    Fitness_TrackerTheme { FitnessApp() }
}