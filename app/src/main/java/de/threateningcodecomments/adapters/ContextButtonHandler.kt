package de.threateningcodecomments.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.forEachIndexed
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import de.threateningcodecomments.accessibility.JPopupMenu

class ContextButtonHandler(val fragment: Fragment) : View.OnClickListener {
    private val names = ArrayList<String>()
    private val clickables = ArrayList<View>()
    private val menuBearers = ArrayList<View>()
    private val contextOptions = ArrayList<Array<String>>()
    private val contextOptionIcons = ArrayList<Array<Drawable>>()
    private val icons = ArrayList<View?>()
    private val textViews = ArrayList<TextView?>()
    private val contextPopupOverrides = ArrayList<View?>()

    fun addButton(buttonName: String, clickable: View, icon: View? = null, text: TextView? = null,
                  contextPopupOverride: View? = null, contextOptions: Array<String>, contextOptionIcons:
                  Array<Drawable> = arrayOf(ColorDrawable(Color.TRANSPARENT))) {

        names.add(buttonName)

        clickable.setOnClickListener(this)
        clickables.add(clickable)

        val menuBearer =
                contextPopupOverride ?: clickable
        menuBearers.add(menuBearer)

        this.contextOptions.add(contextOptions)
        this.contextOptionIcons.add(contextOptionIcons)

        textViews.add(text)

        icons.add(icon)

        contextPopupOverrides.add(contextPopupOverride)
    }

    private lateinit var menu: PopupMenu

    override fun onClick(v: View?) {
        v ?: return

        val index = clickables.indexOf(v)

        val options = contextOptions[index]
        val optionIcons = contextOptionIcons[index]
        val menuView = menuBearers[index]
        val gravity =
                when (index) {
                    0 ->
                        Gravity.START
                    clickables.lastIndex ->
                        Gravity.END
                    else ->
                        Gravity.CENTER
                }

        menu = JPopupMenu(fragment.requireContext(), menuView, gravity)

        for (option in options) {
            menu.menu.add(option)
        }

        for ((optionIndex, option) in menu.menu.iterator().withIndex())
            option.icon =
                    if (optionIndex < optionIcons.size)
                        optionIcons[optionIndex]
                    else
                        null

        menu.setOnMenuItemClickListener { item ->
            val indexInMenu = menu.indexOf(item)

            item ?: return@setOnMenuItemClickListener false

            //the options have spacing for the icons, this needs to be removed
            val option = item.title.toString().substring(JPopupMenu.spacingText.length)

            clickListener(names[index], option, indexInMenu)

            true
        }

        menu.show()
    }

    private fun PopupMenu.indexOf(item: MenuItem?): Int {
        var index = -1

        item ?: return index

        menu.forEachIndexed { otherIndex, otherItem ->
            if (otherItem == item) {
                index = otherIndex
                return index
            }
        }

        return index
    }

    private var clickListener: (name: String, option: String, optionIndex: Int) -> Unit = { name, option, optionIndex -> }

    fun doOnClick(block: (name: String, option: String, optionIndex: Int) -> Unit) {
        clickListener = block
    }
}
