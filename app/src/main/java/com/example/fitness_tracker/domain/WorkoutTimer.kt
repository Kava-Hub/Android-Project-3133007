package com.example.fitness_tracker.domain

/** timer independent of screen redraws and timezone changes. */
internal data class WorkoutTimer(val accumulatedMs: Long = 0, val runningSinceMs: Long = -1) {
    val running get() = runningSinceMs >= 0
    fun elapsedMs(now: Long) = accumulatedMs + if (running) (now - runningSinceMs).coerceAtLeast(0) else 0
    fun pause(now: Long) = WorkoutTimer(elapsedMs(now))
    fun resume(now: Long) = if (running) this else copy(runningSinceMs = now)
}
