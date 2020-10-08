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
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.transition.platform.MaterialArcMotion
import com.google.android.material.transition.platform.MaterialContainerTransform
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [SelectRoutineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SelectRoutineFragment : Fragment(), View.OnClickListener {
    private lateinit var activity: AppCompatActivity
    private lateinit var toolbar: Toolbar
    private lateinit var createFab: ExtendedFloatingActionButton
    private lateinit var toolBarLayout: CollapsingToolbarLayout

    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: MyAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var createForm: FrameLayout
    private lateinit var createFormCard: MaterialCardView
    private lateinit var createFormDismissView: View
    private lateinit var createNameField: EditText
    private lateinit var createModeDropdown: MaterialAutoCompleteTextView
    private lateinit var createSaveButton: MaterialButton

    private var routines: ArrayList<Routine>? = null
    private var bufferRoutine: Routine? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nhf_MainActivity_navHostFragment
            duration = 300.toLong()
            scrimColor = Color.TRANSPARENT
        }

        super.onCreate(savedInstanceState)

        activity = requireActivity() as AppCompatActivity

        initBufferViews()

        initListenersAndAdapters()

        initRoutines()

        activity.setSupportActionBar(toolbar)

        initRecyclerView()
    }

    override fun onStart() {
        super.onStart()

        ResourceClass.loadRoutines()
        updateRoutines()
        ResourceClass.updateNightMode(activity.application)
        updateUI()
        updateForm()
    }

    private fun updateRoutines() {
        routines = ResourceClass.getRoutines()
    }

    private fun updateUI() {
        if (routines!!.contains(Routine.ERROR_ROUTINE)) {
            routines!!.clear()
            routines!!.add(Routine.ERROR_ROUTINE)
        }

        mAdapter.notifyDataSetChanged()
    }

    private fun initListenersAndAdapters() {
        createFab.setOnClickListener(this)

        createSaveButton.setOnClickListener(this)
        createFormDismissView.setOnClickListener(this)

        val options = arrayOf(SelectRoutineFragment.CONTINUOUS_MODE, SelectRoutineFragment.SEQUENTIAL_MODE)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                R.layout.routine_popup_item,
                options)

        createModeDropdown.setAdapter(adapter)
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateForm();
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        }
        createNameField.addTextChangedListener(textWatcher)
        createModeDropdown.addTextChangedListener(textWatcher)
    }

    private fun updateForm() {
        val name: String? = createNameField.text.toString()
        val mode: String? = createModeDropdown.text.toString()

        createSaveButton.isEnabled = !(name?.length == 0 || mode?.length == 0)

        MyLog.d(mode)
    }

    private fun initBufferViews() {
        recyclerView = requireView().findViewById(R.id.rv_SelectRoutine_recyclerView)
        toolbar = requireView().findViewById(R.id.tb_SelectRoutine_toolbar)
        toolBarLayout = requireView().findViewById(R.id.ctbl_SelectRoutine_collapsingToolbarLayout)
        createFab = requireView().findViewById(R.id.fab_SelectRoutine_add)

        createForm = requireView().findViewById(R.id.fl_SelectRoutine_createRoutine_form)
        createFormCard = requireView().findViewById(R.id.cv_SelectRoutine_createRoutine_form)
        createFormDismissView = requireView().findViewById(R.id.v_SelectRoutine_createRoutine_deselectView)
        createNameField = requireView().findViewById(R.id.et_SelectRoutine_createRoutine_name)
        createModeDropdown = requireView().findViewById(R.id.legacy_dd_SelectRoutine_createRoutine_mode)
        createSaveButton = requireView().findViewById(R.id.btn_SelectRoutine_createRoutine_save)
    }

    private fun initRoutines() {
        routines = ResourceClass.getRoutines()
    }

    private fun initRecyclerView() {
        mAdapter = MyAdapter(routines)
        recyclerView.adapter = mAdapter
        layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
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

    private fun createRoutine() {
        val tmpRoutine: Routine = Routine()

        tmpRoutine.name = createNameField.text.toString()

        val mode: String = createModeDropdown.text.toString()
        if (mode.equals(SEQUENTIAL_MODE)) {
            tmpRoutine.mode = Routine.MODE_SEQUENTIAL
        } else {
            tmpRoutine.mode = Routine.MODE_CONTINUOUS
        }

        val tiles: ArrayList<Tile> = arrayListOf(Tile.DEFAULT_TILE)
        tmpRoutine.tiles = tiles

        tmpRoutine.uid = UUID.randomUUID().toString()

        addRoutine(tmpRoutine);
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

    private fun addRoutine() {
        addRoutine(bufferRoutine!!)
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

        (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(requireView().windowToken, 0)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_routine, container, false)
    }

    companion object {
        const val CONTINUOUS_MODE: String = "Continuous mode"
        const val SEQUENTIAL_MODE: String = "Sequential mode"
    }
}