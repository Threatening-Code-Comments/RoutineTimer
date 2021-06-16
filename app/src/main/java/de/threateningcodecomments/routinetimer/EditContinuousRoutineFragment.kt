package de.threateningcodecomments.routinetimer

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import de.threateningcodecomments.accessibility.*
import kotlinx.android.synthetic.main.fragment_edit_continuous_routine.*

class EditContinuousRoutineFragment : Fragment(), UIContainer {
    private lateinit var routineNameField: EditText
    private var isNightMode: Boolean = false
    private lateinit var currentRoutine: Routine

    private lateinit var dragStartView: MaterialCardView
    private var dragStartIndex = -1

    private lateinit var closeView: ShapeableImageView

    private var gridTiles = ArrayList<MaterialCardView>()
    private lateinit var gridLayout: GridLayout

    private val args: EditContinuousRoutineFragmentArgs by navArgs()

    private lateinit var myShadow: View.DragShadowBuilder
    private var animIsDone: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition

        super.onViewCreated(view, savedInstanceState)

        isNightMode = MainActivity.isNightMode
        MainActivity.currentFragment = this

        currentRoutine = ResourceClass.getRoutineFromUid(args.routineUID)
    }

    override fun onStart() {
        super.onStart()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            currentRoutine.lastUsed = System.currentTimeMillis()
            ResourceClass.updateRoutineInDb(currentRoutine)

            val directions = EditContinuousRoutineFragmentDirections.actionEditContinuousRoutineFragmentToSelectRoutineFragment()

            findNavController().navigate(directions)
        }

        initBufferViews()
        initListeners()

        updateUI()
    }

    private fun initListeners() {
        routineNameField.addTextChangedListener {
            if (it.toString() == currentRoutine.name)
                return@addTextChangedListener

            currentRoutine.name = it.toString()
            ResourceClass.updateRoutineInDb(currentRoutine)
        }

        val longClickListener = View.OnLongClickListener {
            val item = ClipData.Item(it.id.toString())
            val dragData = ClipData(
                    it.id.toString(),
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item)

            myShadow = View.DragShadowBuilder(it)

            dragStartView = it as MaterialCardView
            dragStartIndex = gridLayout.indexOfChild(it)
            it.visibility = View.INVISIBLE

            val returnVal = it.startDragAndDrop(dragData, myShadow, null, 0)

            if (!returnVal)
                Toast.makeText(context, "Something went wrong! (#0024)", Toast.LENGTH_SHORT).show()

            returnVal
        }

        val dragListener = View.OnDragListener { v: View?, event: DragEvent? ->
            if (v !is MaterialCardView)
                return@OnDragListener true

            val end = gridLayout.children.indexOf(v)
            val start = gridLayout.children.indexOf(dragStartView)

            if (event != null && dragStartIndex != -1) {
                when (event.action) {
                    DragEvent.ACTION_DRAG_LOCATION -> {
                        rearrangeTiles(start, end)
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        Handler().postDelayed({ dragStartView.isVisible = true }, 50)

                        val updatedDragIndex = gridTiles.indexOf(dragStartView)

                        val tileToMove = currentRoutine.tiles[dragStartIndex]

                        currentRoutine.tiles.remove(tileToMove)
                        currentRoutine.tiles.add(updatedDragIndex, tileToMove)

                        ResourceClass.updateRoutineInDb(currentRoutine)

                        dragStartIndex = -1
                    }
                }
            }

            //return value for listener
            if (event?.action == DragEvent.ACTION_DRAG_ENDED)
                false
            else
                (event?.action == DragEvent.ACTION_DRAG_STARTED) or (event?.action == DragEvent.ACTION_DRAG_LOCATION)
        }

        val onEditClickListener = View.OnClickListener {
            val index = gridTiles.indexOf(it)
            val tile = currentRoutine.tiles[index]

            val directions = EditContinuousRoutineFragmentDirections
                    .actionEditContinuousRoutineFragmentToTileSettingsFragment(currentRoutine.uid, tile.uid)

            findNavController().navigate(directions)
        }

        gridLayout.setOnDragListener(dragListener)
        for (tile in gridTiles) {
            tile.setOnLongClickListener(longClickListener)
            tile.setOnDragListener(dragListener)
            tile.setOnClickListener(onEditClickListener)
        }
    }

    private fun rearrangeTiles(startIndex: Int, endIndex: Int) {
        if (!animIsDone)
            return

        animIsDone = false

        val range = startIndex..endIndex

        //cards need to move left -> -1; right -> 1
        val dir =
                if (range.first < range.last)
                    -1
                else
                    1

        val newGridTiles = ArrayList<MaterialCardView>()
        for ((index, card) in gridLayout.children.withIndex()) {
            //the dragStartView doesn't get added where it was
            card as MaterialCardView

            if (index == range.first)
                continue

            if (dir < 0)
                newGridTiles.add(card)

            //dragStartView gets added where it's supposed to go
            if (index == range.last)
                newGridTiles.add(dragStartView)

            if (dir > 0)
                newGridTiles.add(card)

            if (index in range || index in range.last..range.first) {
                val xVal = getXValue(index, dir, card).toFloat()
                val yVal = getYValue(index, dir, card).toFloat()

                card.animate().translationX(xVal).translationY(yVal).setDuration(300).start()
            }
        }

        Handler().postDelayed({
            gridTiles = newGridTiles

            gridLayout.removeAllViews()
            for (card in newGridTiles)
                card.animate().translationX(0f).translationY(0f).setDuration(0).start()

            for (card in newGridTiles) {
                if (card.parent != null) {
                    (card.parent as ViewGroup).removeView(card)
                    MyLog.d("huh, oh oh")
                }

                gridLayout.addView(card)
            }

            animIsDone = true
        }, 350)
    }

    private fun getYValue(index: Int, dir: Int, card: View) =
            if (dir < 0)
                if (index % 2 == 0)
                    -card.height - card.marginTop * 2
                else
                    0
            else
                if (index % 2 == 0)
                    0
                else
                    card.height + card.marginBottom * 2

    private fun getXValue(index: Int, dir: Int, card: View) =
            if (dir < 0)
                if (index % 2 == 0)
                    card.width + card.marginLeft * 2
                else
                    -card.width - card.marginLeft * 2
            else
                if (index % 2 == 0)
                    card.width + card.marginLeft * 2
                else
                    -card.width - card.marginLeft * 2

    override fun updateUI() {
        for ((index, grTile) in gridTiles.withIndex()) {
            val tile = currentRoutine.tiles[index]

            grTile.cardElevation =
                    if (tile != Tile.DEFAULT_TILE)
                        5f
                    else
                        0f

            val bgColor =
                    if (tile != Tile.DEFAULT_TILE)
                        tile.backgroundColor
                    else
                        Color.TRANSPARENT
            val contrastColor =
                    if (tile != Tile.DEFAULT_TILE)
                        ResourceClass.Conversions.Colors.calculateContrast(bgColor)
                    else
                        ResourceClass.Resources.Colors.contrastColor

            grTile.setCardBackgroundColor(bgColor)

            val nameView = grTile.findViewById<TextView>(R.id.tv_viewholder_smallTile_name)
            nameView.text =
                    if (tile != Tile.DEFAULT_TILE)
                        tile.name
                    else
                        "Add Tile"
            nameView.setTextColor(contrastColor)

            val iconView = grTile.findViewById<ShapeableImageView>(R.id.iv_viewholder_smallTile_icon)
            val drawable =
                    if (tile != Tile.DEFAULT_TILE)
                        ResourceClass.getIconDrawable(tile)
                    else
                        ResourceClass.Resources.getDrawable(R.drawable.ic_add)

            iconView.setImageDrawable(drawable)
            iconView.setColorFilter(contrastColor)
        }
    }

    private fun initBufferViews() {
        routineNameField = et_EditRoutine_continuous_routineName
        routineNameField.setText(currentRoutine.name)

        gridTiles.add(tile_EditRoutine_continuous_0 as MaterialCardView)
        gridTiles.add(tile_EditRoutine_continuous_1 as MaterialCardView)
        gridTiles.add(tile_EditRoutine_continuous_2 as MaterialCardView)
        gridTiles.add(tile_EditRoutine_continuous_3 as MaterialCardView)
        gridTiles.add(tile_EditRoutine_continuous_4 as MaterialCardView)
        gridTiles.add(tile_EditRoutine_continuous_5 as MaterialCardView)
        gridTiles.add(tile_EditRoutine_continuous_6 as MaterialCardView)
        gridTiles.add(tile_EditRoutine_continuous_7 as MaterialCardView)

        gridLayout = gl_EditRoutine_continuous_tileLayout
    }

    private fun goToSelectRoutine() {
        val directions: NavDirections = EditContinuousRoutineFragmentDirections.actionEditContinuousRoutineFragmentToSelectRoutineFragment()

        findNavController().navigate(directions)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_continuous_routine, container, false)
    }

    override fun updateCurrentTile() {}

    companion object {
        private const val REFLOW_DIR_DOWN = -1
        private const val REFLOW_DIR_UP = 1
    }
}