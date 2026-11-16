package com.example.fitness_tracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.fitness_tracker.data.FitnessStore
import com.example.fitness_tracker.notifications.FitnessNotifications
import java.time.LocalTime

class StandReminder : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) { schedule(context); return }
        if (FitnessStore(context).account().standReminders && LocalTime.now().hour in 9..20) {
            FitnessNotifications(context).post(20, "Time to stand", "Take a moment to stand up and move, if you can.")
        }
    }
    companion object {
        fun schedule(context: Context) {
            val alarm = context.getSystemService(AlarmManager::class.java)
            val pending = PendingIntent.getBroadcast(context, 20, Intent(context, StandReminder::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarm.cancel(pending)
            if (FitnessStore(context).account().standReminders) alarm.setInexactRepeating(
                AlarmManager.RTC, System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR, AlarmManager.INTERVAL_HOUR, pending)
        }
    }
}
