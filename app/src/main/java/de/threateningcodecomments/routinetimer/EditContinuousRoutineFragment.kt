package de.threateningcodecomments.routinetimer

import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.core.animation.doOnEnd
import androidx.core.view.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputLayout
import de.threateningcodecomments.accessibility.*
import de.threateningcodecomments.routinetimer.databinding.FragmentEditContinuousRoutineBinding

class EditContinuousRoutineFragment : Fragment(), UIContainer {
    private var isNightMode: Boolean = false
    private lateinit var currentRoutine: Routine

    private lateinit var dragStartView: MaterialCardView
    private var dragStartIndex = -1

    private lateinit var routineNameField: EditText
    private lateinit var routineNameLayout: TextInputLayout

    private var gridTiles = ArrayList<MaterialCardView>()
    private lateinit var gridLayout: GridLayout

    private lateinit var deleteView: FrameLayout
    private var deleteViewCanBeUpdated = true
    private lateinit var deleteViewTextView: TextView
    private lateinit var deleteViewImageView: ImageView

    private val args: EditContinuousRoutineFragmentArgs by navArgs()

    private lateinit var myShadow: View.DragShadowBuilder
    private var animIsDone: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = RC.Resources.sharedElementTransition

        super.onViewCreated(view, savedInstanceState)

        isNightMode = RC.isNightMode

        currentRoutine = RC.RoutinesAndTiles.getRoutineFromUid(args.routineUID)
    }

    override fun onStart() {
        super.onStart()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            currentRoutine.lastUsed = System.currentTimeMillis()
            RC.Db.updateRoutineInDb(currentRoutine)

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
            RC.Db.updateRoutineInDb(currentRoutine)
        }

        val dragListener = View.OnDragListener { v: View?, event: DragEvent? ->
            if (v is GridLayout && event?.action != DragEvent.ACTION_DROP)
                return@OnDragListener true

            MyLog.d("Dragevent -> ${v.toString().split("app:id/")[1]} |||  ${event?.action}")

            val end = gridLayout.children.indexOf(v)
            val start = gridLayout.children.indexOf(dragStartView)

            if (event != null && dragStartIndex != -1) {
                when (event.action) {
                    DragEvent.ACTION_DRAG_LOCATION -> {
                        if (v is MaterialCardView) {
                            updateDeleteView(false)
                            rearrangeTiles(start, end)
                        } else {
                            val newStart = gridLayout.indexOfChild(dragStartView)
                            val newEnd = dragStartIndex

                            if (newStart != dragStartIndex)
                                rearrangeTiles(newStart, newEnd)
                            updateDeleteView(true)
                        }
                    }
                    DragEvent.ACTION_DROP -> {
                        deleteView.isVisible = false
                        routineNameLayout.visibility = View.VISIBLE

                        val updatedDragIndex = gridTiles.indexOf(dragStartView)

                        val tileToMove = currentRoutine.tiles[dragStartIndex]

                        currentRoutine.tiles.remove(tileToMove)

                        val targetIsDelete =
                                v is FrameLayout ||
                                        v is TextView ||
                                        v is ImageView

                        //handle if this event was from the deleteView
                        val tileToAdd =
                                if (targetIsDelete)
                                    Tile.DEFAULT_TILE
                                else
                                    tileToMove
                        currentRoutine.tiles.add(updatedDragIndex, tileToAdd)

                        RC.Db.updateRoutineInDb(currentRoutine)

                        dragStartIndex = -1

                        Handler().postDelayed({
                            dragStartView.isVisible = true
                        }, 50)

                        //handle deleting of tile
                        if (v is FrameLayout || v is TextView || v is ImageView)
                            updateUI()
                    }
                }
            }

            //return value for listener
            /*when {
                //if it's deleteView
                v is FrameLayout || v is TextView || v is ImageView ->
                    true
                //i do not know
                event?.action == DragEvent.ACTION_DRAG_ENDED ->
                    false
                else ->
                    (event?.action == DragEvent.ACTION_DRAG_STARTED) or (event?.action == DragEvent.ACTION_DRAG_LOCATION)
            }*/
            true
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

            deleteView.isVisible = true
            routineNameLayout.visibility = View.INVISIBLE

            val returnVal = it.startDragAndDrop(dragData, myShadow, null, 0)

            if (!returnVal)
                Toast.makeText(context, "Something went wrong! (#0024)", Toast.LENGTH_SHORT).show()

            returnVal
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
        deleteView.setOnDragListener(dragListener)
        deleteViewTextView.setOnDragListener(dragListener)
        deleteViewImageView.setOnDragListener(dragListener)
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
        //updating tiles
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
                        RC.Conversions.Colors.calculateContrast(bgColor)
                    else
                        RC.Resources.Colors.contrastColor

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
                        RC.getIconDrawable(tile)
                    else
                        RC.Resources.getDrawable(R.drawable.ic_add)

            iconView.setImageDrawable(drawable)
            iconView.setColorFilter(contrastColor)
        }

        updateDeleteView(false)
    }

    private fun updateDeleteView(hovered: Boolean) {
        if (!deleteViewCanBeUpdated)
            return

        deleteViewCanBeUpdated = false

        //animate view
        val currentHeightValue = RC.Conversions.Size.pxToDp(deleteView.height)
        val heightValue =
                if (hovered)
                    200f
                else
                    100f

        ValueAnimator.ofFloat(currentHeightValue.toFloat(), heightValue).apply {
            duration = 500

            addUpdateListener {
                deleteView.updateLayoutParams {
                    val pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, it.animatedValue as Float,
                            context?.resources?.displayMetrics)
                    height = pixels.toInt()
                }
            }
            doOnEnd {
                deleteViewCanBeUpdated = true
            }

            start()
        }

        //updating deleteView
        val deleteColor = RC.Resources.Colors.cancelColor

        val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(deleteColor,
                        Color.TRANSPARENT)
        )
        gradientDrawable.cornerRadius = 0f;

        deleteView.background = gradientDrawable;

        val contrastColor = RC.Resources.Colors.extremeContrastColor

        deleteViewTextView.setTextColor(contrastColor)
        deleteViewImageView.setColorFilter(contrastColor)
    }

    private var _binding: FragmentEditContinuousRoutineBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentEditContinuousRoutineBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    private fun initBufferViews() {
        routineNameField = binding.etEditRoutineContinuousRoutineName
        routineNameLayout = binding.tilEditRoutineContinuousRoutineName
        routineNameField.setText(currentRoutine.name)

        gridTiles.add(binding.tileEditRoutineContinuous0.root)
        gridTiles.add(binding.tileEditRoutineContinuous1.root)
        gridTiles.add(binding.tileEditRoutineContinuous2.root)
        gridTiles.add(binding.tileEditRoutineContinuous3.root)
        gridTiles.add(binding.tileEditRoutineContinuous4.root)
        gridTiles.add(binding.tileEditRoutineContinuous5.root)
        gridTiles.add(binding.tileEditRoutineContinuous6.root)
        gridTiles.add(binding.tileEditRoutineContinuous7.root)

        gridLayout = binding.glEditRoutineContinuousTileLayout

        deleteView = binding.flEditRoutineContinuousDeleteView
        deleteViewTextView = binding.tvEditRoutineContinuousDeleteViewText
        deleteViewImageView = binding.ivEditRoutineContinuousDeleteViewIcon
    }

    private fun goToSelectRoutine() {
        val directions: NavDirections = EditContinuousRoutineFragmentDirections.actionEditContinuousRoutineFragmentToSelectRoutineFragment()

        findNavController().navigate(directions)
    }

    override fun updateCurrentTile() {}

    companion object {
        private const val REFLOW_DIR_DOWN = -1
        private const val REFLOW_DIR_UP = 1
    }
}