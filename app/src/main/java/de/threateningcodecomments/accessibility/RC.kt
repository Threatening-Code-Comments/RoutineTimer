package de.threateningcodecomments.accessibility

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
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
import de.threateningcodecomments.routinetimer.SelectRoutineFragment
import de.threateningcodecomments.routinetimer.SettingsFragment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

@SuppressLint("StaticFieldLeak")
object RC {

    private lateinit var context: Context

    @JvmStatic
    fun updateContext(context: Context) {
        this.context = context
    }

    object Debugging {
        @JvmStatic
        fun shortenUid(uid: String): String {
            val firstLetters = uid.substring(0..5)
            val lastLetters = uid.substring(uid.length - 5)

            return "$firstLetters ... $lastLetters"
        }

        @JvmStatic
        fun toast(message: String, duration: Int) =
                Toast.makeText(context, message, duration).show()
    }

    object Date {
        val date = LocalDateTime.now()

        fun getCurrentDay(): String {
            val formatter = DateTimeFormatter.ofPattern("dd")
            return date.format(formatter)
        }

        /*fun localizeDate(time: Long): String{
            val dateOfBirth: LocalDate = LocalDate.of()

            val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            val formattedDob: String = dateOfBirth.format(dateFormatter)
            println("Born: $formattedDob")
        }*/
    }

    object Local {
        private var endingMap = HashMap<String, String>()

        init {
            //dateEndingMap gets initiated on app launch

            val dateEndings = context.resources.getStringArray(R.array.strarr_specialDateEndings)
            for (str in dateEndings) {
                val splitVal = str.split("|")
                endingMap[splitVal[0]] = splitVal[1]
            }
        }

        fun getEndingForNumber(int: Int): String {
            val number = int.toString().last().toString()

            return if (endingMap[number] == null)
                endingMap["std"]!!
            else
                endingMap[number]!!
        }
    }

    object Resources {
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
            duration = 100.toLong()
            scrimColor = Color.TRANSPARENT
        }

        @JvmStatic
        private val resources = context.resources

        object Colors {
            /**
             * Green color for selecting. Handles Night mode automatically.
             */
            @JvmStatic
            val acceptColor: Int
                get() = getColorWithNightMode(R.color.colorAcceptLight, R.color.colorAcceptDark)

            /**
             * Green color for selecting. Handles Night mode automatically.
             */
            @JvmStatic
            val cancelColor: Int
                get() = getColorWithNightMode(R.color.colorCancelLight, R.color.colorCancelDark)

            @JvmStatic
            val onSurfaceColor: Int
                get() = getColorWithNightMode(R.color.onSurfaceLight, R.color.onSurfaceDark)

            /**
             * Adaptive Color, either #111111 (Light mode) or #b8b8b8 (dark mode)
             */
            @JvmStatic
            val contrastColor: Int
                get() = getColorWithNightMode(R.color.contrastLightMode, R.color.contrastDarkMode)

            @JvmStatic
            val extremeContrastColor: Int
                get() = getColorWithNightMode(R.color.extremeContrastLightMode, R.color.extremeContrastDarkMode)

            @JvmStatic
            val primaryColor: Int
                get() = getColorWithNightMode(R.color.colorPrimary, R.color.colorPrimaryDark)

            @JvmStatic
            val secondaryColor: Int
                get() = getColor(R.color.colorSecondary)

            @JvmStatic
            val disabledColor: Int
                get() = getColorWithNightMode(R.color.disabledColor, R.color.disabledColorDark)

            /**
             * Takes two ids as input and returns the appropriate color for the night mode.
             *
             * @param idLight desired color for light mode
             * @param idDark desired color for dark mode
             */
            @JvmStatic
            private fun getColorWithNightMode(idLight: Int, idDark: Int): Int {
                val colorId =
                        if (isNightMode)
                            idDark
                        else
                            idLight

                return Resources.getColor(colorId)
            }
        }

        object Text {
            @JvmStatic
            val NO_TEXT_CHANGED_CHAR = resources.getString(R.string.constStr_noTextChangedChar).toCharArray()[0]
        }

        object Drawables {
            @JvmStatic
            fun getModeDrawable(tile: Tile): Drawable {
                val id =
                        when (tile.mode) {
                            Tile.MODE_COUNT_UP -> R.drawable.ic_mode_count_up
                            Tile.MODE_COUNT_DOWN -> R.drawable.ic_mode_count_down
                            Tile.MODE_TAP -> R.drawable.ic_mode_tap
                            Tile.MODE_DATA -> R.drawable.ic_mode_data
                            Tile.MODE_ALARM -> R.drawable.ic_mode_alarm
                            else -> throw IllegalStateException("Icon couldn't be loaded")
                        }
                return getDrawable(id)
            }
        }

        @JvmStatic
        fun getDrawable(id: Int): Drawable = resources.getDrawable(id, context.theme)!!

        @JvmStatic
        fun getColor(id: Int): Int {
            return resources.getColor(id, context.theme)//ResourcesCompat.getColor(resources, id, context.theme)
        }

        @JvmStatic
        fun getString(id: Int): String =
                context.resources.getString(id)
    }

    object Conversions {
        object Time {
            @JvmStatic
            fun millisToHHMMSSorMMSS(millis: Long): String {
                val timeHHMMSSmm = millisToHHMMSSmmOrMMSSmm(millis)
                return timeHHMMSSmm.substringBefore('.')
            }

            @JvmStatic
            fun millisToHHMMSSmmOrMMSSmm(millis: Long): String {
                val hours = (millis / (1000 * 60 * 60)).toInt()
                val minutes = ((millis / (1000 * 60)) - (hours * 60)).toInt()
                val secs = (millis / 1000 - hours * 3600 - minutes * 60).toInt()
                val shortMillis = (millis - (hours * 3600 + minutes * 60 + secs) * 1000).toInt() / 10

                return if (hours == 0) String.format(
                        Locale.getDefault(),
                        "%02d:%02d.%02d",
                        minutes, secs, shortMillis)
                else String.format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d.%02d",
                        hours, minutes, secs, shortMillis)
            }

            @JvmStatic
            fun millisToHHMMSS(millis: Long): String {
                val time = millisToHHMMSSorMMSS(millis)

                val HHMMSSArray = ArrayList<String>()

                val split = time.split(':')
                when {
                    split.size == 3 -> {
                        HHMMSSArray.add(split[0])
                        HHMMSSArray.add(split[1])
                        HHMMSSArray.add(split[2])
                    }
                    split.isEmpty() -> {
                        HHMMSSArray.add("00")
                        HHMMSSArray.add("00")
                        HHMMSSArray.add("00")
                    }
                    split.size == 2 -> {
                        HHMMSSArray.add("00")
                        HHMMSSArray.add(split[0])
                        HHMMSSArray.add(split[1])
                    }
                }

                var text = ""
                for ((index, str) in HHMMSSArray.withIndex()) {
                    var textToEdit = ""

                    textToEdit = when {
                        str.length > 2 ->
                            throw IllegalStateException("String length of $str in $HHMMSSArray is invalid")
                        str.length == 1 ->
                            "0$str"
                        str.isEmpty() ->
                            "00"
                        else ->
                            str
                    }
                }


                text = "${HHMMSSArray[0]}:${HHMMSSArray[1]}:${HHMMSSArray[2]}"

                return text
            }

            @JvmStatic
            fun convertReadableToMillis(str: String): Long {
                val timeStrValues = str.split(':')

                val timeValues = ArrayList<Int>()
                for (timeStr in timeStrValues)
                    timeValues.add(timeStr.toInt())

                var seconds = 0
                var minutes = 0
                var hours = 0

                when (timeValues.size) {
                    //ss
                    1 -> {
                        hours = 0
                        minutes = 0
                        seconds = timeValues[0]
                    }
                    //mm:ss
                    2 -> {
                        hours = 0
                        minutes = timeValues[0]
                        seconds = timeValues[1]
                    }
                    3 -> {
                        hours = timeValues[0]
                        minutes = timeValues[1]
                        seconds = timeValues[2]
                    }
                }

                minutes += hours * 60
                seconds += minutes * 60

                return (seconds * 1000).toLong()
            }

            fun addDigitToTimeString(timeStr: String, charToAdd: Char): String {
                val timeCharArrayList = ArrayList<Char>(timeStr.toMutableList())

                //make hours HHMMSS and not HHHMMSS
                if (timeCharArrayList.indexOf(':') == 2 && charToAdd != TIME_CHANGE_BACKSPACE_CHAR)
                //if the format currently would be HHH:MM:SS and the first H can be removed (eg. 012:02:45),
                // remove it
                    if (timeCharArrayList[0] == '0')
                        timeCharArrayList.removeAt(0)
                    //if it can't be removed, the string is "full"
                    else
                        return timeCharArrayList.toCharArray().concatToString()

                //remove : chars to convert from timeStr = [h, h, :, m, m, :, s, s, x] to [h, h, m, m, s, s, x]
                timeCharArrayList.removeAll(arrayOf(':'))

                if (charToAdd == TIME_CHANGE_BACKSPACE_CHAR) {
                    timeCharArrayList.removeLast()
                    timeCharArrayList.add(0, '0')
                } else
                    timeCharArrayList.add(charToAdd)

                //formats hhmmsss to hhh:mm:ss -> places : at best indices
                val firstIndexAdd = timeCharArrayList.lastIndex - 1
                timeCharArrayList.add(firstIndexAdd, ':')

                val secondIndexAdd = timeCharArrayList.lastIndex - 4
                timeCharArrayList.add(secondIndexAdd, ':')

                return timeCharArrayList.toCharArray().concatToString()
            }

            fun shortenTimeString(cdTimeText: String): String {
                val hhmmss = cdTimeText.split(':')

                return when {
                    hhmmss[0] == "00" ->
                        if (hhmmss[1] == "00")
                            "${hhmmss[2]}s"
                        else
                            "${hhmmss[1]}:${hhmmss[2]}"
                    else -> cdTimeText
                }
            }


            const val TIME_CHANGE_BACKSPACE_CHAR = 'b'
        }

        object Colors {
            fun calculateColorFromHue(hue: Float): Int {
                val hsv = floatArrayOf(hue, 1.0f, 1.0f)
                var color = Color.HSVToColor(hsv)
                if (hue < 0.1) {
                    color = Tile.DEFAULT_COLOR_DARK
                }
                color = convertColorDayNight(isNightMode, color)
                return color
            }

            fun getHueOfColor(color: Int): Float {
                if (color == Tile.DEFAULT_COLOR_DARK || color == Tile.DEFAULT_COLOR)
                    return 0f

                val hsv = floatArrayOf(0f, 0f, 0f)

                Color.colorToHSV(color, hsv)

                return hsv[0]
            }

            public fun convertColorDayNight(isNightMode: Boolean, oldColor: Int): Int {
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
                val bgColorCl = Color.valueOf(bgColor)
                val average = (bgColorCl.red() + bgColorCl.blue() + bgColorCl.green()) / 3.toDouble()
                val contrastColor: Int
                contrastColor = if (average > 0.6) {
                    ResourcesCompat.getColor(context.resources, R.color.contrastLightMode, null)
                } else {
                    Color.WHITE
                }
                return contrastColor
            }
        }

        object Size {
            fun pxToDp(px: Int): Int {
                val displayMetrics: DisplayMetrics = context.resources.displayMetrics
                return (px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
            }

            fun dpToPx(dp: Int) =
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            dp.toFloat(),
                            android.content.res.Resources.getSystem().displayMetrics)

        }

        @JvmStatic
        fun convertUidToInt(uid: String): Int {
            var numberStr = ""
            var number: Long

            val stepSize = 10
            for (i in uid.indices step stepSize) {
                var temp = 0
                for (j in 0 until stepSize) {
                    if (i + j >= uid.length)
                        break
                    temp += uid.toCharArray()[i + j].toInt()
                }
                numberStr += temp.toString()
            }

            number = numberStr.toLong()
            while (number > Int.MAX_VALUE) {
                number /= 2
            }
            return number.toInt()
        }
    }

    class ResizeAnimation(var view: View, val targetHeight: Int) : Animation() {
        val startWidth: Int = view.height

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            val newHeight = (startWidth + (targetHeight - startWidth) * interpolatedTime).toInt()
            view.layoutParams.height = newHeight
            view.requestLayout()
        }


        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    object Anim {
        private lateinit var animContext: Context

        lateinit var slideUpIn: Animation
        lateinit var slideUpOut: Animation
        lateinit var slideDownIn: Animation
        lateinit var slideDownOut: Animation
        lateinit var slideLeftOut: Animation
        lateinit var slideLeftIn: Animation

        lateinit var scaleDown: Animation
        lateinit var scaleUp: Animation
        lateinit var scaleUpSlow: Animation
        lateinit var scaleDownVertical: Animation
        lateinit var scaleUpVertical: Animation

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
            slideLeftOut = loadAnimation(R.anim.slide_left_out)
            slideLeftIn = loadAnimation(R.anim.slide_left_in)

            scaleDown = loadAnimation(R.anim.scale_down)
            scaleUp = loadAnimation(R.anim.scale_up)
            scaleUpSlow = loadAnimation(R.anim.scale_up_slow)
            scaleDownVertical = loadAnimation(R.anim.scale_down_vertical)
            scaleUpVertical = loadAnimation(R.anim.scale_up_vertical)

            expandTileLeft = loadAnimation(R.anim.expand_tile_left)
            expandTileRight = loadAnimation(R.anim.expand_tile_right)
            collapseTileLeft = loadAnimation(R.anim.collapse_tile_left)
            collapseTileRight = loadAnimation(R.anim.collapse_tile_right)
        }

        private fun loadAnimation(id: Int): Animation = AnimationUtils.loadAnimation(animContext, id)
    }

    @JvmStatic
    val isNightMode: Boolean
        get() {
            val nightModeFlags =
                    context.resources.configuration.uiMode and
                            Configuration.UI_MODE_NIGHT_MASK

            return when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    true
                }
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    false
                }
                else -> {
                    false
                }
            }
        }

    private fun random(start: Int, end: Int): Int {
        val randomVal = Math.random()
        return ((randomVal + start) * end).toInt()
    }

    var routines: ArrayList<Routine> = ArrayList()

    var currentTiles: Db.CurrentTileMap = Db.CurrentTileMap()
    var previousCurrentTiles = HashMap<String, Tile>()

    object Db {
        @JvmStatic
        fun updatePreference(preference: Preference, newValue: Any) {
            val user = FirebaseAuth.getInstance().currentUser ?: return

            val prefCategory = preference.parent!!.title.toString().toLowerCase(Locale.getDefault())

            val path = "/users/${user.uid}/preferences/$prefCategory"
            val key = preference.key

            saveToDb(path, key, newValue)
        }

        @JvmStatic
        fun handlePrefUpdate(snapshot: DataSnapshot) {
            snapshot.child("general").value ?: initPreferenceValues(SettingsFragment.Preferences
                    .General())

            snapshot.child("dev").value ?: initPreferenceValues(SettingsFragment.Preferences.Dev())

            val preferences = snapshot.getValue(SettingsFragment.Preferences::class.java)

            SettingsFragment.preferences = preferences ?: SettingsFragment.Preferences()
        }

        @JvmStatic
        private fun initPreferenceValues(classPassed: SettingsFragment.Preferences.PreferenceCategory) {

            val user = FirebaseAuth.getInstance().currentUser ?: return

            val path = "/users/${user.uid}/preferences"
            val key = classPassed.name

            //if name field != null, it gets saved to db, so removing it on a copy of the original classPassed saves data
            val value = classPassed.copyWithoutName()

            saveToDb(path, key, value)
        }

        private var lastUser: FirebaseUser? = null
        private var database: FirebaseDatabase? = null
        private var valueEventListener: ValueEventListener? = null

        @JvmStatic
        fun loadDatabaseRes() {
            database = FirebaseDatabase.getInstance()
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                routines = ArrayList()
                MyLog.d("FUCK FUCK FUCK ROUTINES IS NULL BECUASE OF NO USER HELP")
                return
            }
            if (user != lastUser) {
                lastUser = user
                val routineRef = database!!.getReference("/users/" + user.uid + "/routines/")
                val prefRef = database!!.getReference("/users/${user.uid}/preferences")

                if (valueEventListener != null) {
                    routineRef.removeEventListener(valueEventListener!!)
                    prefRef.removeEventListener(valueEventListener!!)
                }

                valueEventListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        when (snapshot.ref) {
                            routineRef -> handleRoutineUpdate(snapshot)
                            prefRef -> handlePrefUpdate(snapshot)
                            else -> MyLog.d("snapshot ref is null!")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                }

                routineRef.addValueEventListener(valueEventListener!!)
                hasRoutineRefListener = true
                prefRef.addValueEventListener(valueEventListener!!)
            }
        }

        @JvmStatic
        fun saveToDb(path: String?, key: String?, value: Any?) {
            val pathRef = database!!.getReference(path!!)
            val child = pathRef.child(key!!)
            child.setValue(value)

            Log.d("database tag", "$key was set to $value in $path")
        }

        @JvmStatic
        private fun startEvent(tile: Tile) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val date = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                val parentRoutine = RoutinesAndTiles.getRoutineOfTile(tile)

                val basePath = "/users/${user.uid}/routineData/$date/${parentRoutine.uid}/"

                val eventStart = tile.countingStart

                val path = basePath + eventStart
                val key = "start"
                val value = eventStart.toString()

                saveToDb(path, key, value)
            }
        }

        @JvmStatic
        private fun stopEvent(tile: Tile) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                //countingStart should be negative here, to indicate that the tile is stopped
                val eventStart = abs(tile.countingStart)

                val date = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                val parentRoutine = RoutinesAndTiles.getRoutineOfTile(tile)

                val basePath = "/users/${user.uid}/routineData/$date/${parentRoutine.uid}/"

                //cancels event if countdownTile is stopped prematurely
                if (tile.mode == Tile.MODE_COUNT_DOWN && System.currentTimeMillis() - eventStart <= tile.countDownSettings.countDownTime) {
                    cancelEvent(tile)
                    return
                }

                //saves to db
                val path = basePath + eventStart

                var key = "tile"
                var value = tile.uid
                saveToDb(path, key, value)

                key = "duration"
                value = (System.currentTimeMillis() - eventStart).toString()
                saveToDb(path, key, value)
            }
        }

        @JvmStatic
        private fun cancelEvent(tile: Tile) {
            val eventStart = abs(tile.countingStart)

            if (eventStart == 0L || tile.mode == Tile.MODE_COUNT_UP)
                return

            val user = FirebaseAuth.getInstance().currentUser!!
            val date = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
            val parentRoutine = RoutinesAndTiles.getRoutineOfTile(tile)

            val path = "/users/${user.uid}/routineData/$date/${parentRoutine.uid}/"
            val key = eventStart.toString()
            val value = null

            saveToDb(path, key, value)
        }

        @JvmStatic
        /**
         * Updates the current tile in the users list of current tiles under /users/userUid/currentTiles/routineUid/
         *
         * @param tile currentTile to save
         * @param routine parent routine
         */
        internal fun updateCurrentTileInDB(tile: Tile?, routine: Routine) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val path = "/users/${user.uid}/currentTiles/${routine.uid}"
                val key = "currentTile"

                val value = tile?.uid

                saveToDb(path, key, value)
            }
        }

        @JvmStatic
        fun updateRoutineInDb(tile: Tile) {
            val routine = RoutinesAndTiles.getRoutineOfTile(tile)
            var index = 0

            for ((i, forTile) in routine.tiles.withIndex()) {
                if (forTile.uid == tile.uid)
                    index = i
            }

            routine.tiles[index] = tile

            updateRoutineInDb(routine)
        }

        @JvmStatic
        fun updateRoutineInDb(routine: Routine) {
            saveRoutine(routine)
        }

        @JvmStatic
        private var hasRoutineRefListener: Boolean = false
        fun removeRoutineListener() {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val routineRef = database!!.getReference("/users/" + user.uid + "/routines/")
                if (valueEventListener != null && hasRoutineRefListener) {
                    routineRef.removeEventListener(valueEventListener!!)
                    hasRoutineRefListener = false
                }
            }
        }

        @JvmStatic
        private fun handleRoutineUpdate(snapshot: DataSnapshot) {
            val routinesIterable = snapshot.children
            if (snapshot.value == null) {
                routines.clear()
                routines.add(Routine.ERROR_ROUTINE)
                return
            }
            routines.clear()
            //iterates through all routines
            for (routineDataSnapshot in routinesIterable) {
                val routine = routineDataSnapshot.getValue(Routine::class.java)!!
                routines.add(routine)
            }
            if (MainActivity.currentFragment is SelectRoutineFragment) {
                MainActivity.currentFragment.updateUI()
            }

            Log.d("database tag", "${snapshot.ref} was accessed")
        }

        @JvmStatic
        fun saveRoutine(routine: Routine) {
            val user = FirebaseAuth.getInstance().currentUser
            database = FirebaseDatabase.getInstance()
            val path = "/users/" + user!!.uid + "/routines/"
            val key = routine.uid
            val value: Any = routine
            saveToDb(path, key, value)
        }

        @JvmStatic
        fun removeRoutine(routine: Routine) {
            val user = FirebaseAuth.getInstance().currentUser
            database = FirebaseDatabase.getInstance()
            val path = "/users/" + user!!.uid + "/routines/"
            val key = routine.uid
            saveToDb(path, key, null)
        }

        @JvmStatic
        fun generateRandomRoutine(): Routine {
            val tiles = ArrayList<Tile>()
            for (i in 0 until ((Math.random() + 1) * 8).toInt()) {
                tiles.add(Tile("random nr." + random(0, 200) + "!",
                        random(0, 80),
                        Color.rgb(Math.random().toFloat() - 0.2f, Math.random().toFloat(), Math.random().toFloat()),
                        Tile.MODE_COUNT_UP,
                        UUID.randomUUID().toString(),
                        CountdownSettings((Math.random() * 60000 + 10000).toLong())
                ))
                if (Math.random() < 0.5) {
                    tiles[i].mode = Tile.MODE_COUNT_DOWN
                }
            }
            return Routine(round(Math.random()).toInt(), "Random routine " + random(0, 100), tiles, System.currentTimeMillis())
        }

        class CurrentTileMap : HashMap<String, Tile?>() {

            private var lastValue: CurrentTileMap = this

            override fun put(key: String, value: Tile?): Tile? {
                val oldValue = lastValue[key]

                if (oldValue == value)
                    return super.put(key, value)

                if (oldValue != null) {
                    previousCurrentTiles[key] = oldValue
                }

                val validTile = value ?: oldValue
                Db.updateCurrentTileInDB(value, RoutinesAndTiles.getRoutineOfTile(validTile!!))

                when {
                    value != null -> startEvent(value)
                    oldValue == null -> /*hier haben wir ein problem*/ MyLog.d("hey")
                    else -> stopEvent(oldValue)
                }

                if (value != null || oldValue != null)
                    lastValue = this

                val returnVal = super.put(key, value)

                MainActivity.currentFragment.updateUI()
                return returnVal
            }

        }

    }

    object RoutinesAndTiles {
        @JvmStatic
        fun getRoutineFromUid(oldUid: String?): Routine {
            var uid = oldUid
            if (oldUid == null || oldUid == "") {
                uid = Routine.ERROR_UID
            }

            var returnVal: Routine = Routine.ERROR_ROUTINE
            for (routine in routines) {
                if (uid == routine.uid) {
                    returnVal = routine
                }
            }

            return returnVal
        }

        @JvmStatic
        fun sortRoutines(routines: ArrayList<Routine>): ArrayList<Routine> {
            routines.sortWith({ a, b -> (b.lastUsed!! - a.lastUsed!!).toInt() })
            return routines
        }

        @JvmStatic
        fun getTileFromUid(uid: String?): Tile {
            if (uid == null) {
                return Tile.ERROR_TILE
            } else {
                for (routine in routines) {
                    for (tile in routine.tiles) {
                        if (uid == tile.uid) {
                            return tile
                        }
                    }
                }
            }
            return Tile.ERROR_TILE
        }

        @JvmStatic
        fun getRoutineOfTile(tile: Tile): Routine {
            for (routine in routines) {
                if (routine.tiles.contains(tile)) {
                    return routine
                }
            }
            return Routine.ERROR_ROUTINE
        }

        fun updateCurrentTile(tile: Tile?, routineUid: String) {
            if (currentTiles[routineUid] != tile) {
                currentTiles[routineUid] = tile
            }
        }
    }

    //region Icon Pack
    private var iconPack: IconPack? = null

    @JvmStatic
    fun getIconPack(): IconPack {
        return if (iconPack != null) iconPack!! else loadIconPack()!!
    }

    private fun loadIconPack(): IconPack? {
        // Create an icon pack loader with application context.
        val loader = IconPackLoader(context)

        // Create an icon pack and load all drawables.
        iconPack = createDefaultIconPack(loader)
        iconPack!!.loadDrawables(loader.drawableLoader)
        return iconPack
    }

    @JvmStatic
    fun initIconPack(c: Context?) {
        context = c!!

        // Load the icon pack on application start.
        loadIconPack()
    }

    @JvmStatic
    fun getIconDrawable(tile: Tile): Drawable? {
        val iconID = tile.iconID
        val icon: Drawable?
        icon = if (iconID != Tile.ERROR_ICONID) {
            getIconPack().getIcon(iconID)!!.drawable
        } else {
            Resources.errorDrawable
        }
        return icon
    } //endregion
}