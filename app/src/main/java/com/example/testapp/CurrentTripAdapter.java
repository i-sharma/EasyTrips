package com.example.testapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.List;

public class CurrentTripAdapter extends PagerAdapter {

    private List<CurrentTripModel> models;
    private LayoutInflater layoutInflater;
    private Context context;
    ImageView imageView;
    TextView title, desc;

    public CurrentTripAdapter(List<CurrentTripModel> models, Context context) {
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
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.layout_current_trip_item, container, false);


        imageView = view.findViewById(R.id.image);
        title = view.findViewById(R.id.title);
        desc = view.findViewById(R.id.desc);

        Log.d("title is","" + models.get(position).getTitle());

        updateView(position,container);

        //imageView.setImageResource(models.get(position).getImage());

//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, DetailActivity.class);
//                intent.putExtra("param", models.get(position).getTitle());
//                context.startActivity(intent);
//                // finish();
//            }
//        });

        container.addView(view, 0);

        return view;
    }

    private void updateView(int position,ViewGroup container) {
        title.setText(models.get(position).getTitle());
        desc.setText(models.get(position).getDesc());

        Glide.with(context)
                .load(models.get(position).getImage())
                .placeholder(R.drawable.wait)
                .into(imageView);
//        GlideApp.with(context)
//                .load(models.get(position).getImage())
//                .placeholder(R.drawable.wfi)
//                .into(imageView);
        startUpdate(container);

        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
