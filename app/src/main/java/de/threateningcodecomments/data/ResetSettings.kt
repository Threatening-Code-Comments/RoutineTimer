package de.threateningcodecomments.data

import android.content.res.Resources
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.routinetimer.R
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.util.*

class ResetSettings(
    var resets: Boolean = false,
    var amount: Int? = 1,
    var resetUnit: Int? = Unit.DEFAULT,
    var weekdays: MutableList<Int>? = mutableListOf(),
    var startDate: Date = Calendar.getInstance().time,
    var resetsPerDayOfMonth: Boolean? = null
) {

    @JvmName("_getLocalizedDate")
    fun getLocalizedDate(locale: Locale): String {
        val yourDate = Date.from(getStartDateAsCalendar().toInstant())
        val df: DateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale)
        return df.format(yourDate)
    }

    @JvmName("_getLocalizedTime")
    fun getLocalizedTime(locale: Locale): String {
        val df = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
        return df.format(startDate)
    }


    @JvmName("_getResetsPerWeekString")
    fun getResetsPerWeekOfMonthString(res: Resources): String {
        val calendar = getStartDateAsCalendar()

        val month = calendar.get(Calendar.MONTH)
        val monthStr = DateFormatSymbols.getInstance().months[month]

        val unitIsMonth = resetUnit == Unit.MONTH

        val dayOfWeekInMonth = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH)
        val numberEnding = RC.Local.getEndingForNumber(dayOfWeekInMonth)
        val dayOfWeekInt = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfWeek = DateFormatSymbols.getInstance().weekdays[dayOfWeekInt]

        return if (unitIsMonth)
            res.getString(
                R.string.str_TileSettings_resetPerDayOfMonth_false,
                dayOfWeekInMonth, numberEnding, dayOfWeek
            )
        else
            res.getString(
                R.string.str_TileSettings_resetPerDayOfMonth_year_false,
                dayOfWeekInMonth, numberEnding, dayOfWeek, monthStr
            )
    }

    @JvmName("_getResetsPerDayString")
    fun getResetsPerDayOfMonthString(res: Resources): String {
        val calendar = getStartDateAsCalendar()

        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dayAbbreviation = RC.Local.getEndingForNumber(dayOfMonth)

        val month = calendar.get(Calendar.MONTH)
        val monthStr = DateFormatSymbols().months[month]

        val unitIsMonth = (resetUnit == Unit.MONTH)

        return if (unitIsMonth)
            res.getString(
                R.string.str_TileSettings_resetPerDayOfMonth_true,
                dayOfMonth,
                dayAbbreviation
            )
        else
            res.getString(
                R.string.str_TileSettings_resetPerDayOfMonth_year_true,
                dayOfMonth, dayAbbreviation, monthStr
            )
    }

    @JvmName("_getStartDateAsCalendar")
    fun getStartDateAsCalendar(): Calendar =
        Calendar.getInstance().apply {
            if (startDate == null)
                startDate = time

            time = startDate!!
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