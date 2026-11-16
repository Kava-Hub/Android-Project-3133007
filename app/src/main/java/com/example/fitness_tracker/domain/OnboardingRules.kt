package com.example.fitness_tracker.domain

import com.example.fitness_tracker.data.BiologicalSex
import com.example.fitness_tracker.data.UserProfile
import java.time.LocalDate

internal data class ProfileDraft(
    val birthDate: String = "",
    val sex: String = "",
    val height: String = "",
    val weight: String = "",
) {
    fun errors(today: LocalDate = LocalDate.now()): Map<String, String> = buildMap {
        val birth = runCatching { LocalDate.parse(birthDate) }.getOrNull()
        if (birth == null || birth > today || birth < today.minusYears(120)) {
            put("birthDate", "Choose a date of birth within the last 120 years.")
        }
        if (BiologicalSex.entries.none { it.name == sex }) put("sex", "Choose an option.")
        if (height.decimal()?.let { it.isFinite() && it in 30.0..275.0 } != true) {
            put("height", "Enter a height from 30 to 275 cm.")
        }
        if (weight.decimal()?.let { it.isFinite() && it in 1.0..454.0 } != true) {
            put("weight", "Enter a weight from 1 to 454 kg.")
        }
    }

    fun toProfile(): UserProfile {
        require(errors().isEmpty()) { "Profile must be validated before saving" }
        return UserProfile(
            LocalDate.parse(birthDate), BiologicalSex.valueOf(sex), height.decimal()!!, weight.decimal()!!
        )
    }
}

private fun String.decimal() = trim().replace(',', '.').toDoubleOrNull()

internal object MoveGoalRules {
    const val MIN = 10
    const val MAX = 5000
    const val STEP = 10
    val presets = linkedMapOf("Lightly" to 150, "Moderately" to 240, "Highly" to 360)
    fun adjust(current: Int, direction: Int) = (current + direction.coerceIn(-1, 1) * STEP).coerceIn(MIN, MAX)
}
