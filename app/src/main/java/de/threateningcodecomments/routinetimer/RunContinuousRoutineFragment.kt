package de.threateningcodecomments.routinetimer

import accessibility.ResourceClass
import accessibility.Routine
import accessibility.Tile
import accessibility.UIContainer
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView


class RunContinuousRoutineFragment : Fragment(), View.OnClickListener, UIContainer {
    private var gridTiles: ArrayList<MaterialCardView> = ArrayList()
    private var gridRows: ArrayList<ConstraintLayout> = ArrayList()

    private lateinit var closeView: ShapeableImageView
    private lateinit var routineNameView: MaterialTextView

    private var currentTile: Tile? = null

    private val args: RunContinuousRoutineFragmentArgs by navArgs()

    private lateinit var routine: Routine

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition
        super.onViewCreated(view, savedInstanceState)

        MainActivity.currentFragment = this

        initBufferViews()

        initListeners()

        routine = ResourceClass.getRoutineFromUid(args.routineUid)

        updateUI()
    }

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

    //region UI
    override fun updateUI() {
        var tileIndex: Int? = routine.tiles.indexOf(ResourceClass.currentTile)
        tileIndex = if (tileIndex == -1) null else tileIndex

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
        ResourceClass.currentTile = null
        val currentGridTile = gridTiles[tileIndex]
        val currentTile = routine.tiles[tileIndex]
        updateUI()

        startingTime = null

        val totalTimeMainView = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeMain)
        totalTimeMainView.startAnimation(ResourceClass.anim.scaleUp)
        totalTimeMainView.visibility = View.VISIBLE

        countDownTimer?.cancel()

        ResourceClass.saveRoutine(routine)
    }

    private var startingTime: Long? = null
    private var countDownTimer: CountDownTimer? = null
    private fun startCountingTile(tileIndex: Int) {
        ResourceClass.currentTile = routine.tiles[tileIndex]
        val currentGridTile = gridTiles[tileIndex]
        val currentTile = routine.tiles[tileIndex]
        updateUI()

        val currentTimeField = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTime)
        val totalTimeField = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTime)

        startingTime = System.currentTimeMillis()

        var currentTime =
                if (currentTile.mode == Tile.MODE_COUNT_UP) 0L
                else currentTile.countDownSettings.countDownTime
        val totalTimeBuffer = currentTile.totalCountedTime

        currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeInfo).visibility = View.VISIBLE
        totalTimeField.visibility = View.VISIBLE

        val handler = Handler()
        if (currentTile.mode == Tile.MODE_COUNT_UP) {
            handler.post(object : Runnable {
                override fun run() {
                    if (startingTime == null)
                        return

                    currentTime = System.currentTimeMillis() - startingTime!!
                    currentTimeField.text = ResourceClass.millisToHHMMSSmm(currentTime)

                    val totalTime = totalTimeBuffer + currentTime
                    currentTile.totalCountedTime = totalTime
                    totalTimeField.text = ResourceClass.millisToHHMMSSmm(totalTime)

                    handler.postDelayed(this, 10)
                }
            })
        } else {
            val notification = NotificationCompat.Builder(requireContext(), App.TIMING_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(currentTile.name)
                    .setContentText(currentTile.totalCountedTime.toString())
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                    .build()
            val notificationManager = NotificationManagerCompat.from(requireContext())
            notificationManager.notify(1, notification)
            currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTimeInfo).text = getString(R.string.str_tv_viewholder_runTile_currentTimeInfo_countDown)

            currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeInfo).visibility = View.GONE
            totalTimeField.visibility = View.GONE

            countDownTimer = object : CountDownTimer(currentTile.countDownSettings.countDownTime, 10) {
                override fun onTick(millisUntilFinished: Long) {
                    currentTime = System.currentTimeMillis() - startingTime!!
                    val remainingTime = currentTile.countDownSettings.countDownTime - currentTime
                    currentTimeField.text = ResourceClass.millisToHHMMSSmm(remainingTime)
                }

                override fun onFinish() {
                    //reminderForTile(currentTile)
                    currentTile.totalCountedTime += currentTile.countDownSettings.countDownTime
                    toggleTileSize(tileIndex)
                    stopCountingTile(tileIndex)
                }
            }.start()
        }

        val totalTimeMainView = currentGridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeMain)
        totalTimeMainView.startAnimation(ResourceClass.anim.scaleDown)
        totalTimeMainView.visibility = View.GONE
    }

    override fun updateCurrentTile() {
        currentTile = ResourceClass.currentTile
    }
    //endregion

    //region tile size
    private var expandedTile: Int? = null
    private fun toggleTileSize(indexOf: Int) {
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
        }, ResourceClass.anim.scaleDown.duration * (2 / 3))
        hideCard.startAnimation(ResourceClass.anim.scaleDown)

        startCountingTile(indexOf)
    }

    private fun minimizeTile(indexOf: Int, showIndex: Int) {
        val firstCard = gridTiles[indexOf]
        val showCard = gridTiles[showIndex]

        Handler().postDelayed({
            (showCard.parent as View).visibility = View.VISIBLE
            firstCard.findViewById<ConstraintLayout>(R.id.cl_viewholder_runTile_timeLayout).visibility = View.GONE
        }, ResourceClass.anim.scaleUp.duration * (2 / 3))
        showCard.startAnimation(ResourceClass.anim.scaleUp)

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

    private fun navigateToSelectRoutine() {
        ResourceClass.currentTile = null

        val directions = RunContinuousRoutineFragmentDirections.actionRunContinuousRoutineToSelectEditRoutineFragment()

        findNavController().navigate(directions)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_run_continuous_routine, container, false)
    }

    companion object {
        const val UI_INIT_KEY = 69
    }
}