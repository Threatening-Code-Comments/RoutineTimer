package accessibility

import accessibility.ResourceClass.calculateContrast
import accessibility.ResourceClass.convertColorDayNight
import accessibility.ResourceClass.wasNightMode
import android.graphics.Color

class Tile//region Constructor
(var name: String? = DEFAULT_NAME, var iconID: Int = DEFAULT_ICONID, backgroundColor: Int = DEFAULT_COLOR, contrastColor: Int = DEFAULT_COLOR_DARK, private var isNightMode: Boolean = false, var mode: Int = MODE_COUNT_UP) {

    var backgroundColor: Int = backgroundColor
        get() {
            field = convertColorDayNight(wasNightMode(), field)
            return field
        }
        set(value) {
            field = convertColorDayNight(wasNightMode(), field)
            field = value
        }
    var contrastColor: Int = contrastColor
        get() {
            field = calculateContrast(backgroundColor)
            return field
        }

    constructor(name: String?, iconID: Int, color: Int) : this(name, iconID, color, calculateContrast(color), false, MODE_COUNT_UP) {}
    constructor(tile: Tile) : this(tile.name, tile.iconID, tile.backgroundColor, tile.contrastColor, tile.isNightMode, tile.mode)

    //endregion

    //region random
    private fun setAccessibility() {
        setAccessibility(isNightMode)
    }

    private fun setDayNightMode(isNightMode: Boolean) {
        this.isNightMode = isNightMode
        backgroundColor = convertColorDayNight(isNightMode, backgroundColor)
    }

    private fun setDayNightMode() {
        setDayNightMode(isNightMode)
    }

    fun setAccessibility(isNightMode: Boolean) {
        setDayNightMode(isNightMode)
        contrastColor = calculateContrast(backgroundColor)
    }

    override fun equals(tile: Any?): Boolean {
        if (tile !is Tile) {
            MyLog.d("comparison of ${tile?.javaClass} and Tile is not possible!")
            return false
        }

        if (tile.mode != this.mode || tile.name != this.name || tile.iconID != this.iconID || tile.backgroundColor != this.backgroundColor) {
            return false
        }
        return true
    }
    //endregion

    //region getters and setters

    fun setIconWithID(ID: Int) {
        setAccessibility()
        iconID = ID
    }


    fun isNightMode(): Boolean {
        return isNightMode
    }

    fun setNightMode(nightMode: Boolean) {
        isNightMode = nightMode
        setAccessibility()
    } //endregion

    override fun toString(): String {
        var stingray: String = ""

        stingray += (this.name + " (name), ")
        stingray += (this.backgroundColor.toString() + " (backgroundColor), ")
        stingray += (this.iconID.toString() + " (icon):END!")

        return super.toString()
    }

    companion object {
        const val DEFAULT_NAME = ""
        const val ERROR_NAME = "HELP THIS IS ERROR AAAH"
        const val DEFAULT_ICONID = 0
        const val ERROR_ICONID = 69420
        const val DEFAULT_COLOR = -0x1
        const val DEFAULT_COLOR_DARK = -0xdbdbdc
        const val MODE_COUNT_UP = 1
        const val COUNT_UP_MESSAGE = "Counting up"
        const val MODE_COUNT_DOWN = -1
        const val COUNT_DOWN_MESSAGE = "Counting down"

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