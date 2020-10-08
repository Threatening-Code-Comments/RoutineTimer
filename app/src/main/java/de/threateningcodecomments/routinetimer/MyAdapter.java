package de.threateningcodecomments.routinetimer;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ShapeableImageView imageView;

        public MyViewHolder(View v) {
            super(v);

            textView = v.findViewById(R.id.tv_SelectRoutine_rv_textView);
            imageView = v.findViewById(R.id.iv_SelectRoutine_rv_imageView);
        }
    }

    private ArrayList<Routine> routines;

    public MyAdapter(ArrayList<Routine> routines) {
        if (routines.size() == 0) {
            routines.add(Routine.ERROR_ROUTINE);
        } else {
            this.routines = routines;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_viewholder, parent, false);

        MyViewHolder vh = new MyViewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Routine tmpRoutine = routines.get(position);
        Tile firstTile = tmpRoutine.getTiles().get(0);

        String name = tmpRoutine.getName();
        if (name.equals(Routine.ERROR_NAME)) {
            name = "Nothing here yet!";
        }

        int iconID = firstTile.getIconID();
        Drawable icon;
        if (iconID != Tile.ERROR_ICONID)
            icon = ResourceClass.getIconPack().getIcon(iconID).getDrawable();
        else
            icon = ResourceClass.getErrorDrawable();

        int color = firstTile.getBackgroundColor();

        holder.textView.setText(name);
        holder.imageView.setImageDrawable(icon);
        holder.imageView.setBackgroundColor(color);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return routines.size();
    }
}
