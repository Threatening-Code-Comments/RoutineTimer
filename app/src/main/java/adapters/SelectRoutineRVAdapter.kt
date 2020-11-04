package adapters

import accessibility.ResourceClass
import accessibility.Routine
import accessibility.Tile
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import de.threateningcodecomments.routinetimer.R
import de.threateningcodecomments.routinetimer.SelectRoutineFragment


internal class SelectRoutineRVAdapter(routines: ArrayList<Routine>?) : RecyclerView.Adapter<MyViewHolder>() {

    private var routines: ArrayList<Routine>? = null

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_viewholder, parent, false)
        return MyViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val isNightMode = ResourceClass.wasNightMode()
        val tmpRoutine = routines!![position]
        val firstTile = tmpRoutine.tiles[0]

        tmpRoutine.setAccessibility(isNightMode)

        val uid = tmpRoutine.uid

        var name = tmpRoutine.name
        if (tmpRoutine == Routine.ERROR_ROUTINE) {
            name = "Nothing here yet!"
        }
        holder.nameView.text = name
        if (isNightMode) holder.nameView.setTextColor(Color.WHITE) else holder.nameView.setTextColor(Color.BLACK)

        val mode = when {
            tmpRoutine == Routine.ERROR_ROUTINE -> ""
            tmpRoutine.mode == Routine.MODE_CONTINUOUS -> Routine.CONTINUOUS_MESSAGE
            tmpRoutine.mode == Routine.MODE_SEQUENTIAL -> Routine.SEQUENTIAL_MESSAGE
            else -> "the mode couldn't be loaded"
        }
        holder.modeView.text = mode

        //initializes tile buffer to restore order of tiles and errortiles for continuous routine
        val tilesWithoutErrors = ArrayList<Tile>(tmpRoutine.tiles)
        while (tilesWithoutErrors.contains(Tile.ERROR_TILE)) {
            tilesWithoutErrors.remove(Tile.ERROR_TILE)
        }

        if (tilesWithoutErrors.size < 4) {
            holder.singleImageView.transitionName = uid + "icon"
            holder.setSingleImage()
            val icon = ResourceClass.getIconDrawable(firstTile)
            holder.singleImageView.setImageDrawable(icon)
            holder.singleImageView.setBackgroundColor(firstTile.backgroundColor)
            holder.singleImageView.setColorFilter(firstTile.contrastColor)
        } else {
            holder.fourImages[0].transitionName = uid + "icon"
            holder.setFourImages()
            for (i in 0..3) {
                val currentTile = tilesWithoutErrors[i]
                val currentImageView = holder.fourImages[i]
                val icon = ResourceClass.getIconDrawable(currentTile)
                currentImageView.setImageDrawable(icon)
                currentImageView.setBackgroundColor(currentTile.backgroundColor)
                currentImageView.setColorFilter(currentTile.contrastColor)
            }
        }

        if (tmpRoutine != Routine.ERROR_ROUTINE) {
            holder.layout.setOnCreateContextMenuListener { contextMenu, _, _ ->
                contextMenu.add("Duplicate").setOnMenuItemClickListener {
                    SelectRoutineFragment.fragment.handleCMDuplicate(position, tmpRoutine.uid)
                    true
                }
                contextMenu.add("Delete").setOnMenuItemClickListener {
                    SelectRoutineFragment.fragment.handleCMDelete(position, tmpRoutine.uid)
                    true
                }
            }
            holder.layout.transitionName = tmpRoutine.uid
            holder.nameView.transitionName = tmpRoutine.uid + "name"
            holder.layout.setOnClickListener {
                val iv: ShapeableImageView
                if (tilesWithoutErrors.size < 4) {
                    iv = holder.singleImageView
                } else {
                    iv = holder.fourImages[0]
                    for ((index, image) in holder.fourImages.withIndex()) {
                        if (index != 0) {
                            image.visibility = View.INVISIBLE
                        }
                    }
                }
                iv.transitionName = tmpRoutine.uid + "icon"
                SelectRoutineFragment.fragment.goToEditRoutine(holder.layout, iv, holder.nameView, tmpRoutine)
            }
        } else {
            holder.layout.setOnCreateContextMenuListener(null)
            holder.layout.setOnClickListener(null)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return if (routines == null) 0 else routines!!.size
    }

    init {
        var tmpRoutines = routines

        if (tmpRoutines == null) {
            tmpRoutines = ArrayList()
        }
        if (tmpRoutines.size == 0) {
            tmpRoutines.add(Routine.ERROR_ROUTINE)
        } else {
            this.routines = tmpRoutines
        }
    }
}