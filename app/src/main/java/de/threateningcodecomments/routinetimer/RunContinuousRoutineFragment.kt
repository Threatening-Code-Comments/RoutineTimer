package de.threateningcodecomments.routinetimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textview.MaterialTextView
import de.threateningcodecomments.accessibility.*
import de.threateningcodecomments.data.Routine
import de.threateningcodecomments.data.Tile
import de.threateningcodecomments.routinetimer.databinding.FragmentRunContinuousRoutineBinding
import de.threateningcodecomments.services_etc.TileEventService
import de.threateningcodecomments.views.ContinuousTile


class RunContinuousRoutineFragment : Fragment(), UIContainer, TileEventService.TileRunFragment {
    private var _binding: FragmentRunContinuousRoutineBinding? = null
    private val binding get() = _binding!!

    private var gridTiles: ArrayList<ContinuousTile> = ArrayList()
    private var gridRows: ArrayList<ConstraintLayout> = ArrayList()

    private lateinit var routineNameView: MaterialTextView

    private var currentTile: Tile? = null

    private val args: RunContinuousRoutineFragmentArgs by navArgs()

    private lateinit var routine: Routine
    private lateinit var routineUid: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = RC.Resources.sharedElementTransition
        super.onViewCreated(view, savedInstanceState)

        routine = RC.RoutinesAndTiles.getRoutineFromUid(args.routineUid)
        routineUid = routine.uid

        initBufferViews()
    }

    override fun onStart() {
        super.onStart()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val directions =
                RunContinuousRoutineFragmentDirections.actionRunContinuousRoutineToSelectEditRoutineFragment()

            findNavController().navigate(directions)
        }

        RC.Db.removeRoutineListener()
        routine.setAccessibility(RC.isNightMode)

        initUI()

        initListeners()

        updateUI()

        TileEventService.tileRunFragment = this
    }

    private fun initListeners() {
        for (tile in gridTiles)
            tile.apply {
                doOnClick {
                    if (RC.runningTiles.containsTile(it))
                        RC.runningTiles[routineUid].remove(it)
                    else
                        RC.runningTiles[routineUid].add(it)

                    updateUI()
                }
            }
    }

    private fun initUI() {
        for ((index, gridTile) in gridTiles.withIndex()) {
            val tile = routine.tiles[index]
            gridTile.updateUI(tile, null, null)
            MyLog.d("tile uid: ${tile.uid}")
        }

        routineNameView.text = routine.name
    }

    override fun updateRunningTile(tile: Tile, elapsedTime: Long, totalTime: Long) {
        val index = routine.tiles.indexOf(tile)
        val gTile = gridTiles[index]

        val info1: String
        val info2: String?

        when (tile.mode) {
            Tile.MODE_COUNT_UP -> {
                info1 = RC.Conversions.Time.millisToHHMMSSorMMSS(elapsedTime)
                info2 = RC.Conversions.Time.millisToHHMMSSorMMSS(totalTime)
            }

            Tile.MODE_COUNT_DOWN -> {
                val time = tile.countDownSettings.countDownTime - elapsedTime

                info1 = RC.Conversions.Time.millisToHHMMSSorMMSS(time)

                val times = (totalTime / tile.countDownSettings.countDownTime).toInt()

                info2 = times.toString()
            }

            Tile.MODE_TAP -> {
                info1 = tile.tapSettings.count.toString()
                info2 = null
            }

            Tile.MODE_DATA -> {
                TODO("Implement Data")
            }

            else ->
                throw Tile.TileModeException()
        }

        requireActivity().runOnUiThread {
            gTile.updateUI(tile, info1, info2)
        }

        updateUI()
    }

    override fun updateUI() {
        routineNameView.text = routine.name

        val tiles = routine.tiles
        for ((gridTileIndex, gridTile) in gridTiles.withIndex()) {
            val tile = tiles[gridTileIndex]

            updateTileActive(tile, gridTile)

            updateTileInfo(gridTile, tile)
        }
    }

    private fun updateTileActive(tile: Tile, gridTile: ContinuousTile) {
        val tileIsRunning =
            RC.runningTiles.containsTile(tile.uid)
        val noTileIsRunning =
            RC.runningTiles[routineUid].isEmpty()

        val shouldBeActivated =
            noTileIsRunning ||
                    (tileIsRunning && !noTileIsRunning)

        if (shouldBeActivated)
            gridTile.activate()
        else
            gridTile.deactivate()
    }

    private fun updateTileInfo(gridTile: ContinuousTile, tile: Tile) {
        //TODO implement
    }

    private fun initBufferViews() {
        val v = requireView()

        binding.apply {
            gridTiles.addAll(
                listOf(
                    tileRunCon0,
                    tileRunCon1,
                    tileRunCon2,
                    tileRunCon3,
                    tileRunCon4,
                    tileRunCon5,
                    tileRunCon6,
                    tileRunCon7
                )
            )
        }

        routineNameView = v.findViewById(R.id.tv_RunRoutine_continuous_routine_name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        TileEventService.tileRunFragment = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRunContinuousRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        lateinit var instance: RunContinuousRoutineFragment
    }
}