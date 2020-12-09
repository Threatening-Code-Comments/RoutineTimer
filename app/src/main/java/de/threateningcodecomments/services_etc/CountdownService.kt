package de.threateningcodecomments.services_etc

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import de.threateningcodecomments.accessibility.ResourceClass
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.routinetimer.App
import de.threateningcodecomments.routinetimer.MainActivity
import de.threateningcodecomments.routinetimer.R
import de.threateningcodecomments.routinetimer.RunContinuousRoutineFragment


class CountdownService : Service() {
    private lateinit var notificationCountdownTimer: CountDownTimer

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
        val currentTile = ResourceClass.getCurrentTile(routineUid)

        if (Notifications.notifications.keys.size == 0) {
            Notifications.initNotification(routineUid)
        }
        if (currentTile?.mode == Tile.MODE_COUNT_DOWN)
            startCountdown(currentTile, routineUid)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startCountdown(currentTile: Tile, routineUid: String) {
        val routine = ResourceClass.getRoutineFromUid(routineUid)
        val tileIndex = routine.tiles.indexOf(currentTile)
        val gridTile = RunContinuousRoutineFragment.instance.gridTiles[tileIndex]
        Timers.startCountdown(gridTile, currentTile)
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

    object Notifications {
        private lateinit var cancelIntent: PendingIntent

        fun initNotification(routineUid: String) {
            MainActivity.countdownServiceRunning = true
            val tile = ResourceClass.getCurrentTile(routineUid)!!

            val notification: Notification = Notification.Builder(instance, App.TIMING_CHANNEL_ID)
                    .setOnlyAlertOnce(true)
                    .build()

            instance.startForeground(STATIC_NOTIFICATION_ID, notification)

            val routine = ResourceClass.getRoutineOfTile(tile)

            val cancelI = Intent(MyBroadcastReceiver.CANCEL_ACTION)
            cancelI.putExtra(MyBroadcastReceiver.ROUTINE_UID_KEY, routine.uid)
            cancelIntent = PendingIntent.getBroadcast(instance, 0, cancelI, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun updateTileNotification(tile: Tile, millisUntilFinished: Long) {
            val nameStr = "${tile.name}"
            val timeStr = "Time remaining: ${ResourceClass.millisToHHMMSS(millisUntilFinished)}"

            //TODO update pendingintent for routine
            val tileIcon = ResourceClass.getIconDrawable(tile)!!.toBitmap(200, 200, null)

            val view = RemoteViews(instance.packageName, R.layout.custom_noti_layout)

            view.setBitmap(R.id.iv_viewholder_noti_icon, "setImageBitmap", tileIcon)

            view.setInt(R.id.ll_viewholder_noti_root, "setBackgroundColor", tile.backgroundColor)
            view.setInt(R.id.iv_viewholder_noti_icon, "setColorFilter", tile.contrastColor)
            view.setInt(R.id.tv_viewholder_noti_name, "setTextColor", tile.contrastColor)
            view.setInt(R.id.tv_viewholder_noti_time, "setTextColor", tile.contrastColor)

            view.setTextViewText(R.id.tv_viewholder_noti_name, nameStr)
            view.setTextViewText(R.id.tv_viewholder_noti_time, timeStr)



            val notification: Notification = Notification.Builder(instance, App.TIMING_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setCustomContentView(view)
                    .build()

            val routineUid = ResourceClass.getRoutineOfTile(tile).uid
            if (!notifications.containsKey(routineUid)) {
                notifications[routineUid] = notification
            }
            sendNotification(notificationIds[routineUid]!!, notification)
        }

        fun updateNotifications() {
            val notificationManager = instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()

            for (key in notifications.keys) {
                val id = notificationIds[key]
                val notification = notifications[key]

                sendNotification(id!!, notification!!)
            }
        }

        val notifications = object : HashMap<String, Notification>() {

            override fun put(key: String, value: Notification): Notification? {
                notificationIds.put(key, ResourceClass.convertUidToInt(key))
                return super.put(key, value)
            }

            override fun remove(key: String): Notification? {
                notificationIds.remove(key)
                if (this.size - 1 == 1) {
                    val notificationKey = notificationIds.keys.elementAt(0)
                    notificationIds[notificationKey] = STATIC_NOTIFICATION_ID
                }
                return super.remove(key)
            }
        }
        val notificationIds = object : HashMap<String, Int>() {
            override fun put(key: String, value: Int): Int? {
                val fickDich = super.put(key, value)
                onUpdate()
                return fickDich
            }

            override fun remove(key: String): Int? {
                val temp = super.remove(key)
                onUpdate()
                return temp
            }

            fun onUpdate() {
                updateNotifications()
            }
        }

        fun sendNotification(id: Int, notification: Notification) {
            val notificationManager = instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(id, notification)
        }
    }

    object Timers {
        private var countdownTimers: HashMap<String, CountDownTimer?> = HashMap()

        fun startCountdown(gridTile: MaterialCardView, tile: Tile) {
            val routineUid = ResourceClass.getRoutineOfTile(tile).uid

            if (countdownTimers[routineUid] == null) {
                createTimer(gridTile, tile, routineUid)
            }
        }

        private fun createTimer(currentGridTile: MaterialCardView, currentTile: Tile, routineUid: String) {
            val totalTimeField = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTime)
            val currentTimeField = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTime)

            currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTimeInfo).text = MainActivity.activityBuffer.getString(R.string.str_tv_viewholder_runTile_currentTimeInfo_countDown)

            currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeInfo).visibility = View.GONE
            totalTimeField.visibility = View.GONE

            var currentTime: Long

            var lastTimeValue = ""
            //MainActivity.activityBuffer.startCountdownService(routineUid)
            countdownTimers[routineUid] = object : CountDownTimer(currentTile.countDownSettings.countDownTime, 10) {
                override fun onTick(millisUntilFinished: Long) {
                    currentTime = System.currentTimeMillis() - currentTile.countingStart
                    var remainingTime = currentTile.countDownSettings.countDownTime - currentTime
                    if (remainingTime < 0)
                        remainingTime = 0L
                    currentTimeField.text = ResourceClass.millisToHHMMSSmm(remainingTime)

                    val currentTimeString = ResourceClass.millisToHHMMSS(millisUntilFinished)
                    if (currentTimeString != lastTimeValue) {
                        Notifications.updateTileNotification(currentTile, millisUntilFinished)
                        lastTimeValue = currentTimeString
                    }
                }

                override fun onFinish() {
                    tileTimerFinished(currentTile, routineUid)
                }
            }.start()
        }

        private fun tileTimerFinished(currentTile: Tile, routineUid: String) {
            //reminderForTile(currentTile)
            Toast.makeText(MainActivity.activityBuffer.baseContext, "Timer finished!", Toast.LENGTH_SHORT).show()
            currentTile.totalCountedTime += currentTile.countDownSettings.countDownTime
            val routine = ResourceClass.getRoutineFromUid(routineUid)
            if (MainActivity.currentFragment is RunContinuousRoutineFragment)
                (MainActivity.currentFragment as RunContinuousRoutineFragment).toggleTileSize(routine.tiles.indexOf(currentTile))
            stopNotificationCountdown(routineUid)
        }

        fun stopNotificationCountdown(routineUid: String) {
            val currentTile = ResourceClass.getCurrentTile(routineUid)
            if (currentTile != null)
                tileTimerFinished(currentTile, routineUid)

            val timer = instance.notificationCountdownTimer
            timer.cancel()
            instance.stopService()
            /*val timer = CountdownService.instance.notificationCountdownTimer
                timer.cancel()

                tileCountdownTimers[routineUid]?.cancel()

                tileCountdownTimers.remove(routineUid)
                CountdownService.instance.stopService()*/
        }
    }

    companion object {
        lateinit var instance: CountdownService

        const val CANCEL_RQ_CODE = 420
        const val ROUTINE_UID_KEY = "routine_uid"

        const val STATIC_NOTIFICATION_ID = 69420
    }
}