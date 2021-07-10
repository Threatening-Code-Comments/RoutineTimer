package de.threateningcodecomments.adapters

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.maltaisn.icondialog.data.Category
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.data.IconTag
import de.threateningcodecomments.accessibility.*
import de.threateningcodecomments.routinetimer.MainActivity
import de.threateningcodecomments.routinetimer.R
import de.threateningcodecomments.routinetimer.TileSettingsFragment
import de.threateningcodecomments.views.TileSettingsMain
import de.threateningcodecomments.views.WeekdayPicker
import kotlinx.android.synthetic.main.cvl_tile_settings_main.*
import kotlinx.android.synthetic.main.layout_tile_settings_common.*
import kotlinx.android.synthetic.main.layout_tile_settings_timing.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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
                        modeElement = ModeElement(currentTile)
                        modeElement
                    }
                    else -> AppearanceElement(currentTile)
                }
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putInt(ARG_OBJECT, position + 1)
        }

        return fragment
    }

    fun updateUI(excludeElement: Element? = null) {
        try {
            if (excludeElement != commonElement as Element)
                commonElement.updateUI()
        } catch (e: Exception) {
        }
        try {
            if (excludeElement != appearanceElement as Element)
                appearanceElement.updateUI()
        } catch (e: Exception) {
        }
        try {
            if (excludeElement != appearanceElement as Element)
                modeElement.updateUI()
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
        private lateinit var modeDropDown: AutoCompleteTextView

        private lateinit var colorLayout: LinearLayout
        private lateinit var colorInfo: MaterialTextView
        private lateinit var colorSummary: View
        private lateinit var colorIcon: ImageView
        private lateinit var colorEditLayout: LinearLayout
        private lateinit var colorHueInfo: MaterialTextView
        private lateinit var colorHueSlider: Slider
        //endregion

        override fun onResume() {
            super.onResume()
            updateUI()
        }

        override fun onStart() {
            super.onStart()

            initBuffers()
            initListeners()

            updateUI()
        }

        override fun onClick(v: View?) {
            v ?: return

            //if one of the main layouts is clicked, expand the editLayout
            if (v == nameLayout || v == iconLayout || v == modeLayout || v == colorLayout) {
                val editView =
                        when (v.id) {
                            R.id.ll_TileSettings_common_name_main ->
                                nameEditLayout

                            R.id.ll_TileSettings_common_icon_main ->
                                iconEditLayout

                            R.id.ll_TileSettings_common_mode_main -> {
                                modeEditLayout
                            }

                            R.id.ll_TileSettings_common_color_main ->
                                colorEditLayout
                            else -> {
                                MyLog.t("Something went wrong.")
                                nameEditLayout
                            }

                        }

                val viewIsVisible = editView.isVisible
                editView.isVisible = !viewIsVisible
            }

            updateUI()
        }

        override fun updateUI() {
            //set content
            nameSummary.text = currentTile.name
            if (currentTile.name != nameEditField.text.toString())
                nameEditField.setText(currentTile.name)

            val dr = RC.getIconDrawable(currentTile)
            iconIcon.setImageDrawable(dr)

            modeSummary.text = currentTile.getModeAsString()
            if (modeDropDown.text.toString() != currentTile.getModeAsString())
                modeDropDown.setText(currentTile.getModeAsString(), false)

            val selectColor = RC.Resources.Colors.primaryColor
            val deselectColor = RC.Resources.Colors.onSurfaceColor

            colorSummary.background = currentTile.backgroundColor.toDrawable()
            colorHueSlider.value = RC.Conversions.Colors.getHueOfColor(currentTile.backgroundColor)

            //handle visibility
            val contrastCol = RC.Resources.Colors.contrastColor

            nameInfo.setTextColor(contrastCol)
            nameIcon.setColorFilter(contrastCol)
            nameSummary.setTextColor(contrastCol)

            iconInfo.setTextColor(contrastCol)
            iconIcon.setColorFilter(contrastCol)

            modeInfo.setTextColor(contrastCol)
            modeSummary.setTextColor(contrastCol)
            modeIcon.setColorFilter(contrastCol)

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

            val options = listOf(
                    Tile.COUNT_UP_MESSAGE, Tile.COUNT_DOWN_MESSAGE, Tile.TAP_MESSAGE, Tile.DATA_MESSAGE, Tile.ALARM_MESSAGE
            )
            val adapter: ArrayAdapter<String> = DropdownArrayAdapter(
                    requireContext(),
                    R.layout.routine_popup_item,
                    options)

            modeDropDown.setAdapter(adapter)
            modeDropDown.doOnTextChanged { text, _, _, _ ->
                if (text.toString() != currentTile.getModeAsString()) {
                    currentTile.setModeFromString(text.toString())
                    updateUI()
                }
            }

            // set a GridLayoutManager with default vertical orientation and 3 number of columns
            val gridLayoutManager = GridLayoutManager(context, 5)
            iconRecyclerView.layoutManager = gridLayoutManager // set LayoutManager to RecyclerView
            var iconList = ArrayList<Icon>(RC.getIconPack().icons.values)
            iconRecyclerView.adapter = IconRVAdapter(requireContext(), iconList)

            val categories = (requireActivity() as MainActivity).iconDialogIconPack.categories.values
            for (category in categories) {
                iconCategoryTabLayout.addTab(iconCategoryTabLayout.newTab().setText(category.name))
            }

            iconCategoryTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val text = tab?.text ?: "All"

                    var category: Any = "All"

                    for (forCategory in categories) {
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

                        iconRecyclerView.adapter = IconRVAdapter(requireContext(), foundIcons)
                    } else {
                        iconRecyclerView.adapter = IconRVAdapter(requireContext(), iconList)
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
                val tags = (requireActivity() as MainActivity).iconDialogIconPack.tags
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

                iconRecyclerView.adapter = IconRVAdapter(requireContext(), foundIcons)
            }

            colorHueSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    val color = RC.Conversions.Colors.calculateColorFromHue(value)
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
            modeDropDown = dd_TileSettings_common_mode_editDropDown

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

    class ModeElement(val currentTile: Tile) : Fragment(), Element {
        override val name: String = "Mode"

        constructor() : this(TileSettingsFragment.instance.currentTile)

        //region vars
        private lateinit var modeLayout: TileSettingsMain
        private lateinit var modeSummary: MaterialTextView
        private lateinit var modeEditLayout: LinearLayout
        private lateinit var modeDropDown: AutoCompleteTextView

        private lateinit var countdownSettingsLayout: LinearLayout

        private lateinit var cdTimeLayout: LinearLayout
        private lateinit var cdTimeInfo: MaterialTextView
        private lateinit var cdTimeSummary: MaterialTextView
        private lateinit var cdTimeIcon: ImageView
        private lateinit var cdTimeEditLayout: LinearLayout
        private lateinit var cdTimeInputEdit: EditText
        private lateinit var cdTimeInputDisplay: MaterialTextView

        private lateinit var resetLayout: TileSettingsMain
        private lateinit var resetEditLayout: LinearLayout
        private lateinit var resetEditInfo: TextView
        private lateinit var resetOnButton: MaterialCardView
        private lateinit var resetOnButtonText: MaterialTextView
        private lateinit var resetOffButton: MaterialCardView
        private lateinit var resetOffButtonText: MaterialTextView
        private lateinit var resetAdjustLayout: LinearLayout
        private lateinit var resetEditAmount: AutoCompleteTextView
        private lateinit var resetUnitDropDown: AutoCompleteTextView
        private lateinit var resetDatePicker: MaterialDatePicker<Long>
        private lateinit var resetWeekdayPicker: WeekdayPicker
        private lateinit var resetDateAndTimeButton: MaterialButton
        private lateinit var resetWeekOfMonthUnitDropdown: AutoCompleteTextView

        private lateinit var remindsLayout: LinearLayout
        private lateinit var remindsInfo: MaterialTextView
        private lateinit var remindsSummary: MaterialTextView
        private lateinit var remindsIcon: ImageView
        private lateinit var remindsEditLayout: LinearLayout
        private lateinit var remindsOnButton: MaterialCardView
        private lateinit var remindsOffButton: MaterialCardView
        //endregion

        override fun onResume() {
            super.onResume()
            updateUI()
        }

        override fun onStart() {
            super.onStart()

            initBuffers()
            initListeners()

            updateUI()
            initVisibility()
        }

        private fun initVisibility() {
            resetLayout.visibilityViewsToIgnore =
                    setOf(
                            resetOnButton, resetOffButton, resetWeekdayPicker
                    )

            modeLayout.visibilityTypesToIgnore =
                    setOf(
                            AutoCompleteTextView::javaClass.name
                    )
        }

        override fun updateUI() {
            //region content
            val selectColor = RC.Resources.Colors.primaryColor
            val deselectColor = RC.Resources.Colors.onSurfaceColor

            //makes the layout visible only if the tile mode is cd, else collapses all editLayouts beneath
            if (currentTile.mode == Tile.MODE_COUNT_DOWN)
                countdownSettingsLayout.isVisible = true
            else {
                cdTimeEditLayout.isVisible = false
                remindsEditLayout.isVisible = false
                countdownSettingsLayout.isVisible = false
            }

            //mode
            modeLayout.doOnUIUpdate {
                modeLayout.summary = currentTile.getModeAsString()
                if (modeDropDown.text.toString() != currentTile.getModeAsString())
                    modeDropDown.setText(currentTile.getModeAsString(), false)
            }
            modeLayout.updateUI()

            //resets
            val buttonToSelect =
                    if (currentTile.resetSettings.resets)
                        resetOnButton
                    else
                        resetOffButton
            val buttonToDeselect =
                    if (buttonToSelect == resetOnButton)
                        resetOffButton
                    else
                        resetOnButton

            buttonToSelect.setCardBackgroundColor(selectColor)
            buttonToDeselect.setCardBackgroundColor(deselectColor)

            (buttonToSelect.getChildAt(0) as MaterialTextView)
                    .setTextColor(RC.Conversions.Colors.calculateContrast(selectColor))
            (buttonToDeselect.getChildAt(0) as MaterialTextView)
                    .setTextColor(RC.Conversions.Colors.calculateContrast(deselectColor))

            resetAdjustLayout.isVisible = currentTile.resetSettings.resets
            resetLayout.icon =
                    RC.Resources.getDrawable(
                            if (currentTile.resetSettings.resets)
                                R.drawable.ic_reset_on
                            else
                                R.drawable.ic_reset_off)

            (resetUnitDropDown.parent.parent as TextInputLayout).error =
                    if (resetUnitDropDown.text.toString() == "")
                        "Unit must be selected!"
                    else
                        null
            val resetUnit = currentTile.resetSettings.resetUnit
            resetUnitDropDown.setText(
                    if (currentTile.resetSettings.resetUnit != ResetSettings.Unit.DEFAULT)
                        if (currentTile.resetSettings.amount == 1)
                            ResetSettings.Unit.STRINGS_SINGULAR[resetUnit]
                        else
                            ResetSettings.Unit.STRINGS_PLURAL[resetUnit]
                    else
                        null, false)
            val resetStringVal = currentTile.resetSettings.amount.toString()
            if (resetStringVal != resetEditAmount.text.toString())
                resetEditAmount.setText(resetStringVal)

            resetWeekdayPicker.isVisible = false
            if (resetUnit == ResetSettings.Unit.WEEK && currentTile.resetSettings.resets) {
                resetWeekdayPicker.isVisible = true
                if (currentTile.resetSettings.weekdays != null)
                    resetWeekdayPicker.selectedDays = currentTile.resetSettings.weekdays!!
            }

            val unitIsMonthOrYear =
                    currentTile.resetSettings.resetUnit == ResetSettings.Unit.MONTH ||
                            currentTile.resetSettings.resetUnit == ResetSettings.Unit.YEAR

            (resetWeekOfMonthUnitDropdown.parent as View).isVisible =
                    unitIsMonthOrYear

            resetDateAndTimeButton.isVisible = currentTile.resetSettings.resets

            if (currentTile.resetSettings.startDate != null)
                setAdapterForWeekOfMonthDropdown()

            setSummary()

            //countdown time
            val cdTimeText = currentTile.countDownSettings.countDownTimeString
            cdTimeSummary.text = RC.Conversions.Time.shortenTimeString(cdTimeText)

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

            (remindsButtonToSelect.getChildAt(0) as MaterialTextView)
                    .setTextColor(RC.Conversions.Colors.calculateContrast(selectColor))
            (remindsButtonToDeselect.getChildAt(0) as MaterialTextView)
                    .setTextColor(RC.Conversions.Colors.calculateContrast(deselectColor))
            //endregion
        }

        private fun setSummary() {
            val settings = currentTile.resetSettings

            if (!settings.resets || settings.startDate == null || settings.startTime == null) {
                resetLayout.summary = ""
                return
            }

            val localizedDate = settings.getLocalizedDate(Locale.getDefault())
            val localizedTime = settings.getLocalizedTime(Locale.getDefault())

            val together = "$localizedDate; $localizedTime"
            resetLayout.summary = together
        }

        override fun initListeners() {
            val buttonSelectingListener = object : View.OnClickListener {
                override fun onClick(v: View?) {
                    v ?: return

                    when (v) {
                        remindsOnButton ->
                            currentTile.countDownSettings.reminds = true
                        remindsOffButton ->
                            currentTile.countDownSettings.reminds = false
                        resetOnButton ->
                            currentTile.resetSettings.resets = true
                        resetOffButton ->
                            currentTile.resetSettings.resets = false
                        else ->
                            RC.Debugging.toast("Problem in buttonSelectingListener, please see priest", Toast.LENGTH_SHORT)
                    }

                    updateUI()
                }
            }

            //mode
            initModeLayoutListeners()

            //reset
            initResetListeners(buttonSelectingListener)


            //reminds
            remindsOnButton.setOnClickListener(buttonSelectingListener)
            remindsOffButton.setOnClickListener(buttonSelectingListener)

            //cdTime
            initCdTimeListener()

        }

        private fun initCdTimeListener() =
                cdTimeInputEdit.addTextChangedListener(object : TextWatcher {
                    lateinit var textBefore: String

                    val NO_TEXT_CHANGED_CHAR = RC.Resources.Text.NO_TEXT_CHANGED_CHAR

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
                            changedChar = RC.Conversions.Time.TIME_CHANGE_BACKSPACE_CHAR
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

                            val newText = RC.Conversions.Time.addDigitToTimeString(timeStr, changedChar)

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


        private fun initResetListeners(buttonSelectingListener: View.OnClickListener) {
            val resetUnitsSingular = ResetSettings.Unit.STRINGS_SINGULAR.values.toMutableList()
            val resetUnitsPlural = ResetSettings.Unit.STRINGS_PLURAL.values.toMutableList()

            val resetAdapterSingular = DropdownArrayAdapter<String>(
                    requireContext(),
                    R.layout.routine_popup_item,
                    resetUnitsSingular)
            val resetAdapterPlural = DropdownArrayAdapter<String>(
                    requireContext(),
                    R.layout.routine_popup_item,
                    resetUnitsPlural
            )

            resetUnitDropDown.setAdapter(resetAdapterSingular)
            resetUnitDropDown.doOnTextChanged { text_p, _, _, _ ->
                val text = text_p.toString()

                if (text == "")
                    return@doOnTextChanged

                val currentUnit = currentTile.resetSettings.resetUnit
                val currentTileResetUnitSingular = ResetSettings.Unit.STRINGS_SINGULAR[currentUnit]
                val currentTileResetUnitPlural = ResetSettings.Unit.STRINGS_PLURAL[currentUnit]

                val isSing = text == currentTileResetUnitSingular
                val isPlur = text == currentTileResetUnitPlural

                val isDifferent = (!isSing && !isPlur)

                if (isDifferent) {
                    val isOfSingular = text in ResetSettings.Unit.STRINGS_SINGULAR.values

                    //gets the unit for the message
                    val unitInt =
                            if (isOfSingular)
                                ResetSettings.Unit.STRINGS_SINGULAR.filterValues {
                                    it == text
                                }.keys.first()
                            else
                                ResetSettings.Unit.STRINGS_PLURAL.filterValues {
                                    it == text
                                }.keys.first()

                    currentTile.resetSettings.resetUnit = unitInt

                    updateUI()
                }
            }

            resetEditAmount.doOnTextChanged { text, _, _, _ ->
                if (text == null || text.toString() == "")
                    return@doOnTextChanged

                val intVal = try {
                    text.toString().toInt()
                } catch (e: Exception) {
                    resetEditAmount.setText(null, false)
                    return@doOnTextChanged
                }

                if (intVal != currentTile.resetSettings.amount) {
                    currentTile.resetSettings.amount = intVal

                    if (intVal != 1) {
                        if (resetUnitDropDown.adapter != resetAdapterPlural) {
                            resetUnitDropDown.setAdapter(resetAdapterPlural)
                        }
                    } else {
                        if (resetUnitDropDown.adapter != resetAdapterSingular) {
                            resetUnitDropDown.setAdapter(resetAdapterSingular)
                        }
                    }


                    updateUI()
                }

            }

            resetWeekdayPicker.doOnDayChange {
                currentTile.resetSettings.weekdays = resetWeekdayPicker.selectedDays
            }

            resetOnButton.setOnClickListener(buttonSelectingListener)
            resetOffButton.setOnClickListener(buttonSelectingListener)

            resetWeekOfMonthUnitDropdown.doAfterTextChanged {
                val text = it.toString()

                val settings = currentTile.resetSettings

                settings.resetsPerDayOfMonth = (text == settings.getResetsPerDayOfMonthString(resources))
            }

            val timePicker = MaterialTimePicker.Builder()
                    .setTitleText("Set time")
                    .build()
                    .apply {
                        addOnPositiveButtonClickListener {
                            val settings = currentTile.resetSettings

                            currentTile.resetSettings.startTime = "$hour:$minute"

                            setAdapterForWeekOfMonthDropdown()
                        }
                    }

            resetDatePicker.apply {
                addOnCancelListener { MyLog.d("Cancelled") }
                addOnPositiveButtonClickListener {
                    val date = Date(it)
                    val formatter = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
                    currentTile.resetSettings.startDate = formatter.format(date)

                    timePicker.show(parentFragmentManager, MaterialTimePicker::class.java.canonicalName)
                }
            }

            resetDateAndTimeButton.setOnClickListener {
                resetDatePicker.show(parentFragmentManager, MaterialDatePicker::class.java.canonicalName)
            }
        }

        private fun setAdapterForWeekOfMonthDropdown() {
            val settings = currentTile.resetSettings

            val resetsPerWeekString =
                    settings.getResetsPerWeekOfMonthString(resources)

            val resetsPerDayString =
                    settings.getResetsPerDayOfMonthString(resources)

            val dropdownOptions = listOf(resetsPerDayString, resetsPerWeekString)
            val adapter = DropdownArrayAdapter(
                    requireContext(),
                    R.layout.support_simple_spinner_dropdown_item,
                    dropdownOptions
            )
            resetWeekOfMonthUnitDropdown.setAdapter(adapter)

            val initText =
                    if (settings.resetsPerDayOfMonth != true)
                        resetsPerWeekString
                    else
                        resetsPerDayString

            resetWeekOfMonthUnitDropdown.setText(initText, false)

            val hasDate = settings.startDate != null
            val unitIsMonthOrYear =
                    (settings.resetUnit == ResetSettings.Unit.MONTH) || (settings.resetUnit == ResetSettings.Unit.YEAR)
            val isVisible = hasDate && unitIsMonthOrYear

            (resetWeekOfMonthUnitDropdown.parent.parent as View).isVisible = isVisible
            (resetWeekOfMonthUnitDropdown.parent as View).isVisible = isVisible
        }

        private fun initModeLayoutListeners() {
            val modeOptions = arrayListOf(
                    Tile.COUNT_UP_MESSAGE, Tile.COUNT_DOWN_MESSAGE, Tile.TAP_MESSAGE, Tile.DATA_MESSAGE, Tile.ALARM_MESSAGE
            )
            val modeAdapter: ArrayAdapter<String> = DropdownArrayAdapter(
                    requireContext(),
                    R.layout.routine_popup_item,
                    modeOptions)

            modeDropDown.setAdapter(modeAdapter)
            modeDropDown.doOnTextChanged { text, _, _, _ ->
                if (text.toString() != currentTile.getModeAsString()) {
                    currentTile.setModeFromString(text.toString())
                    updateUI()
                }
            }
        }

        override fun initBuffers() {
            modeLayout = ml_TileSettings_timing_mode_main
            modeEditLayout = ll_TileSettings_timing_mode_editLayout
            modeDropDown = dd_TileSettings_timing_mode_editDropDown

            countdownSettingsLayout = ll_TileSettings_timing_countdownSettings

            cdTimeLayout = ll_TileSettings_timing_cdTime_main
            cdTimeInfo = tv_TileSettings_timing_cdTime_info
            cdTimeSummary = tv_TileSettings_timing_cdTime_summary
            cdTimeIcon = iv_TileSettings_timing_cdTime_icon
            cdTimeEditLayout = ll__TileSettings_timing_cdTime_editLayout
            cdTimeInputEdit = et_TileSettings_timing_cdTime_timeInput
            cdTimeInputDisplay = tv_TileSettings_timing_cdTime_inputDisplay

            resetLayout = ml_TileSettings_timing_reset_main
            resetOnButton = cv_TileSettings_timing_resets_on
            resetOnButtonText = tv_TileSettings_timing_resets_on
            resetOffButton = cv_TileSettings_timing_resets_off
            resetOffButtonText = tv_TileSettings_timing_resets_off
            resetAdjustLayout = ll_TileSettings_timing_reset_setReset
            resetEditLayout = ll_TileSettings_timing_reset_editLayout
            resetEditAmount = atv_TileSettings_timing_reset_amount
            resetEditInfo = tv_TileSettings_timing_reset_editInfo
            resetUnitDropDown = dd_TileSettings_timing_reset_editDropDown
            resetDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.str_TileSettings_timing_reset_datePicker_title))
                    .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                    .build()
            resetWeekdayPicker = wdp_TileSettings_timing_reset_my_weekdayPicker
            resetDateAndTimeButton = btn_TileSettings_timing_datePicker_dateAndTime
            resetWeekOfMonthUnitDropdown = dd_TileSettings_timing_reset_weekOfMonth_unitDropdown

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
        var modeElement = ModeElement(Tile.ERROR_TILE)
    }
}

private const val ARG_OBJECT = "object"