package de.threateningcodecomments.services_etc

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import de.threateningcodecomments.accessibility.MyLog
import de.threateningcodecomments.accessibility.ResourceClass
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.routinetimer.App
import de.threateningcodecomments.routinetimer.MainActivity
import de.threateningcodecomments.routinetimer.R
import kotlin.math.abs


class CountingService : Service() {
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
        val currentTile = ResourceClass.currentTiles[routineUid]

        if (Notifications.notifications.keys.size == 0) {
            Notifications.initNotification(routineUid)
        }

        Timers.startCounting(currentTile!!)

        return super.onStartCommand(intent, flags, startId)
    }

    fun stopService() {
        if (Notifications.notifications.size == 1) {
            MainActivity.countdownServiceRunning = false

            val notificationManager = instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()

            stopSelf()
        }
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
            val tile = ResourceClass.currentTiles[routineUid]!!

            val notification: Notification = Notification.Builder(instance, App.TIMING_CHANNEL_ID)
                    .setOnlyAlertOnce(true)
                    .build()

            instance.startForeground(STATIC_NOTIFICATION_ID, notification)

            val routine = ResourceClass.getRoutineOfTile(tile)

            val cancelI = Intent(MyBroadcastReceiver.CANCEL_ACTION)
            cancelI.putExtra(MyBroadcastReceiver.ROUTINE_UID_KEY, routine.uid)
            cancelIntent = PendingIntent.getBroadcast(instance, 0, cancelI, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun updateTileNotification(tile: Tile, currentTime: Long) {
            val nameStr = "${tile.name}"
            val timeStr =
                    if (tile.mode == Tile.MODE_COUNT_DOWN)
                        "Time remaining: ${ResourceClass.millisToHHMMSS(tile.countDownSettings.countDownTime - currentTime)}"
                    else
                        "Counted ${ResourceClass.millisToHHMMSS(currentTime)}"

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
                    .setContentTitle(nameStr)
                    .setContentText(ResourceClass.millisToHHMMSS(
                            if (tile.mode == Tile.MODE_COUNT_UP)
                                currentTime
                            else
                                tile.countDownSettings.countDownTime - currentTime
                    ))
                    .setSmallIcon(R.drawable.ic_logo)
                    .setCustomContentView(view)
                    .setOnlyAlertOnce(true)
                    .setGroup(App.TILE_GROUP)
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

                if (id != null && notification != null)
                    sendNotification(id, notification)
            }
            updateGroupNotification()
        }

        private fun updateGroupNotification() {
            val title = "Running ${notificationIds.size} tiles"
            val inboxStyle = NotificationCompat.InboxStyle()
                    .setBigContentTitle(title)
                    .setSummaryText("Running tiles")

            val summaryNotification = NotificationCompat.Builder(instance, App.TIMING_CHANNEL_ID)
                    .setContentTitle(title)
                    //set content text to support devices running API level < 24
                    .setContentText("Legacy content!")
                    .setSmallIcon(R.drawable.ic_logo)
                    //build summary info into InboxStyle template
                    .setStyle(inboxStyle)
                    //specify which group this notification belongs to
                    .setGroup(App.TILE_GROUP)
                    //set this notification as the summary for the group
                    .setGroupSummary(true)
                    .setOnlyAlertOnce(true)
                    .build()

            sendNotification(App.TILE_GROUP_SUMMARY_ID, summaryNotification)
        }

        val notifications = object : HashMap<String, Notification>() {
            override fun put(key: String, value: Notification): Notification? {
                notificationIds.put(key, ResourceClass.convertUidToInt(key))
                return super.put(key, value)
            }

            override fun remove(key: String): Notification? {
                notificationIds.remove(key)
                return super.remove(key)
            }
        }
        val notificationIds = object : HashMap<String, Int>() {
            override fun put(key: String, value: Int): Int? {
                var value1 = value
                if (this.size == 0)
                    value1 = STATIC_NOTIFICATION_ID

                val fickDich = super.put(key, value1)
                onUpdate()
                return fickDich
            }

            override fun remove(key: String): Int? {
                val temp = super.remove(key)

                if (this.size == 1) {
                    val idKey = this.keys.elementAt(0)
                    this[idKey] = STATIC_NOTIFICATION_ID
                }

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
        private var tileTimers: HashMap<String, Runnable> = HashMap()

        fun startCounting(tile: Tile) {
            val routineUid = ResourceClass.getRoutineOfTile(tile).uid

            if (tileTimers[routineUid] == null || tileTimers.containsKey(routineUid)) {
                createTimer(tile, routineUid)
            }
        }

        private fun createTimer(currentTile: Tile, routineUid: String) {
            currentTile.countingStart = System.currentTimeMillis()

            var currentTime: Long

            var lastTimeValue = ""
            val counter = object : Runnable {
                override fun run() {
                    currentTime = System.currentTimeMillis() - abs(currentTile.countingStart)
                    val curTimeStr = ResourceClass.millisToHHMMSS(currentTime)
                    if (lastTimeValue != curTimeStr) {
                        lastTimeValue = curTimeStr
                        Notifications.updateTileNotification(currentTile, currentTime)


                        if (currentTile.mode == Tile.MODE_COUNT_DOWN) {
                            val remainingTime = currentTile.countDownSettings.countDownTime - currentTime
                            if (remainingTime < 30) {
                                stopCounting(routineUid)
                            }
                        }

                    }

                    if (ResourceClass.currentTiles[routineUid] == currentTile)
                        Handler().postDelayed(this, 300)
                    else
                        MyLog.d("stopping")
                }
            }
            counter.run()
            tileTimers[routineUid] = counter

            ResourceClass.updateRoutine(currentTile)
        }

        fun stopCounting(routineUid: String) {
            val currentTile = ResourceClass.currentTiles[routineUid]
                    ?: ResourceClass.previousCurrentTiles[routineUid]
            val routine = ResourceClass.getRoutineOfTile(currentTile!!)

            if (currentTile.countingStart < 0)
                return

            val elapsedTime = if (currentTile.mode == Tile.MODE_COUNT_DOWN)
                currentTile.countDownSettings.countDownTime
            else
                System.currentTimeMillis() - abs(currentTile.countingStart)
            if (currentTile.mode == Tile.MODE_COUNT_UP || currentTile.countDownSettings.countDownTime >= elapsedTime) {
                MyLog.d(" elapsed time is $elapsedTime")
                currentTile.totalCountedTime += elapsedTime
            }
            ResourceClass.currentTiles[routineUid] = null

            if (Notifications.notifications.size == 1)
                instance.stopService()
            else {
                Notifications.notifications.remove(routineUid)
            }

            ResourceClass.updateRoutine(currentTile)
        }
    }

    companion object {
        lateinit var instance: CountingService

        const val CANCEL_RQ_CODE = 420
        const val ROUTINE_UID_KEY = "routine_uid"

        const val STATIC_NOTIFICATION_ID = 69420
    }
}