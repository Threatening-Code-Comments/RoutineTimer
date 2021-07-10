package de.threateningcodecomments.accessibility

import android.graphics.Color
import com.google.firebase.database.Exclude
import de.threateningcodecomments.routinetimer.R
import java.util.*

class Tile {
    var name: String?
    var iconID: Int
    private var isNightMode: Boolean = false
        set(value) {
            backgroundColor = RC.Conversions.Colors.convertColorDayNight(isNightMode, backgroundColor)
            field = value
        }
    var mode: Int

    var resetSettings: ResetSettings = ResetSettings()

    var countDownSettings: CountdownSettings = CountdownSettings()
    var tapCount: Int = 0
    var data: TileData = TileData()
    var alarmSettings: AlarmSettings = AlarmSettings()

    var totalCountedTime: Long = 0L

    var countingStart: Long = -1L
        set(value) {
            isRunning =
                    (value > 0L)
            val alreadyRuns = RC.currentTiles.values.contains(this)
            if (isRunning && !alreadyRuns || !isRunning) {
                field = value
            } else if (alreadyRuns) {
                field = RC.currentTiles[RC.RoutinesAndTiles.getRoutineOfTile(this).uid]!!.countingStart
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
            val wasNightMode = RC.isNightMode
            field = RC.Conversions.Colors.convertColorDayNight(wasNightMode, field)
            return field
        }
        set(value) {
            val wasNightMode = RC.isNightMode
            field = RC.Conversions.Colors.convertColorDayNight(wasNightMode, field)
            field = value
        }
    var contrastColor: Int = Color.WHITE
        get() {
            field = RC.Conversions.Colors.calculateContrast(backgroundColor)
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
        contrastColor = RC.Conversions.Colors.calculateContrast(backgroundColor)
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

    @Exclude
    fun getModeAsString(): String {
        val countdownValue = RC.Resources.getString(R.string.constStr_tileMode_countDown)
        val countUpValue = RC.Resources.getString(R.string.constStr_tileMode_countUp)
        val tapValue = RC.Resources.getString(R.string.constStr_tileMode_tap)
        val dataValue = RC.Resources.getString(R.string.constStr_tileMode_data)
        val alarmValue = RC.Resources.getString(R.string.constStr_tileMode_alarm)


        return when (this.mode) {
            MODE_COUNT_DOWN -> countdownValue
            MODE_COUNT_UP -> countUpValue
            MODE_TAP -> tapValue
            MODE_DATA -> dataValue
            MODE_ALARM -> alarmValue
            else -> throw IllegalStateException("Tile mode is wrong")
        }
    }

    fun setModeFromString(text: String) {
        val countdownValue = RC.Resources.getString(R.string.constStr_tileMode_countDown)
        val countUpValue = RC.Resources.getString(R.string.constStr_tileMode_countUp)
        val tapValue = RC.Resources.getString(R.string.constStr_tileMode_tap)
        val dataValue = RC.Resources.getString(R.string.constStr_tileMode_data)
        val alarmValue = RC.Resources.getString(R.string.constStr_tileMode_alarm)


        this.mode = when (text) {
            countdownValue -> MODE_COUNT_DOWN
            countUpValue -> MODE_COUNT_UP
            tapValue -> MODE_TAP
            dataValue -> MODE_DATA
            alarmValue -> MODE_ALARM
            else -> throw IllegalStateException("Tile mode is wrong (text is $text)")
        }
    }

    companion object {
        const val MODE_LIMBO = 0
        const val MODE_COUNT_UP = 1
        const val MODE_COUNT_DOWN = 2
        const val MODE_TAP = 3
        const val MODE_DATA = 4
        const val MODE_ALARM = 5

        const val DEFAULT_NAME = ""
        const val ERROR_NAME = "HELP THIS IS ERROR AAAH"
        const val DEFAULT_ICONID = 0
        const val ERROR_ICONID = 69420
        const val DEFAULT_COLOR = -0x1
        const val DEFAULT_COLOR_DARK = -0xdbdbdc
        const val DEFAULT_TILE_UID = "default tile uid"

        const val COUNT_UP_MESSAGE = "Count up mode"
        const val COUNT_DOWN_MESSAGE = "Count down mode"
        const val TAP_MESSAGE = "Tap mode"
        const val DATA_MESSAGE = "Data mode"
        const val ALARM_MESSAGE = "Alarm mode"
        val MODE_MESSAGES = setOf(
                COUNT_UP_MESSAGE, COUNT_DOWN_MESSAGE, TAP_MESSAGE, DATA_MESSAGE, ALARM_MESSAGE)

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