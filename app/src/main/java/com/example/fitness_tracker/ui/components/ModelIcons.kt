package com.example.fitness_tracker.ui.components

import com.example.fitness_tracker.data.Category
import com.example.fitness_tracker.data.Friend
import com.example.fitness_tracker.data.WorkoutType
import com.example.fitness_tracker.ui.components.Glyph

// Presentation mappings stay out of the persisted models and business rules.
internal val Category.icon: Glyph
    get() = when (this) {
    Category.Steps -> Glyph.Steps
    Category.Distance -> Glyph.Route
    Category.Sessions -> Glyph.Run
    Category.Awards -> Glyph.Award
    Category.Trends -> Glyph.Trend
}
internal val WorkoutType.icon: Glyph get() = when (this) {
    WorkoutType.Walk -> Glyph.Walk
    WorkoutType.Run -> Glyph.Run
    WorkoutType.Cycle -> Glyph.Cycle
    WorkoutType.Strength -> Glyph.Strength
    WorkoutType.Core -> Glyph.Core
    WorkoutType.Interval -> Glyph.Bolt
}
internal val Friend.icon: Glyph get() = when (headline) {
    "All rings closed" -> Glyph.Rings
    "Outdoor Run" -> Glyph.Run
    else -> Glyph.Award
}
