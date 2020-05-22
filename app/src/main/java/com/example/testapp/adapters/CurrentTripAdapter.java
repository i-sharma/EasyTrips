package com.example.testapp.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.testapp.R;
import com.example.testapp.dragListView.DragItemAdapter;
import com.example.testapp.models.ExploreModel;

import java.util.List;

public class CurrentTripAdapter extends DragItemAdapter<String, CurrentTripAdapter.ViewHolder> {

    private static final String TAG = "CurrentTripAdapter";
    private List<ExploreModel> models;
    private Context context;


    private boolean dragOnLongPress;
    private DisplayMetrics metrics;
    private int itemMargin = 0, itemWidth = 0;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_current_trip_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
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
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {

        private View v;
        private ImageView imageView;
        private TextView title;
        private TextView time_to_cover;

        ViewHolder(View view) {
            super(view, R.id.layout_current_trip_item, dragOnLongPress);
            v = view;
            imageView = v.findViewById(R.id.image);
            title = v.findViewById(R.id.title);
            time_to_cover = v.findViewById(R.id.time_to_cover);
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
    }

    public void setItemMargin(int itemMargin){
        this.itemMargin = itemMargin;
    }

    public void updateDisplayMetrics(){
        itemWidth = metrics.widthPixels - itemMargin * 2;
    }

}
