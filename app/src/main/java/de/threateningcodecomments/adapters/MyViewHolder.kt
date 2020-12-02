package de.threateningcodecomments.adapters

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import de.threateningcodecomments.routinetimer.R

class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    var layout: LinearLayout = v.findViewById(R.id.fl_rvItem_layout)

    var nameView: TextView = v.findViewById(R.id.tv_rvItem_name)

    var modeView: TextView = v.findViewById(R.id.tv_rvItem_mode)

    var singleImageView: ShapeableImageView = v.findViewById(R.id.iv_rvItem_singleImage)
    var fourImages: ArrayList<ShapeableImageView> = ArrayList()

    var grabImage: ShapeableImageView = v.findViewById(R.id.iv_rvItem_grab)

    init {
        var tmp: ShapeableImageView = v.findViewById(R.id.iv_rvItem_smallImage_1)
        fourImages.add(tmp)
        tmp = v.findViewById(R.id.iv_rvItem_smallImage_2)
        fourImages.add(tmp)
        tmp = v.findViewById(R.id.iv_rvItem_smallImage_3)
        fourImages.add(tmp)
        tmp = v.findViewById(R.id.iv_rvItem_smallImage_4)
        fourImages.add(tmp)
    }

    fun setSingleImage() {
        singleImageView.visibility = View.VISIBLE
        for (iv in fourImages) {
            iv.visibility = View.INVISIBLE
        }
    }

    fun setFourImages() {
        singleImageView.visibility = View.INVISIBLE
        for (iv in fourImages) {
            iv.visibility = View.VISIBLE
        }
    }
}