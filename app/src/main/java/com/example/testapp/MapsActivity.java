package com.example.testapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.logicbeanzs.uberpolylineanimation.MapAnimator;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    LatLng origin,destination;
    Boolean optimization;
    Switch optimize_switch;
    String opt_off,opt_on;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    LinkedHashMap<Integer, HashMap<String,String>> trip_data = new LinkedHashMap<>();
    ArrayList<Integer> waypoint_order = new ArrayList<>();
    Boolean same;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        loadTripData();
        Intent it = getIntent();
        optimization = it.getExtras().getBoolean("optimization");
        origin = it.getParcelableExtra("origin");
        destination = it.getParcelableExtra("destination");
        waypoint_order = it.getExtras().getIntegerArrayList("waypoints");
        same  = it.getExtras().getBoolean("same");

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

                if(same){
                    Toast.makeText(MapsActivity.this,
                            "Trip Already Optimized", Toast.LENGTH_SHORT).show();
                }else{
                    loadApiResult(optimization);
                    plotMap(optimization);
                }

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

        if(origin != null && destination != null && map != null){
            map.addMarker(new MarkerOptions().position(origin).title("Start location")).showInfoWindow();
            //map.addMarker(new MarkerOptions().position(destination).title("destination"));
            map.moveCamera(CameraUpdateFactory.newLatLng(origin));
            map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);

            for(int id:trip_data.keySet()){
                double lat = Double.parseDouble(trip_data.get(id).get("lat"));
                double lon = Double.parseDouble(trip_data.get(id).get("lon"));
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(lat,lon))
                );
            }

            //addMarkerstoMap();
        }else{
        Log.e("map null", "onMapReady: kya ho rha h?");
        }

        if (!success) {
        Log.e("changing ui", "Style parsing failed.");
        }
    }

    /*private void addMarkerstoMap() {
        if(!optimization){
            int count = 1;
            for(int id:trip_data.keySet()){
                double lat = Double.parseDouble(trip_data.get(id).get("lat"));
                double lon = Double.parseDouble(trip_data.get(id).get("lon"));
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(lat,lon))
                        .title("" + count)
                );
                count++;
            }
        }else{
            Object[] ids_array = trip_data.keySet().toArray();
            int count = 1;
            if(waypoint_order != null) {
                for (int index : waypoint_order) {
                    int id = (int) ids_array[index];
                    double lon = Double.parseDouble(trip_data.get(id).get("lon"));
                    double lat = Double.parseDouble(trip_data.get(id).get("lat"));
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
                Toast.makeText(MapsActivity.this,"No Internet Connection Available",Toast.LENGTH_SHORT).show();
            }

        }

    }

}





