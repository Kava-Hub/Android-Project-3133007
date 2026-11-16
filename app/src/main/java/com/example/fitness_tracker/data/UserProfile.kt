package com.example.fitness_tracker.data

import java.time.LocalDate

// stores biometric data of the user
// biometric details use metric units so future sensor calculations need no UI conversions
internal data class UserProfile(
    val dateOfBirth: LocalDate,
    val sex: BiologicalSex,
    val heightCm: Double,
    val weightKg: Double,
)

internal enum class BiologicalSex(val label: String) {
    NotSet("Not Set"), Female("Female"), Male("Male"), Other("Other")
}
