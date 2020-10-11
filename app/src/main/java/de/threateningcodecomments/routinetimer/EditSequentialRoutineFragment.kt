package de.threateningcodecomments.routinetimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView

class EditSequentialRoutineFragment : Fragment() {
    private lateinit var tileCardView: MaterialCardView
    private lateinit var tileIconView: ShapeableImageView
    private lateinit var tileNameView: TextView

    private lateinit var routineNameView: EditText

    var routines: ArrayList<Routine>? = null
    lateinit var currentRoutine: Routine
    val args: EditSequentialRoutineFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = ResourceClass.sharedElementTransition
        //postponeEnterTransition()
        super.onViewCreated(view, savedInstanceState)

        initBufferViews()

        initRoutines()

        updateRoutineNameHint()

        updateCard(currentRoutine.tiles!![0])

        //startPostponedEnterTransition()
    }

    private fun updateRoutineNameHint() {
        routineNameView.setText(currentRoutine.name)
    }

    private fun updateCard(tile: Tile) {
        val tileName = tile.name
        tileNameView.text = tileName
        tileNameView.setTextColor(tile.contrastColor)

        val icon = ResourceClass.getIconDrawable(tile)
        tileIconView.setImageDrawable(icon)
        tileIconView.setColorFilter(tile.contrastColor)

        val color = tile.backgroundColor
        tileCardView.setCardBackgroundColor(color)
    }

    private fun initRoutines() {
        routines = ResourceClass.getRoutines()
        if (routines == null) {
            Toast.makeText(context, "Oh no, routines are null. Good bye.", Toast.LENGTH_LONG).show()
        }
        val position = args.routinePosition
        currentRoutine = routines!![position]
    }

    private fun initBufferViews() {
        val v = requireView()

        tileCardView = v.findViewById(R.id.cv_EditRoutine_sequential_card)
        tileIconView = v.findViewById(R.id.iv_EditRoutine_sequential_icon)
        tileNameView = v.findViewById(R.id.tv_EditRoutine_sequential_name)

        routineNameView = v.findViewById(R.id.et_EditRoutine_sequential_routineName)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_sequential_routine, container, false)
    }

    companion object {
    }

}