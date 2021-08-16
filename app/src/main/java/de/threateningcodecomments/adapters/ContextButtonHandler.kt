package de.threateningcodecomments.adapters

import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import de.threateningcodecomments.routinetimer.R

class ContextButtonHandler(val fragment: Fragment) : View.OnClickListener {
    private val names = ArrayList<String>()
    private val clickables = ArrayList<View>()
    private val menuBearers = ArrayList<View>()
    private val contextOptions = ArrayList<Array<String>>()
    private val icons = ArrayList<View?>()
    private val textViews = ArrayList<TextView?>()
    private val contextPopupOverrides = ArrayList<View?>()

    fun addButton(buttonName: String, clickable: View, icon: View? = null, text: TextView? = null,
                  contextPopupOverride: View? = null, contextOptions: Array<String>) {

        names.add(buttonName)

        clickable.setOnClickListener(this)
        clickables.add(clickable)

        val menuBearer =
                contextPopupOverride ?: clickable
        menuBearers.add(menuBearer)

        this.contextOptions.add(contextOptions)

        textViews.add(text)

        icons.add(icon)

        contextPopupOverrides.add(contextPopupOverride)
    }

    private lateinit var menu: PopupMenu

    override fun onClick(v: View?) {
        v ?: return

        val index = clickables.indexOf(v)

        val options = contextOptions[index]
        val menuView = menuBearers[index]

        menu = PopupMenu(fragment.context, menuView)

        for (option in options) {
            menu.menu.add(option)
        }

        for (option in menu.menu)
            option.icon = ResourcesCompat.getDrawable(fragment.resources,
                    R.drawable.ic_defaultdrawable, fragment.requireContext().theme)

        menu.setOnMenuItemClickListener { item ->
            val indexInMenu = menu.indexOf(item)

            item ?: return@setOnMenuItemClickListener false

            clickListener(names[index], item.title.toString(), indexInMenu)

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
