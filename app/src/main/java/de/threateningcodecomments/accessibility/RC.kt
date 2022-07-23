package de.threateningcodecomments.accessibility

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Handler
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.maltaisn.icondialog.pack.IconPack
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack
import de.threateningcodecomments.data.CountdownSettings
import de.threateningcodecomments.data.Routine
import de.threateningcodecomments.data.Tile
import de.threateningcodecomments.data.TileEvent
import de.threateningcodecomments.routinetimer.MainActivity
import de.threateningcodecomments.routinetimer.R
import de.threateningcodecomments.routinetimer.SelectRoutineFragment
import de.threateningcodecomments.routinetimer.SettingsFragment
import de.threateningcodecomments.services_etc.TileEventService
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.BiFunction
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

@SuppressLint("StaticFieldLeak")
object RC {
    val handler: Handler = Handler()

    /**
     * Adding or removing automatically launches / handles tile events.
     */
    var runningTiles = RunningTilesMap()
        //removes all empty lists from map
        get() = field.apply {
            val nullValues = filterValues { it.size == 0 }
            for (entry in nullValues)
                field.remove(entry.key)
        }

    var tileEvents = TileEventList()

    class TileEventList : HashMap<String, ObservableList<TileEvent>>() {
        private val updateRunnable = { lb: Any, operation: UROperation, element: Any? ->
            if (lb !is ObservableList<*>)
                throw IllegalStateException(
                    "Wrong list was passed.\n" +
                            "Expected list type was ObservableList<TileEvent> and actual type was " +
                            "${lb.javaClass.canonicalName}"
                )
            val listBefore = lb as ObservableList<*>

            /*
            * What needs to happen:
            *
            * When a tile event is:
            *   added: save tile event to db
            *   removed: remove tile event from db
            *   replaced: remove old tile event from db and add new one
            *
            * Conversion: we get an element.
            * The element should include a raw TileEvent to add to db or an indication of where to remove the old one.
            *
            * Then all elements to remove get removed and all to be added get added.*/

            var eventsToAdd = setOf<TileEvent>()
            var eventsToRemove = setOf<TileEvent>()

            when (operation) {
                in UROperation.ADDERS -> {
                    //eventsToRemove doesn't get touched

                    val events = mutableSetOf<TileEvent>()


                    if (operation == UROperation.ADD)
                        events.add(element as TileEvent)
                    else
                        events.addAll(element as Collection<TileEvent>)

                    eventsToAdd = events
                }
                in UROperation.DIMINISHERS -> {
                    //eventsToAdd doesn't get touched

                    val events = mutableSetOf<TileEvent>()

                    when (operation) {
                        UROperation.REMOVE -> events.add(element as TileEvent)
                        UROperation.REMOVE_AT -> {
                            val event = (listBefore as List<*>)[element as Int]
                            events.add(event as TileEvent)
                        }
                        UROperation.REMOVE_ALL -> events.addAll(element as Collection<TileEvent>)
                        UROperation.CLEAR -> events.addAll(listBefore as List<TileEvent>)
                        else -> throw IllegalStateException("Illegal operation passed: $operation")
                    }

                    eventsToRemove = events
                }
                in UROperation.MODIFIERS -> {
                    val toAdd = mutableSetOf<TileEvent>()
                    val toRemove = mutableSetOf<TileEvent>()

                    when (operation) {
                        //SET
                        UROperation.SET -> {
                            //element is Pair(index, element)
                            element as Pair<*, *>

                            val before = listBefore[element.first as Int]
                            toRemove.add(before as TileEvent)

                            val after = element.second
                            toAdd.add(after as TileEvent)
                        }
                        //REPLACE_ALL
                        UROperation.REMOVE_ALL -> TODO("Implement")
                        else -> throw IllegalStateException("Illegal operation was passed: $operation")
                    }
                }
                else -> {
                    //the operation was GET
                }
            }

            for (event in eventsToRemove) {
                val tileUid = event.tileUID
                val routineUid = RC.RoutinesAndTiles.getRoutineOfTileOrNull(tileUid)?.uid

                //event gets removed by setting value to null
                val path = "routineData/$routineUid/$tileUid"
                val key = event.start.toString()
                val value = null

                RC.Db.saveToUserDb(path, key, value)
            }

            for (event in eventsToAdd) {
                val tileUid = event.tileUID
                val routineUid = RC.RoutinesAndTiles.getRoutineOfTileOrNull(tileUid)?.uid

                val path = "routineData/$routineUid/$tileUid"
                val key = event.start.toString()
                val value = event.asDbObject()

                RC.Db.saveToUserDb(path, key, value)
            }
        }

        private fun getObList() =
            ObservableList<TileEvent>().apply {
                doOnUpdate(updateRunnable)
            }

        override fun get(key: String): ObservableList<TileEvent> {
            if (super.get(key) == null)
                put(key, getObList())

            return super.get(key)!!
        }

        fun getEventsOfTile(tile: Tile): List<TileEvent> {
            return flatMap { it.value }.filter { it.tileUID == tile.uid }
        }
    }

    object Directions {
        const val UP = 1
        const val DOWN = 2
        const val LEFT = 3
        const val RIGHT = 4
    }

    class RunningTilesMap : HashMap<String, RunningTilesPerRoutine>() {
        private fun update() = TileEventService.update()

        val tiles: Int
            get() = flatMap { it.value }.size

        fun containsTile(tile: Tile): Boolean {
            val list = flatMap { it.value }
            return list.contains(tile)
        }

        fun removeTile(tile: Tile) {
            for (routine in this.values)
                routine.remove(tile)
        }

        fun containsTile(uid: String): Boolean {
            val list = flatMap { it.value }.filter { it.uid == uid }
            return list.isNotEmpty()
        }

        fun putTile(tile: Tile) {
            val routine = RC.RoutinesAndTiles.getRoutineOfTileOrNull(tile)

            routine ?: throw RoutineNullException(tile)

            this[routine.uid].add(tile)
        }

        fun getTileByUid(uid: String): Tile {
            val list = flatMap { it.value }
            val candidates = list.filter { it.uid == uid }
            return candidates.first()
        }

        override fun put(key: String, value: RunningTilesPerRoutine): RunningTilesPerRoutine? {
            val put = super.put(key, value)
            update()
            return put
        }

        override fun putAll(from: Map<out String, RunningTilesPerRoutine>) {
            super.putAll(from)
            update()
        }

        override fun remove(key: String): RunningTilesPerRoutine? {
            val remove = super.remove(key)
            update()
            return remove
        }

        override fun remove(key: String?, value: RunningTilesPerRoutine?): Boolean {
            val remove = super.remove(key, value)
            update()
            return remove
        }

        override fun clear() {
            super.clear()
            update()
        }

        override fun replaceAll(
            function: BiFunction<in String, in RunningTilesPerRoutine, out
            RunningTilesPerRoutine>
        ) {
            super.replaceAll(function)
            update()
        }

        override fun replace(
            key: String,
            oldValue: RunningTilesPerRoutine?,
            newValue: RunningTilesPerRoutine
        ): Boolean {
            val replace = super.replace(key, oldValue, newValue)
            update()
            return replace
        }

        override fun replace(key: String, value: RunningTilesPerRoutine): RunningTilesPerRoutine? {
            val replace = super.replace(key, value)
            update()
            return replace
        }

        override fun get(key: String): RunningTilesPerRoutine {
            //add an empty list if entry is null
            super.get(key) ?: super.put(key, RunningTilesPerRoutine())
            return super.get(key)!!
        }
    }

    class RunningTilesPerRoutine : ArrayList<Tile>() {
        private fun update(
            isPause: Boolean = false,
            isRepeat: Boolean = false,
            tile: Tile? = null
        ) =
            TileEventService.update(isPause, isRepeat, tile)

        fun first() = firstOrNull()

        fun contains(uid: String): Boolean {
            val list = filter { it.uid == uid }
            return list.isNotEmpty()
        }

        override fun add(element: Tile): Boolean {
            val add = super.add(element)
            update()
            return add
        }

        fun add(element: Tile, isPause: Boolean = false, isRepeat: Boolean = false) {
            super.add(element)

            if (isPause || isRepeat)
                update(isPause, isRepeat, element)
            else
                update()
        }

        override fun add(index: Int, element: Tile) {
            super.add(index, element)
            update()
        }

        override fun addAll(elements: Collection<Tile>): Boolean {
            val addAll = super.addAll(elements)
            update()
            return addAll
        }

        override fun addAll(index: Int, elements: Collection<Tile>): Boolean {
            val addAll = super.addAll(index, elements)
            update()
            return addAll
        }

        override fun clear() {
            super.clear()
            update()
        }

        override fun set(index: Int, element: Tile): Tile {
            val set = super.set(index, element)
            update()
            return set
        }

        fun remove(element: Tile, isRepeat: Boolean = false, isPause: Boolean = false) {
            super.remove(element)

            if (isPause || isRepeat)
                update(isPause, isRepeat, element)
            else
                update()
        }

        override fun remove(element: Tile): Boolean {
            val remove = super.remove(element)
            update()
            return remove
        }

        override fun removeAll(elements: Collection<Tile>): Boolean {
            val removeAll = super.removeAll(elements)
            update()
            return removeAll
        }
    }

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

        val sharedElementTransition = null
        // TODO: 23.07.2021 reimplement transitions
        /*MaterialContainerTransform().apply {
            drawingViewId = R.id.nhf_MainActivity_navHostFragment
            duration = 100.toLong()
            scrimColor = Color.TRANSPARENT
        }*/

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

                return getColor(colorId)
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
                        else -> throw IllegalStateException("Icon couldn't be loaded")
                    }
                return getDrawable(id)
            }
        }

        @JvmStatic
        fun getDrawable(id: Int): Drawable =
            ResourcesCompat.getDrawable(resources, id, context.theme)!!

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
            fun millisToHHMMSSorMMSS(millis: Long?): String {
                val timeHHMMSSmm = millisToHHMMSSmmOrMMSSmm(millis ?: 0L)
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
                    minutes, secs, shortMillis
                )
                else String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d.%02d",
                    hours, minutes, secs, shortMillis
                )
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

            fun millisToShortTimeString(time: Long): String {
                val timeString = millisToHHMMSS(time)
                return shortenTimeString(timeString)
            }


            const val TIME_CHANGE_BACKSPACE_CHAR = 'b'
        }

        object Dates {
            private fun getDateFromOffset(dayOfYear_p: Int = -1, offsetDays: Int): Calendar {
                //init
                val cal = Calendar.getInstance()

                val dayOfYear =
                    if (dayOfYear_p == -1)
                        cal.get(Calendar.DAY_OF_YEAR)
                    else
                        dayOfYear_p

                val getC = { key: Int ->
                    cal.get(key)
                }

                val setC = { key: Int, value: Int ->
                    cal.set(key, value)
                }

                setC(Calendar.DAY_OF_YEAR, dayOfYear)

                //if no offset
                if (offsetDays == 0)
                    return cal

                //if offset smaller 0
                if (offsetDays < 0) {
                    if (dayOfYear + offsetDays > 0)
                        setC(Calendar.DAY_OF_YEAR, dayOfYear - 1)
                    else {
                        val year = getC(Calendar.YEAR)
                        setC(Calendar.YEAR, year - 1)

                        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_YEAR)

                        setC(Calendar.DAY_OF_YEAR, maxDay)
                    }

                    return cal
                }

                //if offset greater 0
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
                if (dayOfYear + offsetDays <= maxDay)
                    setC(Calendar.DAY_OF_YEAR, dayOfYear + 1)
                else {
                    val year = getC(Calendar.YEAR)
                    setC(Calendar.YEAR, year + 1)

                    setC(Calendar.DAY_OF_YEAR, 0)
                }

                return cal
            }

            const val LENGTH_LONG = 2
            const val LENGTH_SHORT = 0
            const val LENGTH_MID = 1

            fun getAbbreviation(
                calendar: Calendar,
                locale: Locale = Locale.getDefault(),
                length: Int = LENGTH_SHORT
            ): String {
                return when (length) {
                    LENGTH_SHORT -> SimpleDateFormat("EEEEE", locale).format(calendar.time)
                    LENGTH_LONG -> SimpleDateFormat("EEEE", locale).format(calendar.time)
                    else -> SimpleDateFormat("EEEEEE", locale).format(calendar.time)
                }
            }

            fun getAbbreviation(
                dayOfWeek: Int,
                locale: Locale = Locale.getDefault(),
                length: Int = LENGTH_SHORT
            ): String {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, dayOfWeek)
                }

                return getAbbreviation(calendar, locale, length)
            }
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
                val contrastColor: Int = if (average > 0.6) {
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
                    android.content.res.Resources.getSystem().displayMetrics
                )

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

        fun intToUid(int: Int): String {
            return int.toString(36)
        }

        fun uidToInt(uid: String) = uid.toInt(36)

        fun incrementUid(uid: String, increment: Int = 1): String {
            val int = uidToInt(uid)
            return intToUid(int + increment)
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

    var routines: ObservableList<Routine> = ObservableList<Routine>().apply {
        doOnUpdate { listBefore, operation, element ->
        }
    }

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

        private var removeTileEventListener = {}

        private fun setUpEventListener(user: FirebaseUser) {
            val ref = database!!.getReference("").limitToFirst(5)

            val tileEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    MyLog.d("new; count = ${snapshot.childrenCount}")
                }

                override fun onCancelled(error: DatabaseError) {}
            }

            ref.addValueEventListener(tileEventListener)

            removeTileEventListener = {
                ref.removeEventListener(tileEventListener)
            }
        }

        /**
         * Gets called from [loadDatabaseRes], handles all updates concerning TileEvents
         */
        @JvmStatic
        fun handleEventUpdate(snapshot: DataSnapshot) {
            //todo implement

            val byDateMap = HashMap<String, Any?>()

            snapshot.children.forEach {
                byDateMap[it.key ?: throw java.lang.IllegalStateException()] = it.value
            }

            MyLog.d(byDateMap)
        }

        @JvmStatic
        fun handlePrefUpdate(snapshot: DataSnapshot) {
            snapshot.child("general").value ?: initPreferenceValues(
                SettingsFragment.Preferences
                    .General()
            )

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
                routines = ObservableList()
                MyLog.d("FUCK FUCK FUCK ROUTINES IS NULL BECUASE OF NO USER HELP")
                return
            }
            if (user != lastUser) {
                lastUser = user
                val routineRef = database!!.getReference("/users/" + user.uid + "/routines/")
                val prefRef = database!!.getReference("/users/${user.uid}/preferences")
                setUpEventListener(user)

                if (valueEventListener != null) {
                    routineRef.removeEventListener(valueEventListener!!)
                    prefRef.removeEventListener(valueEventListener!!)
                    removeTileEventListener()
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
        fun saveToUserDb(path: String, key: String, value: Any?) {
            val user = FirebaseAuth.getInstance().currentUser!!

            val userPath = "/users/${user.uid}/"

            val fullPath = "$userPath$path"

            saveToDb(fullPath, key, value)
        }

        @JvmStatic
        fun saveToDb(path: String, key: String, value: Any?) {
            val pathRef = database!!.getReference(path)
            val child = pathRef.child(key)
            child.setValue(value)

            Log.d(MyLog.DATABASE_TAG, "$path$key \nwas set to $value")
        }

        @JvmStatic
        private fun startEvent(tile: Tile) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val date = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                val parentRoutine = RoutinesAndTiles.getRoutineOfTileOrNull(tile)

                val basePath = "/users/${user.uid}/routineData/$date/${parentRoutine?.uid}/"

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
                val parentRoutine = RoutinesAndTiles.getRoutineOfTileOrNull(tile)

                val basePath = "/users/${user.uid}/routineData/$date/${parentRoutine?.uid}/"

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
            val parentRoutine = RoutinesAndTiles.getRoutineOfTileOrNull(tile)

            val path = "/users/${user.uid}/routineData/$date/${parentRoutine?.uid}/"
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
        internal fun updateCurrentTileInDB(tile: Tile?, routine: Routine?) {
            routine ?: return

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val path = "/users/${user.uid}/currentTiles/${routine.uid}"
                val key = "currentTile"

                val value = tile?.uid

                saveToDb(path, key, value)
            }
        }

        //übergib doch einfach die dummme drecks routine
        @JvmStatic
        fun updateRoutineInDb(tile: Tile) {
            if (tile.uid == Tile.DEFAULT_TILE_UID)
                return

            val routine = RoutinesAndTiles.getRoutineOfTileOrNull(tile)
            var index = 0

            routine ?: return

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
                val routine = routineDataSnapshot.getValue(Routine.RoutineForDB::class.java)!!
                routines.add(routine.asRoutine())
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
            val value: Any = routine.asDBObject()
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
            val routineUid = RC.Conversions.intToUid(routines.size)

            val tiles = ArrayList<Tile>()
            for (i in 0 until ((Math.random() + 1) * 8).toInt()) {
                tiles.add(
                    Tile(
                        name =
                        "random nr." + random(0, 200) + "!",

                        iconID =
                        random(0, 80),

                        backgroundColor =
                        Color.rgb(Math.random().toFloat() - 0.2f, Math.random().toFloat(), Math.random().toFloat()),

                        mode =
                        Tile.MODE_COUNT_UP,

                        uid =
                        "${routineUid}_${RC.Conversions.intToUid(tiles.size)}",

                        countdownSettings =
                        CountdownSettings((Math.random() * 60000 + 10000).toLong())
                    )
                )
                if (Math.random() < 0.5) {
                    tiles[i].mode = Tile.MODE_COUNT_DOWN
                }
            }
            return Routine(
                mode =
                round(Math.random()).toInt(),
                uid = routineUid,
                name = "Random routine " + random(0, 100),
                tiles = tiles
            )
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
                updateCurrentTileInDB(value, RoutinesAndTiles.getRoutineOfTileOrNull(validTile!!))

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
        fun generateRandomRoutine() = Db.generateRandomRoutine()

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
            routines.sortWith { a, b -> (b.lastUsed!! - a.lastUsed!!).toInt() }
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
        fun getRoutineOfTileOrNull(tile: Tile): Routine? {
            val routines = routines.filter { it.tiles.contains(tile) }.toMutableList()
            routines.remove(Routine.ERROR_ROUTINE)

            return routines.firstOrNull()
        }

        fun getRoutineOfTile(tile: Tile): Routine {
            val routines = routines.filter { it.tiles.contains(tile) }.toMutableList()
            routines.remove(Routine.ERROR_ROUTINE)

            return routines.first() ?: throw RoutineNullException(tile)
        }

        fun getRoutineOfTileOrNull(uid: String): Routine? {
            return routines.firstOrNull { routine ->
                routine.tiles.any { it.uid == uid }
            }
        }
    }

    class RoutineNullException(tile: Tile?) : IllegalStateException(
        "Routine is null!${
            if (tile != null)
                " Tile is $tile"
            else
                ""
        }"
    )

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