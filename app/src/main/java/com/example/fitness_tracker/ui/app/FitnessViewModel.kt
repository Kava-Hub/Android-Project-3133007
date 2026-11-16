package com.example.fitness_tracker.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fitness_tracker.AthlimaApplication
import com.example.fitness_tracker.data.*

/** Owns app-level actions; composables do not read or write SharedPreferences. */
internal class FitnessViewModel(private val repository: FitnessRepository, private val notifications: com.example.fitness_tracker.notifications.FitnessNotifications) : ViewModel() {
    val state = repository.state
    fun saveGoals(goals: Goals) = repository.saveGoals(goals)
    fun saveCards(cards: Set<Category>) = repository.saveCards(cards)
    fun saveSession(log: WorkoutLog) { updateActivity { repository.saveSession(log) } }
    fun saveActivity(day: ActivityDay) { updateActivity { repository.saveActivity(day) } }
    private fun updateActivity(update: () -> Unit) {
        val old = state.value
        update()
        val latest = state.value
        latest.awards.filter { award -> award.unlocked && old.awards.none { it.id == award.id && it.unlocked && it.count >= award.count } }
            .forEach { notifications.goalCompleted(it.id.hashCode(), "Award earned", it.title) }
        val today = java.time.LocalDate.now()
        val before = old.activity.firstOrNull { it.day.date == today }
        val after = latest.activity.firstOrNull { it.day.date == today }
        if (after != null) {
            val now = after.day.progress(after.goals)
            val prior = before?.day?.progress(before.goals) ?: listOf(0f,0f,0f)
            listOf("Move", "Exercise", "Stand").forEachIndexed { index, title ->
                if (now[index] >= 1 && prior[index] < 1) notifications.goalCompleted(100 + index, "$title goal complete", "You closed your $title ring today.")
            }
        }
    }

    fun saveProfile(profile: UserProfile) = repository.saveProfile(profile)
    fun saveAccount(account: AccountSettings) = repository.saveAccount(account)

    companion object {
        val Factory = viewModelFactory {
            initializer { FitnessViewModel((this[APPLICATION_KEY] as AthlimaApplication).repository, (this[APPLICATION_KEY] as AthlimaApplication).notifications) }
        }
    }
}
