package de.threateningcodecomments.routinetimer

import accessibility.ResourceClass
import accessibility.Routine
import accessibility.Tile
import accessibility.UIContainer
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

class RunContinuousRoutineFragment : Fragment(), View.OnClickListener, UIContainer {
    private var gridTiles: ArrayList<MaterialCardView> = ArrayList()
    private var gridRows: ArrayList<ConstraintLayout> = ArrayList()

    private lateinit var closeView: ShapeableImageView
    private lateinit var routineNameView: MaterialTextView

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
            if (gridTiles.contains(v as MaterialCardView)) {
                toggleTileSize(gridTiles.indexOf(v as MaterialCardView))
            }
        }
        when (v!!.id) {
            R.id.iv_RunRoutine_continuous_close -> {
                navigateToSelectRoutine()
            }
        }
    }

    override fun updateUI() {
        updateUI(null)
    }

    private fun updateUI(tileIndex: Int?) {
        routineNameView.text = routine.name

        val tiles = routine.tiles
        for ((gridTileIndex, gridTile) in gridTiles.withIndex()) {
            val tile = tiles[gridTileIndex]
            val nameView = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_name)
            val imageView = gridTile.findViewById<ShapeableImageView>(R.id.iv_viewholder_runTile_icon)
            val currentTimeInfo = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTimeInfo)
            val currentTimeDisplay = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_currentTime)
            val totalTimeInfo = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTimeInfo)
            val totalTimeDisplay = gridTile.findViewById<MaterialTextView>(R.id.tv_viewholder_runTile_totalTime)

            if (tile != Tile.ERROR_TILE) {
                gridTile.visibility = View.VISIBLE

                val oldColor: Int
                val newColor =
                        if (tileIndex == null || gridTileIndex == tileIndex) {
                            oldColor = Color.GRAY
                            tile.backgroundColor
                        } else {
                            oldColor = tile.backgroundColor
                            Color.GRAY
                        }
                val contrastColor =
                        if (tileIndex == null || gridTileIndex == tileIndex) tile.contrastColor
                        else Color.WHITE

                animateColor(gridTile, oldColor, newColor)

                nameView.setTextColor(contrastColor)
                nameView.text = tile.name
                imageView.setColorFilter(contrastColor)
                imageView.setImageDrawable(ResourceClass.getIconDrawable(tile))

                animateColor(currentTimeInfo, oldColor, newColor)
                animateColor(currentTimeDisplay, oldColor, newColor)
                currentTimeDisplay.text = tile.countedTime.toString()

                animateColor(totalTimeInfo, oldColor, newColor)
                animateColor(totalTimeDisplay, oldColor, newColor)
                totalTimeDisplay.text = tile.totalCountedTime.toString()
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

    //region timing
    private fun stopCountingTile(tileIndex: Int) {
        updateUI()
    }

    private fun startCountingTile(tileIndex: Int) {
        updateUI(tileIndex)
    }
    //endregion

    //region tile size
    private var expandedTile: Int? = null
    private fun toggleTileSize(indexOf: Int) {
        val tilePosition = getTilePosition(indexOf)
        val nextTile = if (tilePosition == 0) indexOf + 1 else indexOf - 1

        if (expandedTile == null) {
            expandTile(indexOf, tilePosition, nextTile)
            expandedTile = indexOf
        } else if (expandedTile == indexOf) {
            minimizeTile(indexOf, tilePosition, nextTile)
            expandedTile = null
        }
    }

    private fun expandTile(indexOf: Int, tilePosition: Int, hideIndex: Int) {
        val firstCard = gridTiles[indexOf]
        val hideCard = gridTiles[hideIndex]

        Handler().postDelayed({
            (hideCard.parent as View).visibility = View.GONE
            firstCard.findViewById<ConstraintLayout>(R.id.cl_viewholder_runTile_countdownRoot).visibility = View.VISIBLE
        }, ResourceClass.scaleDown.duration * (2 / 3))
        hideCard.startAnimation(ResourceClass.scaleDown)

        startCountingTile(indexOf)
    }

    private fun minimizeTile(indexOf: Int, tilePosition: Int, showIndex: Int) {
        val firstCard = gridTiles[indexOf]
        val showCard = gridTiles[showIndex]

        Handler().postDelayed({
            (showCard.parent as View).visibility = View.VISIBLE
            firstCard.findViewById<ConstraintLayout>(R.id.cl_viewholder_runTile_countdownRoot).visibility = View.GONE
        }, ResourceClass.scaleUp.duration * (2 / 3))
        showCard.startAnimation(ResourceClass.scaleUp)

        stopCountingTile(indexOf)
    }

    private fun getTilePosition(tileIndex: Int): Int {
        return if (tileIndex % 2 != 0) 1 else 0
    }

    private fun getTileRow(tileIndex: Int): Int {
        val compareCard = gridTiles[tileIndex]

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
        val directions = RunContinuousRoutineFragmentDirections.actionRunContinuousRoutineToSelectEditRoutineFragment()

        findNavController().navigate(directions)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_run_continuous_routine, container, false)
    }
}