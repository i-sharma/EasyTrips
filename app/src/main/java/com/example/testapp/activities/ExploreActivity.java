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
import android.view.View;
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

    ImageView explore_city_image;
    LinkedHashMap<String, TourismSpotModel> data_models_map = new LinkedHashMap<>();
    ProgressBar progressBar;
    enum city_options  {
            DELHI, CURR_LOC
    }
    city_options selected_city = null;
    enum city_fetch_status {
        LOCATION_NOT_FOUND, LOCALITY_NULL, FOUND
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_explore);
        explore_city_image = findViewById(R.id.explore_city_image);
        progressBar = findViewById(R.id.explore_progress);
        currentUser = mAuth.getCurrentUser();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        explore_city_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                fetchLocation();
                CFAlertDialog.Builder builder = new CFAlertDialog.Builder(ExploreActivity.this);
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
                        progressBar.setVisibility(View.VISIBLE);
                        if(selected_city == city_options.DELHI){
                            save_shared_pref(0, "Delhi");
                            updateUI(0, "Delhi");
                        }else if(selected_city == city_options.CURR_LOC){
                            String city = fetchLocation();
                            if(city != null){
                                save_shared_pref(Integer.valueOf(city.charAt(0)), city.substring(1));
                                updateUI(Integer.valueOf(city.charAt(0)), city.substring(1));
                            }else{
                                updateUI(3, null);
                            }
                        }


                    }
                });
                builder.show();
            }
        });
        rootRef = FirebaseFirestore.getInstance();
        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.shared_pref_file_name), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
//        loadTripData();
        setUpRecyclerView();
        adapter.startListening();
        bottomNavigation();
    }


    

    private void updateUI(int i, String city) {
        switch (i){
            case 0:
                Toast.makeText(this, "Delhi", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, "City: " + city, Toast.LENGTH_SHORT).show();

                break;
            case 2:
                Toast.makeText(this, "LatLon: " + city, Toast.LENGTH_SHORT).show();

                break;
            case 3:
                Toast.makeText(this, "City could not be fetched", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void save_shared_pref(Integer status, String city) {
        editor.putInt("city_code", status);
        editor.putString("current_city", city);
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        loadTripData();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        easyWayLocation.endUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        easyWayLocation.startLocation();
//        loadTripData();
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

    private String fetchLocation() {
        final city_fetch_status[] status = {null};
        final String[] response = {null};

        while (ActivityCompat.checkSelfPermission((Activity)this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
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
                                                Log.d(TAG, "onFinished: 11");
                                                status[0] = city_fetch_status.FOUND;
                                                response[0] = results.get(0).getLocality();
                                            }
                                            else {
                                                Log.d(TAG, "onFinished: 22");
                                                status[0] = city_fetch_status.LOCALITY_NULL;
                                                response[0] = location.getLatitude() + " " +  location.getLongitude();
                                            }
                                        }else{
                                            Log.d(TAG, "onFinished: 33");
                                            status[0] = city_fetch_status.LOCALITY_NULL;
                                            response[0] = location.getLatitude() + " " +  location.getLongitude();
                                        }
                                    }
                                });
                            }else{
                                Log.d(TAG, "onFinished: 44");
                                status[0] = city_fetch_status.LOCATION_NOT_FOUND;
                            }
                        }
                    });
        }else{

        }
        if(status[0]!=null){
            Log.d(TAG, "onFinished: status not null ");
            switch (status[0]){
                case FOUND:
                    Log.d(TAG, "onFinished: resp " + response[0]);
                    return "1"+response[0];
//                break;
                case LOCALITY_NULL:
                    return "2" + response[0];
//                break;
                case LOCATION_NOT_FOUND:
                    return null;
//                break;
            }
        }
        Log.d(TAG, "onFinished: status null");

        return null;
    }


}
