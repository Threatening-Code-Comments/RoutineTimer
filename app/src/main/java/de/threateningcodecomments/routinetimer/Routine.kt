package de.threateningcodecomments.routinetimer

import java.util.*
import kotlin.collections.ArrayList

class Routine {
    var name: String? = null
    var uid: String? = null
        get() = if (field == null) {
            val randomUid = UUID.randomUUID().toString()
            field = randomUid
            randomUid
        } else {
            field
        }
        set(value) {
            if (field == null) {
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

    var mode = 0
    var tiles: ArrayList<Tile>? = null

    //region Constructors
    constructor(mode: Int, name: String, tiles: ArrayList<Tile>, lastUsed: Long) {
        this.mode = mode
        this.name = name
        this.tiles = tiles
        if (uid == null) {
            uid = UUID.randomUUID().toString()
        }
        this.lastUsed = lastUsed
    }

    constructor(name: String?, uid: String, tiles: ArrayList<Tile>?) {
        this.name = name
        this.uid = uid
        this.tiles = tiles
    }

    constructor() {}

    constructor(name: String, uid: String?, mode: Int, tiles: ArrayList<Tile>) {
        this.name = name
        this.uid = uid
        this.mode = mode
        this.tiles = tiles
    }
    //endregion

    fun setAccessibility(isNightMode: Boolean) {
        if (tiles == null) {
            tiles = ArrayList()
        }

        for (tmpTile in tiles!!) {
            tmpTile.setAccessibility(isNightMode)
        }
    }

    companion object {
        const val ERROR_NAME = Tile.ERROR_NAME
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