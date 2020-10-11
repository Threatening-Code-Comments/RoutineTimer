package de.threateningcodecomments.routinetimer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import de.threateningcodecomments.routinetimer.MyAdapter.MyViewHolder


internal class MyAdapter(routines: ArrayList<Routine>?) : RecyclerView.Adapter<MyViewHolder>() {

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var layout: LinearLayout = v.findViewById(R.id.fl_SelectRoutine_createRoutine_content)

        var routineUidView: TextView = v.findViewById(R.id.tv_SelectRoutine_rv_routineUid)

        var nameView: TextView = v.findViewById(R.id.tv_SelectRoutine_rv_routineName)

        var modeView: TextView = v.findViewById(R.id.tv_SelectRoutine_rv_routineMode)

        var singleImageView: ShapeableImageView = v.findViewById(R.id.iv_SelectRoutine_rv_singleImage)
        var fourImages: ArrayList<ShapeableImageView> = ArrayList()

        init {
            var tmp: ShapeableImageView = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_1)
            fourImages.add(tmp)
            tmp = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_2)
            fourImages.add(tmp)
            tmp = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_3)
            fourImages.add(tmp)
            tmp = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_4)
            fourImages.add(tmp)
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
        val isNightMode = ResourceClass.wasNightMode()
        val tmpRoutine = routines!![position]
        val firstTile = tmpRoutine.tiles!![0]

        tmpRoutine.setAccessibility(isNightMode)

        val uid = tmpRoutine.uid
        holder.routineUidView.text = uid

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

        if (tmpRoutine.tiles!!.size < 4) {
            holder.singleImageView.visibility = View.VISIBLE
            val icon = ResourceClass.getIconDrawable(firstTile)
            holder.singleImageView.setImageDrawable(icon)
            holder.singleImageView.setBackgroundColor(firstTile.backgroundColor)
            holder.singleImageView.setColorFilter(firstTile.contrastColor)
            for (imageView in holder.fourImages) {
                imageView.visibility = View.GONE
            }
        } else {
            holder.singleImageView.visibility = View.GONE
            for (i in 0..3) {
                val currentTile = tmpRoutine.tiles!![i]
                val currentImageView = holder.fourImages[i]
                currentImageView.visibility = View.VISIBLE
                val icon = ResourceClass.getIconDrawable(currentTile)
                currentImageView.setImageDrawable(icon)
                currentImageView.setBackgroundColor(currentTile.backgroundColor)
                currentImageView.setColorFilter(currentTile.contrastColor)
            }
        }

        if (tmpRoutine != Routine.ERROR_ROUTINE) {
            MyLog.d("${tmpRoutine.name} is getting a listener!")
            holder.layout.setOnCreateContextMenuListener { contextMenu, _, _ ->
                contextMenu.add("Duplicate").setOnMenuItemClickListener {
                    SelectRoutineFragment.fragment.handleCMDuplicate(position)
                    true
                }
                contextMenu.add("Delete").setOnMenuItemClickListener {
                    SelectRoutineFragment.fragment.handleCMDelete(position)
                    true
                }
            }
        } else {
            holder.layout.setOnCreateContextMenuListener(null)
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