package de.threateningcodecomments.routinetimer;

import android.graphics.Color;
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
        public TextView nameView;
        public ShapeableImageView singleImageView;

        public ArrayList<ShapeableImageView> fourImages;

        public MyViewHolder(View v) {
            super(v);

            nameView = v.findViewById(R.id.tv_SelectRoutine_rv_routineName);
            singleImageView = v.findViewById(R.id.iv_SelectRoutine_rv_singleImage);

            fourImages = new ArrayList<>();
            ShapeableImageView tmp = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_1);
            fourImages.add(tmp);
            tmp = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_2);
            fourImages.add(tmp);
            tmp = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_3);
            fourImages.add(tmp);
            tmp = v.findViewById(R.id.iv_SelectRoutine_rv_smallImage_4);
            fourImages.add(tmp);
        }
    }

    private ArrayList<Routine> routines;

    public MyAdapter(ArrayList<Routine> routines) {
        if (routines == null) {
            routines = new ArrayList<>();
        }

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
        boolean isNightMode = ResourceClass.wasNightMode();

        Routine tmpRoutine = routines.get(position);
        tmpRoutine.setAccessibility(isNightMode);
        Tile firstTile = tmpRoutine.getTiles().get(0);


        String name = tmpRoutine.getName();
        if (name.equals(Routine.ERROR_NAME)) {
            name = "Nothing here yet!";
        }
        holder.nameView.setText(name);
        if (isNightMode)
            holder.nameView.setTextColor(Color.WHITE);
        else
            holder.nameView.setTextColor(Color.BLACK);

        if (tmpRoutine.getTiles().size() < 4) {
            holder.singleImageView.setVisibility(View.VISIBLE);

            Drawable icon = ResourceClass.getIconDrawable(firstTile);
            holder.singleImageView.setImageDrawable(icon);

            holder.singleImageView.setBackgroundColor(firstTile.getBackgroundColor());
            holder.singleImageView.setColorFilter(firstTile.getContrastColor());

            for (ShapeableImageView imageView : holder.fourImages) {
                imageView.setVisibility(View.GONE);
            }
        } else {
            holder.singleImageView.setVisibility(View.GONE);

            for (int i = 0; i < 4; i++) {
                Tile currentTile = tmpRoutine.getTiles().get(i);
                ShapeableImageView currentImageView = holder.fourImages.get(i);
                currentImageView.setVisibility(View.VISIBLE);

                Drawable icon = ResourceClass.getIconDrawable(currentTile);
                currentImageView.setImageDrawable(icon);

                currentImageView.setBackgroundColor(currentTile.getBackgroundColor());
                currentImageView.setColorFilter(currentTile.getContrastColor());
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return routines.size();
    }
}
