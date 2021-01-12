package de.threateningcodecomments.routinetimer

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.transition.platform.MaterialArcMotion
import com.google.android.material.transition.platform.MaterialContainerTransform
import de.threateningcodecomments.accessibility.ResourceClass
import de.threateningcodecomments.accessibility.Routine
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.accessibility.UIContainer
import de.threateningcodecomments.adapters.SelectRoutineRVAdapter
import java.util.*
import kotlin.collections.ArrayList

class SelectRoutineFragment : Fragment(), View.OnClickListener, UIContainer {
    private lateinit var activity: AppCompatActivity

    private lateinit var rootLayout: CoordinatorLayout

    private lateinit var toolbar: Toolbar
    private lateinit var createFab: ExtendedFloatingActionButton
    private lateinit var toolBarLayout: CollapsingToolbarLayout

    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: SelectRoutineRVAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var createForm: FrameLayout
    private lateinit var createFormCard: MaterialCardView
    private lateinit var createFormDismissView: View
    private lateinit var createNameField: EditText
    private lateinit var createModeDropdown: MaterialAutoCompleteTextView
    private lateinit var createSaveButton: MaterialButton

    private var routines: ArrayList<Routine>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition
        ResourceClass.loadDatabaseRes()

        postponeEnterTransition()

        super.onViewCreated(view, savedInstanceState)

        super.onCreate(savedInstanceState)

        MainActivity.currentFragment = this

        startPostponedEnterTransition()

        initBufferViews()

        initRandom()

        initListenersAndAdapters()

        initRoutines()

        initRecyclerView()
    }

    override fun onStart() {
        super.onStart()

        //ResourceClass.loadDatabaseRes()
        ResourceClass.loadDatabaseRes()
        updateRoutines()
        ResourceClass.updateNightMode(SelectRoutineFragment.activity.application)
        updateUI()
        updateForm()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab_SelectRoutine_add -> {
                maximizeForm()
            }
            R.id.v_SelectRoutine_createRoutine_deselectView -> {
                if (createForm.visibility == View.VISIBLE) {
                    minimizeForm()
                }
            }
            R.id.btn_SelectRoutine_createRoutine_save -> {
                createRoutine()
                minimizeForm()
            }
            else -> Toast.makeText(context, "Wrong onClickListener ID. It seems you fucked up monumentally.", Toast.LENGTH_LONG).show()
        }
    }

    //region init

    private fun initBufferViews() {
        rootLayout = requireView().findViewById(R.id.cl_SelectRoutine_root)

        recyclerView = requireView().findViewById(R.id.rv_SelectRoutine_recyclerView)
        toolbar = requireView().findViewById(R.id.tb_SelectRoutine_toolbar)
        toolBarLayout = requireView().findViewById(R.id.ctbl_SelectRoutine_collapsingToolbarLayout)
        createFab = requireView().findViewById(R.id.fab_SelectRoutine_add)

        createForm = requireView().findViewById(R.id.fl_SelectRoutine_createRoutine_form)
        createFormCard = requireView().findViewById(R.id.cv_SelectRoutine_createRoutine_form)
        createFormDismissView = requireView().findViewById(R.id.v_SelectRoutine_createRoutine_deselectView)
        createNameField = requireView().findViewById(R.id.et_EditRoutine_sequential_routine_name)
        createModeDropdown = requireView().findViewById(R.id.dd_SelectRoutine_createRoutine_mode)
        createSaveButton = requireView().findViewById(R.id.btn_SelectRoutine_createRoutine_save)
    }

    private fun initRandom() {
        activity = requireActivity() as AppCompatActivity
        SelectRoutineFragment.activity = activity
        fragmentEditSelect = this
        activity.setSupportActionBar(toolbar)
    }

    private fun initListenersAndAdapters() {
        toolbar.setNavigationOnClickListener {
            val startFragment = StartFragment()
            startFragment.sharedElementEnterTransition = MaterialContainerTransform()

            val extras = FragmentNavigatorExtras(rootLayout to "setupButton")
            val directions = SelectRoutineFragmentDirections.actionSelectRoutineFragmentToStartFragment()

            findNavController().navigate(directions, extras)
        }

        createFab.setOnClickListener(this)

        createSaveButton.setOnClickListener(this)
        createFormDismissView.setOnClickListener(this)

        val options = arrayOf(Routine.CONTINUOUS_MESSAGE, Routine.SEQUENTIAL_MESSAGE)
        val adapter: ArrayAdapter<String> = ArrayAdapter(
                requireContext(),
                R.layout.routine_popup_item,
                options)
        createModeDropdown.setAdapter(adapter)

        val textWatcher: TextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateForm()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        }
        createNameField.addTextChangedListener(textWatcher)
        createModeDropdown.addTextChangedListener(textWatcher)
    }

    private fun initRoutines() {
        routines = ResourceClass.getRoutines()

        if (routines?.size == 0) {
            routines!!.add(Routine.ERROR_ROUTINE)
        }
    }

    private fun initRecyclerView() {
        if (routines?.size == 0) {
            ResourceClass.loadDatabaseRes()
            routines = ResourceClass.getRoutines()
        }
        mAdapter = SelectRoutineRVAdapter(routines!!)
        recyclerView.adapter = mAdapter
        layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager

        registerForContextMenu(recyclerView)
    }

    //endregion

    //region handle context menu

    fun handleCMDuplicate(uid: String?) {
        duplicateRoutine(ResourceClass.getRoutineFromUid(uid as String))
    }

    fun handleCMDelete(uid: String?) {
        removeRoutine(ResourceClass.getRoutineFromUid(uid as String))
    }

    //endregion

    //region handle routines

    override fun updateUI() {
        updateRoutines()
        if (routines == null) {
            routines = ArrayList()
        }

        routines = ResourceClass.sortRoutines(routines!!)

        if (routines!!.contains(Routine.ERROR_ROUTINE)) {
            routines!!.clear()
            routines!!.add(Routine.ERROR_ROUTINE)
        }

        mAdapter = SelectRoutineRVAdapter(routines!!)
        recyclerView.adapter = mAdapter
        //mAdapter.notifyDataSetChanged()
    }

    private fun updateRoutines() {
        routines = ResourceClass.getRoutines()
    }

    private fun createRoutine() {
        val tmpRoutine = Routine()

        tmpRoutine.name = createNameField.text.toString()

        val mode: String = createModeDropdown.text.toString()
        if (mode == Routine.SEQUENTIAL_MESSAGE) {
            tmpRoutine.mode = Routine.MODE_SEQUENTIAL
        } else {
            tmpRoutine.mode = Routine.MODE_CONTINUOUS
        }

        val tiles: ArrayList<Tile> = arrayListOf(Tile.DEFAULT_TILE)
        tmpRoutine.tiles = tiles

        tmpRoutine.uid

        tmpRoutine.lastUsed

        addRoutine(tmpRoutine)

        goToEditRoutine(null, null, null, tmpRoutine)
    }

    private fun removeRoutine(routine: Routine) {
        if (routines == null) {
            routines = ResourceClass.getRoutines()
        }

        ResourceClass.removeRoutine(routine)
        routines!!.remove(routine)

        if (routines!!.size == 0) {
            routines!!.add(Routine.ERROR_ROUTINE)
        }

        updateUI()
    }

    private fun duplicateRoutine(routine: Routine) {
        val tmpRoutine = Routine(routine.name!! + " - Copy", UUID.randomUUID().toString(), routine.mode, routine.tiles)

        addRoutine(tmpRoutine)
    }

    private fun addRoutine(routine: Routine) {
        if (routines == null) {
            routines = ResourceClass.getRoutines()
        }

        ResourceClass.saveRoutine(routine)
        routines!!.add(routine)
        routines!!.remove(Routine.ERROR_ROUTINE)

        updateUI()
    }

    //endregion

    //region handle form

    private fun updateForm() {
        val name: String = createNameField.text.toString()
        val mode: String = createModeDropdown.text.toString()

        createSaveButton.isEnabled = !(name.length == 0 || mode.length == 0)
    }

    private fun minimizeForm() {
        val transform = MaterialContainerTransform().apply {
            startView = createFormCard
            endView = createFab

            // Ensure the container transform only runs on a single target
            addTarget(endView)

            // Optionally add a curved path to the transform
            pathMotion = MaterialArcMotion()

            scrimColor = Color.TRANSPARENT
        }

        // Begin the transition by changing properties on the start and end views or
        // removing/adding them from the hierarchy.
        TransitionManager.beginDelayedTransition(view as ViewGroup, transform)
        createFab.visibility = View.VISIBLE
        createForm.visibility = View.GONE

        (SelectRoutineFragment.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun maximizeForm() {
        val transform = MaterialContainerTransform().apply {
            startView = createFab
            endView = createFormCard

            // Ensure the container transform only runs on a single target
            addTarget(endView)

            // Optionally add a curved path to the transform
            pathMotion = MaterialArcMotion()

            scrimColor = Color.TRANSPARENT
        }

        // Begin the transition by changing properties on the start and end views or
        // removing/adding them from the hierarchy.
        TransitionManager.beginDelayedTransition(view as ViewGroup, transform)
        createFab.visibility = View.GONE
        createForm.visibility = View.VISIBLE
    }

    //endregion

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_routine, container, false)
    }

    fun goToEditRoutine(v: LinearLayout?, iv: ShapeableImageView?, routineNameView: TextView?, tmpRoutine: Routine) {
        updateRoutines()
        updateUI()

        if (v == null || iv == null || routineNameView == null) {
            val directions: NavDirections = if (tmpRoutine.mode == Routine.MODE_SEQUENTIAL) {
                SelectRoutineFragmentDirections.actionSelectRoutineFragmentToEditSequentialRoutineFragment(tmpRoutine.uid)
            } else {
                SelectRoutineFragmentDirections.actionSelectRoutineFragmentToEditContinuousRoutineFragment(tmpRoutine.uid)
            }

            findNavController().navigate(directions)
        } else {

            val editRoutineFragment = EditSequentialRoutineFragment()
            editRoutineFragment.sharedElementEnterTransition = MaterialContainerTransform()

            val directions: NavDirections
            val extras: FragmentNavigator.Extras
            if (tmpRoutine.mode == Routine.MODE_SEQUENTIAL) {
                directions = SelectRoutineFragmentDirections.actionSelectRoutineFragmentToEditSequentialRoutineFragment(tmpRoutine.uid)
                extras = FragmentNavigatorExtras(v to "editRoutineRoot", iv to "editRoutineIcon", routineNameView to "editRoutineRoutineName")
            } else {
                directions = SelectRoutineFragmentDirections.actionSelectRoutineFragmentToEditContinuousRoutineFragment(tmpRoutine.uid)
                extras = FragmentNavigatorExtras(v to "editCRoutineRoot", routineNameView to "editCRoutineName")
            }

            findNavController().navigate(directions, extras)
        }
    }

    fun goToRunRoutine(layout: LinearLayout?, iv: ShapeableImageView?, nameView: TextView?, tmpRoutine: Routine) {
        val directions =
                if (tmpRoutine.mode == Routine.MODE_CONTINUOUS)
                    SelectRoutineFragmentDirections.actionSelectEditRoutineFragmentToRunContinuousRoutine(tmpRoutine.uid)
                else
                    SelectRoutineFragmentDirections.actionSelectRoutineFragmentToRunSequentialRoutine(tmpRoutine.uid)


        findNavController().navigate(directions)
    }

    override fun updateCurrentTile() {}

    companion object {
        lateinit var activity: AppCompatActivity
        lateinit var fragmentEditSelect: SelectRoutineFragment
    }

}