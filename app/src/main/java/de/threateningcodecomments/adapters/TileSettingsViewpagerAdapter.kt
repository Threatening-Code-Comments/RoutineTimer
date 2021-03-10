package de.threateningcodecomments.adapters

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import com.maltaisn.icondialog.data.Category
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.data.IconTag
import de.threateningcodecomments.accessibility.MyLog
import de.threateningcodecomments.accessibility.ResourceClass
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.routinetimer.MainActivity
import de.threateningcodecomments.routinetimer.R
import de.threateningcodecomments.routinetimer.TileSettingsFragment
import kotlinx.android.synthetic.main.layout_tile_settings_common.*
import kotlinx.android.synthetic.main.layout_tile_settings_timing.*


class TileSettingsViewpagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    var currentTile: Tile = Tile.ERROR_TILE

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment =
                when (position) {
                    0 -> {
                        commonElement = CommonElement(currentTile)
                        commonElement
                    }
                    1 -> {
                        appearanceElement = AppearanceElement(currentTile)
                        appearanceElement
                    }
                    2 -> {
                        timingElement = TimingElement(currentTile)
                        timingElement
                    }
                    else -> AppearanceElement(currentTile)
                }
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putInt(ARG_OBJECT, position + 1)
        }

        return fragment
    }

    fun updateUI() {
        try {
            commonElement.updateUI()
        } catch (e: Exception) {
        }
        try {
            appearanceElement.updateUI()
        } catch (e: Exception) {
        }
        try {
            timingElement.updateUI()
        } catch (e: Exception) {
        }
    }


    class CommonElement : Fragment, Element, View.OnClickListener {
        private var currentTile: Tile

        constructor() {
            currentTile = TileSettingsFragment.instance.currentTile
        }

        constructor(currentTile: Tile) : super() {
            this.currentTile = currentTile
        }

        override val name = "Most Common"

        //region vars
        private lateinit var nameLayout: LinearLayout
        private lateinit var nameInfo: MaterialTextView
        private lateinit var nameSummary: MaterialTextView
        private lateinit var nameIcon: ImageView
        private lateinit var nameEditLayout: LinearLayout
        private lateinit var nameEditField: EditText

        private lateinit var iconLayout: LinearLayout
        private lateinit var iconInfo: MaterialTextView
        private lateinit var iconIcon: ImageView
        private lateinit var iconEditLayout: LinearLayout
        private lateinit var iconRecyclerView: RecyclerView
        private lateinit var iconSearchEditText: EditText
        private lateinit var iconCategoryTabLayout: TabLayout

        private lateinit var modeLayout: LinearLayout
        private lateinit var modeInfo: MaterialTextView
        private lateinit var modeSummary: MaterialTextView
        private lateinit var modeIcon: ImageView
        private lateinit var modeEditLayout: LinearLayout
        private lateinit var modeEditCutButton: MaterialCardView
        private lateinit var modeEditCdtButton: MaterialCardView

        private lateinit var colorLayout: LinearLayout
        private lateinit var colorInfo: MaterialTextView
        private lateinit var colorSummary: View
        private lateinit var colorIcon: ImageView
        private lateinit var colorEditLayout: LinearLayout
        private lateinit var colorHueInfo: MaterialTextView
        private lateinit var colorHueSlider: Slider
        //endregion

        override fun onStart() {
            super.onStart()

            initBuffers()
            initListeners()

            updateUI()
        }

        override fun onClick(v: View?) {
            v ?: return

            //if one of the main layouts is clicked, expand the editLayout
            if (v.id == nameLayout.id || v.id == iconLayout.id || v.id == modeLayout.id || v.id == colorLayout.id) {
                val editView =
                        when (v.id) {
                            R.id.ll_TileSettings_common_name_main ->
                                nameEditLayout

                            R.id.ll_TileSettings_common_icon_main ->
                                iconEditLayout

                            R.id.ll_TileSettings_common_mode_main ->
                                modeEditLayout

                            R.id.ll_TileSettings_common_color_main ->
                                colorEditLayout
                            else -> {
                                Toast.makeText(MainActivity.instance, "Something went wrong.", Toast.LENGTH_SHORT).show()
                                nameEditLayout
                            }

                        }

                val viewIsVisible = editView.isVisible
                editView.isVisible = !viewIsVisible
            }

            when (v.id) {
                R.id.cv_TileSettings_common_mode_cdt -> {
                    currentTile.mode = Tile.MODE_COUNT_DOWN
                }
                R.id.cv_TileSettings_common_mode_cut -> {
                    currentTile.mode = Tile.MODE_COUNT_UP
                }
            }

            updateUI()
        }

        override fun updateUI() {
            //set content
            nameSummary.text = currentTile.name
            if (currentTile.name != nameEditField.text.toString())
                nameEditField.setText(currentTile.name)

            val dr = ResourceClass.getIconDrawable(currentTile)
            iconIcon.setImageDrawable(dr)

            modeSummary.text = currentTile.getModeAsString()
            val modeButtonToSelect =
                    if (currentTile.mode == Tile.MODE_COUNT_DOWN)
                        modeEditCdtButton
                    else
                        modeEditCutButton
            val modeButtonToDeselect =
                    if (modeButtonToSelect == modeEditCutButton)
                        modeEditCdtButton
                    else
                        modeEditCutButton
            val selectColor = ResourceClass.Resources.Colors.primaryColor
            val deselectColor = ResourceClass.Resources.Colors.onSurfaceColor
            modeButtonToSelect.setCardBackgroundColor(selectColor)
            modeButtonToDeselect.setCardBackgroundColor(deselectColor)

            colorSummary.background = currentTile.backgroundColor.toDrawable()
            colorHueSlider.value = ResourceClass.Conversions.Colors.getHueOfColor(currentTile.backgroundColor)

            //handle visibility
            val contrastCol = ResourceClass.Resources.Colors.contrastColor

            nameInfo.setTextColor(contrastCol)
            nameIcon.setColorFilter(contrastCol)
            nameSummary.setTextColor(contrastCol)

            iconInfo.setTextColor(contrastCol)
            iconIcon.setColorFilter(contrastCol)

            modeInfo.setTextColor(contrastCol)
            modeSummary.setTextColor(contrastCol)
            modeIcon.setColorFilter(contrastCol)

            //handle visibility on buttons
            val selectedModeButton = modeButtonToSelect.getChildAt(0)
            var contrastColor = ResourceClass.Conversions.Colors.calculateContrast(selectColor)
            (selectedModeButton as MaterialTextView).setTextColor(contrastColor)

            val modeButtonText = modeButtonToDeselect.getChildAt(0)
            contrastColor = ResourceClass.Conversions.Colors.calculateContrast(deselectColor)
            (modeButtonText as MaterialTextView).setTextColor(contrastColor)

            colorInfo.setTextColor(contrastCol)
            colorIcon.setColorFilter(contrastCol)

            TileSettingsFragment.instance.updateUI()
        }

        override fun initListeners() {
            //listeners for ui
            nameLayout.setOnClickListener(this)
            nameIcon.setOnClickListener(this)

            iconLayout.setOnClickListener(this)

            modeLayout.setOnClickListener(this)

            colorLayout.setOnClickListener(this)

            //listeners for logic
            nameEditField.addTextChangedListener(
                    afterTextChanged = { text ->
                        //preventing a loop, where every time the ui is updated, the text gets updated...
                        //you might see where this lead
                        if (currentTile.name != text.toString()) {

                            //makes sure that a tile name always has at least one character
                            if (text.toString().isNotEmpty()) {
                                currentTile.name = text.toString()

                                updateUI()
                            }
                        }
                    }
            )

            // set a GridLayoutManager with default vertical orientation and 3 number of columns
            val gridLayoutManager = GridLayoutManager(MainActivity.instance.applicationContext, 5)
            iconRecyclerView.layoutManager = gridLayoutManager // set LayoutManager to RecyclerView
            var iconList = ArrayList<Icon>(ResourceClass.getIconPack().icons.values)
            iconRecyclerView.adapter = IconRVAdapter(MainActivity.instance, iconList)

            for (category in MainActivity.instance.iconDialogIconPack.categories.values) {
                iconCategoryTabLayout.addTab(iconCategoryTabLayout.newTab().setText(category.name))
            }

            iconCategoryTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val text = tab?.text ?: "All"

                    var category: Any = "All"

                    for (forCategory in MainActivity.instance.iconDialogIconPack.categories.values) {
                        if (text == forCategory.name) {
                            category = forCategory
                            break
                        }
                    }

                    if (category is Category) {
                        val foundIcons = ArrayList<Icon>()
                        for (icon in iconList)
                            if (icon.categoryId == category.id)
                                foundIcons.add(icon)

                        iconRecyclerView.adapter = IconRVAdapter(MainActivity.instance, foundIcons)
                    } else {
                        iconRecyclerView.adapter = IconRVAdapter(MainActivity.instance, iconList)
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    // Handle tab reselect
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    // Handle tab unselect
                }
            })

            iconSearchEditText.doAfterTextChanged {
                //i'm pretty sure this is incredibly suboptimal, but it works, so too bad!

                val text = it.toString().replace("_", " ")
                val tags = MainActivity.instance.iconDialogIconPack.tags
                val foundTagKeys = ArrayList<String>()
                val foundTags = ArrayList<IconTag>()
                val foundIcons = ArrayList<Icon>()

                for (tagKey in tags.keys) {
                    val iconTag = tags[tagKey]

                    if (iconTag!!.name.contains(text))
                        foundTagKeys.add(tagKey)
                }

                iconCategoryTabLayout.selectTab(iconCategoryTabLayout.getTabAt(0))
                for (icon in iconList)
                    for (tag in icon.tags) {
                        if (foundTagKeys.contains(tag)) {
                            foundIcons.add(icon)
                            break
                        }
                    }

                iconRecyclerView.adapter = IconRVAdapter(MainActivity.instance, foundIcons)
            }


            modeEditCutButton.setOnClickListener(this)
            modeEditCdtButton.setOnClickListener(this)

            colorHueSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    val color = ResourceClass.Conversions.Colors.calculateColorFromHue(value)
                    currentTile.backgroundColor = color

                    updateUI()
                }
            }
        }

        override fun initBuffers() {
            nameLayout = ll_TileSettings_common_name_main
            nameInfo = tv_TileSettings_common_name_info
            nameSummary = tv_TileSettings_common_name_summary
            nameIcon = iv_TileSettings_common_name_icon
            nameEditLayout = ll_TileSettings_common_name_editLayout
            nameEditField = et_TileSettings_common_name_editField

            iconLayout = ll_TileSettings_common_icon_main
            iconInfo = tv_TileSettings_common_icon_info
            iconIcon = iv_TileSettings_common_icon_icon
            iconRecyclerView = rv_TileSettings_common_icon_recyclerview
            iconEditLayout = ll_Tilesettings_common_icon_editLayout
            iconSearchEditText = et_TileSettings_common_icon_search
            iconCategoryTabLayout = tl_TileSettings_common_icon_tabLayout

            modeLayout = ll_TileSettings_common_mode_main
            modeInfo = tv_TileSettings_common_mode_info
            modeSummary = tv_TileSettings_common_mode_summary
            modeIcon = iv_TileSettings_common_mode_icon
            modeEditLayout = ll_TileSettings_common_mode_editLayout
            modeEditCdtButton = cv_TileSettings_common_mode_cdt
            modeEditCutButton = cv_TileSettings_common_mode_cut

            colorLayout = ll_TileSettings_common_color_main
            colorInfo = tv_TileSettings_common_color_info
            colorSummary = v_TileSettings_common_color_summary
            colorIcon = iv_TileSettings_common_color_icon
            colorEditLayout = ll_TileSettings_common_color_editLayout
            colorHueInfo = tv_TileSettings_common_color_editLayout_hueInfo
            colorHueSlider = sl_TileSettings_common_color_editLayout_hueSlider
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.layout_tile_settings_common, container, false)
        }
    }

    class AppearanceElement(val currentTile: Tile) : Fragment(), Element {
        constructor() : this(TileSettingsFragment.instance.currentTile)

        override val name: String = "Appearance"

        override fun onStart() {
            super.onStart()
        }

        override fun updateUI() {
        }

        override fun initBuffers() {
            TODO("Not yet implemented")
        }

        override fun initListeners() {
            TODO("Not yet implemented")
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.layout_tile_settings_appearance, container, false)
        }
    }

    class TimingElement(val currentTile: Tile) : Fragment(), Element {
        override val name: String = "Timing"

        constructor() : this(TileSettingsFragment.instance.currentTile)

        //region vars
        private lateinit var modeLayout: LinearLayout
        private lateinit var modeInfo: MaterialTextView
        private lateinit var modeSummary: MaterialTextView
        private lateinit var modeIcon: ImageView
        private lateinit var modeEditLayout: LinearLayout
        private lateinit var modeEditCutButton: MaterialCardView
        private lateinit var modeEditCdtButton: MaterialCardView

        private lateinit var countdownSettingsLayout: LinearLayout

        private lateinit var cdTimeLayout: LinearLayout
        private lateinit var cdTimeInfo: MaterialTextView
        private lateinit var cdTimeSummary: MaterialTextView
        private lateinit var cdTimeIcon: ImageView
        private lateinit var cdTimeEditLayout: LinearLayout
        private lateinit var cdTimeInputEdit: EditText
        private lateinit var cdTimeInputDisplay: MaterialTextView

        private lateinit var remindsLayout: LinearLayout
        private lateinit var remindsInfo: MaterialTextView
        private lateinit var remindsSummary: MaterialTextView
        private lateinit var remindsIcon: ImageView
        private lateinit var remindsEditLayout: LinearLayout
        private lateinit var remindsOnButton: MaterialCardView
        private lateinit var remindsOffButton: MaterialCardView
        //endregion

        override fun onStart() {
            super.onStart()

            initBuffers()
            initListeners()

            updateUI()
        }

        override fun updateUI() {
            //region content

            //visibility
            //makes the layout visible only if the tile mode is cd, else collapses all editLayouts beneath
            if (currentTile.mode == Tile.MODE_COUNT_DOWN)
                countdownSettingsLayout.isVisible = true
            else {
                cdTimeEditLayout.isVisible = false
                remindsEditLayout.isVisible = false
                countdownSettingsLayout.isVisible = false
            }

            //mode
            modeSummary.text = currentTile.getModeAsString()

            val selectColor = ResourceClass.Resources.Colors.primaryColor
            val deselectColor = ResourceClass.Resources.Colors.onSurfaceColor

            val modeButtonToSelect =
                    if (currentTile.mode == Tile.MODE_COUNT_DOWN)
                        modeEditCdtButton
                    else
                        modeEditCutButton
            val modeButtonToDeselect =
                    if (modeButtonToSelect == modeEditCutButton)
                        modeEditCdtButton
                    else
                        modeEditCutButton

            modeButtonToSelect.setCardBackgroundColor(selectColor)
            modeButtonToDeselect.setCardBackgroundColor(deselectColor)

            //countdown time
            val cdTimeText = currentTile.countDownSettings.countDownTimeString
            cdTimeSummary.text = currentTile.countDownSettings.countDownTime.toString()//cdTimeText TODO change here

            if (cdTimeInputDisplay.text.toString() != cdTimeText)
                cdTimeInputDisplay.text = cdTimeText

            //reminds
            val text =
                    if (currentTile.countDownSettings.reminds)
                        "On"
                    else
                        "Off"
            remindsSummary.text = text

            val remindsButtonToSelect =
                    if (currentTile.countDownSettings.reminds)
                        remindsOnButton
                    else
                        remindsOffButton
            val remindsButtonToDeselect =
                    if (remindsButtonToSelect == remindsOffButton)
                        remindsOnButton
                    else
                        remindsOffButton

            remindsButtonToSelect.setCardBackgroundColor(selectColor)
            remindsButtonToDeselect.setCardBackgroundColor(deselectColor)

            //endregion

            //region visibility
            val contrastColor = ResourceClass.Resources.Colors.contrastColor

            //mode
            modeIcon.setColorFilter(contrastColor)
            modeInfo.setTextColor(contrastColor)
            modeSummary.setTextColor(contrastColor)

            var selectButtonTextView = modeButtonToSelect.getChildAt(0) as MaterialTextView
            var deselectButtonTextView = modeButtonToDeselect.getChildAt(0) as MaterialTextView
            selectButtonTextView.setTextColor(ResourceClass.Conversions.Colors.calculateContrast(selectColor))
            deselectButtonTextView.setTextColor(ResourceClass.Conversions.Colors.calculateContrast(deselectColor))

            //cd time
            cdTimeIcon.setColorFilter(contrastColor)
            cdTimeInfo.setTextColor(contrastColor)
            cdTimeSummary.setTextColor(contrastColor)


            //reminds
            remindsIcon.setColorFilter(contrastColor)
            remindsInfo.setTextColor(contrastColor)
            remindsSummary.setTextColor(contrastColor)

            selectButtonTextView = remindsButtonToSelect.getChildAt(0) as MaterialTextView
            deselectButtonTextView = remindsButtonToDeselect.getChildAt(0) as MaterialTextView
            selectButtonTextView.setTextColor(ResourceClass.Conversions.Colors.calculateContrast(selectColor))
            deselectButtonTextView.setTextColor(ResourceClass.Conversions.Colors.calculateContrast(deselectColor))
            //endregion
        }

        override fun initListeners() {
            //main layouts
            val mainClickListener = object : View.OnClickListener {
                override fun onClick(v: View?) {
                    v ?: return

                    val viewToToggle =
                            when (v.id) {
                                R.id.ll_TileSettings_timing_mode_main ->
                                    modeEditLayout
                                R.id.ll_TileSettings_timing_cdTime_main ->
                                    cdTimeEditLayout
                                R.id.ll_TileSettings_timing_reminds_main ->
                                    remindsEditLayout
                                else ->
                                    modeEditLayout
                            }

                    val wasVisible = viewToToggle.isVisible

                    viewToToggle.isVisible = !wasVisible
                }
            }

            modeLayout.setOnClickListener(mainClickListener)
            cdTimeLayout.setOnClickListener(mainClickListener)
            remindsLayout.setOnClickListener(mainClickListener)


            //mode
            val buttonSelectingListener = object : View.OnClickListener {
                override fun onClick(v: View?) {
                    v ?: return

                    when (v.id) {
                        R.id.cv_TileSettings_timing_mode_cdt ->
                            currentTile.mode = Tile.MODE_COUNT_DOWN
                        R.id.cv_TileSettings_timing_mode_cut ->
                            currentTile.mode = Tile.MODE_COUNT_UP
                        R.id.cv_TileSettings_timing_reminds_on ->
                            currentTile.countDownSettings.reminds = true
                        R.id.cv_TileSettings_timing_reminds_off ->
                            currentTile.countDownSettings.reminds = false
                        else ->
                            Toast.makeText(MainActivity.instance, "Problem in buttonSelectingListener, please see priest", Toast.LENGTH_SHORT).show()
                    }

                    updateUI()
                }
            }

            modeEditCutButton.setOnClickListener(buttonSelectingListener)
            modeEditCdtButton.setOnClickListener(buttonSelectingListener)
            remindsOnButton.setOnClickListener(buttonSelectingListener)
            remindsOffButton.setOnClickListener(buttonSelectingListener)

            cdTimeInputEdit.addTextChangedListener(object : TextWatcher {
                lateinit var textBefore: String

                val NO_TEXT_CHANGED_CHAR = ResourceClass.Resources.Text.NO_TEXT_CHANGED_CHAR

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    textBefore = s.toString()
                }

                override fun afterTextChanged(s: Editable) {
                    var changedChar = NO_TEXT_CHANGED_CHAR
                    val textBeforeChars = textBefore.toCharArray()
                    val sCharArray = s.toString().toCharArray()
                    val sCharArrayList = ArrayList<Char>(sCharArray.toMutableList())

                    //if a char was removed from the text, the char was a backspace
                    if (sCharArrayList.size == textBeforeChars.size - 1) {
                        changedChar = ResourceClass.Conversions.Time.TIME_CHANGE_BACKSPACE_CHAR
                        //if not, there is a char to extract
                    } else {
                        for ((index, char) in sCharArrayList.withIndex()) {
                            if (index == textBefore.toCharArray().size) {
                                changedChar = char
                                break
                            }

                            if (textBeforeChars[index] != char) {
                                changedChar = char
                                break
                            }
                        }
                    }

                    //if no char was changed, don't do anything
                    if (changedChar != NO_TEXT_CHANGED_CHAR) {
                        //buffer the cdTime of tile in format hh:mm:ss and convert it to hhmmss
                        val timeStr = currentTile.countDownSettings.countDownTimeString

                        val newText = ResourceClass.Conversions.Time.addDigitToTimeString(timeStr, changedChar)

                        currentTile.countDownSettings.countDownTimeString = newText

                        cdTimeInputDisplay.text = newText

                        //add a NO_TEXT_CHANGED_CHAR to the input to make unlimited backspaces possible
                        val noTextChangedString = NO_TEXT_CHANGED_CHAR.toString()

                        MyLog.d("noTextChangedString is \"$noTextChangedString\"")

                        cdTimeInputEdit.inputType = InputType.TYPE_CLASS_TEXT
                        cdTimeInputEdit.append(noTextChangedString)
                        cdTimeInputEdit.inputType = InputType.TYPE_CLASS_NUMBER

                        updateUI()
                    }
                }
            })

        }

        override fun initBuffers() {
            modeLayout = ll_TileSettings_timing_mode_main
            modeInfo = tv_TileSettings_timing_mode_info
            modeSummary = tv_TileSettings_timing_mode_summary
            modeIcon = iv_TileSettings_timing_mode_icon
            modeEditLayout = ll_TileSettings_timing_mode_editLayout
            modeEditCutButton = cv_TileSettings_timing_mode_cut
            modeEditCdtButton = cv_TileSettings_timing_mode_cdt

            countdownSettingsLayout = ll_TileSettings_timing_countdownSettings

            cdTimeLayout = ll_TileSettings_timing_cdTime_main
            cdTimeInfo = tv_TileSettings_timing_cdTime_info
            cdTimeSummary = tv_TileSettings_timing_cdTime_summary
            cdTimeIcon = iv_TileSettings_timing_cdTime_icon
            cdTimeEditLayout = ll__TileSettings_timing_cdTime_editLayout
            cdTimeInputEdit = et_TileSettings_timing_cdTime_timeInput
            cdTimeInputDisplay = tv_TileSettings_timing_cdTime_inputDisplay

            remindsLayout = ll_TileSettings_timing_reminds_main
            remindsInfo = tv_TileSettings_timing_reminds_info
            remindsSummary = tv_TileSettings_timing_reminds_summary
            remindsIcon = iv_TileSettings_timing_reminds_icon
            remindsEditLayout = ll_TileSettings_timing_reminds_editLayout
            remindsOnButton = cv_TileSettings_timing_reminds_on
            remindsOffButton = cv_TileSettings_timing_reminds_off
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.layout_tile_settings_timing, container, false)
        }
    }

    interface Element {
        val name: String

        fun updateUI()
        fun initBuffers()
        fun initListeners()
    }

    companion object {
        var commonElement = CommonElement(Tile.ERROR_TILE)
        var appearanceElement = AppearanceElement(Tile.ERROR_TILE)
        var timingElement = TimingElement(Tile.ERROR_TILE)
    }
}

private const val ARG_OBJECT = "object"