package de.threateningcodecomments.accessibility

import android.graphics.Color
import de.threateningcodecomments.routinetimer.MainActivity
import de.threateningcodecomments.routinetimer.R
import java.util.*

class Tile {
    var name: String?
    var iconID: Int
    private var isNightMode: Boolean = false
        set(value) {
            backgroundColor = ResourceClass.Conversions.Colors.convertColorDayNight(isNightMode, backgroundColor)
            field = value
        }
    var mode: Int

    var countDownSettings: CountdownSettings = DEFAULT_COUNTDOWN_SETTINGS

    var totalCountedTime: Long = 0L

    var countingStart: Long = -1L
        set(value) {
            isRunning =
                    (value > 0L)
            val alreadyRuns = ResourceClass.currentTiles.values.contains(this)
            if (isRunning && !alreadyRuns || !isRunning) {
                field = value
            } else if (alreadyRuns) {
                field = ResourceClass.currentTiles[ResourceClass.getRoutineOfTile(this).uid]!!.countingStart
            }
        }
    var isRunning: Boolean = countingStart > 0L

    var uid: String = DEFAULT_TILE_UID
        get() {
            if (field == DEFAULT_TILE_UID) {
                val uid = UUID.randomUUID().toString()
                field = uid
            }
            return field
        }
        set(value) {
            if (field == DEFAULT_TILE_UID) {
                field = value
            }
        }

    var backgroundColor: Int
        get() {
            val wasNightMode = ResourceClass.wasNightMode()
            field = ResourceClass.Conversions.Colors.convertColorDayNight(wasNightMode, field)
            return field
        }
        set(value) {
            val wasNightMode = ResourceClass.wasNightMode()
            field = ResourceClass.Conversions.Colors.convertColorDayNight(wasNightMode, field)
            field = value
        }
    var contrastColor: Int = Color.WHITE
        get() {
            field = ResourceClass.Conversions.Colors.calculateContrast(backgroundColor)
            return field
        }

    constructor(name: String? = DEFAULT_NAME, iconID: Int = DEFAULT_ICONID, backgroundColor: Int = DEFAULT_COLOR, mode: Int = MODE_COUNT_UP, uid: String = DEFAULT_TILE_UID, totalCountedTime: Long = DEFAULT_COUNTED_TIME, countdownSettings: CountdownSettings = DEFAULT_COUNTDOWN_SETTINGS) {
        this.name = name
        this.iconID = iconID
        this.mode = mode
        this.uid = uid
        this.uid = DEFAULT_TILE_UID
        this.backgroundColor = backgroundColor
        this.totalCountedTime = totalCountedTime
        this.countDownSettings = countdownSettings
    }

    constructor() : this(DEFAULT_NAME, DEFAULT_ICONID, DEFAULT_COLOR, MODE_COUNT_UP, DEFAULT_TILE_UID, DEFAULT_COUNTDOWN_SETTINGS)
    constructor(name: String, iconID: Int, backgroundColor: Int, mode: Int, uid: String, countdownSettings: CountdownSettings) : this(name, iconID, backgroundColor, mode, uid, DEFAULT_COUNTED_TIME, countdownSettings)
    constructor(name: String, iconID: Int, backgroundColor: Int, mode: Int, uid: String) : this(name, iconID, backgroundColor, mode, uid, DEFAULT_COUNTDOWN_SETTINGS)
    constructor(name: String, iconID: Int, color: Int) : this(name, iconID, color, MODE_COUNT_UP, UUID.randomUUID().toString(), DEFAULT_COUNTDOWN_SETTINGS)
    constructor(tile: Tile) : this(tile.name, tile.iconID, tile.backgroundColor, tile.mode, tile.uid, tile.totalCountedTime, tile.countDownSettings) {
        this.totalCountedTime = tile.totalCountedTime
    }

    fun setAccessibility(isNightMode: Boolean) {
        this.isNightMode = isNightMode
        contrastColor = ResourceClass.Conversions.Colors.calculateContrast(backgroundColor)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Tile) {
            return false
        }

        if (other.mode != this.mode || other.name != this.name || other.iconID != this.iconID || other.backgroundColor != this.backgroundColor) {
            return false
        }
        return true
    }

    override fun toString(): String {
        var stingray = ""

        stingray += (this.name + " (name), ")
        stingray += (this.backgroundColor.toString() + " (backgroundColor), ")
        stingray += (this.iconID.toString() + " (icon):END!")

        return super.toString()
    }

    fun getModeAsString(): String {
        val countdownValue = MainActivity.instance.getString(R.string.constStr_tileMode_countDown)
        val countUpValue = MainActivity.instance.getString(R.string.constStr_tileMode_countUp)

        return if (this.mode == MODE_COUNT_DOWN)
            countdownValue
        else
            countUpValue
    }

    companion object {
        const val MODE_COUNT_UP = 1
        const val MODE_COUNT_DOWN = -1

        const val DEFAULT_NAME = ""
        const val ERROR_NAME = "HELP THIS IS ERROR AAAH"
        const val DEFAULT_ICONID = 0
        const val ERROR_ICONID = 69420
        const val DEFAULT_COLOR = -0x1
        const val DEFAULT_COLOR_DARK = -0xdbdbdc
        const val DEFAULT_TILE_UID = "default tile uid"

        const val COUNT_UP_MESSAGE = "Counting up"
        const val COUNT_DOWN_MESSAGE = "Counting down"

        const val DEFAULT_COUNTED_TIME = 0L

        val DEFAULT_COUNTDOWN_SETTINGS = CountdownSettings()


        val ERROR_TILE = Tile(ERROR_NAME, ERROR_ICONID, Color.RED)
            get() {
                val name = field.name
                val iconID = field.iconID
                val color = field.backgroundColor
                return Tile(name, iconID, color)
            }
        val DEFAULT_TILE = Tile("Nothing here yet!", 12, DEFAULT_COLOR)
            get() {
                val name = field.name
                val iconID = field.iconID
                val color = field.backgroundColor
                return Tile(name, iconID, color)
            }
    }
}