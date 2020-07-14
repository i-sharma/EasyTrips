package com.example.testapp.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.example.testapp.R;
import com.example.testapp.dragListView.DragItemAdapter;
import com.example.testapp.models.ExploreModel;

import java.util.List;
import java.util.Random;

public class CurrentTripAdapter extends DragItemAdapter<String, CurrentTripAdapter.ViewHolder> {

    private static final String TAG = "CurrentTripAdapter";
    private List<ExploreModel> models;
    private Context context;
    int origin_index = -1, destination_index = -1;
//    private CurrentTripAdapter.OnItemClickListener listener;


    Random rnd = new Random();
    int currentColor;
    private boolean dragOnLongPress;
    private DisplayMetrics metrics;
    private int itemMargin = 0, itemWidth = 0;
    private ViewGroup mParent;

    public CurrentTripAdapter(List<ExploreModel> models, Context context, DisplayMetrics metrics, boolean dragOnLongPress) {
        this.models = models;
        this.context = context;
        this.dragOnLongPress = dragOnLongPress;
        this.metrics = metrics;
        setItemList(models);
        setHasStableIds(true);
    }

    @Override
    public long getUniqueItemId(int position) {
        return models.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mParent = parent;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_current_trip_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        int currentItemWidth = itemWidth;
        int height = holder.itemView.getLayoutParams().height;
        holder.setData(position);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(currentItemWidth, height);
        if(position == 0){
            params.setMargins(itemMargin, 0, 0, 0);
        } else if (position == models.size() - 1){
            params.setMargins(0,0,itemMargin, 0);
        }
        holder.itemView.setLayoutParams(params);


        if(origin_index == position){
            holder.origin.setVisibility(View.VISIBLE);
        }else{
            holder.origin.setVisibility(View.INVISIBLE);
        }

        if(destination_index == position){
            holder.dest.setVisibility(View.VISIBLE);
        }else {
            holder.dest.setVisibility(View.INVISIBLE);
        }
//        if(mParent!=null) {
//            currentColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//            mParent.setBackgroundColor(currentColor);

//        }
    }

    public class ViewHolder extends DragItemAdapter.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        private View v;
        private ImageView imageView, origin, dest;
        private TextView title;
        private TextView time_to_cover;
        private Button delete, menu_button;
        public boolean isClicked = true;

        ViewHolder(View view) {
            super(view, R.id.layout_current_trip_item, dragOnLongPress);
            v = view;
            imageView = v.findViewById(R.id.image);
            title = v.findViewById(R.id.title);
            time_to_cover = v.findViewById(R.id.time_to_cover);
            delete = v.findViewById(R.id.curr_trip_delete);
            menu_button = v.findViewById(R.id.curr_trip_menu_button);
            origin = v.findViewById(R.id.origin);
            dest = v.findViewById(R.id.destination);
            menu_button.setOnClickListener(this);
        }

        void setData(int position){
            title.setText(models.get(position).getTitle());
            time_to_cover.setText(models.get(position).getDuration_required_to_visit());

            Boolean isCustom = models.get(position).getIsCustom();

            if(!isCustom){
                Glide.with(context)
                        .load(models.get(position).getFb_image_url())
                        .placeholder(R.drawable.wait)
                        .into(imageView);
            }

            else{
                Glide.with(context)
                        .load(R.drawable.custom_location)
                        .into(imageView);
            }
        }

        @Override
        public void onClick(View v) {
            showPopupMenu(v);
        }

        private void showPopupMenu(View v){
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.inflate(R.menu.menu_curr_trip);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.set_origin:
                    origin_index = getAdapterPosition();
                    notifyDataSetChanged();
                    Log.d(TAG, "onMenuItemClick: " + isClicked);
                    return true;
                case R.id.set_dest:
                    destination_index = getAdapterPosition();
                    notifyDataSetChanged();
                    return true;
                case R.id.set_waypoint:
                    if(getAdapterPosition() == origin_index)
                        origin_index = -1;
                    if(getAdapterPosition() == destination_index)
                        destination_index = -1;
                    notifyDataSetChanged();
                    return true;
            }
            return false;
        }
    }

    public void setItemMargin(int itemMargin){
        this.itemMargin = itemMargin;
    }

    public void updateDisplayMetrics(){
        itemWidth = metrics.widthPixels - itemMargin * 2;
    }

}
