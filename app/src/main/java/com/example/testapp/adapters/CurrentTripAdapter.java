package com.example.testapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.testapp.activities.CurrentTripActivity;
import com.example.testapp.dragListView.DragItemAdapter;
import com.example.testapp.models.TourismSpotModel;

import java.util.List;
import java.util.Random;

public class CurrentTripAdapter extends DragItemAdapter<String, CurrentTripAdapter.ViewHolder> {

    private static final String TAG = "CurrentTripAdapter";
    private List<TourismSpotModel> models;
    private Context context;
    int origin_index = -1, destination_index = -1;
    private ICurrTrip iCurrTrip;
    Random rnd = new Random();
    int currentColor;
    private boolean dragOnLongPress;
    private DisplayMetrics metrics;
    private int itemMargin = 0, itemWidth = 0;
    private ViewGroup mParent;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    Boolean edit_mode = false;

    public CurrentTripAdapter(List<TourismSpotModel> models, Context context, DisplayMetrics metrics, boolean dragOnLongPress) {
        this.models = models;
        this.context = context;
        this.dragOnLongPress = dragOnLongPress;
        this.metrics = metrics;
        setItemList(models);
        setHasStableIds(true);
        sharedPref = context.getSharedPreferences(
                context.getString(R.string.shared_pref_file_name), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        this.iCurrTrip = (ICurrTrip) context;

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


        if(models.get(position).getOrigin()){
            holder.origin.setVisibility(View.VISIBLE);
        }else{
            holder.origin.setVisibility(View.INVISIBLE);
        }

        if(models.get(position).getDestination()){
            holder.dest.setVisibility(View.VISIBLE);
        }else {
            holder.dest.setVisibility(View.INVISIBLE);
        }

        if(edit_mode){
            holder.menu_button.setVisibility(View.VISIBLE);
        }else{
            holder.menu_button.setVisibility(View.INVISIBLE);
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
            loadSharedPref();
            switch (item.getItemId()){
                case R.id.set_origin:
                    Log.d(TAG, "onMenuItemClick: org " + origin_index + " pos " + getAdapterPosition());
                    if(origin_index != getAdapterPosition() && origin_index != -1) {
                        models.get(origin_index).setOrigin(false);
                        iCurrTrip.dragTopBottom(false, null);
                    }
                    origin_index = getAdapterPosition();
                    models.get(getAdapterPosition()).setOrigin(true);
                    notifyDataSetChanged();
                    saveSharedPref();
                    return true;
                case R.id.set_dest:
                    if(destination_index != getAdapterPosition() && destination_index != -1){
                        models.get(destination_index).setDestination(false);
                        iCurrTrip.dragTopBottom(null, false);
                    }
                    destination_index = getAdapterPosition();
                    models.get(getAdapterPosition()).setDestination(true);
                    notifyDataSetChanged();
                    saveSharedPref();
                    return true;
                case R.id.set_waypoint:
                    if(models.get(getAdapterPosition()).getOrigin()){
                        models.get(getAdapterPosition()).setOrigin(false);
                        origin_index = -1;
                        iCurrTrip.dragTopBottom(false, null);
                    }

                    if(models.get(getAdapterPosition()).getDestination()){
                        models.get(getAdapterPosition()).setDestination(false);
                        destination_index = -1;
                        iCurrTrip.dragTopBottom(null, false);
                    }
                    notifyDataSetChanged();
                    saveSharedPref();
                    return true;
            }
            return false;
        }
    }

    public void setEdit_mode(Boolean val){
        this.edit_mode = val;
    }
    private void loadSharedPref(){
        origin_index = sharedPref.getInt("origin_index", -1);
        destination_index = sharedPref.getInt("destination_index", -1);
    }

    private void saveSharedPref() {
        editor.putInt("origin_index", origin_index);
        editor.putInt("destination_index", destination_index);
        editor.commit();
    }

    public void setItemMargin(int itemMargin){
        this.itemMargin = itemMargin;
    }

    public void updateDisplayMetrics(){
        itemWidth = metrics.widthPixels - itemMargin * 2;
    }

    public interface ICurrTrip{
        public void dragTopBottom(Boolean topmost, Boolean bottommost);
    }
}
