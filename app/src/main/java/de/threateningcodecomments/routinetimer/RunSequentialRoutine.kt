package de.threateningcodecomments.routinetimer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import de.threateningcodecomments.accessibility.*
import de.threateningcodecomments.adapters.CVButtonHandler
import de.threateningcodecomments.data.Routine
import de.threateningcodecomments.data.Tile
import de.threateningcodecomments.routinetimer.databinding.FragmentRunSequentialRoutineBinding
import de.threateningcodecomments.services_etc.TileEventService
import de.threateningcodecomments.views.ContinuousTile
import kotlin.math.sign

class RunSequentialRoutine : Fragment(), UIContainer, TileEventService.TileRunFragment {

    private val args: RunSequentialRoutineArgs by navArgs()

    private lateinit var routine: Routine
    private lateinit var routineUid: String

    private lateinit var routineNameView: TextView

    private lateinit var tile0: ContinuousTile
    private lateinit var tile1: ContinuousTile
    private lateinit var tile2: ContinuousTile
    private lateinit var tileTemp: ContinuousTile

    private lateinit var restartButtonCard: MaterialCardView
    private lateinit var restartButtonIcon: ShapeableImageView
    private lateinit var restartHandler: CVButtonHandler

    private lateinit var pauseButtonCard: MaterialCardView
    private lateinit var pauseButtonIcon: ShapeableImageView
    private lateinit var pauseHandler: CVButtonHandler

    private lateinit var touchTarget: View

    private var currentIndex: Int = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = RC.Resources.sharedElementTransition
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStop() {
        super.onStop()

        TileEventService.tileRunFragment = null
    }

    override fun onStart() {
        super.onStart()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            navigateBack()
        }

        routineUid = args.routineUid
        routine = RC.RoutinesAndTiles.getRoutineFromUid(routineUid)

        routine.lastUsed = System.currentTimeMillis()

        TileEventService.tileRunFragment = this

        initBufferViews()
        initListeners()

        updateCurrentIndex()

        updateUI()
    }

    private fun updateCurrentIndex() {
        val noTileRunning = RC.runningTiles[routine.uid].isEmpty()
        if (noTileRunning) {
            currentIndex = 0
            return
        }

        val tile = RC.runningTiles[routineUid].first()
        currentIndex = routine.tiles.indexOf(tile)
    }


    override fun updateRunningTile(tile: Tile, elapsedTime: Long, totalTime: Long) {
        val isActiveTile =
            routine.tiles.indexOf(tile) == currentIndex

        if (!isActiveTile)
            setActiveTile(tile)

        val info1: String?
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
                info1 = null
                info2 = null
            }

            else ->
                throw Tile.TileModeException()
        }

        tile1.updateUI(tile, info1, info2)
    }

    private fun setActiveTile(tile: Tile) {
        val newIndex = routine.tiles.indexOf(tile)

        currentIndex =
            if (newIndex < currentIndex)
                newIndex + 1
            else
                newIndex - 1

        if (newIndex < currentIndex)
            cycleBack()
        else
            cycleForward()
    }

    private fun initListeners() {
        val swipeListener =
            OnSwipeTouchListener(requireContext()) { direction ->
                when (direction) {
                    RC.Directions.UP -> cycleForward(animationOnly = false)
                    RC.Directions.DOWN -> cycleBack(animationOnly = false)
                    else -> {
                    }
                }
            }

        tile0.setOnTouchListener(swipeListener)
        tile1.setOnTouchListener(swipeListener)
        tile2.setOnTouchListener(swipeListener)
        touchTarget.setOnTouchListener(swipeListener)

        tile1.doOnClick {
            toggleTileEvent(it, isPause = false, isRepeat = false)
        }


        //button handler
        pauseHandler = CVButtonHandler(
            card = pauseButtonCard,
            icon = pauseButtonIcon,
            deselectColor = RC.Resources.Colors.acceptColor,
            selectColor = RC.Resources.Colors.cancelColor
        ) {
            toggleTileEvent(isPause = true)
        }


        restartHandler = CVButtonHandler(
            card = restartButtonCard,
            icon = restartButtonIcon,
            mode = CVButtonHandler.MODE_TAP,
            selectColor = RC.Resources.Colors.primaryColor
        ) {
            toggleTileEvent(isRepeat = true)
        }

    }

    private val currentTile: Tile?
        get() =
            routine.tiles.getOrNull(currentIndex)

    private fun toggleTileEvent(it: Tile? = currentTile, isPause: Boolean = false, isRepeat: Boolean = false) {
        it ?: throw IllegalStateException("CurrentTile is null!!")

        val isRunning = RC.runningTiles.containsTile(it)

        if (isRunning)
            RC.runningTiles[routineUid].remove(it, isPause, isRepeat)
        else
            RC.runningTiles[routineUid].add(it, isPause, isRepeat)

        //inverted because tile gets toggled
        setPlayButton(!isRunning)

        tile1.updateUI(it)

        updateUI()
    }

    private fun cycleBack(animationOnly: Boolean = true) {
        val isPossible =
            currentIndex - 1 >= 0

        if (!isPossible)
        //TODO animateCycleError
            return

        if (!animationOnly)
            RC.runningTiles.removeTile(currentTile ?: throw IllegalStateException("No tile is running"))

        currentIndex--

        if (!animationOnly)
            RC.runningTiles[routineUid].add(currentTile ?: throw IllegalStateException("No tile is running"))

        animateDown()
    }

    private fun cycleForward(animationOnly: Boolean = true) {
        val isPossible =
            currentIndex + 1 < routine.tiles.size

        if (!isPossible)
        //TODO animateCycleError
            return

        if (!animationOnly)
            RC.runningTiles.removeTile(currentTile ?: throw IllegalStateException("No tile is running"))

        currentIndex++

        if (!animationOnly)
            RC.runningTiles[routineUid].add(currentTile ?: throw IllegalStateException("No tile is running"))

        animateUp()
    }

    private fun resetTiles() {
        animateY(
            tile0,
            tile0.top,
            isInstant = true
        )

        animateY(
            tile1,
            tile1.top,
            isInstant = true
        )

        animateY(
            tile2,
            tile2.top,
            isInstant = true
        )

        animateY(
            tileTemp,
            -1000,
            isInstant = true
        )

        updateUI()
    }

    private fun animateY(tile: ContinuousTile, y: Int, endAction: () -> Unit = {}, isInstant: Boolean = false) =
        animateY(tile, y.toFloat(), endAction, isInstant)

    private fun animateY(tile: ContinuousTile, y: Float, endAction: () -> Unit = {}, isInstant: Boolean = false) =
        tile.animate()
            .setDuration(
                if (isInstant) 0
                else 250L
            )
            .y(y)
            .withEndAction(endAction)
            .start()


    private fun animateDown() {
        initTempTile(false)

        animateY(
            tileTemp,
            tile0.top
        )

        animateY(
            tile0,
            tile1.top
        )

        animateY(
            tile1,
            tile2.top
        )

        animateY(
            tile2,
            tile2.bottom + tile2.marginTop,
            endAction = {
                resetTiles()
            }
        )
    }

    private fun animateUp() {
        initTempTile(true)

        animateY(
            tile0,
            -tileTemp.height.toFloat() - tile0.marginTop
        )

        animateY(
            tile1,
            tile0.top
        )

        animateY(
            tile2,
            tile1.top
        )

        animateY(
            tileTemp,
            tile2.top,
            endAction = {
                resetTiles()
            }
        )

    }

    private fun initTempTile(isUp: Boolean) {
        tileTemp.visibility = View.INVISIBLE

        val tileIndex =
            if (isUp)
                currentIndex + 1
            else
                currentIndex - 1

        //if tile is null, so if it doesn't exist
        val tile = routine.tiles.getOrNull(tileIndex)
        tile ?: return

        tileTemp.visibility = View.VISIBLE

        tileTemp.updateUI(tile)

        tileTemp.deactivate(true)

        val y =
            if (isUp)
                tile2.bottom + tile2.marginTop.toFloat()
            else
                -tile2.height - tile2.marginTop

        tileTemp.animate()
            .y(y.toFloat())
            .setDuration(0L)
            .start()
    }

    private fun initBufferViews() {

        binding.apply {
            routineNameView = tvRunRoutineSequentialInfoRoutineName

            tile0 = tileRunRoutineSequential0
            tile1 = tileRunRoutineSequential1
            tile2 = tileRunRoutineSequential2
            tileTemp = tileRunRoutineSequentialTemp

            restartButtonCard = cvRunRoutineSequentialRestartButton
            restartButtonIcon = ivRunRoutineSequentialResetIcon

            pauseButtonCard = cvRunRoutineSequentialPauseButton
            pauseButtonIcon = ivRunRoutineSequentialPauseIcon

            touchTarget = vRunRoutineSequentialTouchTarget
        }
    }

    override fun updateUI() {
        routineNameView.text = routine.name

        //current tile
        val currentTile = routine.tiles[currentIndex]
        tile1.updateUI(currentTile)
        tile1.activate(true)
        setPlayButton(isRunning = true)

        //previous tile
        val hasPreviousTile = (currentIndex > 0)
        if (hasPreviousTile) {
            val previousTile = routine.tiles[currentIndex - 1]

            tile0.updateUI(previousTile)
            tile0.deactivate(true)
            tile0.visibility = View.VISIBLE
        } else {
            tile0.visibility = View.INVISIBLE
        }

        //next tile
        val hasNextTile = (currentIndex < routine.tiles.size - 1)
        if (hasNextTile) {
            val nextTile = routine.tiles[currentIndex + 1]

            tile2.updateUI(nextTile)
            tile2.deactivate(true)
            tile2.visibility = View.VISIBLE
        } else {
            tile2.visibility = View.INVISIBLE
        }
    }

    private fun setPlayButton(isRunning: Boolean) {
        val drawID =
            if (isRunning)
                R.drawable.ic_pause
            else
                R.drawable.ic_play
        val draw =
            RC.Resources.getDrawable(drawID)

        pauseButtonIcon.setImageDrawable(draw)

        if (isRunning)
            pauseHandler.select(execOnClickRun = false)
        else
            pauseHandler.deselect(execOnClickRun = false)
    }

    private fun restartRoutine() {

    }

    private fun navigateBack() {
        val directions = RunSequentialRoutineDirections.actionRunSequentialRoutineToSelectRoutineFragment()

        findNavController().navigate(directions)
    }

    private var _binding: FragmentRunSequentialRoutineBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRunSequentialRoutineBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    companion object
}