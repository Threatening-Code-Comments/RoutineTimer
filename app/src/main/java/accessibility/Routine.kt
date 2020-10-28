package accessibility

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

    override fun equals(other: Any?): Boolean {
        if (other !is Routine)
            return false
        var r2 = other as Routine
        MyLog.d("val: $this")
        MyLog.d("com: $r2")
        MyLog.d(name == r2.name && uid == r2.uid && lastUsed == r2.lastUsed && tiles == r2.tiles)
        return name == r2.name && uid == r2.uid && lastUsed == r2.lastUsed && tiles == r2.tiles
    }

    var mode = 0
    lateinit var tiles: ArrayList<Tile>

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

    constructor() {}

    constructor(name: String, uid: String?, mode: Int, tiles: ArrayList<Tile>) {
        this.name = name
        this.uid = uid
        this.mode = mode
        this.tiles = tiles
    }
    //endregion

    override fun toString(): String {
        var stingray: String = ""

        stingray += (this.name + " (name), ")
        stingray += (if (this.mode == Routine.MODE_CONTINUOUS) Routine.CONTINUOUS_MESSAGE else Routine.SEQUENTIAL_MESSAGE + " (mode), ")
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