package de.threateningcodecomments.services_etc

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.CountDownTimer
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import de.threateningcodecomments.accessibility.CountdownSettings
import de.threateningcodecomments.accessibility.MyLog
import de.threateningcodecomments.accessibility.ResourceClass
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.routinetimer.App
import de.threateningcodecomments.routinetimer.MainActivity
import de.threateningcodecomments.routinetimer.R
import de.threateningcodecomments.routinetimer.RunContinuousRoutineFragment


class CountdownService : Service() {
    private val isRunning = false
    private lateinit var countdownTimer: CountDownTimer

    fun remind(tile: Tile) {
        val settings = tile.countDownSettings

        Toast.makeText(this, "${tile.name} reminds", Toast.LENGTH_SHORT).show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val routineUid = intent!!.getStringExtra(ROUTINE_UID_KEY)
        if (routineUid == null) {
            stopService()
            return super.onStartCommand(intent, flags, startId)
        }

        MainActivity.countdownServiceRunning = true
        var tile = Tile("Test Tile!", 3, Color.GREEN, -1, "uid", 5999,
                CountdownSettings(10000, true, false, CountdownSettings.DEFAULT_RING))
        for (curTile in ResourceClass.currentTiles.runningTiles) {
            if (curTile.mode == Tile.MODE_COUNT_DOWN) {
                tile = curTile
                break
            }
        }

        val pendingIntent: PendingIntent =
                Intent(this, MainActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, 0)
                }
        val notification: Notification = Notification.Builder(this, App.TIMING_CHANNEL_ID)
                .setContentTitle("Counting down ${tile.name}")
                .setContentText("Currently counting down ${ResourceClass.millisToHHMMSS(tile.countDownSettings.countDownTime)} for ${tile.name}")
                .setSmallIcon(R.drawable.ic_logo)
                .setContentIntent(pendingIntent)
                .setTicker("ticker text")
                .setColor(tile.backgroundColor)
                .setColorized(true)
                .setOnlyAlertOnce(true)
                .build()

        // Notification ID cannot be 0.
        startForeground(App.COUNTDOWN_NOTIFICATION_ID, notification)

        var lastTimeValue = ""
        countdownTimer = object : CountDownTimer(tile.countDownSettings.countDownTime, 400) {
            override fun onTick(millisUntilFinished: Long) {
                val currentTime = ResourceClass.millisToHHMMSS(millisUntilFinished)
                if (currentTime != lastTimeValue) {
                    updateNotification(tile, millisUntilFinished)
                    lastTimeValue = currentTime
                }
            }

            override fun onFinish() {
                stopService()
            }
        }.start()

        return super.onStartCommand(intent, flags, startId)
    }

    fun updateNotification(tile: Tile, millisUntilFinished: Long) {
        val pendingIntent: PendingIntent =
                Intent(this, MainActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, 0)
                }

        val notiBmp = ResourcesCompat.getDrawable(resources, R.drawable.ic_logo, null)?.toBitmap(200, 200, null)
        val notiIcon = Icon.createWithBitmap(notiBmp)
        notiIcon.setTint(ResourceClass.calculateContrast(tile.backgroundColor))

        val intent = Intent(MyBroadcastReceiver.CANCEL_ACTION)
        val cancelIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        val notification: Notification = Notification.Builder(this, App.TIMING_CHANNEL_ID)
                .setContentTitle("Counting down ${tile.name}")
                .setContentText("Time remaining: ${ResourceClass.millisToHHMMSS(millisUntilFinished)}")
                .setSmallIcon(notiIcon)
                .setLargeIcon(notiIcon)
                .setContentIntent(pendingIntent)
                .setTicker("ticker text")
                .setColor(tile.backgroundColor)
                .setColorized(true)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .addAction(android.R.drawable.ic_menu_view, "Cancel", cancelIntent)
                .setOnlyAlertOnce(true)
                .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(App.COUNTDOWN_NOTIFICATION_ID, notification)
    }

    fun stopService() {
        MainActivity.countdownServiceRunning = false

        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: CountdownService

        const val CANCEL_RQ_CODE = 420
        const val ROUTINE_UID_KEY = "routine_uid"
    }

    object Timers {
        private var countdownTimers: HashMap<String, CountDownTimer?> = HashMap()

        private lateinit var totalTimeField: MaterialTextView
        private lateinit var currentTimeField: MaterialTextView
        fun startCountdown(millisToCount: Long, gridTile: MaterialCardView, tile: Tile) {
            val routineUid = ResourceClass.getRoutineOfTile(tile).uid

            totalTimeField = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTime)
            currentTimeField = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTime)
            gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeInfo).visibility = View.VISIBLE
            totalTimeField.visibility = View.VISIBLE

            gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTimeInfo).text = MainActivity.activityBuffer.getString(R.string.str_tv_viewholder_runTile_currentTimeInfo_countDown)

            gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeInfo).visibility = View.GONE
            totalTimeField.visibility = View.GONE

            if (countdownTimers[routineUid] == null) {
                createTimer(millisToCount, gridTile, tile, routineUid!!)
            }
        }

        private fun createTimer(millisToCount: Long, currentGridTile: MaterialCardView, currentTile: Tile, routineUid: String) {
            var currentTime =
                    if (currentTile.mode == Tile.MODE_COUNT_UP) 0L
                    else currentTile.countDownSettings.countDownTime

            MainActivity.activityBuffer.startCountdownService(routineUid)
            countdownTimers[routineUid] = object : CountDownTimer(currentTile.countDownSettings.countDownTime, 10) {
                override fun onTick(millisUntilFinished: Long) {
                    MyLog.d("counting! ${ResourceClass.millisToHHMMSS(millisUntilFinished)}")
                    currentTime = System.currentTimeMillis() - currentTile.countingStart
                    var remainingTime = currentTile.countDownSettings.countDownTime - currentTime
                    if (remainingTime < 0)
                        remainingTime = 0L
                    currentTimeField.text = ResourceClass.millisToHHMMSSmm(remainingTime)
                }

                override fun onFinish() {
                    //reminderForTile(currentTile)
                    Toast.makeText(MainActivity.activityBuffer.baseContext, "Timer finished!", Toast.LENGTH_SHORT).show()
                    currentTile.totalCountedTime += currentTile.countDownSettings.countDownTime
                    val routine = ResourceClass.getRoutineFromUid(routineUid)
                    if (MainActivity.currentFragment is RunContinuousRoutineFragment)
                        (MainActivity.currentFragment as RunContinuousRoutineFragment).toggleTileSize(routine.tiles.indexOf(currentTile))
                    stopCountdown(routineUid)
                }
            }.start()
        }

        fun stopCountdown(routineUid: String) {
            val timer = countdownTimers[routineUid]
            timer?.cancel()
            countdownTimers[routineUid] = null
            CountdownService.instance.stopSelf()
        }
    }
}