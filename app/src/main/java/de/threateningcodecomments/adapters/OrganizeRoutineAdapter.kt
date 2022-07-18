package de.threateningcodecomments.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.data.Routine
import de.threateningcodecomments.data.Tile
import de.threateningcodecomments.routinetimer.EditSequentialRoutineFragment
import de.threateningcodecomments.routinetimer.R
import java.util.*

class OrganizeRoutineAdapter(private val startDragListener: OnStartDragListener, private val routine: Routine) :
        RecyclerView.Adapter<MyViewHolder>(),
        ItemMoveCallbackListener.Listener {
    val tiles = routine.tiles

    override fun getItemCount(): Int {
        return tiles.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val tile = routine.tiles[position]

        holder.setSingleImage()
        holder.singleImageView.setImageDrawable(RC.getIconDrawable(tile))
        holder.singleImageView.setBackgroundColor(tile.backgroundColor)
        holder.singleImageView.setColorFilter(tile.contrastColor)

        holder.nameView.text = tile.name

        val mode = when (tile.mode) {
            Tile.MODE_COUNT_DOWN -> Tile.COUNT_DOWN_MESSAGE
            Tile.MODE_COUNT_UP -> Tile.COUNT_UP_MESSAGE
            Tile.MODE_TAP -> Tile.TAP_MESSAGE
            Tile.MODE_DATA -> Tile.DATA_MESSAGE
            else -> throw IllegalStateException("Tile mode is wrong")
        }
        holder.modeView.text = mode

        holder.grabImage.visibility = View.VISIBLE
        holder.grabImage.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                this.startDragListener.onStartDrag(holder)
            }
            return@setOnTouchListener true
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_viewholder, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(tiles, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(tiles, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        EditSequentialRoutineFragment.fragment.updateUI()
    }

    override fun onRowSelected(itemViewHolder: MyViewHolder) {

    }

    override fun onRowClear(itemViewHolder: MyViewHolder) {

    }
}