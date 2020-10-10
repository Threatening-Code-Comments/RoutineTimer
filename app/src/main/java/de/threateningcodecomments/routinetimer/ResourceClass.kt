package de.threateningcodecomments.routinetimer

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.maltaisn.icondialog.pack.IconPack
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack
import de.threateningcodecomments.routinetimer.Routine
import java.util.*

internal object ResourceClass {
    //region random
    var errorDrawable: Drawable? = null
        get() {
            if (field == null) {
                MyLog.d("LMAO THE ERROR DRAWABLE IS NULL")
                return object : Drawable() {
                    override fun draw(canvas: Canvas) {}
                    override fun setAlpha(alpha: Int) {}
                    override fun setColorFilter(colorFilter: ColorFilter?) {}
                    override fun getOpacity(): Int {
                        return PixelFormat.OPAQUE
                    }
                }
            }
            return field
        }

    @JvmStatic
    fun convertColorDayNight(isNightMode: Boolean, oldColor: Int): Int {
        val hsvValues = FloatArray(3)
        val red = Color.red(oldColor)
        val green = Color.green(oldColor)
        val blue = Color.blue(oldColor)
        Color.RGBToHSV(red, green, blue, hsvValues)
        if (hsvValues[0] == 0F) {
            return if (isNightMode) {
                Tile.DEFAULT_COLOR_DARK
            } else {
                Tile.DEFAULT_COLOR
            }
        }
        if (isNightMode) {
            hsvValues[1] = 0.5f
        } else {
            hsvValues[1] = 1f
        }
        return Color.HSVToColor(hsvValues)
    }

    @JvmStatic
    fun isNightMode(application: Application): Boolean {
        val nightModeFlags = application.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                updateNightMode(true)
                true
            }
            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                updateNightMode(false)
                false
            }
            else -> {
                updateNightMode(false)
                false
            }
        }
    }

    private var isNightMode = false

    @JvmStatic
    fun wasNightMode(): Boolean {
        return isNightMode
    }

    fun updateNightMode(nightMode: Boolean) {
        isNightMode = nightMode
    }

    fun updateNightMode(application: Application) {
        updateNightMode(isNightMode(application))
    }

    @JvmStatic
    fun calculateContrast(bgColor: Int): Int {
        val bgColor_cl = Color.valueOf(bgColor)
        val average = (bgColor_cl.red() + bgColor_cl.blue() + bgColor_cl.green()) / 3.toDouble()
        val contrastColor: Int
        contrastColor = if (average > 0.6) {
            Color.BLACK
        } else {
            Color.WHITE
        }
        return contrastColor
    }

    fun random(start: Int, end: Int): Int {
        val randomVal = Math.random()
        return ((randomVal + start) * end).toInt()
    }

    //endregion
    //region Routines
    var listenerAdded = false
    private var database: FirebaseDatabase? = null
    private var lastUser: FirebaseUser? = null
    private var valueEventListener: ValueEventListener? = null
    private var routines: ArrayList<Routine>? = ArrayList()
    fun loadRoutines() {
        database = FirebaseDatabase.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            routines = ArrayList()
            MyLog.d("FUCK FUCK FUCK ROUTINES IS NULL BECUASE OF NO USER HELP")
            return
        }
        if (user !== lastUser) {
            lastUser = user
            val routineRef = database!!.getReference("/users/" + user.uid + "/routines/")
            if (valueEventListener != null) {
                routineRef.removeEventListener(valueEventListener!!)
                MyLog.d("old listener is being removed!")
            }
            MyLog.d("new listener is being added!")
            valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    handleRoutineUpdate(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            routineRef.addValueEventListener(valueEventListener as ValueEventListener)
        }
    }

    private fun handleRoutineUpdate(snapshot: DataSnapshot) {
        val routinesIterable = snapshot.children
        MyLog.d("new value for routines in db: " + snapshot.value)
        if (snapshot.value == null) {
            routines!!.clear()
            routines!!.add(Routine.ERROR_ROUTINE)
            return
        }
        routines!!.clear()
        //iterates through all routines
        for (routineDataSnapshot in routinesIterable) {
            val routine = routineDataSnapshot.getValue(Routine::class.java)!!
            routines!!.add(routine)
        }
    }

    fun getRoutines(): ArrayList<Routine> {
        val routines = if (routines == null) ArrayList() else routines!!
        sortRoutines(routines)
        return routines
    }

    fun sortRoutines(routines: ArrayList<Routine>): ArrayList<Routine> {
        routines.sortWith(java.util.Comparator { a, b -> (b.getLastUsed() - a.getLastUsed()).toInt() })
        return routines
    }

    fun setRoutines(routines: ArrayList<Routine>?) {
        if (routines == null) {
            ResourceClass.routines = ArrayList()
            return
        }
        ResourceClass.routines = routines
    }

    fun saveRoutine(routine: Routine) {
        val user = FirebaseAuth.getInstance().currentUser
        database = FirebaseDatabase.getInstance()
        val path = "/users/" + user!!.uid + "/routines/"
        val key = routine.getUID()
        val value: Any = routine
        saveToDb(path, key, value)
    }

    fun generateRandomRoutine(): Routine {
        val tiles = ArrayList<Tile>()
        for (i in 0 until ((Math.random() + 1) * 3).toInt()) {
            tiles.add(Tile("random tile name " + random(0, 5) + "!",
                    random(0, 80),
                    Color.rgb(Math.random().toFloat() - 0.2f, Math.random().toFloat(), Math.random().toFloat())
            ))
        }
        return Routine(Routine.MODE_SEQUENTIAL, "Random routine " + random(0, 100), tiles, System.currentTimeMillis())
    }

    //endregion
    //region Database handling
    fun saveToDb(path: String?, key: String?, value: Any?) {
        val pathRef = database!!.getReference(path!!)
        val child = pathRef.child(key!!)
        child.setValue(value)
    }

    //endregion
    //region tmpTile
    @JvmStatic
    var tmpTile = Tile()
        private set

    fun resetTmpTile() {
        tmpTile = Tile()
    }

    //endregion
    //region Icon Pack
    private var iconPack: IconPack? = null
    private var context: Context? = null

    @JvmStatic
    fun getIconPack(): IconPack? {
        return if (iconPack != null) iconPack else loadIconPack()
    }

    private fun loadIconPack(): IconPack? {
        // Create an icon pack loader with application context.
        val loader = IconPackLoader(context!!)

        // Create an icon pack and load all drawables.
        iconPack = createDefaultIconPack(loader)
        iconPack!!.loadDrawables(loader.drawableLoader)
        return iconPack
    }

    @JvmStatic
    fun initIconPack(c: Context?) {
        context = c

        // Load the icon pack on application start.
        loadIconPack()
    }

    fun getIconDrawable(tile: Tile): Drawable? {
        val iconID = tile.iconID
        val icon: Drawable?
        icon = if (iconID != Tile.ERROR_ICONID) {
            getIconPack()!!.getIcon(iconID)!!.drawable
        } else {
            errorDrawable
        }
        return icon
    } //endregion
}