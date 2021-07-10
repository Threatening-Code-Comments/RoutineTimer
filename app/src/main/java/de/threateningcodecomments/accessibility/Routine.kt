package de.threateningcodecomments.accessibility

import de.threateningcodecomments.routinetimer.SettingsFragment
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class Routine {
    var name: String? = null
        get() =
            if (SettingsFragment.preferences.dev.debug)
                this.uid
            else
                field

    var uid: String = "DEFAULT"
        get() = if (field == "DEFAULT") {
            val randomUid = UUID.randomUUID().toString()
            field = randomUid
            randomUid
        } else {
            field
        }
        set(value) {
            if (field == "DEFAULT") {
                field = value
            }
        }

    var lastUsed: Long? = null
        get() = if (field == null) {
            val currentTime = System.currentTimeMillis()
            field = currentTime
            field
        } else {
            field
        }

    override fun equals(other: Any?): Boolean {
        if (other !is Routine)
            return false
        return name == other.name && uid == other.uid && lastUsed == other.lastUsed && tiles == other.tiles
    }

    var mode = 0
    var tiles: ArrayList<Tile> by Delegates.observable(initialValue = ArrayList(), onChange = { _, _, newValue ->
        for (tile in newValue) {
            tile.setAccessibility(RC.isNightMode)
        }
    })

    //region Constructors
    constructor(mode: Int, name: String, tiles: ArrayList<Tile>, lastUsed: Long) {
        this.mode = mode
        this.name = name
        this.tiles = tiles
        this.lastUsed = lastUsed
    }

    constructor() {
        //this is the default constructor, needs to stay in place. The following line is to confuse the compiler out
        // of thinking this fucking thing is irrelevant.
        this.name
    }

    constructor(routine: Routine) {
        this.mode = routine.mode

        this.name = routine.name

        val tmpTiles: ArrayList<Tile> = ArrayList()
        for (tile in routine.tiles) {
            tmpTiles.add(Tile(tile))
        }
        this.tiles = tmpTiles

        this.uid = routine.uid

        this.lastUsed = routine.lastUsed
    }

    constructor(name: String?, uid: String, tiles: ArrayList<Tile>) {
        this.name = name
        this.uid = uid
        this.tiles = tiles
    }

    constructor(name: String, uid: String, mode: Int, tiles: ArrayList<Tile>) {
        this.name = name
        this.uid = uid
        this.mode = mode
        this.tiles = tiles
    }
    //endregion

    override fun toString(): String {
        var stingray = ""

        stingray += (this.name + " (name), ")
        stingray += (if (this.mode == MODE_CONTINUOUS) CONTINUOUS_MESSAGE else "$SEQUENTIAL_MESSAGE (mode), ")
        stingray += (this.tiles + " (tiles), ")
        stingray += (this.uid + " (uid):END!")

        return stingray
    }

    fun setAccessibility(isNightMode: Boolean) {
        for (tmpTile in tiles) {
            tmpTile.setAccessibility(isNightMode)
        }
    }

    companion object {
        private const val ERROR_NAME = Tile.ERROR_NAME
        const val ERROR_UID = "69420"

        const val MODE_CONTINUOUS = 1
        const val MODE_SEQUENTIAL = 0

        const val CONTINUOUS_MESSAGE: String = "Continuous mode"
        const val SEQUENTIAL_MESSAGE: String = "Sequential mode"

        @JvmField
        val ERROR_ROUTINE = Routine(ERROR_NAME, ERROR_UID,
                ArrayList(
                        listOf(Tile.ERROR_TILE)
                )
        )
    }
}