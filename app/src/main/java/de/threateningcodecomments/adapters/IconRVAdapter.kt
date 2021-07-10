package de.threateningcodecomments.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.maltaisn.icondialog.data.Icon
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.routinetimer.R
import de.threateningcodecomments.routinetimer.TileSettingsFragment


class IconRVAdapter(var context: Context, var icons: ArrayList<Icon>) : RecyclerView.Adapter<IconRVAdapter
.IconRVAdapterViewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconRVAdapterViewholder {
        // infalte the item Layout
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_icon, parent, false)

        return IconRVAdapterViewholder(v)
    }

    override fun onBindViewHolder(holder: IconRVAdapterViewholder, position: Int) {
        val drawable = icons[position].drawable
        holder.iconView.setImageDrawable(drawable)

        val parentLayout = holder.iconView.parent as View
        parentLayout.setOnClickListener {
            val icon = icons[position]

            TileSettingsFragment.instance.currentTile.iconID = icon.id
            (TileSettingsFragment.instance.viewPager.adapter as TileSettingsViewpagerAdapter).updateUI()
        }
    }

    override fun getItemCount(): Int {
        return icons.size
    }


    class IconRVAdapterViewholder : RecyclerView.ViewHolder {
        lateinit var iconView: ImageView

        constructor(itemView: View) : super(itemView) {
            iconView = itemView.findViewById(R.id.iv_viewholder_icon_imageView)
            iconView.setColorFilter(RC.Resources.Colors.contrastColor)
        }

    }
}