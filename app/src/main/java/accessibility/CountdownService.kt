package accessibility

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import de.threateningcodecomments.routinetimer.App
import de.threateningcodecomments.routinetimer.MainActivity
import de.threateningcodecomments.routinetimer.R

class CountdownService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    fun remind(tile: Tile) {
        val settings = tile.countDownSettings

        Toast.makeText(this, "${tile.name} reminds", Toast.LENGTH_SHORT).show()
    }

    fun start(tile: Tile) {
        val pendingIntent: PendingIntent =
                Intent(this, MainActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, 0)
                }

        val notification: Notification = Notification.Builder(this, App.TIMING_CHANNEL_ID)
                .setContentTitle("Counting down ${tile.name}")
                .setContentText("Currently counting down ${ResourceClass.millisToHHMMSSmm(tile.countDownSettings.countDownTime)} for ${tile.name}")
                .setSmallIcon(R.drawable.fui_ic_github_white_24dp)
                .setContentIntent(pendingIntent)
                .setTicker("ticker text")
                .build()

        // Notification ID cannot be 0.
        startForeground(App.COUNTDOWN_NOTIFICATION_ID, notification)
    }
}