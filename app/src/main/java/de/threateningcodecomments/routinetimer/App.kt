package de.threateningcodecomments.routinetimer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    TIMING_CHANNEL_ID,
                    "Tile controls",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Notifications for Tile controlling"
            channel.enableVibration(false)
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val TIMING_CHANNEL_ID = "tileTiming"
        const val COUNTDOWN_NOTIFICATION_ID = 69
    }
}