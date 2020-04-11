package com.example.testapp;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class CurrentTripActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private ArrayList<Integer> trip_indices;
    ViewPager viewPager;
    CurrentTripAdapter adapter;
    List<CurrentTripModel> models = new ArrayList<>();
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
//    private int[] ids = new int[]{12,1,13,2,4,5,6};
    Button route;
    LinearLayout removeItem;

    BottomNavigationView navigation;
    ArrayList<Integer> ids;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_current_trip);

        Integer[] colors_temp = {
                getResources().getColor(R.color.color7),
                getResources().getColor(R.color.color9),
                getResources().getColor(R.color.color10),
                getResources().getColor(R.color.color11),
                getResources().getColor(R.color.color12),
                getResources().getColor(R.color.color13),
                getResources().getColor(R.color.color14),
                getResources().getColor(R.color.color15),
                getResources().getColor(R.color.color16)
        };

        colors = colors_temp;

//        ids = (ArrayList<Integer>) getIntent().getSerializableExtra("trip_indices");
        loadTripData();

        route = findViewById(R.id.showRoute);
        viewPager = findViewById(R.id.viewPager);
        removeItem = findViewById(R.id.removeItemFromTrip);

        createStorageReference("delhi");

        bottomNavigation();

        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

        removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFromModel();
                saveTripData();
            }
        });

    }

    private void saveTripData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(trip_indices);
        editor.putString("trip_indices", json);
        editor.apply();
    }

    private void loadTripData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("trip_indices", null);
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        ids = gson.fromJson(json, type);
        if(ids == null){
            ids = new ArrayList<Integer>();
        }
    }

    private void bottomNavigation() {
        navigation = (BottomNavigationView) findViewById(R.id.navigation_bar);
        navigation.getMenu().findItem(R.id.menu_item1).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item0:
                        Intent a = new Intent(CurrentTripActivity.this, Explore.class);
                        startActivity(a);

                        break;
                    case R.id.menu_item1:
                        break;
                    case R.id.menu_item2:
                        Intent b = new Intent(CurrentTripActivity.this, AccountActivity.class);
                        startActivity(b);
                        break;
                }
                return false;
            }
        });
    }

    private HashMap<Integer, DocumentReference> CreateDocReference(String... cities) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference city_reference = db.collection("ts_data").document(cities[0]);
        HashMap<Integer, DocumentReference> tourist_places = new HashMap<>();

        for (int id : ids) {
            String tourist_places_id = cities[0] + "::" + id;
            tourist_places.put(id,city_reference.collection(cities[0] + "_data").document(tourist_places_id));
        }
        return tourist_places;
    }

    private void setViewPagerBackground() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if(adapter != null){

                    if (position < (adapter.getCount() -1) && position < (colors.length - 1)) {
                        viewPager.setBackgroundColor(

                                (Integer) argbEvaluator.evaluate(
                                        positionOffset,
                                        colors[position],
                                        colors[position + 1]
                                )
                        );
                    }

                    else {
                        viewPager.setBackgroundColor(colors[colors.length - 1]);
                    }

                }

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });
    }

    private void createStorageReference(String... cities) {

        HashMap<Integer, DocumentReference> tp = CreateDocReference(cities);
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        for(final int id : ids){

            tp.get(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (!document.exists()) {
                            Log.d("existence: ", "No such document");
                        } else {
                            String img_name = "" + document.get("image_name");
                            Log.d("img_name is ",img_name);
                            final String title = "" + document.get("title");
                            final String short_description = "" + document.get("short_description");
                            StorageReference spaceRef  = storageReference.child("photos_delhi/" + img_name);
                            Log.d("spaceRef is",spaceRef.getName());
                            spaceRef.getDownloadUrl()
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if(task.isSuccessful()) {
                                                addToModel(task.getResult(),title,short_description,id);
                                            } else Toast.makeText(getApplicationContext(),"Check Internet",Toast.LENGTH_SHORT);
                                        }
                                    });
                        }
                    } else {
                        Log.d("error: ", "got failed with ", task.getException());
                    }
                }
            });
        }

    }

    public void removeFromModel() {
        Log.d("deleting:",""+viewPager.getCurrentItem());
        int position = viewPager.getCurrentItem();

        models.remove(position);

        adapter = new CurrentTripAdapter(models, this);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.setPadding(100, 0, 100, 0);
        setViewPagerBackground();
    }

    private void addToModel(Uri result, String title, String short_description, int id) {
        models.add(new CurrentTripModel(result,title,short_description));
        Log.d("model contains ",""+id);

        adapter = new CurrentTripAdapter(models, this);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(130, 0, 130, 0);

        setViewPagerBackground();
    }

}
