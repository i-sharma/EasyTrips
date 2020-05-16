package com.example.testapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.example.testapp.models.CurrentTripModel;
import com.example.testapp.R;
import com.example.testapp.models.ExploreModel;

import java.util.List;

public class CurrentTripAdapter extends PagerAdapter {

    private List<ExploreModel> models;
    private Context context;
    private ImageView imageView;
    private TextView title, time_to_cover;

    public CurrentTripAdapter(List<ExploreModel> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View view = layoutInflater.inflate(R.layout.layout_current_trip_item, container, false);

        imageView = view.findViewById(R.id.image);
        title = view.findViewById(R.id.title);
        time_to_cover = view.findViewById(R.id.time_to_cover);

        Log.d("title is","" + models.get(position).getTitle());

        updateView(position,container);

        container.addView(view, 0);

        return view;
    }

    private void updateView(int position,ViewGroup container) {
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

        startUpdate(container);

        notifyDataSetChanged();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
