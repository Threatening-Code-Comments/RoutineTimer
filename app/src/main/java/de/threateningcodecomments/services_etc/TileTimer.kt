package de.threateningcodecomments.services_etc

import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.data.Tile
import de.threateningcodecomments.data.TileEvent
import kotlinx.coroutines.*
import kotlin.math.abs

class TileTimer(val tile: Tile, private val startEvent: TileEvent? = null) {
    val previouslyElapsed: Long = (startEvent?.data?.toLongOrNull()) ?: 0L

    val routine = RC.RoutinesAndTiles.getRoutineOfTile(tile)
    private val runningTilesOfRoutine = RC.runningTiles[routine.uid]

    var totalTime = 0L
    var elapsedTime = 0L

    private val handler = RC.handler

    private var doOnStopRun: (tile: Tile) -> Unit = {}
    fun doOnStop(block: (tile: Tile) -> Unit) {
        doOnStopRun = block
    }

    init {
        startEvent?.isPaused = false

        //ooga booga
        if (tile.mode == Tile.MODE_COUNT_DOWN || tile.mode == Tile.MODE_COUNT_UP) {
            updateRoutineLastUsed()

            initCountingStart()

            //run timers
            CoroutineScope(Dispatchers.Main).launch {
                if (tile.mode == Tile.MODE_COUNT_UP)
                    runCUTTimer()
                else
                    runCDTimer()
            }
        }
    }

    private var updateRunnable: UpdateRunnable = { _, _, _ -> }
    fun doOnUpdate(updateRunnable: UpdateRunnable) {
        this.updateRunnable = updateRunnable
    }

    private fun updateRoutineLastUsed() {
        if (tile.countingStart > 0)
            return

        val routine = RC.RoutinesAndTiles.getRoutineOfTileOrNull(tile)

        routine ?: return

        routine.lastUsed = System.currentTimeMillis()
    }

    private fun initCountingStart() {
        if (tile.countingStart < 0)
            tile.countingStart = System.currentTimeMillis()
    }

    private suspend fun runCUTTimer() {
        var shouldBeRunning = runningTilesOfRoutine.contains(tile)

        while (shouldBeRunning) {
            shouldBeRunning = runningTilesOfRoutine.contains(tile)

            elapsedTime =
                (System.currentTimeMillis() - tile.countingStart) + previouslyElapsed

            //total time is already buffered
            totalTime =
                tile.totalCountedTime + elapsedTime - previouslyElapsed

            delay(1000 - (elapsedTime % 1000))

            shouldBeRunning = runningTilesOfRoutine.contains(tile)

            if (!shouldBeRunning)
                break

            updateRunnable(tile, elapsedTime, totalTime)
        }

        stopTimer()
    }

    private suspend fun runCDTimer() {
        var shouldBeRunning = runningTilesOfRoutine.contains(tile)

        while (shouldBeRunning) {
            shouldBeRunning =
                runningTilesOfRoutine.contains(tile) &&
                        //small buffer for counting inaccuracies
                        elapsedTime <= tile.countDownSettings.countDownTime - 300

            elapsedTime =
                (System.currentTimeMillis() - tile.countingStart) + previouslyElapsed

            //total time is already buffered
            totalTime =
                tile.totalCountedTime + elapsedTime - previouslyElapsed

            //delay for 1s minus the last inaccuracy in delay
            delay(1000 - (elapsedTime % 1000))

            shouldBeRunning =
                runningTilesOfRoutine.contains(tile) &&
                        //small buffer for counting inaccuracies
                        elapsedTime <= tile.countDownSettings.countDownTime - 300

            if (!shouldBeRunning)
                break

            updateRunnable(tile, elapsedTime, totalTime)
        }

        stopTimer()
    }

    fun stopTimer() {
        //update total time
        val elapsedTime =
            System.currentTimeMillis() - tile.countingStart

        val acceptCountUp = tile.mode == Tile.MODE_COUNT_UP

        val greaterThanCDTime = tile.countDownSettings.countDownTime <= elapsedTime
        val acceptCountDown = tile.mode == Tile.MODE_COUNT_DOWN && greaterThanCDTime

        if (acceptCountUp || acceptCountDown)
            tile.totalCountedTime += elapsedTime


        //indicate that tile isn't running
        tile.countingStart = -abs(tile.countingStart)

        runningTilesOfRoutine.remove(tile)

        doOnStopRun(tile)

        RC.Db.updateRoutineInDb(tile)
    }
}

typealias UpdateRunnable = (tile: Tile, elapsedTime: Long, totalTime: Long) -> Unit