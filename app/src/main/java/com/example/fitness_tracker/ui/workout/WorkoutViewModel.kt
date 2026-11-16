package com.example.fitness_tracker.ui.workout

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_tracker.data.WorkoutLog
import com.example.fitness_tracker.data.WorkoutType
import com.example.fitness_tracker.domain.WorkoutTimer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

internal data class WorkoutState(val seconds: Long = 0, val running: Boolean = false, val ending: Boolean = false)


internal class WorkoutViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private var timer = WorkoutTimer(savedState["accumulated"] ?: 0L, savedState["runningSince"] ?: -1L)
    private var ending = savedState["ending"] ?: false
    private var resumeAfterDismiss = savedState["resumeAfterDismiss"] ?: false
    private val mutableState = MutableStateFlow(WorkoutState())
    val state = mutableState.asStateFlow()

    init {
        publish()
        viewModelScope.launch {
            while (true) {
                if (timer.running) publish()
                delay(250)
            }
        }
    }
    private fun publish() {
        savedState["accumulated"] = timer.accumulatedMs
        savedState["runningSince"] = timer.runningSinceMs
        savedState["ending"] = ending
        savedState["resumeAfterDismiss"] = resumeAfterDismiss
        mutableState.value = WorkoutState(timer.elapsedMs(SystemClock.elapsedRealtime()) / 1000, timer.running, ending)
    }
    fun start() {
        timer = WorkoutTimer(runningSinceMs = SystemClock.elapsedRealtime())
        ending = false
        resumeAfterDismiss = false
        publish()
    }
    fun togglePause() {
        val now = SystemClock.elapsedRealtime()
        timer = if (timer.running) timer.pause(now) else timer.resume(now)
        publish()
    }
    fun end() {
        resumeAfterDismiss = timer.running
        timer = timer.pause(SystemClock.elapsedRealtime())
        ending = true
        publish()
    }
    fun dismissEnd() {
        ending = false
        if (resumeAfterDismiss) timer = timer.resume(SystemClock.elapsedRealtime())
        resumeAfterDismiss = false
        publish()
    }
    fun log(type: WorkoutType) = WorkoutLog(System.currentTimeMillis(), type, state.value.seconds, LocalDate.now())
    fun clear() {
        timer = WorkoutTimer()
        ending = false
        resumeAfterDismiss = false
        publish()
    }
}
