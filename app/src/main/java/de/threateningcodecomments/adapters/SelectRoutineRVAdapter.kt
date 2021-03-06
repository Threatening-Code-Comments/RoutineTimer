package de.threateningcodecomments.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.accessibility.Routine
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.routinetimer.R
import de.threateningcodecomments.routinetimer.SelectRoutineFragment


internal class SelectRoutineRVAdapter : RecyclerView.Adapter<MyViewHolder> {

    constructor(routines: ArrayList<Routine>) : super() {
        val tmpRoutines = ArrayList(routines.sortedByDescending { it.lastUsed })
        if (tmpRoutines.size == 0) {
            tmpRoutines.add(Routine.ERROR_ROUTINE)
        } else {
            this.routines = tmpRoutines
        }
    }

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
        val isNightMode = RC.isNightMode
        val tmpRoutine = routines!![position]

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

        //initializes tile buffer to restore order of tiles and ErrorTiles for continuous routine
        var tilesWithoutErrors = ArrayList<Tile>(tmpRoutine.tiles)
        while (tilesWithoutErrors.contains(Tile.ERROR_TILE))
            tilesWithoutErrors.remove(Tile.ERROR_TILE)

        val tilesWithOutDefault = ArrayList(tilesWithoutErrors)
        while (tilesWithOutDefault.contains(Tile.DEFAULT_TILE))
            tilesWithOutDefault.remove(Tile.DEFAULT_TILE)

        if (tilesWithOutDefault.size != 0)
            tilesWithoutErrors = tilesWithOutDefault

        if (tilesWithoutErrors.size < 4) {
            val firstTile =
                    if (tilesWithoutErrors.size != 0)
                        tilesWithoutErrors.first()
                    else
                        tmpRoutine.tiles.first()

            holder.singleImageView.transitionName = uid + "icon"
            holder.setSingleImage()
            val icon = RC.getIconDrawable(firstTile)
            holder.singleImageView.setImageDrawable(icon)
            holder.singleImageView.setBackgroundColor(firstTile.backgroundColor)
            holder.singleImageView.setColorFilter(firstTile.contrastColor)
        } else {
            holder.fourImages[0].transitionName = uid + "icon"
            holder.setFourImages()
            for (i in 0..3) {
                val currentTile = tilesWithoutErrors[i]
                val currentImageView = holder.fourImages[i]
                val icon = RC.getIconDrawable(currentTile)
                currentImageView.setImageDrawable(icon)
                currentImageView.setBackgroundColor(currentTile.backgroundColor)
                currentImageView.setColorFilter(currentTile.contrastColor)
            }
        }

        if (tmpRoutine != Routine.ERROR_ROUTINE) {
            holder.layout.transitionName = tmpRoutine.uid
            holder.nameView.transitionName = tmpRoutine.uid + "name"
            val iv: ShapeableImageView = if (tilesWithoutErrors.size < 4) holder.singleImageView else holder.fourImages[0]
            iv.transitionName = tmpRoutine.uid + "icon"

            holder.layout.setOnClickListener {
                SelectRoutineFragment.fragmentEditSelect.goToRunRoutine(holder.layout, iv, holder.nameView, tmpRoutine)
            }

            holder.layout.setOnCreateContextMenuListener { contextMenu, _, _ ->
                contextMenu.add("Edit").setOnMenuItemClickListener {
                    SelectRoutineFragment.fragmentEditSelect.goToEditRoutine(holder.layout, iv, holder.nameView, tmpRoutine)
                    true
                }
                contextMenu.add("Duplicate").setOnMenuItemClickListener {
                    SelectRoutineFragment.fragmentEditSelect.handleCMDuplicate(tmpRoutine.uid)
                    true
                }
                contextMenu.add("Delete").setOnMenuItemClickListener {
                    SelectRoutineFragment.fragmentEditSelect.handleCMDelete(tmpRoutine.uid)
                    true
                }
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
}