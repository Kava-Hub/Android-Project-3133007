package com.example.fitness_tracker.data

import com.example.fitness_tracker.domain.*
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.filterNot

internal data class FitnessState(
    val goals: Goals,
    val goalsConfirmed: Boolean,
    val cards: Set<Category>,
    val savedSessions: List<WorkoutLog>,
    val profile: UserProfile?,
    val onboardingComplete: Boolean,
    val account: AccountSettings = AccountSettings(),
    val activity: List<ActivityEntry> = emptyList(),
) {
    val awards get() = Awards.evaluate(activity)
    val week get() = if (activity.isEmpty()) DemoWeek else (6 downTo 0).map { offset ->
        val date = LocalDate.now().minusDays(offset.toLong())
        activity.firstOrNull { it.day.date == date }?.day ?: ActivityDay(date, 0, 0, 0, 0, 0.0)
    }
    val sessions get() = savedSessions + DemoSessions
}

/** Single source of truth for persisted app data. Screens observe state and send actions upwards. */
internal class FitnessRepository(private val store: FitnessStore) {
    private val mutableState = MutableStateFlow(FitnessState(
        store.goals(), store.goalsConfirmed(), store.visibleCards(), store.sessions(),
        store.profile(), store.onboardingComplete(), store.account(), store.activity(),
    ))
    val state = mutableState.asStateFlow()

    fun saveGoals(goals: Goals) {
        require(goals.move > 0 && goals.exercise > 0 && goals.stand in 1..24)
        if (state.value.goalsConfirmed && state.value.goals.move != goals.move) store.setPendingMoveGoal(true)
        store.saveGoals(goals)
        mutableState.update { it.copy(goals = goals, goalsConfirmed = true) }
    }
    fun saveCards(cards: Set<Category>) {
        store.saveCards(cards)
        mutableState.update { it.copy(cards = cards) }
    }
    fun saveSession(log: WorkoutLog) {
        val sessions = listOf(log) + state.value.savedSessions
        store.saveSessions(sessions)
        mutableState.update { it.copy(savedSessions = sessions) }
        val old = state.value.activity.firstOrNull { it.day.date == log.date }?.day ?: ActivityDay(log.date, 0, 0, 0, 0, 0.0)
        // measure the duration
        saveActivity(old.copy(exercise = old.exercise + (log.seconds / 60).toInt()))
    }
    fun saveProfile(profile: UserProfile) {
        store.saveProfile(profile)
        mutableState.update { it.copy(profile = profile) }
    }
    fun completeOnboarding() {
        store.completeOnboarding()
        mutableState.update { it.copy(onboardingComplete = true) }
    }
    fun saveAccount(account: AccountSettings) {
        store.saveAccount(account)
        mutableState.update { it.copy(account = account) }
    }

    fun saveActivity(day: ActivityDay) {
        require(day.date <= LocalDate.now() && day.move >= 0 && day.exercise >= 0 && day.stand in 0..24 && day.steps >= 0 && day.distance.isFinite() && day.distance >= 0)
        val prior = state.value.activity.firstOrNull { it.day.date == day.date }
        val pending = store.pendingMoveGoal() && day.date == LocalDate.now()
        val entry = ActivityEntry(day, if (pending) state.value.goals else prior?.goals ?: state.value.goals,
            prior?.newMoveGoal == true || (pending && day.move >= state.value.goals.move))
        if (pending && entry.newMoveGoal) store.setPendingMoveGoal(false)
        val updated = (state.value.activity.filterNot { it.day.date == day.date } + entry).sortedBy { it.day.date }
        store.saveActivity(updated)
        mutableState.update { it.copy(activity = updated) }
    }
}
