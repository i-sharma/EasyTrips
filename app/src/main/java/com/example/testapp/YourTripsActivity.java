package com.example.testapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class YourTripsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<YourTripsModel> models;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_trips);

        recyclerView =  findViewById(R.id.your_trips_recycler_view);
//        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        models = new ArrayList<>();
        set_models();

        // specify an adapter (see also next example)
        YourTripsAdapter adapter = new YourTripsAdapter(models, getResources());
        recyclerView.setAdapter(adapter);
    }

    private void set_models() {
        for(int i = 0; i < 10; i++){
            YourTripsModel new_model = new YourTripsModel("Red Fort", "India Gate", "4");
            models.add(new_model);
        }
    }
}
