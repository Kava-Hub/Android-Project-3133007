package com.example.fitness_tracker.data

internal enum class MeasureUnits { Metric, Imperial }
internal data class AccountSettings(
    val name: String = "Athlima User",
    val avatar: String? = null,
    val units: MeasureUnits = MeasureUnits.Metric,
    val standReminders: Boolean = false,
    val goalCompletions: Boolean = true,
    val activitySharing: Boolean = true,
) {
    val initials get() =
        name.trim()
        .split(Regex("\\s+"))
        .take(2)
        .mapNotNull {
            it.firstOrNull()
        }
        .joinToString("")
        .uppercase()
        .ifEmpty { "A" }
}
