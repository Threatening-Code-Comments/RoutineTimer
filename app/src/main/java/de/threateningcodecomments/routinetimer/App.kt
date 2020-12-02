package de.threateningcodecomments.routinetimer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.IntentFilter
import android.os.Build
import de.threateningcodecomments.services_etc.MyBroadcastReceiver

class App : Application() {
    val myBroadcastReceiver = MyBroadcastReceiver()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val filter = IntentFilter(MyBroadcastReceiver.CANCEL_ACTION)
        registerReceiver(myBroadcastReceiver, filter)
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(myBroadcastReceiver)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    TIMING_CHANNEL_ID,
                    "Tile controls",
                    NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications for Tile controlling"
            channel.enableVibration(false)
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val TIMING_CHANNEL_ID = "tileTiming"
    }
}