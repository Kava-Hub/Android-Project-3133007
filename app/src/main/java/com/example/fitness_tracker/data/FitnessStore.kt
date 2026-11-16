package com.example.fitness_tracker.data

import com.example.fitness_tracker.domain.ActivityEntry
import android.content.Context
import com.example.fitness_tracker.data.BiologicalSex
import com.example.fitness_tracker.data.UserProfile
import com.example.fitness_tracker.data.AccountSettings
import com.example.fitness_tracker.data.MeasureUnits
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import kotlin.collections.map

// local persistence boundary
internal class FitnessStore(context: Context) {
    private val prefs = context.getSharedPreferences("fitness", Context.MODE_PRIVATE)

    fun visibleCards() = prefs.getStringSet("cards", null)?.let { names ->
        Category.entries.filter { it.name in names }.toSet()
    } ?: Category.entries.toSet()

    fun saveCards(cards: Set<Category>) {
        prefs.edit().putStringSet("cards", cards.map { it.name }.toSet()).apply()
    }

    fun goals() = Goals(prefs.getInt("move", 600), prefs.getInt("exercise", 30), prefs.getInt("stand", 12))
    fun goalsConfirmed() = prefs.getBoolean("goals_confirmed", false)

    fun saveGoals(goals: Goals) {
        prefs.edit().putInt("move", goals.move).putInt("exercise", goals.exercise)
            .putInt("stand", goals.stand).putBoolean("goals_confirmed", true).apply()
    }

    fun sessions(): List<WorkoutLog> = runCatching {
        val array = JSONArray(prefs.getString("sessions", "[]") ?: "[]")
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            WorkoutLog(
                item.getLong("id"), WorkoutType.valueOf(item.getString("type")),
                item.getLong("seconds"), LocalDate.parse(item.getString("date")),
            )
        }
    }.getOrDefault(emptyList())

    fun saveSessions(sessions: List<WorkoutLog>) {
        val array = JSONArray()
        sessions.forEach { session ->
            array.put(JSONObject().put("id", session.id).put("type", session.type.name)
                .put("seconds", session.seconds).put("date", session.date.toString()))
        }
        prefs.edit().putString("sessions", array.toString()).apply()
    }

    fun profile(): UserProfile? = runCatching {
        val json = JSONObject(prefs.getString("profile", null) ?: return null)
        UserProfile(
            LocalDate.parse(json.getString("birthDate")),
            BiologicalSex.valueOf(json.getString("sex")),
            json.getDouble("heightCm"),
            json.getDouble("weightKg")
        )
    }.getOrNull()

    fun saveProfile(profile: UserProfile) {
        val json = JSONObject().put("birthDate", profile.dateOfBirth.toString())
            .put("sex", profile.sex.name).put("heightCm", profile.heightCm).put("weightKg", profile.weightKg)
        prefs.edit().putString("profile", json.toString()).apply()
    }

    fun onboardingComplete() = prefs.getBoolean("onboarding_complete", false)

    // completion is written only after the notification permission result, never on first launch.
    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_complete", true).apply()
    }

    fun account() = AccountSettings(
        prefs.getString("name", "Athlima User") ?: "Athlima User", prefs.getString("avatar", null),
        runCatching { MeasureUnits.valueOf(prefs.getString("units", "Metric")!!) }.getOrDefault(
            MeasureUnits.Metric
        ),
        prefs.getBoolean("stand_reminders", false), prefs.getBoolean("goal_completions", true),
        prefs.getBoolean("activity_sharing", true),
    )
    fun saveAccount(account: AccountSettings) {
        prefs.edit().putString("name", account.name).putString("avatar", account.avatar)
            .putString("units", account.units.name).putBoolean("stand_reminders", account.standReminders)
            .putBoolean("goal_completions", account.goalCompletions).putBoolean("activity_sharing", account.activitySharing).apply()
    }

    fun activity(): List<ActivityEntry> = runCatching {
        val array = JSONArray(prefs.getString("activity", "[]"))
        List(array.length()) { i ->
            val v = array.getJSONObject(i)
            ActivityEntry(ActivityDay(LocalDate.parse(v.getString("date")), v.getInt("move"), v.getInt("exercise"), v.getInt("stand"), v.getInt("steps"), v.getDouble("distance")),
                Goals(v.getInt("moveGoal"), v.getInt("exerciseGoal"), v.getInt("standGoal")), v.optBoolean("newGoal"))
        }
    }.getOrDefault(emptyList())
    fun saveActivity(entries: List<ActivityEntry>) {
        val array = JSONArray()
        entries.forEach { e -> array.put(JSONObject().put("date", e.day.date.toString()).put("move", e.day.move)
            .put("exercise", e.day.exercise).put("stand", e.day.stand).put("steps", e.day.steps).put("distance", e.day.distance)
            .put("moveGoal", e.goals.move).put("exerciseGoal", e.goals.exercise).put("standGoal", e.goals.stand).put("newGoal", e.newMoveGoal)) }
        prefs.edit().putString("activity", array.toString()).apply()
    }
    fun pendingMoveGoal() = prefs.getBoolean("pending_move_goal", false)
    fun setPendingMoveGoal(pending: Boolean) { prefs.edit().putBoolean("pending_move_goal", pending).apply() }
}
