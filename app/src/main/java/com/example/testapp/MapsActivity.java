package com.example.testapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.logicbeanzs.uberpolylineanimation.MapAnimator;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    LatLng origin,destination;
    Boolean optimization;
    Switch optimize_switch;
    String opt_off,opt_on;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    ArrayList<Integer> waypoint_order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent it = getIntent();
        optimization = it.getExtras().getBoolean("optimization");
        origin = it.getParcelableExtra("origin");
        destination = it.getParcelableExtra("destination");
        loadApiResult(optimization);

        optimize_switch = findViewById(R.id.optimize_switch);
        optimize_switch.setChecked(optimization);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        plotMap(optimization);

        optimize_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optimization = optimize_switch.isChecked();
                loadApiResult(optimization);
                map.clear();
                plotMap(optimization);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

        if(origin != null && map != null){
            map.addMarker(new MarkerOptions().position(origin).title("your location"));
            map.addMarker(new MarkerOptions().position(destination).title("destination"));
            map.moveCamera(CameraUpdateFactory.newLatLng(origin));
            map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
        }else{
            Log.e("map null", "onMapReady: kya ho rha h?");
        }

        if (!success) {
            Log.e("changing ui", "Style parsing failed.");
        }
    }

    private void plotMap(Boolean optimization) {

        if(optimization){
            ParserTask parserTask = new ParserTask();
            parserTask.execute(opt_on);
        }
        else{
            ParserTask parserTask = new ParserTask();
            parserTask.execute(opt_off);
        }
    }

    private void loadApiResult(Boolean opt) {
        if (opt) {
            try {
                File file = new File(getDir("apiResponse", MODE_PRIVATE), "opt_true");
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                opt_on = (String) ois.readObject();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                opt_on = "";
            }
        }else{
            try {
                File file = new File(getDir("apiResponse", MODE_PRIVATE), "opt_false");
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                opt_off = (String) ois.readObject();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                opt_off = "";
            }
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);

                MapsDataParser parser = new MapsDataParser(jObject);

                // Starts parsing data
                routes = parser.parse();
                waypoint_order = parser.get_waypoint_order();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            if(result != null){
                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);

                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(Color.YELLOW);

                    MapAnimator.getInstance().animateRoute(map,points);

                    Log.d("onPostExecute","onPostExecute lineoptions decoded");

                }

                // Drawing polyline in the Google Map for the i-th route
                if(lineOptions != null) {
                    //map.addPolyline(lineOptions);
                }
                else {
                    Log.d("onPostExecute","without Polylines drawn");
                }

                Log.d("waypoints ",waypoint_order+"");
            }

            else{
                Toast.makeText(MapsActivity.this,"No Internet Connection Available",Toast.LENGTH_SHORT).show();
            }

        }

    }

}





