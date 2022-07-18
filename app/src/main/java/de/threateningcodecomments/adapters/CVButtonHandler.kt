package de.threateningcodecomments.adapters

import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.postDelayed
import com.google.android.material.card.MaterialCardView
import de.threateningcodecomments.accessibility.MyLog
import de.threateningcodecomments.accessibility.RC

/**
 * Handler Class to remove duplicate code.
 * Configuring purely with constructor is recommended. [Toggle][toggle], [select] and [deselect] for example are
 * fine
 * though.
 */
class CVButtonHandler(
    val card: MaterialCardView,
    val icon: ImageView? = null,
    val textView: TextView? = null,
    var selectColor: Int = RC.Resources.Colors.acceptColor,
    var deselectColor: Int = RC.Resources.Colors.onSurfaceColor,
    private val viewsAreConnected: Boolean = true,
    var mode: Int = MODE_TOGGLE,
    var startSelected: Boolean = false,
    var doOnClick: () -> Unit = {}
) {

    fun doOnClick(run: () -> Unit) {
        doOnClick = run
    }

    fun select(execOnClickRun: Boolean = true) {
        if (execOnClickRun)
            doOnClick

        setSelected(true)
    }

    fun deselect(execOnClickRun: Boolean = true) {
        if (execOnClickRun)
            doOnClick

        setSelected(false)
    }

    private fun setSelected(selected: Boolean) {
        val color =
            if (selected) selectColor
            else deselectColor

        card.setCardBackgroundColor(color)

        val c = RC.Conversions.Colors.calculateContrast(color)
        icon?.setColorFilter(c)
        textView?.setTextColor(c)
    }

    fun toggle(executeOnClickRun: Boolean = true) {
        if (executeOnClickRun)
            doOnClick()

        toggleColor()

        setVisibility()
    }

    init {
        initSelected()

        card.setOnClickListener {
            doOnClick()

            toggleColor()

            setVisibility()

            if (mode == MODE_TAP) {
                RC.handler.postDelayed(200L) {
                    toggleColor()
                    setVisibility()
                }
            }
        }
    }

    private fun initSelected() {
        isClicked = startSelected

        val colorToSet =
            if (isClicked)
                selectColor
            else
                deselectColor

        card.setCardBackgroundColor(colorToSet)

        setVisibility()
    }

    private var isClicked = false

    private fun toggleColor() {
        val colorToSet =
            if (isClicked)
                deselectColor
            else
                selectColor

        isClicked = !isClicked

        card.setCardBackgroundColor(colorToSet)
    }

    private fun setVisibility() {
        if (!viewsAreConnected)
            return

        val colorOfButton =
            if (isClicked)
                selectColor
            else
                deselectColor

        val contrast = RC.Conversions.Colors.calculateContrast(colorOfButton)

        icon?.setColorFilter(contrast)
        textView?.setTextColor(contrast)
    }

    companion object {
        private const val DEFAULT_SELECT_COLOR = 99
        private const val DEFAULT_DESELECT_COLOR = -0

        const val MODE_TAP = 0
        const val MODE_TOGGLE = 1
    }
}