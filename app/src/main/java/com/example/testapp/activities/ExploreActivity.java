package com.example.testapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.example.testapp.R;
import com.example.testapp.adapters.ExploreAdapter;
import com.example.testapp.models.TourismSpotModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

//import com.robin.locationgetter.EasyLocation;


public class ExploreActivity extends AppCompatActivity {
    private static final String TAG = "Explore";

    private FusedLocationProviderClient mFusedLocationClient;
    LatLng origin,destination;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;
    private FirebaseFirestore rootRef;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;

    int BOOL_ADD_TO_TRIP = 1;
    int last_click_position;

    private ExploreAdapter adapter;
    BottomNavigationView navigation;

    Button explore_city_name;
    ImageView explore_text;
    LinkedHashMap<String, TourismSpotModel> data_models_map = new LinkedHashMap<>();
    ProgressBar progressBar;
    private int city_code;
    private String current_city_name, lat_lon_str;

    enum city_options  {
            DELHI, CURR_LOC
    }
    city_options selected_city = null;

    CFAlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_explore);
        rootRef = FirebaseFirestore.getInstance();
        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.shared_pref_file_name), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        explore_city_name = findViewById(R.id.explore_city_name);
        explore_text = findViewById(R.id.explore_text);
        progressBar = findViewById(R.id.explore_progress);
        currentUser = mAuth.getCurrentUser();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        buildCitySelector();
        load_shared_pref();
        updateUI(city_code, current_city_name);
        explore_city_name.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    explore_city_name.setBackgroundResource(R.drawable.rounded_button_dark_yellow);
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                    explore_city_name.setBackgroundResource(R.drawable.rounded_button_yellow);
//                    explore_city_name.setBackgroundResource(android.R.drawable.btn_default);
//                    explore_city_name.setBackgroundColor(0x00000000);
                    builder.show();
                    return true;
                }
                return false;
            }
        });


//        loadTripData();
//        setUpRecyclerView();
//        adapter.startListening();
        bottomNavigation();
    }

    private void buildCitySelector() {
        builder = new CFAlertDialog.Builder(ExploreActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
        builder.setTitle("Where would you like to Explore?");
        builder.setSingleChoiceItems(new String[]{"Delhi", "Detect my Current Location"}, 3, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                switch (index){
                    case 0 :
                        selected_city = city_options.DELHI;
                        break;
                    case 1:
                        selected_city = city_options.CURR_LOC;
                        break;
                }
            }
        });
        builder.addButton("DONE", -1, -1, CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.CENTER, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(selected_city == city_options.DELHI){
                    save_shared_pref(0, "Delhi", null, null);
                    updateUI(0, "Delhi");
                }else if(selected_city == city_options.CURR_LOC){
                    progressBar.setVisibility(View.VISIBLE);
                    fetchLocation();
                    progressBar.setVisibility(View.GONE);
                }


            }
        });
    }


    private void updateUI(int i, String city) {
        progressBar.setVisibility(View.GONE);
        switch (i){
            case -1:
                explore_city_name.setText("Choose City");
                explore_text.setBackgroundResource(R.drawable.explore_case_neg_1);
                explore_text.setVisibility(View.VISIBLE);
                if(recyclerView!=null){
                    recyclerView.setVisibility(View.GONE);
                }
                break;
            case 0:
                setUpRecyclerView();
                adapter.startListening();
                explore_city_name.setText(city);
                explore_text.setVisibility(View.GONE);
                if(recyclerView!=null){
                    recyclerView.setVisibility(View.VISIBLE);
                }
                break;
            case 1:
                explore_city_name.setText(city);
                explore_text.setBackgroundResource(R.drawable.explore_case123);
                explore_text.setVisibility(View.VISIBLE);
                if(recyclerView!=null){
                    recyclerView.setVisibility(View.GONE);
                }
                break;
            case 2:
                explore_city_name.setText("Current City");
                explore_text.setBackgroundResource(R.drawable.explore_case123);
                explore_text.setVisibility(View.VISIBLE);
                if(recyclerView!=null){
                    recyclerView.setVisibility(View.GONE);
                }
                break;
            case 3:
                explore_city_name.setText("Choose City");
                explore_text.setBackgroundResource(R.drawable.explore_case123);
                explore_text.setVisibility(View.VISIBLE);
                if(recyclerView!=null){
                    recyclerView.setVisibility(View.GONE);
                }

                Toast.makeText(this, "City could not be fetched", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void save_shared_pref(Integer status, String city, Double lat, Double lon) {
        Log.d(TAG, "save_shared_pref: status " + status + " city " + city + " lat " + lat + " lon " + lon);
        if(status == null){
            status = -1;
        }
        if(city == null){
            city = "";
        }
        if(lat == null){
            lat = -1.0;
        }
        if(lon == null){
            lon = -1.0;
        }

        editor.putInt("city_code", status);
        editor.putString("current_city", city);
        editor.putString("lat_lon_str", lat+" "+lon);
        editor.commit();
    }

    private void load_shared_pref() {
        city_code = sharedPref.getInt("city_code", -1);
        current_city_name = sharedPref.getString("current_city", "");
        lat_lon_str = sharedPref.getString("lat_lon_str", "");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.stopListening();
    }

    private void loadTripData() {
        try {
            File file = new File(getDir("data", MODE_PRIVATE), "data_models_map");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            data_models_map = (LinkedHashMap) ois.readObject();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (ClassNotFoundException e){
            data_models_map = new LinkedHashMap<>();
        }
    }

    private void bottomNavigation() {

        navigation = (BottomNavigationView) findViewById(R.id.navigation_bar);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item0:
                        loadTripData();
                        for(String s: data_models_map.keySet()){
                            Log.d(TAG, "onNavigationItemSelected: " + data_models_map.get(s).getId() + " " + data_models_map.get(s).title);
                        }
                        Log.d(TAG, "onNavigationItemSelected: ");
                        break;
                    case R.id.menu_item1:
                        Intent a = new Intent(ExploreActivity.this, CurrentTripActivity.class);
                        a.putExtra("origin",origin);
                        a.putExtra("destination",destination);
                        startActivity(a);
                        break;
                    case R.id.menu_item2:
                        if(currentUser != null){
                            Intent b = new Intent(ExploreActivity.this, AccountActivity.class);
                            startActivity(b);
                        }
                        else {
                            Intent b = new Intent(ExploreActivity.this, LoginActivity.class);
                            startActivity(b);
                        }
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

        FirestoreRecyclerOptions<TourismSpotModel> options = new FirestoreRecyclerOptions
                .Builder<TourismSpotModel>()
                .setQuery(query, TourismSpotModel.class)
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
                Intent it = new Intent(ExploreActivity.this, TSDetailsActivity.class);
                it.putExtra("snapshot", documentSnapshot.toObject(TourismSpotModel.class));
                it.putExtra("click_position", position+1);
                startActivityForResult(it, BOOL_ADD_TO_TRIP);
            }

        });

    }


    public void getCityName(final Location location, final OnGeocoderFinishedListener listener) {
        new AsyncTask<Void, Integer, List<Address>>() {
            @Override
            protected List<Address> doInBackground(Void... arg0) {
                Geocoder coder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
                List<Address> results = null;
                try {
                    results = coder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    // nothing
                }
                return results;
            }

            @Override
            protected void onPostExecute(List<Address> results) {
                if (results != null && listener != null) {
                    listener.onFinished(results);
                }
            }
        }.execute();
    }

    public abstract class OnGeocoderFinishedListener {
        public abstract void onFinished(List<Address> results);
    }

    private void fetchLocation() {

        if (ActivityCompat.checkSelfPermission((Activity)this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions((Activity)this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 10);
        }

        if(ActivityCompat.checkSelfPermission((Activity)this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                getCityName(location, new OnGeocoderFinishedListener() {
                                    @Override
                                    public void onFinished(List<Address> results) {
                                        Log.d(TAG, "onFinished: 2" + results);
//                                        Log.d(TAG, "onFinished: " + );
                                        if(results != null && results.size() > 0){
                                            if(results.get(0).getLocality()!=null){
                                                save_shared_pref(1, results.get(0).getLocality(), location.getLatitude(), location.getLongitude());
                                                updateUI(1, results.get(0).getLocality());
                                            }
                                            else {
                                                save_shared_pref(2, location.getLatitude() + " " +  location.getLongitude(), location.getLatitude(), location.getLongitude());
                                                updateUI(2, location.getLatitude() + " " +  location.getLongitude());
                                            }
                                        }else{
                                            save_shared_pref(2, location.getLatitude() + " " +  location.getLongitude(), location.getLatitude(), location.getLongitude());
                                            updateUI(2, location.getLatitude() + " " +  location.getLongitude());
                                        }
                                    }
                                });
                            }else{
                                save_shared_pref(3, null, null, null);
                                updateUI(3, null);
                            }
                        }
                    });
        }
    }


}
