package de.threateningcodecomments.routinetimer

import android.graphics.Color
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import de.threateningcodecomments.routinetimer.MyAdapter.MyViewHolder


internal class MyAdapter(routines: ArrayList<Routine>?) : RecyclerView.Adapter<MyViewHolder>() {

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnCreateContextMenuListener {
        var layout: LinearLayout
        var nameView: TextView
        var modeView: TextView
        var singleImageView: ShapeableImageView
        var fourImages: ArrayList<ShapeableImageView>

        init {
            layout = v.findViewById(R.id.fl_SelectRoutine_createRoutine_content)

            nameView = v.findViewById(R.id.tv_SelectRoutine_rv_routineName)

            modeView = v.findViewById(R.id.tv_SelectRoutine_rv_routineMode)

            singleImageView = v.findViewById(R.id.iv_SelectRoutine_rv_singleImage)
            fourImages = ArrayList()
            var tmp: ShapeableImageView = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_1)
            fourImages.add(tmp)
            tmp = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_2)
            fourImages.add(tmp)
            tmp = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_3)
            fourImages.add(tmp)
            tmp = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_4)
            fourImages.add(tmp)

        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu!!.add(Menu.NONE, R.menu.select_routine_contextmenu, Menu.NONE, "what?")
        }
    }

    var position = 0

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

        var name = tmpRoutine.name
        if (name == Routine.ERROR_NAME) {
            name = "Nothing here yet!"
        }
        holder.nameView.text = name
        if (isNightMode) holder.nameView.setTextColor(Color.WHITE) else holder.nameView.setTextColor(Color.BLACK)

        val mode = if (tmpRoutine.mode == Routine.MODE_CONTINUOUS) Routine.CONTINUOUS_MESSAGE else Routine.SEQUENTIAL_MESSAGE
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

        MyLog.d("setting layout longclicklistener of ${holder.nameView.text}")
        holder.layout.setOnLongClickListener { view ->
            MyLog.d("long click!")
            val items = arrayOf<CharSequence>("Supprimer", "etc", "etc1")
            /*val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(view!!.context)
            builder.setTitle("Select The Action")
            builder.setItems(items, DialogInterface.OnClickListener { dialog, item -> })
            builder.show()*/
            true
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