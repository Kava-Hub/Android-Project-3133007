package com.example.fitness_tracker.domain

import com.example.fitness_tracker.data.ActivityDay

// temporary class before sensor implementation
internal fun hourlyDistribution(total: Int, seed: Int): List<Int> {
    val weights = (0..23).map { hour -> if (hour in 7..21) ((hour * 7 + seed * 3) % 11 + 1) else 0 }
    val sum = weights.sum()
    val bins = weights.map { total * it / sum }.toMutableList()
    bins[18] += total - bins.sum()
    return bins
}
internal fun standHours(day: ActivityDay):
        List<Int> = (0..23).map {
            if (it in 7 until (7 + day.stand.coerceIn(0, 17))) 1 else 0
        }
