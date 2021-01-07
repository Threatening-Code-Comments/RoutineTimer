package de.threateningcodecomments.routinetimer

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import de.threateningcodecomments.accessibility.*
import de.threateningcodecomments.accessibility.ResourceClass
import kotlinx.android.synthetic.main.fragment_run_sequential_routine.*
import kotlin.math.abs

class RunSequentialRoutine : Fragment(), UIContainer {

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
    private lateinit var closeView: ShapeableImageView

    private var tileIndex = 0
    private var previousTileIndex = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition
        super.onViewCreated(view, savedInstanceState)

        routineUid = args.routineUid
        currentRoutine = ResourceClass.getRoutineFromUid(routineUid)

        initBufferViews()
        initListeners()

        updateUI()
    }

    //region init
    private fun initListeners() {
        closeView.setOnClickListener {
            navigateBack()
        }

        pauseButton.setOnClickListener {
        }
    }

    private fun initBufferViews() {
        closeView = iv_EditRoutine_sequential_close

        tileCardView = cv_RunRoutine_sequential_tileMain
        tileIconView = iv_RunRoutine_sequential_tileIcon
        tileNameView = tv_RunRoutine_sequential_tileName

        routineNameView = tv_RunRoutine_sequential_info_routineName

        currentTimeInfoView = tv_RunRoutine_sequential_info_currentInfo
        currentTimeValueView = tv_RunRoutine_sequential_info_currentValue
        totalTimeInfoView = tv_RunRoutine_sequential_info_totalInfo
        totalTimeValueView = tv_RunRoutine_sequential_info_totalValue

        pauseButton = btn_RunRoutine_sequential_pause
    }
    //endregion

    override fun updateUI() {
        routineNameView.text = currentRoutine.name

        val currentTile = ResourceClass.currentTiles[routineUid]
        val previousTile = ResourceClass.previousCurrentTiles[routineUid]
        val tileAtIndex = currentRoutine.tiles[0]

        if (currentTile == null) {
            if (previousTile != null)
                startTile(previousTile)
            else
                startTile(tileAtIndex)
        } else {
            val newTileIndex = currentRoutine.tiles.indexOf(currentTile)

            previousTileIndex = tileIndex
            tileIndex = newTileIndex
        }
        updateTileInUI(currentTile ?: previousTile ?: tileAtIndex)
    }

    private var isRunning = false
    private fun startTile(tile: Tile) {
        if (!isRunning) {
            App.instance.startTile(tile)
            isRunning = true

            updateUI()

            val updatingHandler = object : Runnable {
                override fun run() {
                    /* val currentTime = System.currentTimeMillis() - tile.countingStart
                     val currentTimeStr = ResourceClass.millisToHHMMSSmm(currentTime)
                     currentTimeValueView.text = currentTimeStr
                     MyLog.d("currentTime is $currentTime, and currentTimeStr is $currentTimeStr")

                     val totalTime = tile.totalCountedTime + currentTime
                     val totalTimeStr = ResourceClass.millisToHHMMSS(totalTime)
                     totalTimeValueView.text = totalTimeStr

                     if (currentTime < tile.countDownSettings.countDownTime)
                         Handler().postDelayed(this, 20)
                     else
                         stopTile(tile)*/

                    //buffers currentTime, prevent currentTime from being negative
                    var currentTime = System.currentTimeMillis() - abs(tile.countingStart)
                    currentTime =
                            if (tile.mode == Tile.MODE_COUNT_DOWN)
                                tile.countDownSettings.countDownTime - currentTime
                            else
                                currentTime

                    if (currentTime < 0L)
                        currentTime = 0L

                    //update currentTimeField, when tile is countUp this is the raw elapsed time, with countdownTile this
                    // is the remaining time
                    val currentTimeStr = ResourceClass.millisToHHMMSSmm(currentTime)
                    currentTimeValueView.text = currentTimeStr

                    //updates the totalTimeField, only needs to be done with countUpTiles
                    if (tile.mode == Tile.MODE_COUNT_UP) {
                        val totalTime = tile.totalCountedTime + currentTime
                        val totalTimeStr = ResourceClass.millisToHHMMSS(totalTime)
                        totalTimeValueView.text = totalTimeStr
                    }

                    if (ResourceClass.currentTiles[routineUid] != null) {
                        Handler().postDelayed(this, 10)
                    } else {
                        updateUI()
                        return
                    }
                }
            }.run()
        }
    }

    private fun stopTile(tile: Tile) {
        App.instance.stopTile(currentRoutine)
        isRunning = false
    }

    private fun updateTileInUI(tile: Tile) {
        tileCardView.setCardBackgroundColor(tile.backgroundColor)

        tileNameView.text = tile.name
        tileNameView.setTextColor(tile.contrastColor)

        tileIconView.setImageDrawable(ResourceClass.getIconDrawable(tile))
        tileIconView.setColorFilter(tile.contrastColor)

        if (tile.mode == Tile.MODE_COUNT_DOWN) {
            currentTimeInfoView.text = "Remaining time:"

            totalTimeInfoView.text = "Pressed:"
            val timesPressed = tile.totalCountedTime / tile.countDownSettings.countDownTime
            totalTimeValueView.text = "${timesPressed}x"
        } else {
            currentTimeInfoView.text = "Current time:"

            totalTimeInfoView.text = "Total time:"
            totalTimeInfoView.text = ResourceClass.millisToHHMMSS(tile.totalCountedTime)
        }
    }

    override fun updateCurrentTile() {
    }

    private fun navigateBack() {
        val directions = RunSequentialRoutineDirections.actionRunSequentialRoutineToSelectRoutineFragment()

        findNavController().navigate(directions)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_run_sequential_routine, container, false)
    }

    companion object {

    }
}