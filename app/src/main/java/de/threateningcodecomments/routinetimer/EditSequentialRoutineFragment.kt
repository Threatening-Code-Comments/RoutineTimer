package de.threateningcodecomments.routinetimer

import accessibility.*
import accessibility.ResourceClass.isNightMode
import accessibility.ResourceClass.scaleDown
import accessibility.ResourceClass.scaleUpSlow
import accessibility.ResourceClass.slideDownIn
import accessibility.ResourceClass.slideDownOut
import accessibility.ResourceClass.slideUpIn
import accessibility.ResourceClass.slideUpOut
import adapters.ItemMoveCallbackListener
import adapters.MyViewHolder
import adapters.OnStartDragListener
import adapters.OrganizeRoutineAdapter
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.google.android.material.slider.Slider
import com.google.android.material.transition.platform.MaterialArcMotion
import com.google.android.material.transition.platform.MaterialContainerTransform


class EditSequentialRoutineFragment : Fragment(), View.OnClickListener, OnStartDragListener, UIContainer {
    private val isNightMode: Boolean
        get() = isNightMode(requireActivity().application)

    private lateinit var root: ConstraintLayout
    private lateinit var closeIcon: ShapeableImageView

    private lateinit var routineLayout: LinearLayout
    private lateinit var routineNameEditText: EditText
    private lateinit var routineOrganizeBtn: MaterialButton

    private lateinit var colorCard: MaterialCardView
    private lateinit var colorSlider: Slider

    private lateinit var tileCardView: MaterialCardView
    private lateinit var tileIconView: ShapeableImageView
    private lateinit var tileNameView: EditText

    private lateinit var tileCycleLayout: LinearLayout
    private lateinit var tileCycleDeleteBtn: MaterialButton
    private lateinit var tileCyclePrevBtn: MaterialButton
    private lateinit var tileCycleNextBtn: MaterialButton

    private lateinit var organizeRoutineRoot: LinearLayout
    private lateinit var organizeRoutineRV: RecyclerView
    private lateinit var organizeRoutineBackBtn: MaterialButton

    private var routines: ArrayList<Routine>? = null
    private lateinit var currentRoutine: Routine
    private var position: Int = 0

    private val args: EditSequentialRoutineFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition

        super.onViewCreated(view, savedInstanceState)

        fragment = this
        MainActivity.currentFragment = this

        initBufferViews()

        initListeners()

        initRoutines()

        updateRoutineNameView()

        initUI()

        ResourceClass.updateNightMode(requireActivity().application)
        initOrganizeRV()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iv_EditRoutine_sequential_close ->
                navigateBack()
            R.id.iv_EditRoutine_sequential_tile_icon ->
                MainActivity.activityBuffer.openIconDialog(currentRoutine.tiles[position])
            R.id.cv_EditRoutine_sequential_tile_card ->
                handleColorModeToggle()
            R.id.btn_EditRoutine_sequential_cycle_delete ->
                handleDeleteButton()
            R.id.btn_EditRoutine_sequential_cycle_next ->
                cycleToNextTile()
            R.id.btn_EditRoutine_sequential_cycle_prev ->
                cycleToPrevTile()
            R.id.btn_EditRoutine_sequential_routines_organize ->
                goIntoOrganizeMode()
            R.id.btn_EditRoutine_sequential_organize_back ->
                leaveOrganizeMode()
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

        colorSlider.addOnChangeListener(Slider.OnChangeListener { _, value, _ ->
            updateColor(value)
        })

        tileIconView.setOnClickListener(this)
        tileNameView.imeOptions = EditorInfo.IME_ACTION_DONE
        tileNameView.setRawInputType(InputType.TYPE_CLASS_TEXT)
        tileNameView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                routineNameEditText.clearFocus()
                (SelectRoutineFragment.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(requireView().windowToken, 0)
            }
            false
        }
        tileNameView.setOnFocusChangeListener { v, hasFocus ->

            //TODO make tile name editing prettier
            if (!hasFocus)
                updateRoutine()
        }
        tileNameView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable?) {
                if (text.toString() != "Tile name")
                    editTileName(text.toString())
            }
        })
        tileCardView.setOnClickListener(this)

        tileCycleDeleteBtn.setOnClickListener(this)
        tileCyclePrevBtn.setOnClickListener(this)
        tileCycleNextBtn.setOnClickListener(this)

        organizeRoutineBackBtn.setOnClickListener(this)
    }

    private fun initBufferViews() {
        val v = requireView()

        root = v.findViewById(R.id.cl_EditRoutine_sequential_root)
        closeIcon = v.findViewById(R.id.iv_EditRoutine_sequential_close)

        routineLayout = v.findViewById(R.id.ll_EditRoutine_sequential_routine_layout)
        routineNameEditText = v.findViewById(R.id.et_EditRoutine_sequential_routine_name)
        routineOrganizeBtn = v.findViewById(R.id.btn_EditRoutine_sequential_routines_organize)

        colorCard = v.findViewById(R.id.cv_EditRoutine_sequential_color_cardView)
        colorSlider = v.findViewById(R.id.sl_EditRoutine_sequential_color_hueSlider)

        tileCardView = v.findViewById(R.id.cv_EditRoutine_sequential_tile_card)
        tileIconView = v.findViewById(R.id.iv_EditRoutine_sequential_tile_icon)
        tileNameView = v.findViewById(R.id.et_EditRoutine_sequential_tile_name)

        tileCycleLayout = v.findViewById(R.id.ll_EditRoutine_sequential_cycle_layout)
        tileCycleDeleteBtn = v.findViewById(R.id.btn_EditRoutine_sequential_cycle_delete)
        tileCyclePrevBtn = v.findViewById(R.id.btn_EditRoutine_sequential_cycle_prev)
        tileCycleNextBtn = v.findViewById(R.id.btn_EditRoutine_sequential_cycle_next)

        organizeRoutineRoot = v.findViewById(R.id.ll_EditRoutine_sequential_organize_root)
        organizeRoutineRV = v.findViewById(R.id.rv_EditRoutine_sequential_organize_recyclerview)
        organizeRoutineBackBtn = v.findViewById(R.id.btn_EditRoutine_sequential_organize_back)
    }

    private fun initRoutines() {
        routines = ResourceClass.getRoutines()
        if (routines == null) {
            Toast.makeText(context, "Oh no, routines are null. Good bye.", Toast.LENGTH_LONG).show()
        }
        val position = args.routinePosition
        currentRoutine = routines!![position]
        currentRoutine.setAccessibility(isNightMode)
        currentRoutine.lastUsed = System.currentTimeMillis()
    }
    //endregion

    //region update ui
    private fun initUI() {
        currentRoutine.setAccessibility(isNightMode)
        routineNameEditText.clearFocus()
        updateCard()

        updateColorSliderHue()
    }

    override fun updateUI() {
        if (currentRoutine.tiles.size - 1 < position + 1) {
            tileCycleNextBtn.text = getString(R.string.str_btn_EditRoutine_sequential_cycle_add)
        } else {
            tileCycleNextBtn.text = getString(R.string.str_btn_EditRoutine_sequential_cycle_next)
        }
        tileCyclePrevBtn.isEnabled = position - 1 >= 0
        updateRoutine()
        MyLog.d("updating ui!" + System.currentTimeMillis() + Math.random())
        updateCard()
        updateColorSliderHue()
    }

    private fun updateCard() {
        val tile: Tile = currentRoutine.tiles[position]

        val tileName = tile.name
        tileNameView.setText(tileName)
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

    //region update tile components

    //region tile name
    private fun editTileName(s: String?) {
        currentRoutine.tiles[position].name = s
        //updateRoutine()
    }
    //endregion

    //region tile color

    private var isInColorMode: Boolean = false
    private fun handleColorModeToggle() {
        updateColorSliderHue()
        if (!isInColorMode) {
            isInColorMode = true
            goIntoColorMode()
            updateColor(colorSlider.value)
        } else {
            isInColorMode = false
            leaveColorMode()
        }
    }

    private fun updateColor(hue: Float) {
        val color = ResourceClass.calculateColorFromHue(hue)
        currentRoutine.tiles[position].backgroundColor = color
        updateUI()
    }

    private fun leaveColorMode() {
        val transform = MaterialContainerTransform().apply {
            startView = colorCard
            endView = routineLayout

            // Ensure the container transform only runs on a single target
            addTarget(endView)

            // Optionally add a curved path to the transform
            pathMotion = MaterialArcMotion()

            scrimColor = Color.TRANSPARENT
        }

        tileCycleLayout.startAnimation(slideUpIn)

        TransitionManager.beginDelayedTransition(view as ViewGroup, transform)
        routineLayout.visibility = View.VISIBLE
        colorCard.visibility = View.GONE
        tileCycleLayout.visibility = View.VISIBLE
    }

    private fun goIntoColorMode() {
        val transform = MaterialContainerTransform().apply {
            startView = routineLayout
            endView = colorCard

            // Ensure the container transform only runs on a single target
            addTarget(endView)

            // Optionally add a curved path to the transform
            pathMotion = MaterialArcMotion()

            scrimColor = Color.TRANSPARENT
        }

        tileCycleLayout.startAnimation(slideDownOut)

        TransitionManager.beginDelayedTransition(view as ViewGroup, transform)
        routineLayout.visibility = View.GONE
        colorCard.visibility = View.VISIBLE
        tileCycleLayout.visibility = View.GONE
    }

    private fun updateColorSliderHue() {
        val tmpTile: Tile = currentRoutine.tiles[position]
        val bgColor = tmpTile.backgroundColor
        val hsv = FloatArray(3)
        Color.colorToHSV(bgColor, hsv)
        val i: Int = ResourceClass.calculateColorFromHue(hsv[0])
        Color.colorToHSV(i, hsv)
        colorSlider.value = hsv[0]
    }

    //endregion

    //tile icon is handled in MainActivity

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
        val extras = FragmentNavigatorExtras(root as View to currentRoutine.uid!!, routineNameEditText to currentRoutine.name!!, tileIconView to currentRoutine.uid + "icon")

        findNavController().navigate(directions, extras)
    }

    //endregion

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_sequential_routine, container, false)
    }

    companion object {
        const val ICON_DIALOG_TAG = "icon-dialog"

        lateinit var fragment: EditSequentialRoutineFragment
    }

}