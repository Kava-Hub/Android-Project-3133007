package com.example.fitness_tracker

import android.app.Application
import com.example.fitness_tracker.data.FitnessRepository
import com.example.fitness_tracker.data.FitnessStore
import com.example.fitness_tracker.notifications.FitnessNotifications

// application container
class AthlimaApplication : Application() {
    internal lateinit var repository: FitnessRepository
    internal val notifications by lazy { FitnessNotifications(this) }

    override fun onCreate() {
        super.onCreate()
        repository = FitnessRepository(FitnessStore(this))
        notifications.createChannel()
        com.example.fitness_tracker.notifications.StandReminder.schedule(this)
    }
}
