package com.example.testapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationClient;
    LatLng origin, destination;
    Boolean optimization = true;
    private GoogleMap map;
    private StringBuffer waypoints_coordinates;
    private ArrayList<Integer> trip_indices;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        loadMapsData();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fetchLocation();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        getWaypointsCoordinates("delhi");

        final Switch optimize_switch = findViewById(R.id.optimize_switch);

        optimize_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                optimization = optimize_switch.isChecked();
                Toast.makeText(getApplicationContext(), "" + optimization, Toast.LENGTH_SHORT).show();
                while (origin == null || destination == null) {
                }

                map.clear();

                String url = getUrl(origin, destination, optimization);
                DownloadTask task = new DownloadTask();
                task.execute(url);

            }
        });

    }

    private void saveMapsData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(trip_indices);
        editor.putString("trip_indices", json);
        editor.apply();
    }

    private void loadMapsData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("trip_indices", null);
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        trip_indices = gson.fromJson(json, type);
        if(trip_indices == null){
            trip_indices = new ArrayList<Integer>();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

        if (!success) {
            Log.e("changing ui", "Style parsing failed.");
        }
    }

    private void fetchLocation() {

        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
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
                                ActivityCompat.requestPermissions(MapsActivity.this,
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
                ActivityCompat.requestPermissions(MapsActivity.this,
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

                                double lat1,lon1,lat2,lon2;
                                double hotel_lat1,hotel_lon1;
                                /*lat1 = location.getLatitude();
                                lon1 = location.getLongitude();*/

                                hotel_lat1 = 28.651685;
                                hotel_lon1 = 77.217220;
                                lat2 = trip_indices.get(trip_indices.size()-1); //humayu's tomb lat
                                lon2 = 77.2507492; //humayu's tomb lon

                                /*lat2 = hotel_lat1;
                                lon2 = hotel_lon1;*/

                                //**********Remember to change origin to lat1 and lat2.
                                origin = new LatLng(hotel_lat1,hotel_lon1);
                                destination = new LatLng(lat2,lon2);
                                //Toast.makeText(MapsActivity.this, "latitude is"+origin.latitude+"\nlongitude is"+origin.longitude, Toast.LENGTH_LONG).show();

                            }
                        }
                    });

        }
    }

    private class DownloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(urls[0]);
                InputStream in = url.openStream();
                InputStreamReader reader = new InputStreamReader(in);
                char[] buffer = new char[1024];
                int bytesRead = reader.read(buffer);
                while(bytesRead != -1){

                    result.append(buffer,0,bytesRead);
                    bytesRead = reader.read(buffer);

                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "not posibble";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("json response is ",result);

            if(origin != null){
                map.addMarker(new MarkerOptions().position(origin).title("your location"));
                map.addMarker(new MarkerOptions().position(destination).title("destination"));
                map.moveCamera(CameraUpdateFactory.newLatLng(origin));
                map.animateCamera(CameraUpdateFactory.zoomTo(12));
            }else{
                Log.e("originNull", "onMapReady: kya ho rha h?");
            }

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

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

                MapsDataParser parser = new MapsDataParser();

                // Starts parsing data
                routes = parser.parse(jObject);

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

                    Log.d("onPostExecute","onPostExecute lineoptions decoded");

                }

                // Drawing polyline in the Google Map for the i-th route
                if(lineOptions != null) {
                    map.addPolyline(lineOptions);
                }
                else {
                    Log.d("onPostExecute","without Polylines drawn");
                }
            }

            else{
                Toast.makeText(MapsActivity.this,"No Internet Connection Available",Toast.LENGTH_SHORT).show();
            }

        }
    }

    private String getUrl(LatLng origin, LatLng dest,Boolean opt) {

        //Directions API URL
        String directions_api = "https://maps.googleapis.com/maps/api/directions/";

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        /*String LLR = "22.321917,87.303345";
        String MAIN_BUILDING = "22.320256,87.310077";

        String waypoints_coordinates = "|" + MAIN_BUILDING + "|" + LLR;*/

        // Sensor enabled
        String sensor = "sensor=true";

        String waypoints;

        while(true){
            if (waypoints_coordinates == null) {
            } else{
                waypoints = "waypoints=optimize:" + opt + waypoints_coordinates.toString();
                break;
            }
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;

        // Output format
        String output = "json";

        //API KEY
        String apiKey ="key="+"YOUR_API_KEY" ;

        return directions_api+output+"?"+parameters+"&"+apiKey+"\n";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private HashMap<Integer, DocumentReference> CreateDocReference(String... cities) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference city_reference = db.collection("ts_data").document(cities[0]);
        HashMap<Integer, DocumentReference> tourist_places = new HashMap<>();

        for (int id : trip_indices) {
            String tourist_places_id = cities[0] + "::" + id;
            tourist_places.put(id,city_reference.collection(cities[0] + "_data").document(tourist_places_id));
        }
        return tourist_places;
    }

    private void getWaypointsCoordinates(String... cities) {
        HashMap<Integer, DocumentReference> tp = CreateDocReference(cities);
        if (tp != null) {
            final StringBuffer tmp = new StringBuffer("");
            for(final int id : trip_indices){
                tp.get(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            assert document != null;
                            if (!document.exists()) Log.d("existence: ", "No such document");
                            else {

                                tmp.append("|").append(document.get("lat")).append(",").append(document.get("long"));

                                if(id == trip_indices.get(trip_indices.size() - 1)){
                                    setWaypointsCoordinates(tmp);
                                }

                            }
                        } else {
                            Log.d("error: ", "got failed with ", task.getException());
                        }
                    }
                });
            }
        }
    }

    private void setWaypointsCoordinates(StringBuffer tmp) {
        waypoints_coordinates = tmp;
        Log.d("waypoints in setWay ","" + waypoints_coordinates);
    }

}





