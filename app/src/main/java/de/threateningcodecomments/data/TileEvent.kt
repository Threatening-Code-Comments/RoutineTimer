package de.threateningcodecomments.data

import com.google.firebase.database.Exclude
import de.threateningcodecomments.accessibility.RC
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*

class TileEvent {
    val startAsDate: String
        get() =
            SimpleDateFormat("yyyyDDMM", Locale.getDefault()).format(Date(start))

    var tileUID: String
        set(value) {
            val change = field != value

            field = value
            if (start != 0L && change)
                updateInDB()
        }

    @Exclude
    var start: Long
        set(value) {
            val change = field != value

            field = value

            if (change)
                updateInDB()
        }

    var data: String?
        set(value) {
            val change = field != value

            field = value

            if (change)
                updateInDB()
        }

    @Exclude
    var isPaused: Boolean = false
        set(value) {
            val change = field != value

            field = value
            
            if (change)
                updateInDB()
        }

    val tileMode: Int
        get() =
            tile?.mode ?: throw IllegalStateException("Tile mode is null or tile is null")

    private var skipDb = false

    constructor(tileUID: String, start: Long, data: String? = null, skipDb: Boolean = false) {
        this.skipDb = skipDb
        this.tileUID = tileUID
        this.start = start
        this.data = data
    }

    constructor(tileUID: String, start: Long, data: Int, skipDb: Boolean = false) :
            this(tileUID, start, data.toString(), skipDb)

    private var tile: Tile? = null
        get() {
            if (field == null) {
                field = RC.RoutinesAndTiles.getTileFromUid(tileUID)
            }

            return field
        }


    private var routine: Routine? = null
        get() {
            if (field == null) {
                field = RC.RoutinesAndTiles.getRoutineOfTileOrNull(tile ?: throw IllegalStateException("Tile is null."))
            }

            return field
        }

    override fun toString(): String {
        var s = "\n"

        s += super.toString()

        s += "\n     start: $start"
        s += "\n     tileUid: $tileUID"
        s += "\n    data: $data"

        if (isPaused)
            s += "\n    isPaused: true"

        return s
    }

    override fun equals(other: Any?): Boolean {
        return if (other is TileEvent)
            (tileUID == other.tileUID)
                    && (start == other.start)
        else
            false
    }

    override fun hashCode(): Int {
        return Objects.hash(tileUID, start)
    }

    class TileEventForDB constructor(
        val tileUid: String? = null,
        val tileMode: Int? = null,
        val start: Long? = null,
        val data: String? = null,
        var paused: Boolean? = null
    ) {
        constructor(te: TileEvent) : this(
            te.tileUID,
            te.tileMode,
            te.start,
            te.data,
            te.isPaused
        )

        init {
            paused =
                if (paused == true)
                    true
                else
                    null
        }

        private val exception = { str: String ->
            throw IllegalStateException("TileEvent does not contain $str!")
        }

        fun asTileEvent(): TileEvent =
            TileEvent(
                tileUid ?: exception("tileUid"),
                start ?: exception("start"),
                data = data,
                skipDb = true
            ).apply {
                this.isPaused = this@TileEventForDB.paused ?: false
                this.skipDb = false
            }
    }

    private fun updateInDB() {
        if (skipDb)
            return

        val routineUid = routine!!.uid

        val millis = start

        val path = "routineData/$routineUid/$tileUID/"
        val key = "$millis"
        val value = TileEventForDB(this)

        if (RC.tileEvents[routineUid].contains(this))
            RC.Db.saveToUserDb(path, key, value)
    }

    fun asDbObject(): TileEventForDB = TileEventForDB(this)
}