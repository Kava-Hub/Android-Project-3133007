package com.example.fitness_tracker.data

import java.time.LocalDate

internal enum class Category(val label: String) {
    Steps("Step Count"),
    Distance("Step Distance"),
    Sessions("Sessions"),
    Awards("Awards"),
    Trends("Trends")
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

internal enum class WorkoutType(val label: String, val detail: String, val minutes: Int) {
    Walk("Outdoor Walk", "Walking", 20),
    Run("Outdoor Run", "Running", 30),
    Cycle("Outdoor Cycle", "Cycling", 30),
    Strength("Functional Strength", "Strength training", 30),
    Core("Core Training", "Core and stability", 15),
    Interval("HIIT", "High intensity intervals", 15)
}

internal data class WorkoutLog(
    val id: Long,
    val type: WorkoutType,
    val seconds: Long,
    val date: LocalDate,
)

internal data class Friend(
    val name: String,
    val initials: String,
    val palette: Int,
    val week: List<ActivityDay>,
    val headline: String,
    val highlight: String,
)

