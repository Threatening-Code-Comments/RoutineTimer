package de.threateningcodecomments.routinetimer

import accessibility.MyLog
import accessibility.ResourceClass
import accessibility.Routine
import accessibility.Tile
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView


class EditContinuousRoutineFragment : Fragment(), View.OnClickListener {
    private var isNightMode: Boolean = false
    private lateinit var currentRoutine: Routine
    private lateinit var tiles: ArrayList<Tile>

    private lateinit var closeView: ShapeableImageView

    private lateinit var gridLayout: GridLayout
    private var gridTiles: ArrayList<MaterialCardView> = ArrayList()

    private val args: EditContinuousRoutineFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition

        super.onViewCreated(view, savedInstanceState)

        isNightMode = ResourceClass.isNightMode(requireActivity().application)

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

    private fun goToSelectRoutine() {
        val directions: NavDirections = EditContinuousRoutineFragmentDirections.actionEditContinuousRoutineFragmentToSelectRoutineFragment()

        findNavController().navigate(directions)
    }

    private fun initListeners() {
        closeView.setOnClickListener(this)

        for ((index, tileView) in gridTiles.withIndex()) {
            tileView.setOnClickListener {
                handleTileClick(index)
            }
        }
    }

    private fun handleTileClick(index: Int) {
        val clickedTile = tiles[index]

        if (clickedTile == Tile.ERROR_TILE) {
            createTile(index)
        }
    }

    private fun createTile(index: Int) {
        tiles.set(index, Tile.DEFAULT_TILE)
        updateUI()
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
    }

    private fun updateUI() {
        for ((index, tileView) in gridTiles.withIndex()) {
            if (index >= tiles.size)
                tiles.add(Tile.ERROR_TILE)

            val iv = tileView.findViewById<ShapeableImageView>(R.id.iv_viewholder_smallTile_icon)
            val tv = tileView.findViewById<TextView>(R.id.tv_viewholder_smallTile_name)
            val cv = tileView.findViewById<MaterialCardView>(R.id.cv_viewholder_smallTile_card)
            val ll = tileView.findViewById<LinearLayout>(R.id.ll_viewholder_smallTile_contentLayout)
            val tmpTile = tiles[index]

            MyLog.d(tmpTile.name)

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

                tv.text = "Add Tile!"
            } else {
                ll.background = null

                val bgColor = tmpTile.backgroundColor
                cv.setCardBackgroundColor(bgColor)

                val drawable = ResourceClass.getIconDrawable(tmpTile)
                iv.setImageDrawable(drawable)
                iv.setColorFilter(tmpTile.contrastColor)

                tv.text = tmpTile.name
                tv.setTextColor(tmpTile.contrastColor)
            }
        }

        saveRoutine()
    }

    private fun saveRoutine() {
        ResourceClass.saveRoutine(currentRoutine)
    }

    private fun initBufferViews() {
        val v: View = requireView()

        closeView = v.findViewById(R.id.iv_EditRoutine_continuous_close)

        gridLayout = v.findViewById(R.id.gl_editRoutine_continuous_tiles)
        for (cardView in gridLayout.children) {
            gridTiles.add((cardView as ViewGroup)[0] as MaterialCardView)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_continuous_routine, container, false)
    }

    companion object {

    }
}