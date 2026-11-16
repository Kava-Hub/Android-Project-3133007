package com.example.fitness_tracker.data

import java.time.LocalDate

internal val DemoToday: LocalDate = LocalDate.now()
internal val DemoWeek = listOf(
    ActivityDay(DemoToday.minusDays(6), 410, 24, 8, 6570, 4.62),
    ActivityDay(DemoToday.minusDays(5), 620, 35, 12, 10160, 7.14),
    ActivityDay(DemoToday.minusDays(4), 510, 27, 10, 8051, 5.66),
    ActivityDay(DemoToday.minusDays(3), 685, 46, 12, 11280, 7.93),
    ActivityDay(DemoToday.minusDays(2), 390, 19, 9, 5368, 3.77),
    ActivityDay(DemoToday.minusDays(1), 593, 34, 11, 9243, 6.50),
    ActivityDay(DemoToday, 486, 24, 9, 8246, 5.80),
)

internal val DemoSessions = listOf(
    WorkoutLog(-1, WorkoutType.Walk, 24 * 60, DemoToday),
    WorkoutLog(-2, WorkoutType.Strength, 32 * 60, DemoToday.minusDays(1)),
    WorkoutLog(-3, WorkoutType.Cycle, 41 * 60, DemoToday.minusDays(3)),
)

internal val DemoFriends = listOf(
    Friend(
        "Maya Chen", "MC", 0,
        DemoWeek.map { it.copy(move = it.move + 152, exercise = it.exercise + 12, stand = (it.stand + 3).coerceAtMost(14)) },
        "All rings closed", "Maya hit all three activity goals today."
    ),
    Friend(
        "Alex Morgan", "AM", 1,
        DemoWeek.map { it.copy(move = it.move - 70, exercise = it.exercise + 3, stand = it.stand + 1) },
        "Outdoor Run", "Alex finished a 30 min run today."
    ),
    Friend(
        "Sam Rivera", "SR", 2,
        DemoWeek.map { it.copy(move = it.move + 20, exercise = it.exercise - 5, stand = it.stand) },
        "Perfect Week", "Sam earned a new Move award."
    )
)

