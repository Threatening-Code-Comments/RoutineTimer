package de.threateningcodecomments.routinetimer

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import de.threateningcodecomments.accessibility.*
import kotlin.math.abs


class RunContinuousRoutineFragment : Fragment(), View.OnClickListener, UIContainer {
    var gridTiles: ArrayList<MaterialCardView> = ArrayList()
    private var gridRows: ArrayList<ConstraintLayout> = ArrayList()

    private lateinit var closeView: ShapeableImageView
    private lateinit var routineNameView: MaterialTextView

    private var currentTile: Tile? = null

    private val args: RunContinuousRoutineFragmentArgs by navArgs()

    private lateinit var routine: Routine
    private lateinit var routineUid: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition
        super.onViewCreated(view, savedInstanceState)

        MainActivity.currentFragment = this

        initBufferViews()

        initListeners()

        routine = ResourceClass.getRoutineFromUid(args.routineUid)
        routineUid = routine.uid

        updateUI(true)
        updateCurrentTile()
    }

    //region UI
    override fun onClick(v: View?) {
        if (v is MaterialCardView) {
            if (gridTiles.contains(v)) {
                toggleTileSize(gridTiles.indexOf(v))
            }
        }
        when (v!!.id) {
            R.id.iv_RunRoutine_continuous_close -> {
                navigateToSelectRoutine()
            }
        }
    }

    override fun updateUI() {
        updateUI(false)
    }

    fun updateUI(isInit: Boolean) {
        var tileIndex: Int? = routine.tiles.indexOf(ResourceClass.currentTiles[routineUid])
        tileIndex = if (tileIndex == -1) null else tileIndex

        if (isInit && tileIndex != null) {
            toggleTileSize(tileIndex)
            return
        }

        routineNameView.text = routine.name

        val tiles = routine.tiles
        for ((gridTileIndex, gridTile) in gridTiles.withIndex()) {
            val tile = tiles[gridTileIndex]

            val nameView = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_name)

            val imageView = gridTile.findViewById<ShapeableImageView>(R.id.iv_viewholder_runTile_icon)

            val totalTimeMainView = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeMain)

            val currentTimeInfo = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTimeInfo)
            val currentTimeDisplay = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTime)

            val totalTimeInfo = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeInfo)
            val totalTimeDisplay = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTime)

            if (tile != Tile.ERROR_TILE) {
                gridTile.visibility = View.VISIBLE

                val oldColor: Int
                val newColor =
                        if (tileIndex == null || gridTileIndex == expandedTile || gridTileIndex == tileIndex) {
                            oldColor =
                                    if (gridTileIndex == expandedTile || gridTileIndex == tileIndex) {
                                        tile.backgroundColor
                                    } else Color.GRAY

                            tile.backgroundColor
                        } else {
                            oldColor = tile.backgroundColor

                            Color.GRAY
                        }
                val contrastColor =
                        if (tileIndex == null || gridTileIndex == expandedTile || gridTileIndex == tileIndex) tile.contrastColor
                        else Color.WHITE

                animateColor(gridTile, oldColor, newColor)

                nameView.setTextColor(contrastColor)
                nameView.text = tile.name
                imageView.setColorFilter(contrastColor)
                imageView.setImageDrawable(ResourceClass.getIconDrawable(tile))

                animateColor(totalTimeMainView, oldColor, newColor)
                if (tile.mode == Tile.MODE_COUNT_UP) {
                    val totalTileTime_str = ResourceClass.millisToHHMMSSmm(tile.totalCountedTime)
                    totalTimeMainView.text = totalTileTime_str.substring(0, totalTileTime_str.length - 3)
                } else {
                    val timesPressed = (tile.totalCountedTime / tile.countDownSettings.countDownTime).toInt()
                    val countDownTime_str = "Pressed ${timesPressed}x times"
                    totalTimeMainView.text = countDownTime_str
                }

                animateColor(currentTimeInfo, oldColor, newColor)
                animateColor(currentTimeDisplay, oldColor, newColor)

                animateColor(totalTimeInfo, oldColor, newColor)
                animateColor(totalTimeDisplay, oldColor, newColor)
            } else {
                gridTile.visibility = View.INVISIBLE

                gridTile.setBackgroundColor(Color.TRANSPARENT)

                nameView.setTextColor(Color.TRANSPARENT)

                imageView.setImageDrawable(null)
            }
        }
    }

    private fun animateColor(view: View, p_oldColor: Int, p_newColor: Int) {
        val oldColor =
                if (view !is MaterialCardView) ResourceClass.calculateContrast(p_oldColor)
                else p_oldColor
        val newColor =
                if (view !is MaterialCardView) ResourceClass.calculateContrast(p_newColor)
                else p_newColor

        val colorAnimation =
                ValueAnimator.ofObject(ArgbEvaluator(), oldColor, newColor)
        colorAnimation.duration = 300L

        if (view is MaterialCardView)
            colorAnimation.addUpdateListener { animator -> view.setCardBackgroundColor(animator.animatedValue as Int) }
        if (view is MaterialTextView)
            colorAnimation.addUpdateListener { animator -> view.setTextColor(animator.animatedValue as Int) }
        if (view is ShapeableImageView)
            colorAnimation.addUpdateListener { animator -> view.setColorFilter(animator.animatedValue as Int) }

        colorAnimation.start()
    }
    //endregion

    //region timing
    private fun stopCountingTile(tileIndex: Int) {
        //buffers values and updates ui
        val currentGridTile = gridTiles[tileIndex]
        val currentTile = routine.tiles[tileIndex]
        updateUI()

        //if tile is running, stop it in the CountingService
        if (ResourceClass.currentTiles.values.contains(currentTile)) {
            App.instance.stopTile(routine)
        }
        //indicate that the tile isn't running in the ui anymore
        startingTime = null

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

        //handles UI visibility
        val totalTimeMainView = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeMain)
        totalTimeMainView.startAnimation(ResourceClass.Anim.scaleUp)
        totalTimeMainView.visibility = View.VISIBLE

        //updates total time main view in compacted tile view
        totalTimeMainView.text = ResourceClass.millisToHHMMSS(currentTile.totalCountedTime)
    }

    private var startingTime: Long? = null
    private fun startCountingTile(tileIndex: Int) {
        //buffers values
        val currentTile = routine.tiles[tileIndex]
        val currentGridTile = gridTiles[tileIndex]

        //starts counting tile externally (CountingService)
        App.instance.startTile(currentTile)
        //indicates internally (this) that tile is being counted
        startingTime = currentTile.countingStart

        updateUI()

        //buffers fields
        val currentTimeInfo = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTimeInfo)
        val currentTimeField = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTime)
        val totalTimeField = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTime)
        val totalTimeInfo = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeInfo)

        /*if tile mode is countdown:
        *   -set text for currentTimeInfo to "remaining time:"
        *   -set text for totalTimeInfo to "times pressed:"
        *   -set text for totalTimeField to times tile was pressed instead of actual tile
        *
        * else:
        *   -just do the opposite lmao
        * */
        if (currentTile.mode == Tile.MODE_COUNT_DOWN) {
            totalTimeInfo.text = "Pressed:"
            val timesPressed = (currentTile.totalCountedTime / currentTile.countDownSettings.countDownTime).toInt()
            totalTimeField.text = "${timesPressed}x"
        } else {
            //Don't need to update value here, gets updated in the loop
            totalTimeField.text = "Total time:"
        }

        //hide the totalTimeMainView in the UI
        val totalTimeMainView = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeMain)
        totalTimeMainView.startAnimation(ResourceClass.Anim.scaleDown)
        totalTimeMainView.visibility = View.GONE

        //update time values in a loop
        val updatingHandler = object : Runnable {
            override fun run() {
                //buffers currentTime, prevent currentTime from being negative
                var currentTime = System.currentTimeMillis() - abs(currentTile.countingStart)
                currentTime =
                        if (currentTile.mode == Tile.MODE_COUNT_DOWN)
                            currentTile.countDownSettings.countDownTime - currentTime
                        else
                            currentTime

                if (currentTime < 0L)
                    currentTime = 0L

                //update currentTimeField, when tile is countUp this is the raw elapsed time, with countdownTile this
                // is the remaining time
                val currentTimeStr = ResourceClass.millisToHHMMSSmm(currentTime)
                currentTimeField.text = currentTimeStr

                //updates the totalTimeField, only needs to be done with countUpTiles
                if (currentTile.mode == Tile.MODE_COUNT_UP) {
                    val totalTime = currentTile.totalCountedTime + currentTime
                    val totalTimeStr = ResourceClass.millisToHHMMSS(totalTime)
                    totalTimeField.text = totalTimeStr
                }

                if (ResourceClass.currentTiles[routineUid] != null) {
                    Handler().postDelayed(this, 10)
                } else {
                    if (expandedTile == tileIndex) {
                        toggleTileSize(tileIndex)
                    }
                    updateUI(false)
                    return
                }
            }
        }.run()
    }

    override fun updateCurrentTile() {
        currentTile = ResourceClass.currentTiles[routineUid]

        if (currentTile != null) {
            val indexOf = routine.tiles.indexOf(currentTile!!)
            startCountingTile(indexOf)
        } else {
            val indexOf = routine.tiles.indexOf(ResourceClass.previousCurrentTiles[routineUid])
            if (indexOf != -1)
                stopCountingTile(indexOf)
        }
    }
    //endregion

    //region tile size
    private var expandedTile: Int? = null
    fun toggleTileSize(indexOf: Int) {
        val tilePosition = getTilePosition(indexOf)
        val nextTile = if (tilePosition == 0) indexOf + 1 else indexOf - 1

        if (expandedTile == null) {
            expandTile(indexOf, nextTile)
            expandedTile = indexOf
        } else if (expandedTile == indexOf) {
            minimizeTile(indexOf, nextTile)
            expandedTile = null
        }
    }

    private fun expandTile(indexOf: Int, hideIndex: Int) {
        val firstCard = gridTiles[indexOf]
        val hideCard = gridTiles[hideIndex]

        Handler().postDelayed({
            (hideCard.parent as View).visibility = View.GONE
            firstCard.findViewById<ConstraintLayout>(R.id.cl_viewholder_runTile_timeLayout).visibility = View.VISIBLE
        }, ResourceClass.Anim.scaleDown.duration * (2 / 3))
        hideCard.startAnimation(ResourceClass.Anim.scaleDown)

        startCountingTile(indexOf)
    }

    private fun minimizeTile(indexOf: Int, showIndex: Int) {
        val firstCard = gridTiles[indexOf]
        val showCard = gridTiles[showIndex]

        Handler().postDelayed({
            (showCard.parent as View).visibility = View.VISIBLE
            firstCard.findViewById<ConstraintLayout>(R.id.cl_viewholder_runTile_timeLayout).visibility = View.GONE
        }, ResourceClass.Anim.scaleUp.duration * (2 / 3))
        showCard.startAnimation(ResourceClass.Anim.scaleUp)

        stopCountingTile(indexOf)
    }

    private fun getTilePosition(tileIndex: Int): Int {
        return if (tileIndex % 2 != 0) 1 else 0
    }

    private fun getTileRow(tileIndex: Int): Int {
        for (row in gridRows) {
            for (rowChild in row.children) {
                val card = rowChild.findViewById<MaterialCardView>(R.id.cv_viewholder_runTile_card)
                if (gridTiles.contains(card)) {
                    return gridRows.indexOf(row)
                }
            }
        }

        return -1
    }
    //endregion

    //region init
    private fun initBufferViews() {
        val v = requireView()

        val cardGridLayout = v.findViewById<LinearLayout>(R.id.ll_RunRoutine_continuous_main)
        for (row in cardGridLayout.children) {
            gridRows.add(row as ConstraintLayout)
            for (child in row.children) {
                val card = child.findViewById<MaterialCardView>(R.id.cv_viewholder_runTile_card)
                gridTiles.add(card)
            }
        }
        closeView = v.findViewById(R.id.iv_RunRoutine_continuous_close)
        routineNameView = v.findViewById(R.id.tv_RunRoutine_continuous_routine_name)
    }

    private fun initListeners() {
        for (card in gridTiles) {
            card.setOnClickListener(this)
        }
        closeView.setOnClickListener(this)
    }
    //endregion

    override fun onStart() {
        super.onStart()
        ResourceClass.removeRoutineListener()
        routine.setAccessibility(ResourceClass.isNightMode(requireActivity().application))
        updateUI(false)
    }

    private fun navigateToSelectRoutine() {
        //ResourceClass.updateCurrentTile(null, routineUid)
        if (this.currentTile != null) {
            val index = routine.tiles.indexOf(currentTile!!)
            stopCountingTile(index)
        }

        val directions = RunContinuousRoutineFragmentDirections.actionRunContinuousRoutineToSelectEditRoutineFragment()

        findNavController().navigate(directions)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        instance = this
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_run_continuous_routine, container, false)
    }

    companion object {
        const val UI_INIT_KEY = 69
        lateinit var instance: RunContinuousRoutineFragment
    }
}