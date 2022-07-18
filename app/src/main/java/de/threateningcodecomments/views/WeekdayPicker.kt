package de.threateningcodecomments.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.widget.LinearLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.iterator
import com.google.android.material.imageview.ShapeableImageView
import de.threateningcodecomments.accessibility.CombinedDrawable
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.accessibility.TextDrawable
import de.threateningcodecomments.routinetimer.R
import java.text.SimpleDateFormat
import java.util.*

class WeekdayPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var locale: Locale = Locale.getDefault()
        set(newLocale) {
            /*val currentSelections = selectedDays
            val currentDisabledDays = disabledDays*/

            field = newLocale
            firstDayOfWeek = Calendar.getInstance(locale).firstDayOfWeek

            /*localizeLabels()
            setDaysIgnoringListenersAndSelectionMode(daysToSelect = currentSelections)

            enableAllDays()
            disableDays(currentDisabledDays)*/
        }

    var firstDayOfWeek = Calendar.MONDAY
        set(day) {
            orderedDays = getOrderedDaysOfWeek(day)
            field = day
            updateUI()
        }

    val allDays = listOf(
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY,
        Calendar.SATURDAY, Calendar.SUNDAY
    )

    var orderedDays = getOrderedDaysOfWeek(firstDayOfWeek)

    var selectedDays = mutableListOf<Int>()
        set(value) {
            field = value
            updateUI()
        }

    private val toggles = ArrayList<ShapeableImageView>()

    private var circleDrawable: Drawable =
        RC.Resources.getDrawable(
            R.drawable.outline_circle
        )

    init {
        inflateLayoutUsing(context)
        initListeners()

        //updateUI gets called due to the implications of changing the locale
        locale = Locale.getDefault()
    }

    private fun inflateLayoutUsing(context: Context) {
        //LayoutInflater.from(context).inflate(R.layout.layout_weekday_picker, this, true)
        this.orientation = LinearLayout.HORIZONTAL
        for (i in 0..6) {
            val dayOfWeek = orderedDays[i]

            this.addView(
                ShapeableImageView(context).apply {
                    //set layout
                    val ivHeight = RC.Conversions.Size.dpToPx(40)
                    layoutParams =
                        LinearLayout.LayoutParams(
                            0,
                            ivHeight.toInt(),
                            1f
                        ).apply {
                            val margin =
                                RC.Conversions.Size.dpToPx(10).toInt()

                            setMargins(0, margin, 0, margin)
                        }

                    //set text
                    setImageDrawable(
                        TextDrawable(Color.WHITE, RC.Conversions.Dates.getAbbreviation(dayOfWeek, locale))
                    )

                    //add to list
                    toggles.add(this)
                }
            )
        }
    }

    private fun toggleSelected(view: ShapeableImageView) {
        val index = this.indexOfChild(view)
        val dayOfWeek = orderedDays[index]

        //logic
        if (dayOfWeek in selectedDays)
            selectedDays.remove(dayOfWeek)
        else
            selectedDays.add(dayOfWeek)

        //visual
        val d1 =
            if (view.colorFilter == null)
                circleDrawable
            else
                null

        setImageDrawable(dayOfWeek, d1, view)

        if (view.colorFilter == null)
            view.setColorFilter(RC.Resources.Colors.primaryColor)
        else
            view.colorFilter = null
    }

    fun resizeDrawable(drawable: Drawable, width: Int, height: Int): Drawable {
        val bitmap = drawable.toBitmap(width, height) //here width and height are in px
        return bitmap.toDrawable(resources)
    }

    private fun setImageDrawable(dayOfWeek: Int, d1: Drawable?, view: ShapeableImageView) {
        val contrastColor = RC.Resources.Colors.extremeContrastColor
        val textDrawable = TextDrawable(contrastColor, RC.Conversions.Dates.getAbbreviation(dayOfWeek, locale))

        val combinedDrawable = CombinedDrawable(d1, textDrawable)

        view.setImageDrawable(combinedDrawable)
    }

    private fun initListeners() {
        val listener = OnClickListener {
            toggleSelected(it as ShapeableImageView)

            listener?.invoke()

        }

        for (view in this)
            view.setOnClickListener(listener)

    }

    fun updateUI() {
        for ((index, day) in orderedDays.withIndex()) {
            val toggle = toggles[index]
            val isSelected = day in selectedDays

            //drawable
            val d1 =
                if (isSelected)
                    circleDrawable
                else
                    null

            val text = TextDrawable(
                RC.Resources.Colors.extremeContrastColor, RC.Conversions.Dates.getAbbreviation
                    (day, locale)
            )
            val combiDrawable = CombinedDrawable(d1, text)
            toggle.setImageDrawable(combiDrawable)

            //color filter
            if (isSelected)
                toggle.setColorFilter(RC.Resources.Colors.primaryColor)
            else
                toggle.clearColorFilter()
        }
    }

    private var listener: (() -> Unit)? = null

    /**
     * Triggered if a day is manually toggled with a click. Doesn't register programmatic changes.
     */
    fun doOnDayChange(listener: () -> Unit) {
        this.listener = listener
    }

    private fun getOrderedDaysOfWeek(firstDayOfWeek: Int): List<Int> {
        val daysOfTheWeekStartingOnSunday = allDays
        val indexOfFirstDay = daysOfTheWeekStartingOnSunday.indexOf(firstDayOfWeek)
        val daysToMoveToEndOfWeek = daysOfTheWeekStartingOnSunday.take(indexOfFirstDay)
        return daysOfTheWeekStartingOnSunday.drop(indexOfFirstDay) + daysToMoveToEndOfWeek
    }

    companion object {
        const val DEFAULT_DAY = 11
    }
}