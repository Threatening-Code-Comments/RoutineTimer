package de.threateningcodecomments.routinetimer

import android.animation.LayoutTransition
import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.os.Handler
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import de.threateningcodecomments.accessibility.MyLog
import de.threateningcodecomments.accessibility.ResourceClass
import de.threateningcodecomments.accessibility.Routine
import de.threateningcodecomments.accessibility.UIContainer
import kotlinx.android.synthetic.main.fragment_edit_continuous_routine.*

class EditContinuousRoutineFragment : Fragment(), UIContainer {
    private var isNightMode: Boolean = false
    private lateinit var currentRoutine: Routine

    private lateinit var dragStartView: MaterialCardView
    private var lastDragIndex = -1

    private lateinit var closeView: ShapeableImageView

    private var gridTiles = ArrayList<MaterialCardView>()
    private lateinit var gridLayout: GridLayout

    private val args: EditContinuousRoutineFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition

        super.onViewCreated(view, savedInstanceState)

        isNightMode = MainActivity.isNightMode
        MainActivity.currentFragment = this

        currentRoutine = ResourceClass.getRoutineFromUid(args.routineUID)
    }

    override fun onStart() {
        super.onStart()

        initBufferViews()
        initListeners()

        updateUI()
    }

    private fun initListeners() {
        val longClickListener = View.OnLongClickListener {

            val item = ClipData.Item(it.id.toString())
            val dragData = ClipData(
                    it.id.toString(),
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item)

            val myShadow = View.DragShadowBuilder(it)

            MyLog.d("starting drag at ${gridTiles.indexOf(it)}!")

            dragStartView = it as MaterialCardView
            it.visibility = View.INVISIBLE

            val returnVal = it.startDragAndDrop(dragData, myShadow, null, 0)

            (dragStartView.parent as GridLayout).removeView(dragStartView)

            returnVal
        }
        val dragListener = View.OnDragListener { v: View?, event: DragEvent? ->

            gridLayout.removeView(dragStartView)
            gridLayout.clearAnimation()

            val index = gridLayout.children.indexOf(v)

            if (event != null) {
                when (event.action) {
                    DragEvent.ACTION_DRAG_LOCATION -> {
                        if (index != lastDragIndex) {
                            MyLog.d("Location on $index")
                            lastDragIndex = index

                            val dropAtIndex =
                                    if (index - 1 == -1)
                                        index - 1
                                    //0
                                    else if (index <= 3)
                                        index - 1
                                    else
                                        index

                            val listener = object : LayoutTransition.TransitionListener {
                                override fun startTransition(transition: LayoutTransition?, container: ViewGroup?, view: View?, transitionType: Int) {
                                    MyLog.d("start transition")
                                }

                                override fun endTransition(transition: LayoutTransition?, container: ViewGroup?, view: View?, transitionType: Int) {

                                    gridLayout.addView(dragStartView, dropAtIndex)
                                    MyLog.d("adding view to $dropAtIndex")

                                    gridLayout.layoutTransition.removeTransitionListener(this)
                                }
                            }

                            if (!gridLayout.layoutTransition.transitionListeners.contains(listener)) {
                                MyLog.d("adding listener at $lastDragIndex")
                                Handler().postDelayed({

                                    for (transitionListener in gridLayout.layoutTransition.transitionListeners) {
                                        gridLayout.layoutTransition.removeTransitionListener(transitionListener)
                                    }

                                    gridLayout.layoutTransition.addTransitionListener(listener)
                                }, 1000)
                            }

                            /*gridLayout.addView(dragStartView, dropAtIndex)*/
                        }
                    }
                    DragEvent.ACTION_DROP -> {
                        MyLog.d("       Dropped on $index")
                        dragStartView.visibility = View.VISIBLE
                        lastDragIndex = -1
                    }
                }
            }

            event != null
        }

        for (gridTile in gridTiles) {
            gridTile.setOnLongClickListener(longClickListener)
            gridTile.setOnDragListener(dragListener)
        }
    }

    /*Legacy reflowing

    private fun reflow(end: Int) {
        val start = gridTiles.indexOf(dragStartView)


        var text = "reflow: $start to $end| "
        if (start < end)
            for (i in start + 1..end - 1) {
                text += ", $i"
                reflowElement(i, REFLOW_DIR_DOWN)
            }
        else
            for (i in start - 1 downTo end - 1) {
                text += "; $i"

                reflowElement(i, REFLOW_DIR_UP)
            }
        MyLog.d(text)

    }
    private fun reflowElement(i: Int, direction: Int) {
        val element = gridTiles[i]
        val parent = element.parent

        val elementRow = gridTileRows.indexOf(parent)

        //if the entity doesn't have a parent, disregard
        parent ?: return
        parent as LinearLayout

        //if the element is alone in its parent container, ignore
        if (parent.childCount == 1)
            return
        //else reflow element
        val newRowIndex = if (direction == REFLOW_DIR_DOWN) {
            elementRow - 1
        } else {
            elementRow + 1
        }

        val newRow = gridTileRows[newRowIndex]

        MyLog.d("reflowing $i from $elementRow to $newRowIndex")

        //removing view from parent
        parent.removeView(element)

        //waiting for layout animation to finish and then adding the view
        val transitionListener = object : LayoutTransition.TransitionListener {
            override fun startTransition(transition: LayoutTransition?, container: ViewGroup?, view: View?, transitionType: Int) {}
            override fun endTransition(transition: LayoutTransition?, container: ViewGroup?, view: View?, transitionType: Int) {
                MyLog.d("removing $i")
                // now you can add the same view to another parent
                newRow.addView(view)

                parent.layoutTransition.removeTransitionListener(this)
            }
        }

        val layoutTransition = parent.layoutTransition
        layoutTransition.addTransitionListener(transitionListener)
    }*/

    override fun updateUI() {
        for ((index, grTile) in gridTiles.withIndex()) {
            val tile = currentRoutine.tiles[index]
            grTile.setCardBackgroundColor(tile.backgroundColor)
        }
    }

    private fun initBufferViews() {
        closeView = iv_EditRoutine_continuous_close

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