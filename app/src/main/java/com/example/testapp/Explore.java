package com.example.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;


public class Explore extends AppCompatActivity {
    private static final String TAG = "Explore";

    private FirebaseFirestore rootRef;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;

    AnimatedVectorDrawable avd2 ;
    AnimatedVectorDrawableCompat avd;

    int BOOL_ADD_TO_TRIP = 1;
    int last_click_position;

    private ArrayList<Integer> trip_indices = new ArrayList<Integer>();
    private ExploreAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        rootRef = FirebaseFirestore.getInstance();

        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        Query query = rootRef.collection("ts_data")
                .document("delhi")
                .collection("delhi_data")
                .orderBy("priority", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<explore_model> options = new FirestoreRecyclerOptions
                .Builder<explore_model>()
                .setQuery(query, explore_model.class)
                .build();

        adapter = new ExploreAdapter(options, getResources());

        recyclerView = findViewById(R.id.recycler_view);

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(adapter);

        adapter.setOnClickListener(new ExploreAdapter.OnItemClickListener() {
            @Override
            public void onViewClick(DocumentSnapshot documentSnapshot, int position) {
                last_click_position = position;
                Intent it = new Intent(Explore.this, tsDetails.class);
                it.putExtra("snapshot", documentSnapshot.toObject(explore_model.class));
                if(trip_indices.contains(position))
                    it.putExtra("already_present_in_trip", 1);
                else
                    it.putExtra("already_present_in_trip", 0);
                startActivityForResult(it, BOOL_ADD_TO_TRIP);
            }

//            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//            @Override
//            public void onButtonClick(int position, ImageView done) {
//                Toast.makeText(Explore.this, "Heres the position: " +
//                        position, Toast.LENGTH_SHORT).show();
//                Drawable drawable = done.getDrawable();
//
//                if(drawable instanceof AnimatedVectorDrawableCompat){
//                    avd = (AnimatedVectorDrawableCompat) drawable;
//                    avd.start();
//                }
//                else if(drawable instanceof AnimatedVectorDrawable){
//                    avd2 = (AnimatedVectorDrawable) drawable;
//                    avd2.start();
//
//                }
//
//            }

        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == BOOL_ADD_TO_TRIP) {
            if(resultCode == Activity.RESULT_OK){
                int add_to_trip_value = data.getIntExtra("add_to_trip_value", 0);
                int remove_from_trip = data.getIntExtra("remove_from_trip", 0);
                if(add_to_trip_value == 1){
                    if(!trip_indices.contains(last_click_position))
                        trip_indices.add(last_click_position);
                }
                if(remove_from_trip == 1){
                if(remove_from_trip == 1){
                    trip_indices.remove(Integer.valueOf(last_click_position));

                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }


    public void show_trip(View view) {
        Log.d(TAG, "show_trip: " + trip_indices);
    }
}
