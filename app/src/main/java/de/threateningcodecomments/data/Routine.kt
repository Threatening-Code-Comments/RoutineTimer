package de.threateningcodecomments.data

import de.threateningcodecomments.accessibility.ObservableList
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.accessibility.UROperation
import de.threateningcodecomments.routinetimer.SettingsFragment
import java.util.*
import kotlin.collections.ArrayList

class Routine {
    var name: String? = null
        get() =
            if (SettingsFragment.preferences.dev.debug)
                this.uid
            else
                field

    var uid: String = DEFAULT_UID
        set(value) {
            if (field == DEFAULT_UID || uidFromConstructor) {
                field = value
                uidFromConstructor = false
            }
        }

    init {
        //if the uid was set before, do not give it a new one
        if (uid == DEFAULT_UID)
            assignUniqueUid()
    }

    private fun assignUniqueUid() {
        var uidNumber: Double
        var uid: String

        do {
            uidNumber = Math.random() * 36 * 4
            uid = RC.Conversions.intToUid(uidNumber.toInt())
        } while (RC.routines.any { it.uid == uid })

        this.uid = uid
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
    var tiles: ObservableList<Tile> = ObservableList()
        set(value) {
            value.doOnUpdate { listBefore, operation, element ->
                var tilesAdded = ArrayList<Tile>()


                when (operation) {
                    in UROperation.ADDERS -> {
                        when (operation) {
                            UROperation.ADD -> {
                                tilesAdded.add(element as Tile)
                            }
                            UROperation.ADD_ALL -> {
                                tilesAdded = element as ArrayList<Tile>
                            }
                            else -> throw IllegalStateException("UROperation Error!")
                        }
                    }
                    in UROperation.DIMINISHERS -> {
                        //what happens here
                    }
                    in UROperation.MODIFIERS -> {
                        when (operation) {
                            //SET
                            UROperation.SET -> {
                                val pair = element as Pair<*, *>

                                //old tile is listBefore[pair.first]

                                tilesAdded.add(pair.second as Tile)
                            }
                            //REPLACE
                            UROperation.REPLACE -> {
                                element as Triple<*, *, *>

                                tilesAdded.add(element.third as Tile)
                            }
                            //REPLACE ALL
                            UROperation.REPLACE_ALL -> {
                                //??????
                            }
                            else -> throw IllegalStateException("UROperation Error!")
                        }
                    }
                    //the operation was GET
                    else -> return@doOnUpdate
                }

                for (tile in tilesAdded) {
                    if (tile != Tile.DEFAULT_TILE)
                        tile.updateUid(this)
                    tile.setAccessibility(RC.isNightMode)
                }
            }
            field = value
        }

    fun setTilesFromArrayList(list: ArrayList<Tile>) {
        tiles = ObservableList(list)
    }

    //region Constructors
    constructor(mode: Int, name: String, tiles: ArrayList<Tile>, lastUsed: Long) {
        this.mode = mode
        this.name = name
        this.tiles = ObservableList(tiles)
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
        setTilesFromArrayList(tmpTiles)

        this.uid = routine.uid

        this.lastUsed = routine.lastUsed
    }

    constructor(name: String?, uid: String, tiles: ArrayList<Tile>) {
        this.name = name
        this.uid = uid
        setTilesFromArrayList(tiles)
    }

    constructor(name: String, uid: String, mode: Int, tiles: ArrayList<Tile>) {
        this.name = name
        this.uid = uid
        this.mode = mode
        setTilesFromArrayList(tiles)
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

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + uid.hashCode()
        result = 31 * result + mode
        result = 31 * result + tiles.hashCode()
        return result
    }

    companion object {
        private const val ERROR_NAME = Tile.ERROR_NAME
        const val ERROR_UID = "69420"

        const val MODE_CONTINUOUS = 1
        const val MODE_SEQUENTIAL = 0

        const val DEFAULT_UID = "DEFAULT"

        const val CONTINUOUS_MESSAGE: String = "Continuous mode"
        const val SEQUENTIAL_MESSAGE: String = "Sequential mode"

        @JvmField
        val ERROR_ROUTINE = Routine(
            ERROR_NAME, ERROR_UID,
            ArrayList(
                listOf(Tile.ERROR_TILE)
            )
        )
    }

    constructor(
        uid: String,
        name: String,
        lastUsed: Long?,
        mode: Int,
        tiles: ArrayList<Tile.TileForDB>
    ) {
        uidFromConstructor = true

        this.uid = uid
        this.name = name
        this.lastUsed = lastUsed
        this.mode = mode

        val list = ArrayList<Tile>()
        for (tileForDB in tiles)
            list.add(tileForDB.asTile())
        this.setTilesFromArrayList(list)
    }

    private var uidFromConstructor = false

    fun asDBObject() =
        RoutineForDB(this)

    //Routine for DB here
    //should make db problems go away if i don't have multiple dumb dumbs
    class RoutineForDB {

        constructor(routine: Routine) {
            this.uid = routine.uid
            this.name = routine.name.toString()
            this.lastUsed = routine.lastUsed
            this.mode = routine.mode
            this.tiles = ArrayList()
            for (tile in routine.tiles)
                tiles.add(tile.asDBObject())
        }

        constructor() {
            uid = ""
            name = ""
            lastUsed = 0
            mode = 0
            tiles = ArrayList()
        }

        var uid: String
        var name: String
        var lastUsed: Long?
        var mode: Int

        var tiles: ArrayList<Tile.TileForDB>

        fun asRoutine(): Routine =
            Routine(uid, name, lastUsed, mode, tiles)
    }
}