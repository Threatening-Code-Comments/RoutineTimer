package de.threateningcodecomments.routinetimer

import accessibility.ResourceClass
import accessibility.Routine
import accessibility.Tile
import accessibility.UIContainer
import android.animation.LayoutTransition
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.contains
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView


class EditContinuousRoutineFragment : Fragment(), View.OnClickListener, UIContainer {
    private var isNightMode: Boolean = false
    private lateinit var currentRoutine: Routine
    private lateinit var tiles: ArrayList<Tile>

    private lateinit var root: ConstraintLayout
    private lateinit var closeView: ShapeableImageView

    private lateinit var tilesMainLayout: LinearLayout
    private var tileRows: ArrayList<LinearLayout> = ArrayList()
    private var gridTiles: ArrayList<MaterialCardView> = ArrayList()

    private val args: EditContinuousRoutineFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition

        super.onViewCreated(view, savedInstanceState)

        isNightMode = ResourceClass.isNightMode(requireActivity().application)
        MainActivity.currentFragment = this

        initBufferViews()

        initRoutine()

        updateUI()

        initListeners()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iv_EditRoutine_continuous_close ->
                goToSelectRoutine()
        }
    }

    //region tile expanding
    private fun handleTileClick(cv: MaterialCardView) {
        val index = gridTiles.indexOf(cv)
        val clickedTile = tiles[index]

        if (clickedTile == Tile.ERROR_TILE) {
            createTile(index)
        } else {
            toggleTileSize(index)
        }
    }

    private var isExpanded: Boolean = false
    private var expandedTile: Int? = null
    private fun toggleTileSize(index: Int) {
        if (!isExpanded) {
            if (expandedTile != null) {
                return
            }
            expandTile(index)
            expandedTile = index
            isExpanded = true
        } else {
            if (expandedTile != index) {
                return
            }
            minimizeTile(index)
            expandedTile = null
            isExpanded = false
        }
    }

    private fun minimizeTile(index: Int) {
        val populatedRow = findRowWithChildCount(3)
        val previousTilePosition: Int
        val nextIndex = if (getTileRow(index) < populatedRow) {
            index + 1
        } else {
            index - 1
        }

        previousTilePosition = getTilePosition(nextIndex)

        val cv = gridTiles[index]
        if (previousTilePosition == 0) {
            cv.startAnimation(ResourceClass.collapseTileLeft)
        } else {
            cv.startAnimation(ResourceClass.collapseTileRight)
        }
        Handler().postDelayed({
            cv.findViewById<ConstraintLayout>(R.id.cl_viewholder_tile_countdownRoot).visibility = View.GONE
        }, ResourceClass.collapseTileLeft.duration / 2)

        Handler().postDelayed({
            moveTile(nextIndex)
        }, ResourceClass.collapseTileLeft.duration)
        /*Collections.swap(gridTiles, index, nextIndex)
        Collections.swap(tiles, index, nextIndex)*/
    }

    private fun expandTile(index: Int) {
        val cv = gridTiles[index]
        val lastCvPos = getTilePosition(index)
        val nextIndex: Int = if (getTilePosition(index) == 0) {
            index + 1
        } else {
            index - 1
        }
        moveTile(nextIndex)

        if (lastCvPos == 0) {
            cv.startAnimation(ResourceClass.expandTileLeft)
            //cv.findViewById<EditText>(R.id.tv_viewholder_smallTile_name).startAnimation(ResourceClass.collapseTileRight)
            //cv.findViewById<ShapeableImageView>(R.id.iv_viewholder_smallTile_icon).startAnimation(ResourceClass.collapseTileRight)
        } else {
            cv.findViewById<LinearLayout>(R.id.ll_viewholder_smallTile_cardContent).startAnimation(ResourceClass.expandTileRight)
        }
        Handler().postDelayed({
            cv.findViewById<ConstraintLayout>(R.id.cl_viewholder_tile_countdownRoot).visibility = View.VISIBLE
        }, ResourceClass.expandTileRight.duration / 2)
        /*Collections.swap(gridTiles, index, nextIndex)
        Collections.swap(tiles, index, nextIndex)*/
    }

    private fun moveTile(index: Int) {
        val cv = gridTiles[index]
        val cvParent = cv.parent as FrameLayout
        val row: Int = getTileRow(index)
        val cvPosition: Int = getTilePosition(index)

        if (index == 0 || index == 7) {
            cvParent.startAnimation(ResourceClass.scaleDown)
            cvParent.visibility = View.GONE
            tileRows[row].removeView(cvParent)
            return
        }
        if (index == 1 || index == 6) {
            cvParent.startAnimation(ResourceClass.scaleUp)
            cvParent.visibility = View.VISIBLE
            val nextIndex = if (index == 0) 1 else 6
            tileRows[row].removeView(gridTiles[nextIndex].parent as View)
            tileRows[row].addView(cvParent)
            return
        }

        var nextRow: Int = 0
        if (isExpanded) {
            nextRow = findRowWithChildCount(1)
        } else {
            nextRow = if (cvPosition == 0) row - 1 else row + 1
        }
        val rowView = tileRows[row]
        val nextRowView = tileRows[nextRow]

        rowView.removeView(cvParent)
        nextRowView.addView(cvParent)

        val previousViews = nextRowView.children.toList()
        if (cvPosition != 0) {
            for ((previousViewIndex, previousView) in previousViews.withIndex()) {
                if (previousView != cvParent) {
                    nextRowView.removeView(previousView)
                    nextRowView.addView(previousView)
                }
            }
        }
    }
    //endregion

    //region tile searches
    private fun findRowWithChildCount(i: Int): Int {
        for (row in tileRows) {
            if (row.childCount == i) {
                return tileRows.indexOf(row)
            }
        }
        return -1
    }

    private fun getTileRow(index: Int): Int {
        val cv = gridTiles[index]
        var row: Int = 0
        for ((rowIndex, currentRow) in tileRows.withIndex()) {
            if (currentRow.contains(cv.parent as View)) {
                row = rowIndex
                break
            }
        }
        return row
    }

    private fun getTilePosition(index: Int): Int {
        val cv = gridTiles[index]
        var cvPosition: Int = 0
        for (currentRow in tileRows) {
            if (currentRow.contains(cv.parent as View)) {
                cvPosition = currentRow.indexOfChild(cv.parent as View)
                break
            }
        }

        return cvPosition
    }

    private fun createTile(index: Int) {
        tiles[index] = Tile.DEFAULT_TILE
        updateUI()
    }
    //endregion

    //region UI and routines
    override fun updateUI() {
        for ((index, tileView) in gridTiles.withIndex()) {
            if (index >= tiles.size)
                tiles.add(Tile.ERROR_TILE)

            val iv = tileView.findViewById<ShapeableImageView>(R.id.iv_viewholder_smallTile_icon)
            val et = tileView.findViewById<TextView>(R.id.tv_viewholder_smallTile_name)
            val cv = tileView.findViewById<MaterialCardView>(R.id.cv_viewholder_smallTile_card)
            val ll = tileView.findViewById<LinearLayout>(R.id.ll_viewholder_smallTile_infoLayout)
            val tmpTile = tiles[index]

            if (tiles[index] == Tile.ERROR_TILE) {
                val shapeDrawable: Drawable
                if (isNightMode)
                    shapeDrawable = ResourcesCompat.getDrawable(resources, R.drawable.white_dashed_outline, null)!!
                else {
                    shapeDrawable = ResourcesCompat.getDrawable(resources, R.drawable.black_dashed_outline, null)!!
                }
                ll.background = shapeDrawable

                iv.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_add, null)!!)
                iv.setColorFilter(if (isNightMode) Color.WHITE else Color.BLACK)

                et.text = "Add Tile!"
                et.inputType = InputType.TYPE_NULL
                et.isCursorVisible = false
            } else {
                ll.background = null

                val bgColor = tmpTile.backgroundColor
                cv.setCardBackgroundColor(bgColor)

                val drawable = ResourceClass.getIconDrawable(tmpTile)
                iv.setImageDrawable(drawable)
                iv.setColorFilter(tmpTile.contrastColor)

                et.text = tmpTile.name
                et.setTextColor(tmpTile.contrastColor)
                et.inputType = InputType.TYPE_CLASS_TEXT
                et.isCursorVisible = true
            }
        }

        saveRoutine()
    }

    private fun saveRoutine() {
        ResourceClass.saveRoutine(currentRoutine)
    }
    //endregion

    //region init
    private fun initBufferViews() {
        val v: View = requireView()

        root = v.findViewById(R.id.cl_EditRoutine_continuous_root)
        root.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        closeView = v.findViewById(R.id.iv_EditRoutine_continuous_close)

        tilesMainLayout = v.findViewById(R.id.ll_EditRoutine_continuous_main)
        tileRows.add(v.findViewById(R.id.ll_EditRoutine_continuous_firstRow))
        tileRows.add(v.findViewById(R.id.ll_EditRoutine_continuous_secondRow))
        tileRows.add(v.findViewById(R.id.ll_EditRoutine_continuous_thirdRow))
        tileRows.add(v.findViewById(R.id.ll_EditRoutine_continuous_fourthRow))

        for (row in tileRows) {
            for (child in row.children) {
                val frameLayout = child as FrameLayout
                val cardView = frameLayout[0] as MaterialCardView
                gridTiles.add(cardView)
            }
        }
    }

    private fun initRoutine() {
        val routines = ResourceClass.getRoutines()
        for (routine in routines) {
            if (routine.uid == args.routineUID) {
                currentRoutine = routine
                break
            }
        }
        tiles = currentRoutine.tiles
        currentRoutine.lastUsed = System.currentTimeMillis()
    }

    private fun initListeners() {
        closeView.setOnClickListener(this)

        for ((index, tileView) in gridTiles.withIndex()) {
            tileView.setOnClickListener {
                handleTileClick(it as MaterialCardView)
            }
        }
    }
    //endregion

    private fun goToSelectRoutine() {
        val directions: NavDirections = EditContinuousRoutineFragmentDirections.actionEditContinuousRoutineFragmentToSelectRoutineFragment()

        findNavController().navigate(directions)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_continuous_routine, container, false)
    }

    companion object {

    }
}