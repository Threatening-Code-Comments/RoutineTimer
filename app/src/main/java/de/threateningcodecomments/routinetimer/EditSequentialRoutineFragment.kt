package de.threateningcodecomments.routinetimer

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import de.threateningcodecomments.accessibility.*
import de.threateningcodecomments.accessibility.ResourceClass.Anim.scaleDown
import de.threateningcodecomments.accessibility.ResourceClass.Anim.scaleUpSlow
import de.threateningcodecomments.accessibility.ResourceClass.Anim.slideDownIn
import de.threateningcodecomments.accessibility.ResourceClass.Anim.slideDownOut
import de.threateningcodecomments.accessibility.ResourceClass.Anim.slideUpIn
import de.threateningcodecomments.accessibility.ResourceClass.Anim.slideUpOut
import de.threateningcodecomments.adapters.ItemMoveCallbackListener
import de.threateningcodecomments.adapters.MyViewHolder
import de.threateningcodecomments.adapters.OnStartDragListener
import de.threateningcodecomments.adapters.OrganizeRoutineAdapter
import kotlinx.android.synthetic.main.fragment_edit_sequential_routine.*


class EditSequentialRoutineFragment : Fragment(), View.OnClickListener, OnStartDragListener, UIContainer {
    private val isNightMode: Boolean
        get() = MainActivity.isNightMode

    private lateinit var root: ConstraintLayout
    private lateinit var closeIcon: ShapeableImageView

    private lateinit var routineLayout: LinearLayout
    private lateinit var routineNameEditText: EditText
    private lateinit var routineOrganizeBtn: MaterialButton

    private lateinit var tileCardView: MaterialCardView
    private lateinit var tileIconView: ShapeableImageView
    private lateinit var tileNameView: MaterialTextView

    private lateinit var settingsPreviewCard: MaterialCardView
    private lateinit var settingsPreviewModeIcon: ImageView
    private lateinit var settingsPreviewModeSummary: MaterialTextView
    private lateinit var settingsPreviewCdTimeInfo: MaterialTextView
    private lateinit var settingsPreviewCdTimeValue: MaterialTextView

    private lateinit var tileCycleLayout: LinearLayout
    private lateinit var tileCycleDeleteBtn: MaterialCardView
    private lateinit var tileCycleDeleteBtnIcon: ImageView
    private lateinit var tileCyclePrevBtn: MaterialCardView
    private lateinit var tileCyclePrevBtnIcon: ImageView
    private lateinit var tileCycleNextBtn: MaterialCardView
    private lateinit var tileCycleNextBtnIcon: ImageView

    private lateinit var tileSettingsButton: MaterialButton

    private lateinit var organizeRoutineRoot: LinearLayout
    private lateinit var organizeRoutineRV: RecyclerView
    private lateinit var organizeRoutineBackBtn: MaterialButton

    private lateinit var currentRoutine: Routine
    private var position: Int = 0

    private val args: EditSequentialRoutineFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition

        super.onViewCreated(view, savedInstanceState)

        fragment = this
        MainActivity.currentFragment = this

    }

    override fun onStart() {
        super.onStart()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            currentRoutine.lastUsed = System.currentTimeMillis()
            ResourceClass.updateRoutineInDb(currentRoutine)

            val directions = EditSequentialRoutineFragmentDirections.actionEditSequentialRoutineFragmentToSelectRoutineFragment()

            findNavController().navigate(directions)
        }

        initBufferViews()

        initListeners()

        initRoutines()

        updateRoutineNameView()

        initUI()

        ResourceClass.updateNightMode(requireActivity())
        initOrganizeRV()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iv_EditRoutine_sequential_close ->
                navigateBack()
            R.id.cv_EditRoutine_sequential_cycle_delete ->
                handleDeleteButton()
            R.id.cv_EditRoutine_sequential_cycle_next ->
                cycleToNextTile()
            R.id.cv_EditRoutine_sequential_cycle_prev ->
                cycleToPrevTile()
            R.id.btn_EditRoutine_sequential_routines_organize ->
                goIntoOrganizeMode()
            R.id.btn_EditRoutine_sequential_organize_back ->
                leaveOrganizeMode()
            R.id.btn_EditRoutine_sequential_tileSettings -> {
                navigateToTileSettings()
            }
            else ->
                Toast.makeText(context, "Wrong onClickListener, wtf did you do?", Toast.LENGTH_LONG).show()
        }
    }

    private fun goIntoOrganizeMode() {
        routineLayout.startAnimation(slideUpOut)
        routineLayout.visibility = View.GONE
        tileCycleLayout.startAnimation(slideDownOut)
        tileCycleLayout.visibility = View.GONE
        tileCardView.startAnimation(scaleDown)
        tileCardView.visibility = View.GONE

        organizeRoutineRoot.startAnimation(scaleUpSlow)
        organizeRoutineRoot.visibility = View.VISIBLE
    }

    private fun leaveOrganizeMode() {
        routineLayout.startAnimation(slideDownIn)
        routineLayout.visibility = View.VISIBLE
        tileCycleLayout.startAnimation(slideUpIn)
        tileCycleLayout.visibility = View.VISIBLE
        tileCardView.startAnimation(scaleUpSlow)
        tileCardView.visibility = View.VISIBLE

        organizeRoutineRoot.startAnimation(scaleDown)
        organizeRoutineRoot.visibility = View.GONE
    }

    private var adapter: OrganizeRoutineAdapter? = null
    private lateinit var touchHelper: ItemTouchHelper
    override fun onStartDrag(viewHolder: MyViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    private fun initOrganizeRV() {
        adapter = OrganizeRoutineAdapter(this, currentRoutine)
        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(adapter!!)

        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(organizeRoutineRV)

        organizeRoutineRV.layoutManager = LinearLayoutManager(requireContext())
        organizeRoutineRV.adapter = adapter
    }

    //region init
    private fun initListeners() {
        closeIcon.setOnClickListener(this)
        closeIcon.setColorFilter(if (isNightMode) Color.WHITE else Color.BLACK)

        routineNameEditText.addTextChangedListener(afterTextChanged = { text: Editable? ->
            if (text.toString().isNotEmpty()) {
                currentRoutine.name = text.toString()
                updateUI()
            }
        })
        routineNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                routineNameEditText.clearFocus()
                (SelectRoutineFragment.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(requireView().windowToken, 0)
            }
            false
        }
        routineNameEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                tileCycleLayout.startAnimation(slideDownOut)
                tileCycleLayout.visibility = View.INVISIBLE
                tileCardView.startAnimation(slideDownOut)
                tileCardView.visibility = View.INVISIBLE
            } else {
                tileCycleLayout.startAnimation(slideUpIn)
                tileCycleLayout.visibility = View.VISIBLE
                tileCardView.startAnimation(slideUpIn)
                tileCardView.visibility = View.VISIBLE
            }
        }
        routineOrganizeBtn.setOnClickListener(this)

        tileCycleDeleteBtn.setOnClickListener(this)
        tileCyclePrevBtn.setOnClickListener(this)
        tileCycleNextBtn.setOnClickListener(this)

        val settingsExpandListener = View.OnClickListener {
            val isExpanded = settingsPreviewModeSummary.isVisible

            settingsPreviewModeSummary.isVisible = !isExpanded

            if (currentRoutine.tiles[position].mode == Tile.MODE_COUNT_UP) {
                settingsPreviewCdTimeInfo.isVisible = false
                settingsPreviewCdTimeValue.isVisible = false
            } else {
                settingsPreviewCdTimeInfo.isVisible = !isExpanded
                settingsPreviewCdTimeValue.isVisible = true
            }

            updateUI()
        }
        settingsPreviewCard.setOnClickListener(settingsExpandListener)

        tileSettingsButton.setOnClickListener(this)

        organizeRoutineBackBtn.setOnClickListener(this)
    }

    private fun initBufferViews() {
        val v = requireView()

        root = v.findViewById(R.id.cl_EditRoutine_sequential_root)
        closeIcon = v.findViewById(R.id.iv_EditRoutine_sequential_close)

        routineLayout = v.findViewById(R.id.ll_EditRoutine_sequential_routine_layout)
        routineNameEditText = v.findViewById(R.id.et_EditRoutine_sequential_routine_name)
        routineOrganizeBtn = v.findViewById(R.id.btn_EditRoutine_sequential_routines_organize)

        tileCardView = v.findViewById(R.id.cv_EditRoutine_sequential_tile_card)
        tileIconView = v.findViewById(R.id.iv_EditRoutine_sequential_tile_icon)
        tileNameView = v.findViewById(R.id.tv_EditRoutine_sequential_tile_name)

        settingsPreviewCard = cv_EditRoutine_sequential_tile_settingsDisplay_card
        settingsPreviewModeIcon = iv_EditRoutine_sequential_tile_settingsDisplay_modeIcon
        settingsPreviewModeSummary = tv_EditRoutine_sequential_tile_settingsDisplay_modeSummary
        settingsPreviewCdTimeInfo = tv_EditRoutine_sequential_tile_settingsDisplay_cdTimeInfo
        settingsPreviewCdTimeValue = tv_EditRoutine_sequential_tile_settingsDisplay_cdTimeValue

        tileCycleLayout = v.findViewById(R.id.ll_EditRoutine_sequential_cycle_layout)
        tileCycleDeleteBtn = cv_EditRoutine_sequential_cycle_delete
        tileCycleDeleteBtnIcon = iv_EditRoutine_sequential_cycle_delete_icon
        tileCyclePrevBtn = cv_EditRoutine_sequential_cycle_prev
        tileCyclePrevBtnIcon = iv_EditRoutine_sequential_cycle_prev_icon
        tileCycleNextBtn = cv_EditRoutine_sequential_cycle_next
        tileCycleNextBtnIcon = iv_EditRoutine_sequential_cycle_next_icon

        tileSettingsButton = btn_EditRoutine_sequential_tileSettings

        organizeRoutineRoot = v.findViewById(R.id.ll_EditRoutine_sequential_organize_root)
        organizeRoutineRV = v.findViewById(R.id.rv_EditRoutine_sequential_organize_recyclerview)
        organizeRoutineBackBtn = v.findViewById(R.id.btn_EditRoutine_sequential_organize_back)
    }

    private fun initRoutines() {
        val position: Int = ResourceClass.routines.indexOf(ResourceClass.getRoutineFromUid(args.routineUid))
        currentRoutine = ResourceClass.routines[position]
        currentRoutine.setAccessibility(isNightMode)
        currentRoutine.lastUsed = System.currentTimeMillis()
    }
    //endregion

    //region update ui
    private fun initUI() {
        currentRoutine.setAccessibility(isNightMode)
        routineNameEditText.clearFocus()
        updateCard()
    }

    override fun updateUI() {
        //region visibility
        val onSurface = ResourceClass.Resources.Colors.contrastColor

        tileCyclePrevBtnIcon.setColorFilter(onSurface)
        tileCycleNextBtnIcon.setColorFilter(onSurface)

        val deleteColor = ResourceClass.Resources.Colors.cancelColor
        val deleteContrastColor = ResourceClass.Conversions.Colors.calculateContrast(deleteColor)

        tileCycleDeleteBtn.setCardBackgroundColor(deleteColor)
        tileCycleDeleteBtnIcon.setColorFilter(deleteContrastColor)

        settingsPreviewModeSummary.setTextColor(onSurface)
        settingsPreviewModeIcon.setColorFilter(onSurface)
        settingsPreviewCdTimeValue.setTextColor(onSurface)
        settingsPreviewCdTimeInfo.setTextColor(onSurface)
        //endregion

        //make the cycleNextButtonIcon turn into an add icon when a new tile will be created
        val drawableId = if (currentRoutine.tiles.size - 1 < position + 1) {
            R.drawable.ic_add
        } else {
            R.drawable.ic_arrow_right
        }
        val cycleDrawable = ResourceClass.Resources.getDrawable(drawableId)
        tileCycleNextBtnIcon.setImageDrawable(cycleDrawable)

        //disable prevButton if this is the first tile
        val isNotAtFirstPosition = position - 1 >= 0

        val disableOrEnableColor = if (isNotAtFirstPosition)
            ResourceClass.Resources.Colors.contrastColor
        else
            ResourceClass.Resources.Colors.disabledColor

        tileCyclePrevBtnIcon.setColorFilter(disableOrEnableColor)

        tileCyclePrevBtn.isClickable = isNotAtFirstPosition

        //set cdTimeInfo visibility to true if the currentTile mode is COUNT_DOWN
        val currentTile = currentRoutine.tiles[position]

        settingsPreviewModeSummary.text = currentTile.getModeAsString()

        val modeDrawable = ResourceClass.Resources.Drawables.getModeDrawable(currentTile)
        settingsPreviewModeIcon.setImageDrawable(modeDrawable)

        settingsPreviewModeSummary.text = currentTile.getModeAsString()

        settingsPreviewCdTimeValue.isVisible = (currentTile.mode != Tile.MODE_COUNT_UP)
        if (currentTile.mode == Tile.MODE_COUNT_UP)
            settingsPreviewCdTimeInfo.isVisible = false

        val cdTimeValue = currentTile.countDownSettings.countDownTime
        settingsPreviewCdTimeValue.text = ResourceClass.millisToHHMMSSorMMSS(cdTimeValue)

        updateRoutine()
        updateCard()
    }

    private fun updateCard() {
        val tile: Tile = currentRoutine.tiles[position]

        val tileName = tile.name
        tileNameView.text = tileName
        tileNameView.setTextColor(tile.contrastColor)

        val icon = ResourceClass.getIconDrawable(tile)
        tileIconView.setImageDrawable(icon)
        tileIconView.setColorFilter(tile.contrastColor)

        val color = tile.backgroundColor
        tileCardView.setCardBackgroundColor(color)
    }

    //endregion

    //region handle routines
    private fun updateRoutineNameView() {
        MyLog.d(currentRoutine.name + " is the name of routine")
        routineNameEditText.setText(currentRoutine.name)
        MyLog.d("after name change")
    }

    private fun updateRoutine() {
        ResourceClass.saveRoutine(currentRoutine)
        if (currentRoutine.name != routineNameEditText.text.toString())
            routineNameEditText.setText(currentRoutine.name)
    }
    //endregion

    //region cycling tiles
    private fun handleDeleteButton() {
        if (currentRoutine.tiles.size > 1) {
            currentRoutine.tiles.removeAt(position)
            if (position - 1 < 0) {
                position++
            } else if (position + 1 > currentRoutine.tiles.size - 1) {
                position--
            }
            updateUI()
        } else {
            Toast.makeText(requireContext(), "Routine must have at least one tile!", Toast.LENGTH_SHORT).show()
        }
        adapter?.notifyDataSetChanged()
    }

    private fun cycleToPrevTile() {
        if (position - 1 >= 0) {
            position--
            updateUI()
        }
        adapter?.notifyDataSetChanged()
    }

    private fun cycleToNextTile() {
        //adds a tile if there is no next tile
        if (currentRoutine.tiles.size - 1 < position + 1) {
            currentRoutine.tiles.add(Tile.DEFAULT_TILE)
            Toast.makeText(requireContext(), "Added a new Tile!", Toast.LENGTH_SHORT).show()
        }
        position++
        updateUI()
        adapter?.notifyDataSetChanged()
    }

    //endregion

    //region navigation
    private fun navigateBack() {
        routineNameEditText.clearFocus()
        tileNameView.clearFocus()

        val directions = EditSequentialRoutineFragmentDirections.actionEditSequentialRoutineFragmentToSelectRoutineFragment()
        val extras = FragmentNavigatorExtras(root as View to currentRoutine.uid, routineNameEditText to currentRoutine.name!!, tileIconView to currentRoutine.uid + "icon")

        findNavController().navigate(directions, extras)
    }

    private fun navigateToTileSettings() {
        val routineUid = currentRoutine.uid
        val tileUid = currentRoutine.tiles[position].uid

        val directions = EditSequentialRoutineFragmentDirections
                .actionEditSequentialRoutineFragmentToTileSettingsFragment(routineUid,
                        tileUid)

        findNavController().navigate(directions)
    }

    //endregion

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_sequential_routine, container, false)
    }

    override fun updateCurrentTile() {}

    companion object {
        const val ICON_DIALOG_TAG = "icon-dialog"

        lateinit var fragment: EditSequentialRoutineFragment
    }

}