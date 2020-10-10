package de.threateningcodecomments.routinetimer

import java.util.*
import kotlin.collections.ArrayList

internal class Routine {
    var name: String? = null
    private var uid: String? = null
    private var lastUsed: Long? = null
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

    constructor(name: String?, tiles: ArrayList<Tile>?) {
        this.name = name
        this.tiles = tiles
    }

    constructor() {}

    //endregion

    fun setAccessibility(isNightMode: Boolean) {
        if (tiles == null) {
            tiles = ArrayList()
        }

        for (tmpTile in tiles!!) {
            tmpTile.setAccessibility(isNightMode)
        }
    }

    //region Getters and Setters
    fun getUID(): String? = if (uid == null) {
        UUID.randomUUID().toString()
    } else {
        uid
    }

    fun setUID(UID: String?) {
        if (this.uid == null) {
            this.uid = UID
        }
    }

    fun getLastUsed(): Long {
        if (lastUsed == null) {
            lastUsed = System.currentTimeMillis()
        }

        return lastUsed!!
    }

    fun setLastUsed(value: Long?) {
        lastUsed = value
    }
    //endregion

    companion object {
        const val ERROR_NAME = Tile.ERROR_NAME
        const val MODE_CONTINUOUS = 1
        const val MODE_SEQUENTIAL = 0

        const val CONTINUOUS_MESSAGE: String = "Continuous mode"
        const val SEQUENTIAL_MESSAGE: String = "Sequential mode"

        @JvmField
        val ERROR_ROUTINE = Routine(ERROR_NAME,
                ArrayList(
                        listOf(Tile.ERROR_TILE)
                )
        )
    }
}