package com.example.testapp.adapters;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.YourTripsModel;

import java.util.List;

public class YourTripsAdapter extends RecyclerView.Adapter<YourTripsAdapter.YourTripsViewHolder>{

    private List<YourTripsModel> models;
    Resources resources;

    public YourTripsAdapter(List<YourTripsModel> models, Resources resources) {
        this.models = models;
        this.resources = resources;
    }

    @NonNull
    @Override
    public YourTripsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_your_trip_item, parent, false);
        return new YourTripsAdapter.YourTripsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YourTripsViewHolder holder, int position) {
        holder.start.setText(models.get(position).getStart_location());
        holder.end.setText(models.get(position).getEnd_location());
        holder.n_locations.setText("+" + models.get(position).getN_locations() + " Stops");
        holder.color_strip.setBackgroundColor(resources.getColor(R.color.color2));


    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class YourTripsViewHolder extends RecyclerView.ViewHolder{
            private TextView start, end, n_locations, color_strip;

        public YourTripsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.color_strip = itemView.findViewById(R.id.color_strip);
            this.start = itemView.findViewById(R.id.your_trip_start);
            this.end = itemView.findViewById(R.id.your_trip_end);
            this.n_locations = itemView.findViewById(R.id.n_stops);
        }

    }
}
