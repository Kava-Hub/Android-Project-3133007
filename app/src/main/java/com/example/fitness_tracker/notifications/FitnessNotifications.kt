package com.example.fitness_tracker.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.fitness_tracker.MainActivity
import com.example.fitness_tracker.R
import com.example.fitness_tracker.data.WorkoutLog
import com.example.fitness_tracker.data.FitnessStore
import kotlin.jvm.java

internal class FitnessNotifications(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun createChannel() {
        manager.createNotificationChannel(NotificationChannel(
            CHANNEL_ID, "Activity updates", NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "Workout completion and activity encouragement from Athlima" })
    }

    fun workoutSaved(log: WorkoutLog) {
        post(log.id.toInt(), "Workout saved", "${log.type.label} complete. Keep moving toward your daily goal!")
    }

    fun goalCompleted(id: Int, title: String, message: String) {
        if (FitnessStore(context).account().goalCompletions) post(id, title, message)
    }
    fun sharedActivity(message: String) {
        if (FitnessStore(context).account().activitySharing) post(21, "Activity Sharing", message)
    }
    fun post(id: Int, title: String, message: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        if (!manager.areNotificationsEnabled()) return
        val openApp = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_activity)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(openApp).setAutoCancel(true).build()
        try { manager.notify(id, notification) } catch (_: SecurityException) { }
    }

    companion object { const val CHANNEL_ID = "activity_updates" }
}
