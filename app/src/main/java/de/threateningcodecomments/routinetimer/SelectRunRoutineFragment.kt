package de.threateningcodecomments.routinetimer

import accessibility.MyLog
import accessibility.ResourceClass
import accessibility.Routine
import accessibility.UIContainer
import adapters.SelectRoutineRVAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class SelectRunRoutineFragment : Fragment(), UIContainer {
    private lateinit var mAdapter: SelectRoutineRVAdapter

    private lateinit var routines: ArrayList<Routine>

    private lateinit var createRoutineButton: MaterialButton

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition

        super.onViewCreated(view, savedInstanceState)

        MainActivity.currentFragment = this

        initBufferViews()

        initListeners()

        createRoutineButton.visibility = View.GONE

        initRoutines()

        initRecyclerView()
    }

    override fun onStart() {
        super.onStart()

        ResourceClass.updateNightMode(requireActivity().application)
        updateUI()
    }

    private fun updateRoutines() {
        routines = ResourceClass.getRoutines()
    }

    private fun initRecyclerView() {
        if (routines.size == 0) {
            ResourceClass.loadRoutines()
            routines = ResourceClass.getRoutines()
        }
        mAdapter = SelectRoutineRVAdapter(routines, SelectRoutineRVAdapter.MODE_RUN)
        recyclerView.adapter = mAdapter
        layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager

        registerForContextMenu(recyclerView)
    }

    fun goToRunRoutine(layout: LinearLayout, iv: ShapeableImageView, nameView: TextView, tmpRoutine: Routine) {
        MyLog.d("going to runroutine")
        val directions = SelectRunRoutineFragmentDirections.actionSelectRunRoutineToRunContinuousRoutine()
        findNavController().navigate(directions)
    }

    private fun initRoutines() {
        routines = ResourceClass.getRoutines()

        if (routines.size == 0) {
            routines.add(Routine.ERROR_ROUTINE)
        }
    }

    private fun initListeners() {
        toolbar.setNavigationOnClickListener {
            val directions = SelectRunRoutineFragmentDirections.actionSelectRunRoutineToStartFragment()

            findNavController().navigate(directions)
        }
    }

    private fun initBufferViews() {
        val v = requireView()

        createRoutineButton = v.findViewById(R.id.fab_SelectRoutine_add)
        toolbar = v.findViewById(R.id.tb_SelectRoutine_toolbar)
        recyclerView = v.findViewById(R.id.rv_SelectRoutine_recyclerView)
    }

    override fun updateUI() {
        updateRoutines()
        routines = ResourceClass.sortRoutines(routines)

        if (routines.contains(Routine.ERROR_ROUTINE)) {
            routines.clear()
            routines.add(Routine.ERROR_ROUTINE)
        }


        mAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_routine, container, false)
    }

    companion object {
    }
}