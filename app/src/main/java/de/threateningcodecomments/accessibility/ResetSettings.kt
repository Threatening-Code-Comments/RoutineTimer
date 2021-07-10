package de.threateningcodecomments.accessibility

import android.content.res.Resources
import de.threateningcodecomments.routinetimer.R
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.util.*

class ResetSettings(var resets: Boolean = false, var amount: Int? = 1, var resetUnit: Int? = Unit.DEFAULT,
                    var weekdays: MutableList<Int>? = mutableListOf(Weekday.DEFAULT),
                    var startDate: String? = null, var startTime: String? = null,
                    var resetsPerDayOfMonth: Boolean? = null) {

    @JvmName("_getLocalizedDate")
    fun getLocalizedDate(locale: Locale): String {
        val yourDate = Date.from(getStartDateAsCalendar().toInstant())
        val df: DateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale)
        return df.format(yourDate)
    }

    @JvmName("_getLocalizedTime")
    fun getLocalizedTime(locale: Locale): String {
        val date = getStartDateAsDate()
        val df = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
        return df.format(date)
    }

    @JvmName("_getStartDateAsDate")
    fun getStartDateAsDate(): Date {
        val arr = getStartDate_yyMMdd_arr()
        val timeArr =
                if (startTime != null)
                    startTime!!.split(":")
                else
                    null
        return makeDate(arr, timeArr)
    }

    private fun makeDate(dateArr: List<Int>, timeArr: List<String>?): Date {
        val instant =
                getStartDateAsCalendar().toInstant()
        return Date.from(instant)
    }


    @JvmName("_getResetsPerWeekString")
    fun getResetsPerWeekOfMonthString(res: Resources): String {
        val calendar = getStartDateAsCalendar()

        val month = calendar.get(Calendar.MONTH)
        val monthStr = DateFormatSymbols().months[month]

        val unitIsMonth = resetUnit == ResetSettings.Unit.MONTH

        val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)
        val weekAbbreviation = RC.Local.getEndingForNumber(weekOfMonth)

        return if (unitIsMonth)
            res.getString(R.string.str_TileSettings_resetPerDayOfMonth_false,
                    weekOfMonth, weekAbbreviation)
        else
            res.getString(R.string.str_TileSettings_resetPerDayOfMonth_year_false,
                    weekOfMonth, weekAbbreviation, monthStr)
    }

    @JvmName("_getResetsPerDayString")
    fun getResetsPerDayOfMonthString(res: Resources): String {
        val calendar = getStartDateAsCalendar()

        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dayAbbreviation = RC.Local.getEndingForNumber(dayOfMonth)

        val month = calendar.get(Calendar.MONTH)
        val monthStr = DateFormatSymbols().months[month]

        val unitIsMonth = (resetUnit == ResetSettings.Unit.MONTH)

        return if (unitIsMonth)
            res.getString(R.string.str_TileSettings_resetPerDayOfMonth_true,
                    dayOfMonth,
                    dayAbbreviation)
        else
            res.getString(R.string.str_TileSettings_resetPerDayOfMonth_year_true,
                    dayOfMonth, dayAbbreviation, monthStr)
    }

    @JvmName("_getStartDateAsCalendar")
    fun getStartDateAsCalendar(): Calendar =
            Calendar.getInstance().apply {
                val dateArr = getStartDate_yyMMdd_arr()

                if (startTime == null)
                    set(dateArr[2], dateArr[1], dateArr[0])
                else {
                    val timeArr = startTime!!.split(":")
                    set(dateArr[2], dateArr[1], dateArr[0], timeArr[0].toInt(), timeArr[1].toInt(), 0)
                }
            }

    private var startDateArrBuffer: List<Int>? = null

    @JvmName("_getStartDate")
    fun getStartDate_yyMMdd_arr(): List<Int> {
        startDate ?: throw IllegalStateException("Date is not Initialized!")

        if (startDateArrBuffer == null) {
            val split = startDate!!.split(".")
            startDateArrBuffer = listOf(
                    split[0].toInt(), split[1].toInt(), split[2].toInt()
            )
        }

        return startDateArrBuffer!!
    }

    object Unit {
        const val ROUTINE = 0
        const val MINUTE = 1
        const val HOUR = 2
        const val DAY = 3
        const val WEEK = 4
        const val MONTH = 5
        const val YEAR = 6
        val DEFAULT = null

        val STRINGS_SINGULAR =
                hashMapOf<Int, String>(
                        Pair(ROUTINE, "Routine"),
                        Pair(MINUTE, "Minute"),
                        Pair(HOUR, "Hour"),
                        Pair(DAY, "Day"),
                        Pair(WEEK, "Week"),
                        Pair(MONTH, "Month"),
                        Pair(YEAR, "Year")
                )

        val STRINGS_PLURAL =
                hashMapOf<Int, String>(
                        Pair(ROUTINE, "Routines"),
                        Pair(MINUTE, "Minutes"),
                        Pair(HOUR, "Hours"),
                        Pair(DAY, "Days"),
                        Pair(WEEK, "Weeks"),
                        Pair(MONTH, "Months"),
                        Pair(YEAR, "Years")
                )
    }

    object Weekday {
        const val MONDAY: Int = Calendar.MONDAY
        const val TUESDAY: Int = Calendar.TUESDAY
        const val WEDNESDAY: Int = Calendar.WEDNESDAY
        const val THURSDAY: Int = Calendar.THURSDAY
        const val FRIDAY: Int = Calendar.FRIDAY
        const val SATURDAY: Int = Calendar.SATURDAY
        const val SUNDAY: Int = Calendar.SUNDAY
        const val DEFAULT: Int = 0
    }

}