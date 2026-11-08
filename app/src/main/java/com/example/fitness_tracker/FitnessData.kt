package com.example.fitness_tracker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal enum class Category(val label: String, val icon: Glyph) {
    Steps("Step Count", Glyph.Steps),
    Distance("Step Distance", Glyph.Route),
    Sessions("Sessions", Glyph.Run),
    Awards("Awards", Glyph.Award),
    Trends("Trends", Glyph.Trend)
}

internal data class Goals(val move: Int = 600, val exercise: Int = 30, val stand: Int = 12)

internal data class ActivityDay(
    val date: LocalDate,
    val move: Int,
    val exercise: Int,
    val stand: Int,
    val steps: Int,
    val distance: Double,
) {
    fun progress(goals: Goals) =
        listOf(move.toFloat() / goals.move, exercise.toFloat() / goals.exercise, stand.toFloat() / goals.stand)
}

internal val DemoToday: LocalDate = LocalDate.of(2026, 11, 8)
internal val DemoWeek = listOf(
    ActivityDay(DemoToday.minusDays(6), 410, 24, 8, 6570, 4.62),
    ActivityDay(DemoToday.minusDays(5), 620, 35, 12, 10160, 7.14),
    ActivityDay(DemoToday.minusDays(4), 510, 27, 10, 8051, 5.66),
    ActivityDay(DemoToday.minusDays(3), 685, 46, 12, 11280, 7.93),
    ActivityDay(DemoToday.minusDays(2), 390, 19, 9, 5368, 3.77),
    ActivityDay(DemoToday.minusDays(1), 593, 34, 11, 9243, 6.50),
    ActivityDay(DemoToday, 486, 24, 9, 8246, 5.80),
)

internal enum class WorkoutType(val label: String, val detail: String, val icon: Glyph, val minutes: Int) {
    Walk("Outdoor Walk", "Walking", Glyph.Walk, 20),
    Run("Outdoor Run", "Running", Glyph.Run, 30),
    Cycle("Outdoor Cycle", "Cycling", Glyph.Cycle, 30),
    Strength("Functional Strength", "Strength training", Glyph.Strength, 30),
    Core("Core Training", "Core and stability", Glyph.Core, 15),
    Interval("HIIT", "High intensity intervals", Glyph.Bolt, 15)
}

internal data class WorkoutLog(
    val id: Long,
    val type: WorkoutType,
    val seconds: Long,
    val date: LocalDate,
)

internal val DemoSessions = listOf(
    WorkoutLog(-1, WorkoutType.Walk, 24 * 60, DemoToday),
    WorkoutLog(-2, WorkoutType.Strength, 32 * 60, DemoToday.minusDays(1)),
    WorkoutLog(-3, WorkoutType.Cycle, 41 * 60, DemoToday.minusDays(3)),
)

internal data class Friend(
    val name: String,
    val initials: String,
    val palette: Int,
    val week: List<ActivityDay>,
    val headline: String,
    val highlight: String,
    val icon: Glyph,
)

internal val DemoFriends = listOf(
    Friend(
        "Maya Chen", "MC", 0,
        DemoWeek.map { it.copy(move = it.move + 152, exercise = it.exercise + 12, stand = (it.stand + 3).coerceAtMost(14)) },
        "All rings closed", "Maya hit all three activity goals today.", Glyph.Rings
    ),
    Friend(
        "Alex Morgan", "AM", 1,
        DemoWeek.map { it.copy(move = it.move - 70, exercise = it.exercise + 3, stand = it.stand + 1) },
        "Outdoor Run", "Alex finished a 30 min run today.", Glyph.Run
    ),
    Friend(
        "Sam Rivera", "SR", 2,
        DemoWeek.map { it.copy(move = it.move + 20, exercise = it.exercise - 5, stand = it.stand) },
        "Perfect Week", "Sam earned a new Move award.", Glyph.Award
    )
)

internal fun LocalDate.displayDate() = format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH))
internal fun LocalDate.shortDate() = format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH))
internal fun Int.grouped() = String.format(Locale.US, "%,d", this)
internal fun Double.kilometres() = String.format(Locale.US, "%.2f km", this)

// Only user preferences and timer sessions are stored. Activity and friends are bundled examples.
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
}