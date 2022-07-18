package de.threateningcodecomments.data

import android.graphics.Color
import com.google.firebase.database.Exclude
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.routinetimer.R
import java.util.*

class Tile {
    class TileModeException : IllegalStateException("Tile Mode is unrecognizable!") {

    }

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
    var data: TileData = TileData()
    var alarmSettings: AlarmSettings = AlarmSettings()
    var tapSettings: TapSettings = TapSettings()


    var totalCountedTime: Long = 0L

    var countingStart: Long = -1L

    var uid: String = DEFAULT_TILE_UID
        set(value) {
            if (field == DEFAULT_TILE_UID || value == DEFAULT_TILE_UID || uidFromConstructor) {
                field = value
                uidFromConstructor = false
            }
        }

    init {
        if (uid == DEFAULT_TILE_UID)
            assignUniqueUid()
    }

    private fun assignUniqueUid() {
        var uidNumber: Double
        var uid: String

        do {
            uidNumber = Math.random() * 36 * 4
            uid = RC.Conversions.intToUid(uidNumber.toInt())

            val uidExists =
                RC.routines.any { routine ->
                    routine.tiles.any { it.uid == uid }
                }
        } while (uidExists)

        this.uid = uid
    }

    fun updateUid(routine: Routine) {
        var uidNumber: Double
        var tileUid: String
        var uid: String

        do {
            uidNumber = Math.random() * 36 * 4
            tileUid = RC.Conversions.intToUid(uidNumber.toInt())
            uid = "${routine.uid}_$tileUid"

            val uidExists =
                routine.tiles.any { it.uid == uid }
        } while (uidExists)

        this.uid = uid
    }

    var backgroundColor: Int = 0
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

    constructor(
        name: String? = DEFAULT_NAME,
        iconID: Int = DEFAULT_ICONID,
        backgroundColor: Int = DEFAULT_COLOR,
        mode: Int = MODE_COUNT_UP,
        uid: String = DEFAULT_TILE_UID,
        totalCountedTime: Long = DEFAULT_COUNTED_TIME,
        countdownSettings: CountdownSettings = DEFAULT_COUNTDOWN_SETTINGS
    ) {
        uidFromConstructor = true

        this.name = name
        this.iconID = iconID
        this.mode = mode
        this.uid = uid
        this.uid = DEFAULT_TILE_UID
        this.backgroundColor = backgroundColor
        this.totalCountedTime = totalCountedTime
        this.countDownSettings = countdownSettings
    }

    constructor() : this(
        DEFAULT_NAME,
        DEFAULT_ICONID,
        DEFAULT_COLOR,
        MODE_COUNT_UP,
        DEFAULT_TILE_UID,
        DEFAULT_COUNTDOWN_SETTINGS
    )

    constructor(
        name: String,
        iconID: Int,
        backgroundColor: Int,
        mode: Int,
        uid: String,
        countdownSettings: CountdownSettings
    ) : this(name, iconID, backgroundColor, mode, uid, DEFAULT_COUNTED_TIME, countdownSettings)

    constructor(name: String, iconID: Int, backgroundColor: Int, mode: Int, uid: String) : this(
        name,
        iconID,
        backgroundColor,
        mode,
        uid,
        DEFAULT_COUNTDOWN_SETTINGS
    )

    constructor(name: String, iconID: Int, color: Int) : this(
        name,
        iconID,
        color,
        MODE_COUNT_UP,
        UUID.randomUUID().toString(),
        DEFAULT_COUNTDOWN_SETTINGS
    )

    constructor(tile: Tile) : this(
        tile.name,
        tile.iconID,
        tile.backgroundColor,
        tile.mode,
        tile.uid,
        tile.totalCountedTime,
        tile.countDownSettings
    ) {
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


        return when (this.mode) {
            MODE_COUNT_DOWN -> countdownValue
            MODE_COUNT_UP -> countUpValue
            MODE_TAP -> tapValue
            MODE_DATA -> dataValue
            else -> throw IllegalStateException("Tile mode is wrong")
        }
    }

    fun setModeFromString(text: String) {
        val countdownValue = RC.Resources.getString(R.string.constStr_tileMode_countDown)
        val countUpValue = RC.Resources.getString(R.string.constStr_tileMode_countUp)
        val tapValue = RC.Resources.getString(R.string.constStr_tileMode_tap)
        val dataValue = RC.Resources.getString(R.string.constStr_tileMode_data)


        this.mode = when (text) {
            countdownValue -> MODE_COUNT_DOWN
            countUpValue -> MODE_COUNT_UP
            tapValue -> MODE_TAP
            dataValue -> MODE_DATA
            else -> throw IllegalStateException("Tile mode is wrong (text is $text)")
        }
    }

    companion object {
        const val MODE_LIMBO = 0
        const val MODE_COUNT_UP = 1
        const val MODE_COUNT_DOWN = 2
        const val MODE_TAP = 3
        const val MODE_DATA = 4

        const val DEFAULT_NAME = ""
        const val ERROR_NAME = "HELP THIS IS ERROR AAAH"
        const val ERROR_UID = "69420"
        const val DEFAULT_ICONID = 0
        const val ERROR_ICONID = 69420
        const val DEFAULT_COLOR = -0x1
        const val DEFAULT_COLOR_DARK = -0xdbdbdc
        const val DEFAULT_TILE_UID = "69420_69420"

        const val COUNT_UP_MESSAGE = "Count up mode"
        const val COUNT_DOWN_MESSAGE = "Count down mode"
        const val TAP_MESSAGE = "Tap mode"
        const val DATA_MESSAGE = "Data mode"
        const val ALARM_MESSAGE = "Alarm mode"
        val MODE_MESSAGES = setOf(
            COUNT_UP_MESSAGE, COUNT_DOWN_MESSAGE, TAP_MESSAGE, DATA_MESSAGE, ALARM_MESSAGE
        )

        const val DEFAULT_COUNTED_TIME = 0L

        val DEFAULT_COUNTDOWN_SETTINGS = CountdownSettings()

        fun isDefaultUid(uid: String) = uid.split("_").last() == DEFAULT_TILE_UID.split("_").last()

        val ERROR_TILE = Tile(
            name = ERROR_NAME, iconID = ERROR_ICONID, backgroundColor = Color.RED, mode = MODE_COUNT_UP, uid =
            ERROR_UID
        )
            get() {
                val name = field.name
                val iconID = field.iconID
                val color = field.backgroundColor
                val mode = field.mode
                val uid = field.uid
                return Tile(name, iconID, color, mode, uid)
            }
        val DEFAULT_TILE: Tile
            get() {
                val name = "Nothing here\u200B yet!"
                val iconID = 12
                val color = DEFAULT_COLOR
                return Tile(name, iconID, color)
            }
    }

    fun asDBObject(): TileForDB = TileForDB(this)

    constructor(
        name: String,
        iconID: Int,
        mode: Int,
        uid: String,
        data: TileData,
        backgroundColor: Int,
        resetSettings: ResetSettings?,
        countdownSettings: CountdownSettings?,
        alarmSettings: AlarmSettings?,
        tapSettings: TapSettings?,
        totalCountedTime: Long?,
        countingStart: Long?
    ) {
        uidFromConstructor = true

        this.name = name
        this.iconID = iconID
        this.mode = mode
        this.uid = uid
        this.data = data
        this.backgroundColor = backgroundColor
        this.resetSettings = resetSettings ?: ResetSettings()
        this.countDownSettings = countDownSettings ?: CountdownSettings()
        this.alarmSettings = alarmSettings ?: AlarmSettings()
        this.tapSettings = tapSettings ?: TapSettings()
        this.totalCountedTime = totalCountedTime ?: 0L
        this.countingStart = countingStart ?: 0L
    }

    private var uidFromConstructor = false

    class TileForDB {
        constructor(tile: Tile) {
            this.name = tile.name.toString()
            this.iconID = tile.iconID
            this.mode = tile.mode
            this.uid = tile.uid
            this.data = tile.data
            this.backgroundColor = tile.backgroundColor
            this.resetSettings = if (tile.resetSettings.resets)
                tile.resetSettings
            else
                null
            this.countDownSettings = if (tile.mode == Tile.MODE_COUNT_DOWN)
                tile.countDownSettings
            else
                null
            this.alarmSettings = if (tile.mode == Tile.MODE_COUNT_DOWN)
                tile.alarmSettings
            else
                null
            this.tapSettings = if (tile.mode == MODE_TAP)
                tile.tapSettings
            else
                null
            this.totalCountedTime = if (tile.mode == MODE_COUNT_DOWN || tile.mode == MODE_COUNT_UP)
                tile.totalCountedTime
            else
                null
            this.countingStart = if (tile.mode == MODE_COUNT_UP || tile.mode == MODE_COUNT_DOWN)
                tile.countingStart
            else
                null
        }

        constructor()

        var name: String = ""
        var iconID: Int = -1
        var mode: Int = -1
        var uid: String = ""
        var data: TileData = TileData()
        var backgroundColor: Int = -1

        var resetSettings: ResetSettings? = ResetSettings()

        var countDownSettings: CountdownSettings? = CountdownSettings()

        var alarmSettings: AlarmSettings? = AlarmSettings()

        var tapSettings: TapSettings? = TapSettings()

        var totalCountedTime: Long? = -1

        var countingStart: Long? = -1

        fun asTile(): Tile =
            Tile(
                name,
                iconID,
                mode,
                uid,
                data,
                backgroundColor,
                resetSettings,
                countDownSettings,
                alarmSettings,
                tapSettings,
                totalCountedTime,
                countingStart
            )
    }
}