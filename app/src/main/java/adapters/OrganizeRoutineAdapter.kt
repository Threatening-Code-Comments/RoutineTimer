package adapters

import accessibility.MyLog
import accessibility.ResourceClass
import accessibility.Routine
import accessibility.Tile
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
        MyLog.d("binding viewholder nr$position !")
        val tile = routine.tiles[position]

        holder.setSingleImage()
        holder.singleImageView.setImageDrawable(ResourceClass.getIconDrawable(tile))
        holder.singleImageView.setBackgroundColor(tile.backgroundColor)
        holder.singleImageView.setColorFilter(tile.contrastColor)

        holder.nameView.text = tile.name

        val mode = if (tile.mode == Tile.MODE_COUNT_DOWN) Tile.COUNT_DOWN_MESSAGE else Tile.COUNT_UP_MESSAGE
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