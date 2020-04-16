package com.example.testapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
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

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationClient;
    LatLng origin,destination;

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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        //fetchLocation();

        rootRef = FirebaseFirestore.getInstance();

        loadTripData();
        setUpRecyclerView();
        adapter.startListening();
        bottomNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadTripData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTripData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.stopListening();
    }

    private void bottomNavigation() {
        loadTripData();
        navigation = (BottomNavigationView) findViewById(R.id.navigation_bar);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item0:
                        break;
                    case R.id.menu_item1:
                        Intent a = new Intent(Explore.this, CurrentTripActivity.class);
                        a.putExtra("origin",origin);
                        a.putExtra("destination",destination);
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


    private void loadTripData() {
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

    private void fetchLocation() {

        if (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getParent(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                new AlertDialog.Builder(this)
                        .setTitle("Required Location Permission")
                        .setMessage("Give permission to access this feature")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getParent(),
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object

                                double lat1,lon1;

                                lat1 = location.getLatitude();
                                lon1 = location.getLongitude();

                                //**********Remember to change origin to lat1 and lat2.
                                origin = new LatLng(lat1,lon1);
                                destination = origin;
                                //Toast.makeText(Explore.this, "latitude is"+origin.latitude+"\nlongitude is"+origin.longitude, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


}
