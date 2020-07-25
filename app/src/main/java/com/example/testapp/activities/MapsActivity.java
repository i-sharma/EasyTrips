package com.example.testapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.example.testapp.models.TourismSpotModel;
import com.example.testapp.utils.MapsDataParser;
import com.example.testapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.Maps;
import com.logicbeanzs.uberpolylineanimation.MapAnimator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private static final String TAG = "MapsActivity";

    LatLng origin,destination;
    Boolean optimization;
    Button changeMapType,navigationBtn,analyticsBtn;
    Switch optimize_switch;
    String opt_off,opt_on,waypoints_coordinates_opt_off,waypoints_coordinates_opt_on;
    private GoogleMap map;
    LinkedHashMap<String, TourismSpotModel> data_models_map = new LinkedHashMap<>();
    ArrayList<Integer> waypoint_order = new ArrayList<>();
    long dist_opt_off,dist_opt_on,time_opt_off,time_opt_on;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        loadTripData();
        loadApiResult(false);
        loadApiResult(true);

        Intent it = getIntent();
        optimization = it.getExtras().getBoolean("optimization");
        origin = it.getParcelableExtra("origin");
        destination = it.getParcelableExtra("destination");
        waypoint_order = it.getExtras().getIntegerArrayList("waypoints");
        waypoints_coordinates_opt_off = it.getExtras().getString("wc_opt_off");
        waypoints_coordinates_opt_on = it.getExtras().getString("wc_opt_on");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        analyticsBtn = findViewById(R.id.analyticsBtn);
        optimize_switch = findViewById(R.id.optimize_switch);
        changeMapType = findViewById(R.id.change_map_type);
        navigationBtn = findViewById(R.id.navigationBtn);
        optimize_switch.setChecked(optimization);

        //this is the condition to call direction api
        if(data_models_map.keySet().size() > 0)  {
            Log.d(TAG, "onCreate: checking conddd");
            loadApiResult(optimization);
            plotMap(optimization);
        }

        createOnClickListeners();

    }

    private void getDistanceAndTime() {
        JSONObject jObject_on,jObject_off;
        try {
            jObject_on = new JSONObject(opt_on);
            jObject_off = new JSONObject(opt_off);
            MapsDataParser parser_on = new MapsDataParser(jObject_on);
            MapsDataParser parser_off = new MapsDataParser(jObject_off);
            dist_opt_on = parser_on.getTotalDistanceAndTime().get(0);
            time_opt_on = parser_on.getTotalDistanceAndTime().get(1);
            dist_opt_off = parser_off.getTotalDistanceAndTime().get(0);
            time_opt_off = parser_off.getTotalDistanceAndTime().get(1);
            Log.d(TAG, "off distance is "+dist_opt_off);
            Log.d(TAG, "on distance is "+dist_opt_on);
            Log.d(TAG, "off time is "+time_opt_off);
            Log.d(TAG, "on time is "+time_opt_on);
        }catch (Exception e)    {
            e.printStackTrace();
        }
    }

    private void createOnClickListeners() {

        analyticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(opt_off == ""){
                    Toast.makeText(MapsActivity.this,"NO INTERNET CONNECTION",Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(getBaseContext(), GraphicalAnalysisActivity.class);
                    startActivity(intent);
                }
            }
        });

        optimize_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                optimization = optimize_switch.isChecked();

                ArrayList<Integer> trip_data_array = new ArrayList<>(waypoint_order.size());
                for (int i = 0; i < waypoint_order.size(); i++) {
                    trip_data_array.add(i);
                }

                Boolean same = false;

                if (waypoint_order.size() == data_models_map.keySet().size() - 1 ||
                        waypoint_order.size() == data_models_map.keySet().size() - 2) {
                    same = true;
                    for (int i = 0; i < waypoint_order.size(); i++) {
                        if (waypoint_order.get(i) != trip_data_array.get(i)) {
                            same = false;
                            break;
                        }
                    }
                }

                if (same) {
                    Toast.makeText(MapsActivity.this,
                            "Trip Already Optimized", Toast.LENGTH_SHORT).show();
                }else{
                    if(data_models_map.keySet().size() <= 3){
                        Toast.makeText(getBaseContext(),"No further optimization",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(optimization)
                            Toast.makeText(MapsActivity.this,
                                    "Optimized", Toast.LENGTH_SHORT).show();
                        loadApiResult(optimization);
                        plotMap(optimization);
                    }
                }

            }
        });
        changeMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeType();
            }
        });
        navigationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: wc_opt_on is "+ waypoints_coordinates_opt_on);

                String url_opt_off = "https://www.google.com/maps/dir/?api=1&destination="+destination.latitude+","+destination.longitude+"&origin="+origin.latitude+","+origin.longitude+"&waypoints="+waypoints_coordinates_opt_off+"&travelmode=driving&dir_action=navigate";
                String url_opt_on = "https://www.google.com/maps/dir/?api=1&destination="+destination.latitude+","+destination.longitude+"&origin="+origin.latitude+","+origin.longitude+"&waypoints="+waypoints_coordinates_opt_on+"&travelmode=driving&dir_action=navigate";

                if(optimization){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url_opt_on));
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url_opt_off));
                    startActivity(intent);
                }
            }
        });
    }

    private void changeType() {
        if(map.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        else if(map.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        else if(map.getMapType() == GoogleMap.MAP_TYPE_HYBRID){
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        boolean success = map.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

        if(origin != null && destination != null && map != null){
            map.addMarker(new MarkerOptions().position(origin).title("Start location")).showInfoWindow();
            //map.addMarker(new MarkerOptions().position(destination).title("destination"));
            map.moveCamera(CameraUpdateFactory.newLatLng(origin));
            map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);

            for(String id:data_models_map.keySet()){

                double lon = Double.parseDouble(data_models_map.get(id).getLon());
                double lat = Double.parseDouble(data_models_map.get(id).getLat());

                map.addMarker(new MarkerOptions()
                        .position(new LatLng(lat,lon))
                );

            }

            //addMarkerstoMap();
        }else if(origin != null && destination != null ){
        Log.e("map null", "onMapReady: kya ho rha h?");
        }
        else if(origin == null) Log.d(TAG,"origin is null");

        if (!success) {
        Log.e("changing ui", "Style parsing failed.");
        }
    }

    /*private void addMarkerstoMap() {
        if(!optimization){
            int count = 1;
            for(int id:data_models_map.keySet()){
                double lat = Double.parseDouble(data_models_map.get(id).get("lat"));
                double lon = Double.parseDouble(data_models_map.get(id).get("lon"));
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(lat,lon))
                        .title("" + count)
                );
                count++;
            }
        }else{
            Object[] ids_array = data_models_map.keySet().toArray();
            int count = 1;
            if(waypoint_order != null) {
                for (int index : waypoint_order) {
                    int id = (int) ids_array[index];
                    double lon = Double.parseDouble(data_models_map.get(id).get("lon"));
                    double lat = Double.parseDouble(data_models_map.get(id).get("lat"));
                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lon))
                            .title("" + count)
                    );
                    count++;
                }
            }else{
                Log.d("why waypoint_order is",null+"");
            }
        }
    }*/

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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }
        // Executes in UI thread, after the parsing process
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            //PolylineOptions lineOptions = null;

            if(result != null){
                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<>();
                    //lineOptions = new PolylineOptions();

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
                    /*lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(Color.YELLOW);*/

                    MapAnimator.getInstance().animateRoute(map,points);

                    Log.d("onPostExecute","onPostExecute lineoptions decoded");

                }

                // Drawing polyline in the Google Map for the i-th route
                /*if(lineOptions != null) {
                    map.addPolyline(lineOptions);
                }
                else {
                    Log.d("onPostExecute","without Polylines drawn");
                }*/

            }
            else{
                Toast.makeText(MapsActivity.this,"NO INTERNET CONNECTION",Toast.LENGTH_SHORT).show();
            }

        }

    }

}





