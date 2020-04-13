package com.example.testapp;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class Explore extends AppCompatActivity {
    private static final String TAG = "Explore";

    private FirebaseFirestore rootRef;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;

    AnimatedVectorDrawable avd2 ;
    AnimatedVectorDrawableCompat avd;

    int BOOL_ADD_TO_TRIP = 1;
    int last_click_position;

    private ExploreAdapter adapter;
    BottomNavigationView navigation;
    Parcelable mListState;
    String LIST_STATE_KEY = "9718";

    LinkedHashMap<Integer, HashMap<String,String>> trip_data = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_explore);
        rootRef = FirebaseFirestore.getInstance();

        loadData();
        setUpRecyclerView();
        adapter.startListening();
        bottomNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.stopListening();
    }

    private void bottomNavigation() {
        loadData();
        navigation = (BottomNavigationView) findViewById(R.id.navigation_bar);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item0:
                        break;
                    case R.id.menu_item1:
                        Intent a = new Intent(Explore.this, CurrentTripActivity.class);
                        startActivity(a);
                        break;
                    case R.id.menu_item2:
                        Intent b = new Intent(Explore.this, AccountActivity.class);
                        startActivity(b);
                        break;
                }
                return false;
            }
        });
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
        adapter.notifyDataSetChanged();

        adapter.setOnClickListener(new ExploreAdapter.OnItemClickListener() {
            @Override
            public void onViewClick(DocumentSnapshot documentSnapshot, int position) {
                last_click_position = position;
                Intent it = new Intent(Explore.this, tsDetails.class);
                it.putExtra("snapshot", documentSnapshot.toObject(explore_model.class));
                it.putExtra("click_position", position+1);
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


    private void loadData() {
        try {
            File file = new File(getDir("data", MODE_PRIVATE), "map");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            trip_data = (LinkedHashMap) ois.readObject();

        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (ClassNotFoundException e){
            trip_data = new LinkedHashMap<>();
        }
    }

}
