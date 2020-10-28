package accessibility

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.maltaisn.icondialog.pack.IconPack
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack
import de.threateningcodecomments.routinetimer.MainActivity
import de.threateningcodecomments.routinetimer.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round


internal object ResourceClass {

    //region random vars

    lateinit var animContext: Context

    lateinit var slideUpIn: Animation
    lateinit var slideUpOut: Animation
    lateinit var slideDownIn: Animation
    lateinit var slideDownOut: Animation

    lateinit var scaleDown: Animation
    lateinit var scaleUp: Animation
    lateinit var scaleUpSlow: Animation

    lateinit var expandTileLeft: Animation
    lateinit var expandTileRight: Animation
    lateinit var collapseTileLeft: Animation
    lateinit var collapseTileRight: Animation
    fun initAnimations(context: Context) {
        animContext = context

        slideUpIn = loadAnimation(R.anim.slide_up_in)
        slideUpOut = loadAnimation(R.anim.slide_up_out)
        slideDownIn = loadAnimation(R.anim.slide_down_in)
        slideDownOut = loadAnimation(R.anim.slide_down_out)

        scaleDown = loadAnimation(R.anim.scale_down)
        scaleUp = loadAnimation(R.anim.scale_up)
        scaleUpSlow = loadAnimation(R.anim.scale_up_slow)

        expandTileLeft = loadAnimation(R.anim.expand_tile_left)
        expandTileRight = loadAnimation(R.anim.expand_tile_right)
        collapseTileLeft = loadAnimation(R.anim.collapse_tile_left)
        collapseTileRight = loadAnimation(R.anim.collapse_tile_right)
    }

    private fun loadAnimation(id: Int): Animation = AnimationUtils.loadAnimation(animContext, id)


    var errorDrawable: Drawable = object : Drawable() {
        override fun draw(canvas: Canvas) {}
        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {}
        override fun getOpacity(): Int {
            return PixelFormat.OPAQUE
        }
    }

    val sharedElementTransition = MaterialContainerTransform().apply {
        drawingViewId = R.id.nhf_MainActivity_navHostFragment
        duration = 300.toLong()
        scrimColor = Color.TRANSPARENT
    }

    //endregion

    //region random

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

    fun random(start: Int, end: Int): Int {
        val randomVal = Math.random()
        return ((randomVal + start) * end).toInt()
    }

    fun spToPx(sp: Float, context: Context): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics).toInt()
    }

    //endregion

    //region colors

    fun calculateColorFromHue(hue: Float): Int {
        val hsv = floatArrayOf(hue, 1.0f, 1.0f)
        var color = Color.HSVToColor(hsv)
        if (hue < 0.1) {
            color = Tile.DEFAULT_COLOR_DARK
        }
        color = convertColorDayNight(wasNightMode(), color)
        return color
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

    //endregion

    //region Routines

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

        MainActivity.currentFragment.updateUI()
    }

    fun getRoutineFromUid(oldUid: String?): Routine {
        var uid = oldUid
        if (oldUid == null || oldUid == "") {
            uid = Routine.ERROR_UID
        }

        var returnVal: Routine = Routine.ERROR_ROUTINE
        for (routine in getRoutines()) {
            if (uid == routine.uid) {
                returnVal = routine
            }
        }

        return returnVal
    }

    fun routineExists(routine: Routine): Boolean {
        for (tmpRoutine in routines!!) {
            if (tmpRoutine == routine) {
                return true
            }
        }
        return false
    }

    fun getRoutines(): ArrayList<Routine> {
        val routines = if (routines == null) ArrayList() else routines!!
        sortRoutines(routines)
        val tempList: ArrayList<Routine> = ArrayList()
        for (routine in routines) {
            tempList.add(Routine(routine))
        }
        return tempList
    }

    fun sortRoutines(routines: ArrayList<Routine>): ArrayList<Routine> {
        routines.sortWith(java.util.Comparator { a, b -> (b.lastUsed!! - a.lastUsed!!).toInt() })
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
        val key = routine.uid
        val value: Any = routine
        saveToDb(path, key, value)
    }

    fun removeRoutine(routine: Routine) {
        val user = FirebaseAuth.getInstance().currentUser
        database = FirebaseDatabase.getInstance()
        val path = "/users/" + user!!.uid + "/routines/"
        val key = routine.uid
        removeFromDb(path, key!!)
    }

    fun generateRandomRoutine(): Routine {
        val tiles = ArrayList<Tile>()
        for (i in 0 until ((Math.random() + 1) * 8).toInt()) {
            tiles.add(Tile("random nr." + random(0, 200) + "!",
                    random(0, 80),
                    Color.rgb(Math.random().toFloat() - 0.2f, Math.random().toFloat(), Math.random().toFloat())
            ))
        }
        return Routine(round(Math.random()).toInt(), "Random routine " + random(0, 100), tiles, System.currentTimeMillis())
    }

    //endregion

    //region Database handling
    fun saveToDb(path: String?, key: String?, value: Any?) {
        val pathRef = database!!.getReference(path!!)
        val child = pathRef.child(key!!)
        child.setValue(value)
    }

    fun removeFromDb(path: String, key: String) {
        val pathRef = database!!.getReference(path)
        val child = pathRef.child(key)
        child.removeValue()
    }
    //endregion

    //region Icon Pack
    private var iconPack: IconPack? = null
    private var context: Context? = null

    @JvmStatic
    fun getIconPack(): IconPack {
        return if (iconPack != null) iconPack!! else loadIconPack()!!
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