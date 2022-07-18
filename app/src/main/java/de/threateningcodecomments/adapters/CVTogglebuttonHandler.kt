package de.threateningcodecomments.adapters

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import de.threateningcodecomments.accessibility.RC

class CVTogglebuttonHandler(val b1: CardView, val b2: CardView, selectColor: Int? = null) : View.OnClickListener {
    private val selectedColor =
            selectColor ?: RC.Resources.Colors.primaryColor
    private val onSurface = RC.Resources.Colors.onSurfaceColor

    init {
        b1.setOnClickListener(this)
        b2.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val buttonToSelect =
                if (v == b1)
                    b1
                else
                    b2

        selectButton(buttonToSelect)
    }

    private fun selectButton(buttonToSelect: CardView) {
        val buttonToDeselect =
                if (buttonToSelect == b1)
                    b2
                else
                    b1

        buttonToSelect.setColor(selectedColor)
        buttonToDeselect.setColor(onSurface)

        //execute onClick lambda
        val buttonIndex =
                if (buttonToSelect == b1)
                    0
                else
                    1
        onClick(values[buttonIndex], buttonIndex)
    }

    private val values = mutableListOf<Any>()

    fun setButtonValues(b1: Any, b2: Any) {
        if (values.size < 1)
            values.add(0)
        values[0] = b1

        if (values.size < 2)
            values.add(0)
        values[1] = b2
    }

    private var onClick: (value: Any, buttonIndex: Int) -> Unit = { _, _ -> }

    fun doOnClick(
            f: (value: Any, buttonIndex: Int) -> Unit
    ) {
        onClick = f
    }

    private fun CardView.setColor(color: Int) {
        setCardBackgroundColor(color)

        val contrast = RC.Conversions.Colors.calculateContrast(color)
        (getChildAt(0) as TextView).setTextColor(contrast)
    }

    fun initButtons(value: Any) {
        val buttonToSelect =
                when (values.indexOf(value)) {
                    0 -> b1
                    1 -> b2
                    else -> throw IllegalArgumentException("No valid argument passed for initial button!")
                }

        selectButton(buttonToSelect)
    }
}