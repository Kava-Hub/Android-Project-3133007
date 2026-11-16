package com.example.fitness_tracker.domain

import com.example.fitness_tracker.data.*
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

internal data class ActivityEntry(val day: ActivityDay, val goals: Goals, val newMoveGoal: Boolean = false)
internal data class AwardStatus(val id: String, val title: String, val symbol: String, val detail: String,
    val progress: Int, val target: Int, val count: Int = 0, val earned: LocalDate? = null) {
    val unlocked get() = earned != null
}
internal object Awards {
    val milestones = listOf(100, 365, 500, 1000, 1250, 1500, 1750, 2000, 2250, 2500, 2750, 3000)

    fun evaluate(records: List<ActivityEntry>): List<AwardStatus> {

        val days = records.associateBy { it.day.date }.values.sortedBy { it.day.date }
        val moved = days.filter { it.day.move >= it.goals.move }
        val result = mutableListOf<AwardStatus>()

        for (multiple in 2..4) {
            val hits = days.filter { it.day.move >= it.goals.move * multiple }
            val best = days.maxOfOrNull {
                (it.day.move.toLong() * 100 / it.goals.move).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            } ?: 0

            result += AwardStatus(
                "move$multiple",
                "Move Goal ${multiple * 100}%",
                "${multiple}×",
                "Reach ${multiple} times your daily Move goal. Counted once per date.",
                best.coerceAtMost(multiple * 100),
                multiple * 100, hits.size,
                hits.firstOrNull()?.day?.date
            )
        }

        val changed = days.firstOrNull { it.newMoveGoal && it.day.move >= it.goals.move }

        result += AwardStatus(
            "newGoal",
            "New Move Goal",
            "+",
            "Set a different Move goal, then reach it with a new activity entry.",
            if (changed != null) 1 else 0,
            1,
            earned = changed?.day?.date
        )

        for ((id, title, symbol) in listOf(Triple("moveRecord", "New Move Record", "M"), Triple("exerciseRecord", "New Exercise Record", "E"))) {
            val values = days.map { if (id == "moveRecord") it.day.move else it.day.exercise }
            var best = values.firstOrNull() ?: 0
            var first: LocalDate? = null
            values.drop(1).forEachIndexed { i, value -> if (value > best) { best = value; if (first == null) first = days[i + 1].day.date } }
            result += AwardStatus(
                id,
                title,
                symbol,
                "Beat a previously recorded daily best. Your first day establishes a baseline.",
                best,
                (best + 1),
                earned = first
            )
        }

        var run = 0;
        var longest = 0;
        var previous: LocalDate? = null;
        var streakDate: LocalDate? = null

        for (entry in moved) {
            run = if (previous?.plusDays(1) == entry.day.date) run + 1 else 1
            if (run > longest) { longest = run; streakDate = entry.day.date }
            previous = entry.day.date
        }

        result += AwardStatus(
            "streak",
            "Longest Move Streak",
            "X",
            "Consecutive dates reaching the Move goal. A missing date breaks the streak.",
            longest,
            maxOf(1, longest),
            earned = streakDate
        )

        val weeks = days.groupBy { it.day.date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
        for (kind in listOf("Move", "Exercise", "Stand", "All Activity")) {
            fun hit(e: ActivityEntry) = when (kind) {
                "Move" -> e.day.move >= e.goals.move
                "Exercise" -> e.day.exercise >= e.goals.exercise
                "Stand" -> e.day.stand >= e.goals.stand
                else -> e.day.move >= e.goals.move && e.day.exercise >= e.goals.exercise && e.day.stand >= e.goals.stand
            }
            val complete = weeks.filter { (_, entries) -> entries.size == 7 && entries.all(::hit) }
            val progress = weeks.values.maxOfOrNull { entries -> entries.count(::hit) } ?: 0

            result += AwardStatus(
                "week$kind",
                "Perfect Week ($kind)",
                "7",
                "Reach your $kind goals every day in one Monday–Sunday calendar week.",
                progress,
                7,
                complete.size,
                complete.keys.minOrNull()?.plusDays(6)
            )
        }

        for (target in milestones) result += AwardStatus("days$target", "$target Move Goals", target.toString(), "Reach the daily Move goal on $target different dates.", moved.size, target, earned = moved.getOrNull(target - 1)?.day?.date)
        return result
    }
}
