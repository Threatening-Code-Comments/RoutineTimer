package de.threateningcodecomments.routinetimer

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.accessibility.Routine
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.accessibility.UIContainer
import de.threateningcodecomments.routinetimer.databinding.FragmentRunSequentialRoutineBinding
import kotlin.math.abs

class RunSequentialRoutine : Fragment(), UIContainer, View.OnClickListener {

    private val args: RunSequentialRoutineArgs by navArgs()

    private lateinit var currentRoutine: Routine
    private lateinit var routineUid: String

    private lateinit var tileCardView: MaterialCardView
    private lateinit var tileIconView: ShapeableImageView
    private lateinit var tileNameView: MaterialTextView

    private lateinit var routineNameView: MaterialTextView
    private lateinit var currentTimeInfoView: MaterialTextView
    private lateinit var currentTimeValueView: MaterialTextView
    private lateinit var totalTimeInfoView: MaterialTextView
    private lateinit var totalTimeValueView: MaterialTextView

    private lateinit var pauseButton: MaterialButton
    private lateinit var restartButton: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = RC.Resources.sharedElementTransition
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            navigateBack()
        }

        routineUid = args.routineUid
        currentRoutine = RC.RoutinesAndTiles.getRoutineFromUid(routineUid)

        initBufferViews()
        initListeners()

        updateUI()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cv_RunRoutine_sequential_tileMain -> {
                App.instance.cycleForward(currentRoutine)
                cycleForward()
            }
            R.id.btn_RunRoutine_sequential_pause -> {
                val currentTile = RC.currentTiles[routineUid] ?: RC
                        .previousCurrentTiles[routineUid] ?: currentRoutine.tiles[0]

                if (!isRunning) {
                    App.instance.startTile(currentTile)
                    startTile(currentTile)
                } else {
                    App.instance.stopTile(currentRoutine)
                    stopTile()
                }
            }
            R.id.btn_RunRoutine_sequential_restart -> restartRoutine()
        }
    }

    //region init
    private fun initListeners() {

        tileCardView.setOnClickListener(this)

        pauseButton.setOnClickListener(this)
        restartButton.setOnClickListener(this)
    }

    private fun initBufferViews() {

        binding.apply {
            tileCardView = cvRunRoutineSequentialTileMain
            tileIconView = ivRunRoutineSequentialTileIcon
            tileNameView = tvRunRoutineSequentialTileName

            routineNameView = tvRunRoutineSequentialInfoRoutineName

            currentTimeInfoView = tvRunRoutineSequentialInfoCurrentInfo
            currentTimeValueView = tvRunRoutineSequentialInfoCurrentValue
            totalTimeInfoView = tvRunRoutineSequentialInfoTotalInfo
            totalTimeValueView = tvRunRoutineSequentialInfoTotalValue

            pauseButton = btnRunRoutineSequentialPause
            restartButton = btnRunRoutineSequentialRestart
        }
    }
    //endregion

    override fun updateUI() {

        routineNameView.text = currentRoutine.name

        val currentTile = RC.currentTiles[routineUid]

        //if the currentTile is null, set isRunning to false
        isRunning = (currentTile != null)

        val isNightMode = RC.isNightMode

        //handle text and color of pause button, functionality is in onClick
        //handle starting of tileUpdatingHandler with startTile
        if (!isRunning) {
            pauseButton.text = getString(R.string.str_RunRoutine_sequential_pauseButtonResume)
            if (isNightMode)
                pauseButton.setBackgroundColor(getColor(requireContext(), R.color.colorAcceptDark))
            else
                pauseButton.setBackgroundColor(getColor(requireContext(), R.color.colorAcceptLight))
        } else {
            pauseButton.text = getString(R.string.str_RunRoutine_sequential_pauseButtonPause)
            if (isNightMode)
                pauseButton.setBackgroundColor(getColor(requireContext(), R.color.colorCancelDark))
            else
                pauseButton.setBackgroundColor(getColor(requireContext(), R.color.colorCancelLight))

            startTile(currentTile!!)
        }

        updateTileInUI(currentTile)
    }

    private fun updateTileInUI(tile: Tile?) {
        val previousTile = RC.previousCurrentTiles[routineUid]
        //hardcode of tile position, might want to change that
        val validTile = tile ?: previousTile ?: currentRoutine.tiles[0]

        val bgColor =
                tile?.backgroundColor ?: Color.GRAY
        val contrastColor =
                RC.Conversions.Colors.calculateContrast(bgColor)

        tileCardView.setCardBackgroundColor(bgColor)

        tileNameView.text =
                if (SettingsFragment.preferences.dev.debug)
                    RC.Debugging.shortenUid(validTile.uid)
                else
                    validTile.name
        tileNameView.setTextColor(contrastColor)

        tileIconView.setImageDrawable(RC.getIconDrawable(validTile))
        tileIconView.setColorFilter(contrastColor)

        if (validTile.mode == Tile.MODE_COUNT_DOWN) {
            currentTimeInfoView.text = getString(R.string.str_RunRoutine_sequential_currentTimeInfo_countdown)

            totalTimeInfoView.text = getString(R.string.str_RunRoutine_sequential_totalTimeInfo_countdown)
            val timesPressed = validTile.totalCountedTime / validTile.countDownSettings.countDownTime
            totalTimeValueView.text = "${timesPressed}x"
        } else {
            currentTimeInfoView.text = getString(R.string.str_RunRoutine_sequential_currentTimeInfo_countup)

            totalTimeInfoView.text = getString(R.string.str_RunRoutine_sequential_totalTimeInfo_countup)
            totalTimeValueView.text = RC.Conversions.Time.millisToHHMMSSorMMSS(validTile.totalCountedTime)
        }

        //if the tile isn't running, change the text on the time displays to "not running"
        if (!isRunning) {
            currentTimeValueView.text = getString(R.string.str_RunRoutine_sequential_currentTimeInfo_notRunning)
        }

    }

    private var isRunning = false
    private var isRunningInUI = false
    private fun startTile(tile: Tile) {
        if (!isRunningInUI) {
            isRunningInUI = true

            updateTileInUI(tile)

            val updatingHandler = object : Runnable {
                override fun run() {
                    //buffers currentTime, prevent currentTime from being negative
                    var currentTime = System.currentTimeMillis() - abs(tile.countingStart)
                    currentTime =
                            if (tile.mode == Tile.MODE_COUNT_DOWN)
                                tile.countDownSettings.countDownTime - currentTime
                            else
                                currentTime

                    if (currentTime <= 0L) {
                        currentTime = 0L

                        if (tile.mode == Tile.MODE_COUNT_DOWN) {
                            cycleForward()
                        }
                    }

                    //update currentTimeField, when tile is countUp this is the raw elapsed time, with countdownTile this
                    // is the remaining time
                    val currentTimeStr = RC.Conversions.Time.millisToHHMMSSmmOrMMSSmm(currentTime)
                    currentTimeValueView.text = currentTimeStr

                    //updates the totalTimeField, only needs to be done with countUpTiles
                    if (tile.mode == Tile.MODE_COUNT_UP) {
                        val totalTime = tile.totalCountedTime + currentTime
                        val totalTimeStr = RC.Conversions.Time.millisToHHMMSSorMMSS(totalTime)
                        totalTimeValueView.text = totalTimeStr
                    }

                    if (RC.currentTiles[routineUid] == tile) {
                        Handler().postDelayed(this, 10)
                    } else {
                        isRunningInUI = false
                        stopTile()
                        updateUI()
                        return
                    }
                }
            }.run()
        }
    }

    private fun stopTile() {
        //if there is no started tile and there was none either, return (this doesn't happen, but kotlin doesn't like
        // it otherwise)
        val currentTile = RC.currentTiles[routineUid] ?: RC.previousCurrentTiles[routineUid]
        ?: return

        //buffers the actual elapsed time
        val realElapsedTime = System.currentTimeMillis() - abs(currentTile.countingStart)
        //sets the cap for elapsedTime for countdownTiles to their countdownTime
        val elapsedTime = if (currentTile.mode == Tile.MODE_COUNT_DOWN)
            currentTile.countDownSettings.countDownTime
        else
            realElapsedTime

        //adds the elapsed time to the tiles total runtime
        if ((currentTile.mode == Tile.MODE_COUNT_UP) || (currentTile.countDownSettings.countDownTime <= realElapsedTime)) {
            currentTile.totalCountedTime += elapsedTime
        }
    }

    private fun restartRoutine() {
        //stops old tile, whichever one that may be
        App.instance.stopTile(currentRoutine)

        //starts first tile of the list
        val newTile = currentRoutine.tiles[0]
        App.instance.startTile(newTile)
    }

    fun cycleForward() {
        val newTile = RC.currentTiles[routineUid]
        val tempIndex = currentRoutine.tiles.indexOf(newTile)
        //when the active tile is the first or last tile, cycle from / to the first tile
        val oldIndex =
                if (tempIndex - 1 < 0 || tempIndex == currentRoutine.tiles.size - 1)
                    0
                else
                    tempIndex - 1
        val oldTile = currentRoutine.tiles[oldIndex]

        updateTileInUI(oldTile)
        //sliding the card out of frame
        tileCardView.startAnimation(RC.Anim.slideLeftOut)

        //waiting for the animation to complete, so it doesn't get overridden
        Handler().postDelayed({
            //if the active tile is the last one in the list, begin from the start again
            if (tempIndex == currentRoutine.tiles.size - 1)
                restartRoutine()

            //animate new entry
            updateTileInUI(newTile)
            tileCardView.startAnimation(RC.Anim.slideLeftIn)

            /*//the currentTile is automatically correctly, look in the getter
            startTile(currentTile)*/
        }, RC.Anim.slideLeftIn.duration)
    }

    override fun updateCurrentTile() {}

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRunSequentialRoutineBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    companion object
}