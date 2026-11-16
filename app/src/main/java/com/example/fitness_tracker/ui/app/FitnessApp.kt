package com.example.fitness_tracker.ui.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitness_tracker.AthlimaApplication
import com.example.fitness_tracker.data.*
import com.example.fitness_tracker.ui.components.*
import com.example.fitness_tracker.ui.summary.*
import com.example.fitness_tracker.ui.workout.*
import com.example.fitness_tracker.ui.sharing.*
import com.example.fitness_tracker.ui.onboarding.OnboardingRoute
import com.example.fitness_tracker.ui.awards.AwardsScreen

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.fitness_tracker.ui.app.FitnessViewModel
import com.example.fitness_tracker.ui.components.FitnessIcon
import com.example.fitness_tracker.ui.components.Glyph
import com.example.fitness_tracker.ui.components.LocalMeasureUnits
import com.example.fitness_tracker.ui.components.press
import com.example.fitness_tracker.ui.sharing.SharingScreen
import com.example.fitness_tracker.ui.summary.ActivityDetails
import com.example.fitness_tracker.ui.summary.CategoriesScreen
import com.example.fitness_tracker.ui.summary.CategoryDetails
import com.example.fitness_tracker.ui.summary.EditSummaryScreen
import com.example.fitness_tracker.ui.summary.GoalScreen
import com.example.fitness_tracker.ui.summary.HealthDetailsScreen
import com.example.fitness_tracker.ui.summary.IdentityScreen
import com.example.fitness_tracker.ui.summary.LogActivityScreen
import com.example.fitness_tracker.ui.summary.NotificationSettingsScreen
import com.example.fitness_tracker.ui.summary.ProfileScreen
import com.example.fitness_tracker.ui.summary.SummaryScreen
import com.example.fitness_tracker.ui.summary.UnitsScreen
import com.example.fitness_tracker.ui.theme.*
import com.example.fitness_tracker.ui.workout.ActiveWorkout
import com.example.fitness_tracker.ui.workout.PrepareWorkout
import com.example.fitness_tracker.ui.workout.WorkoutScreen
import com.example.fitness_tracker.ui.workout.WorkoutSessionScreen
import com.example.fitness_tracker.ui.workout.WorkoutViewModel

private enum class FitnessTab(val title: String, val glyph: Glyph) {
    Summary("Summary", Glyph.Rings),
    Workout("Workout", Glyph.Run),
    Sharing("Sharing", Glyph.People),
}

@Composable
internal fun FitnessApp(
    model: FitnessViewModel = viewModel(factory = FitnessViewModel.Factory),
    workoutModel: WorkoutViewModel = viewModel(),
) {
    val state by model.state.collectAsStateWithLifecycle()
    val workoutState by workoutModel.state.collectAsStateWithLifecycle()
    val application = LocalContext.current.applicationContext as AthlimaApplication
    if (!state.onboardingComplete) {
        OnboardingRoute()
        return
    }
    val goals = state.goals
    val goalsConfirmed = state.goalsConfirmed
    val cards = state.cards
    val sessions = state.sessions

    var tab by rememberSaveable { mutableStateOf(FitnessTab.Summary) }
    var path by rememberSaveable { mutableStateOf(listOf<String>()) }
    val pages = rememberSaveableStateHolder()
    val route = path.lastOrNull()
    val active = route?.startsWith("active/") == true

    fun open(destination: String) {
        if (path.lastOrNull() != destination) path = path + destination
    }
    fun back() {
        path.lastOrNull()?.let(pages::removeState)
        path = path.dropLast(1)
    }
    fun finishWorkout() {
        // Return to Workout rather than leaving the old setup form behind the saved session.
        path.forEach(pages::removeState)
        path = emptyList()
    }

    BackHandler(enabled = route != null && !active) { back() }

    CompositionLocalProvider(LocalMeasureUnits provides state.account.units) {
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
            // Tabs and detail screens keep separate scroll positions and selection state.
            pages.SaveableStateProvider(route ?: "tab/${tab.name}") {
                when {
                    route == null -> when (tab) {
                        FitnessTab.Summary -> SummaryScreen(goals, goalsConfirmed, cards, sessions, ::open, state.account, state.week, state.awards.count { it.unlocked })
                        FitnessTab.Workout -> WorkoutScreen(sessions, ::open)
                        FitnessTab.Sharing -> SharingScreen(DemoFriends, ::open)
                    }
                    route == "activity" -> ActivityDetails(state.week, goals, sessions = sessions, sample = state.activity.isEmpty(), goalsByDate = state.activity.associate { it.day.date to it.goals }, onSession = { open("session/${it.id}") })
                    route == "category/Awards" -> AwardsScreen(state.awards) { open("log") }
                    route == "log" -> LogActivityScreen(state.activity) { model.saveActivity(it); back() }
                    route == "profile" -> ProfileScreen(state.account, ::open)
                    route == "identity" -> IdentityScreen(state.account, model::saveAccount)
                    route == "health" -> HealthDetailsScreen(state.profile, state.account.units, model::saveProfile)
                    route == "units" -> UnitsScreen(state.account, model::saveAccount)
                    route == "notifications" -> NotificationSettingsScreen(state.account) {
                        model.saveAccount(it)
                        com.example.fitness_tracker.notifications.StandReminder.schedule(application)
                    }
                    route == "goals" -> GoalScreen(goals) {
                        model.saveGoals(it)
                        back()
                    }
                    route == "edit" -> EditSummaryScreen(cards) {
                        model.saveCards(it)
                        back()
                    }
                    route == "categories" -> CategoriesScreen(::open)
                    route.startsWith("category/") -> CategoryDetails(
                        Category.valueOf(route.substringAfter('/')), sessions, state.week
                    ) { open("session/${it.id}") }
                    route.startsWith("prepare/") -> {
                        val type = WorkoutType.valueOf(route.substringAfter('/'))
                        PrepareWorkout(type) {
                            workoutModel.start()
                            open("active/${type.name}/$it")
                        }
                    }
                    route.startsWith("active/") -> {
                        val parts = route.split('/')
                        ActiveWorkout(
                            WorkoutType.valueOf(parts[1]), parts[2].toInt(), workoutState,
                            togglePause = workoutModel::togglePause,
                            end = workoutModel::end,
                            dismissEnd = workoutModel::dismissEnd,
                            onSave = {
                                val log = workoutModel.log(WorkoutType.valueOf(parts[1]))
                                model.saveSession(log)
                                application.notifications.workoutSaved(log)
                                workoutModel.clear()
                                finishWorkout()
                                open("session/${log.id}")
                            },
                            onDiscard = {
                                workoutModel.clear()
                                finishWorkout()
                            },
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

