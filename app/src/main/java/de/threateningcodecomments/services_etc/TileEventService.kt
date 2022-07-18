package de.threateningcodecomments.services_etc

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.data.Routine
import de.threateningcodecomments.data.Tile
import de.threateningcodecomments.data.TileEvent
import de.threateningcodecomments.routinetimer.App
import de.threateningcodecomments.routinetimer.R
import java.lang.IllegalStateException
import java.util.*

class TileEventService : Service() {
    /*
    Situation:
    Service gets started because local tile has changed while the service wasn't running.

    Action:
    Update all tile timers. This does include the new tile and helps prevent "ghosting", where multiple things happen at once, but aren't all properly adressed.
    */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        instance = this

        startServiceWithNotification()

        update()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    private val timerList = HashMap<String, TileTimer>()

    private fun update(isPause: Boolean = false, isRepeat: Boolean = false, pauseOrRepeatTile: Tile? = null) {
        //iterate through all running tiles
        for (runningTile in RC.runningTiles.flatMap { it.value }) {

            //if there is a tile there for which there is no timer running, start a timer
            if (timerList.values.none { it.tile == runningTile }) {
                val uid = runningTile.uid

                //add tileevent if there is no paused tileEvent
                val pausedEventsOfTile = RC.tileEvents.flatMap { it.value }.filter {
                    it.tileUID == uid && it.isPaused
                }

                if (pausedEventsOfTile.isEmpty()) {
                    val time = System.currentTimeMillis()
                    val event = TileEvent(runningTile.uid, time)

                    val routine = RC.RoutinesAndTiles.getRoutineOfTileOrNull(runningTile)

                    routine ?: throw RC.RoutineNullException(runningTile)

                    RC.tileEvents[routine.uid].add(event)
                }

                //handle tile starting
                when (runningTile.mode) {
                    Tile.MODE_COUNT_UP, Tile.MODE_COUNT_DOWN -> {

                        timerList[runningTile.uid] =
                            TileTimer(runningTile, pausedEventsOfTile.maxByOrNull { it.start })
                    }
                    Tile.MODE_TAP -> {
                        runningTile.tapSettings.increment()
                        RC.runningTiles.removeTile(runningTile)
                        TileEventService.tileRunFragment?.updateRunningTile(runningTile, 0L, 0L)
                        return
                    }
                    Tile.MODE_DATA -> {
                        if (!runningTile.data.isInitialized) {
                            runningTile.data.isInitialized = true
                            TileEventService.tileRunFragment?.updateRunningTile(runningTile, 0L, 0L)
                            RC.runningTiles.removeTile(runningTile)
                            return
                        }

                        val wasSuccessful = runningTile.data.logFieldValue()
                        if (!wasSuccessful)
                            throw UnknownError("DataTile log went wrong.")

                        TileEventService.tileRunFragment?.updateRunningTile(runningTile, 0L, 0L)

                        runningTile.data.isInitialized = false
                        RC.runningTiles.removeTile(runningTile)
                    }
                    else -> throw IllegalStateException("Tile mode could not be determined ${runningTile.mode}")
                }


                updateTileNotification(runningTile)
            }

            //if there is a timer for which there is no tile there, it will stop itself
        }

        for (timer in timerList.values) {
            timer.apply {
                doOnUpdate { tile, elapsedTime, totalTime ->
                    tileRunFragment?.updateRunningTile(tile, elapsedTime, totalTime)

                    updateTileNotification(tile)
                }

                doOnStop {
                    updateNotifications()

                    //gets data to put into tileEvent
                    val dataToPut =
                        when (tile.mode) {
                            Tile.MODE_COUNT_UP, Tile.MODE_COUNT_DOWN -> timer.elapsedTime
                            Tile.MODE_TAP -> tile.tapSettings.increment
                            Tile.MODE_DATA -> tile.data.list.last()
                            else -> throw Tile.TileModeException()
                        }

                    //gets last tile event of this tile
                    val tileEvent =
                        RC.tileEvents[routine.uid].last {
                            it.tileUID == it.tileUID
                        }
                    tileEvent.data = dataToPut.toString()

                    val isPauseTile = tile == pauseOrRepeatTile
                    val shouldPause = isPause || isRepeat

                    tileEvent.isPaused =
                        isPauseTile and shouldPause

                    //total time is off by about 100 to 300ms, this compensates in the ui
                    if (tileEvent.isPaused)
                        TileEventService.tileRunFragment?.updateRunningTile(
                            tile,
                            dataToPut.toString().toLong(),
                            tile.totalCountedTime - 100
                        )

                    //skips cycling if instructed
                    val isNotPause = (isPauseTile and !shouldPause) or !isPauseTile
                    val modeSequential = routine.mode == Routine.MODE_SEQUENTIAL
                    val cyclingPossible = routine.tiles.size > routine.tiles.indexOf(tile) + 1

                    if (isNotPause and modeSequential and cyclingPossible)
                        cycleForward(tile, routine)
                }
            }
        }
    }

    fun cycleForward(tile: Tile, routine: Routine) {
        //stop old tile
        RC.runningTiles[routine.uid].remove(tile)

        //detect next tile
        val currentIndex = routine.tiles.indexOf(tile)

        val nextIndex =
            if (currentIndex + 1 < routine.tiles.size)
                currentIndex + 1
            else
                0

        val nextTile = routine.tiles[nextIndex]

        //run next tile
        RC.runningTiles[routine.uid].add(nextTile)
    }

    private fun updateGroupNotification() {
        var title = ""

        //todo
        // noti name
        // tile events
        for (timer in timerList.values)
            title += getString(R.string.str_TileEventService_runningTileString, timer.tile.name)

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
            .setSummaryText(getString(R.string.str_TileEventService_runningTileGroupString, timerList.size))

        val summaryNotification = NotificationCompat.Builder(this, App.TIMING_CHANNEL_ID)
            .setContentTitle(title)
            //set content text to support devices running API level < 24
            .setContentText(title)
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

    private fun updateNotifications() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        for (tileId in notifications.keys)
            if (timerList[tileId] == null) {
                notifications.remove(tileId)
                notificationManager.cancel(
                    notificationIds[tileId]!!
                )
            }
    }

    private fun startServiceWithNotification() {
        val notification: Notification = Notification.Builder(this, App.TIMING_CHANNEL_ID)
            .setOnlyAlertOnce(true)
            .build()

        startForeground(STATIC_NOTIFICATION_ID, notification)
    }

    private val notifications = object : HashMap<String, Notification>() {
        override fun put(key: String, value: Notification): Notification? {
            notificationIds[key] = RC.Conversions.convertUidToInt(key)
            return super.put(key, value)
        }

        override fun remove(key: String): Notification? {
            notificationIds.remove(key)
            return super.remove(key)
        }
    }
    private val notificationIds = object : HashMap<String, Int>() {
        override fun put(key: String, value: Int): Int? {
            var value1 = value
            if (this.size == 0 || super.get(key) == STATIC_NOTIFICATION_ID)
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

    private fun updateTileNotification(tile: Tile) {
        if (tile.mode == Tile.MODE_DATA) {
            RC.runningTiles.removeTile(tile)
            return
        }


        val timer = timerList[tile.uid]


        val nameStr = tile.name


        val elapsedTime = timer!!.elapsedTime
        val time =
            if (tile.mode == Tile.MODE_COUNT_DOWN)
                tile.countDownSettings.countDownTime - elapsedTime
            else
                elapsedTime

        val readableTime = RC.Conversions.Time.millisToHHMMSSorMMSS(time)
        val timeDisplayString =
            getString(
                if (tile.mode == Tile.MODE_COUNT_DOWN)
                    R.string.str_TileEventService_timeString_cd
                else
                    R.string.str_TileEventService_timeString_cu,
                readableTime
            )


        val tileIcon = RC.getIconDrawable(tile)!!.toBitmap(200, 200, null)

        val view = RemoteViews(packageName, R.layout.custom_noti_layout)

        view.setBitmap(R.id.iv_viewholder_noti_icon, "setImageBitmap", tileIcon)

        view.setInt(R.id.ll_viewholder_noti_root, "setBackgroundColor", tile.backgroundColor)
        view.setInt(R.id.iv_viewholder_noti_icon, "setColorFilter", tile.contrastColor)
        view.setInt(R.id.tv_viewholder_noti_name, "setTextColor", tile.contrastColor)
        view.setInt(R.id.tv_viewholder_noti_time, "setTextColor", tile.contrastColor)

        view.setTextViewText(R.id.tv_viewholder_noti_name, nameStr)
        view.setTextViewText(R.id.tv_viewholder_noti_time, timeDisplayString)

        val notification: Notification = Notification.Builder(this, App.TIMING_CHANNEL_ID)
            .setContentTitle(nameStr)
            .setContentText(readableTime)
            .setSmallIcon(R.drawable.ic_logo)
            .setCustomContentView(view)
            .setOnlyAlertOnce(true)
            .setGroup(App.TILE_GROUP)
            .build()

        notifications[tile.uid] = notification

        sendNotification(notificationIds[tile.uid]!!, notification)
    }

    private fun sendNotification(id: Int, notification: Notification) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notification)
    }

    companion object {
        const val STATIC_NOTIFICATION_ID = 69420

        private var instance: TileEventService? = null

        var tileRunFragment: TileRunFragment? = null
            set(value) {
                field = value
                update()
            }

        fun cycleForward(tile: Tile, routine: Routine) {
            instance ?: App.instance.startTileEventService()

            instance?.cycleForward(tile, routine)
        }

        fun update(isPause: Boolean = false, isRepeat: Boolean = false, tile: Tile? = null) {
            val anyTileIsRunning =
                RC.runningTiles.tiles > 0

            if (anyTileIsRunning && instance == null)
                App.instance.startTileEventService()

            if (!anyTileIsRunning)
                instance?.stopSelf()

            if (instance != null)
                instance!!.update(isPause, isRepeat, tile)
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    interface TileRunFragment {
        fun updateRunningTile(tile: Tile, elapsedTime: Long, totalTime: Long)
        //var dataField: AutoCompleteTextView
    }
}