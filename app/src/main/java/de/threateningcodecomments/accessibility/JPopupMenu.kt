package de.threateningcodecomments.accessibility

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.iterator
import de.threateningcodecomments.routinetimer.R


class JPopupMenu(context: Context, anchor: View, gravity: Int = Gravity.START) : PopupMenu(context, anchor, gravity) {

    private val iconSize = context.resources.getDimensionPixelSize(R.dimen.popupMenu_iconSize)
    private val contrastColor = RC.Resources.Colors.contrastColor

    override fun show() {
        setUpIcons()

        super.show()
    }

    private fun setUpIcons() {
        if(!hasIcon())
            return

        for(option in menu){
            insertIcon(option)
        }
    }

    private fun insertIcon(option: MenuItem) {
        var icon = option.icon

        if(icon == null) icon = ColorDrawable(Color.TRANSPARENT)

        icon.setBounds(0, 0, iconSize, iconSize)
        icon.setTint(contrastColor)
        val imageSpan = ImageSpan(icon)

        val ssb = SpannableStringBuilder(spacingText + option.title)

        ssb.setSpan(imageSpan, 0, 1, 0)
        option.title = ssb

        option.icon = null
    }

    private fun hasIcon(): Boolean {
        for (option in menu) {
            if (option.icon != null)
                return true
        }

        return false
    }

    companion object{
        const val spacingText = "    "
    }
}