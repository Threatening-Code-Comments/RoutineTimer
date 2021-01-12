package de.threateningcodecomments.routinetimer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.IntentFilter
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import de.threateningcodecomments.accessibility.ResourceClass
import de.threateningcodecomments.accessibility.Routine
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.services_etc.CountingService
import de.threateningcodecomments.services_etc.MyBroadcastReceiver

class App : Application() {
    private val myBroadcastReceiver = MyBroadcastReceiver()

    override fun onCreate() {
        super.onCreate()
        instance = this

        createNotificationChannel()
        val filter = IntentFilter(MyBroadcastReceiver.CANCEL_ACTION)
        registerReceiver(myBroadcastReceiver, filter)
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(myBroadcastReceiver)
        CountingService.instance.stopService()
    }

    fun startTile(tile: Tile) {
        val user = FirebaseAuth.getInstance().currentUser
        val routine = ResourceClass.getRoutineOfTile(tile)

        if (ResourceClass.currentTiles[routine.uid] == null) {

            tile.countingStart = System.currentTimeMillis()
            ResourceClass.updateCurrentTile(tile, routine.uid)

            MainActivity.instance.startCountingService(routine.uid)
        }
    }

    fun stopTile(routine: Routine) {
        ResourceClass.updateCurrentTile(null, routine.uid)
        ResourceClass.saveRoutine(routine)
        CountingService.Timers.stopCounting(routine.uid)
    }

    fun cycleForward(routine: Routine) {
        val previousCurrentTile = ResourceClass.currentTiles[routine.uid] ?: ResourceClass
                .previousCurrentTiles[routine.uid]
        val previousIndex = routine.tiles.indexOf(previousCurrentTile)
        stopTile(routine)

        if (previousIndex + 1 < routine.tiles.size) {
            val newTile = routine.tiles[previousIndex + 1]
            startTile(newTile)
        }
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
            channel.setSound(null, null)
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        lateinit var instance: App

        const val TIMING_CHANNEL_ID = "tileTiming"

        const val TILE_GROUP = "de.threateningcodecomments.routinetimer.tileGroup"
        const val TILE_GROUP_SUMMARY_ID = 12
    }
}