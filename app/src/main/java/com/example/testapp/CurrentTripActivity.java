package com.example.testapp;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.logicbeanzs.uberpolylineanimation.MapAnimator;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class CurrentTripActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    LatLng origin,destination;
    ViewPager viewPager;
    CurrentTripAdapter adapter;
    List<CurrentTripModel> model_opt_off = new ArrayList<>();
    List<CurrentTripModel> model_opt_on =  new ArrayList<>();
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    Button route;
    Switch opt_switch;
    Boolean optimization = false;
    LinearLayout removeItem;
    BottomNavigationView navigation;
    String opt_off,opt_on;
    LinkedHashMap<Integer, HashMap<String,String>> trip_data = new LinkedHashMap<>();
    ArrayList<Integer> waypoint_order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_current_trip);
        loadTripData();

        for(int id:trip_data.keySet()){
            Log.d("original sent trip_data",id+"");
        }

        Intent i = getIntent();
        origin = i.getParcelableExtra("origin");
        destination = i.getParcelableExtra("destination");

        opt_switch = findViewById(R.id.optimize_switch);
        route = findViewById(R.id.showRoute);
        viewPager = findViewById(R.id.viewPager);
        removeItem = findViewById(R.id.removeItemFromTrip);

        setViewPagerBackground();

        if(trip_data.keySet().size() > 1)  optimizeRoute();

        createStorageReference("delhi");

        bottomNavigation();

        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(trip_data.keySet().size() > 0){
                    loadTripData();
                    Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                    intent.putExtra("optimization",optimization);
                    intent.putExtra("origin",origin);
                    intent.putExtra("destination",destination);
                    intent.putExtra("waypoints",waypoint_order);
                    startActivity(intent);
                }else{
                    Toast.makeText(getBaseContext(),"Trip size 0",Toast.LENGTH_SHORT).show();
                }
            }
        });

        removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadTripData();
                optimization = false;
                boolean was_checked = opt_switch.isChecked();
                opt_switch.setChecked(false); //because it may not be optimized.
                removeFromModel(was_checked);
                saveTripData();
                if(trip_data.keySet().size() >= 1) {
                    opt_off = opt_on = "";
                    optimizeRoute();
                }
            }
        });

        opt_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                optimization = opt_switch.isChecked();

                if(optimization){
                    if(trip_data.keySet().size() > 1){
                        loadApiResult(optimization);
                        //opt_on has response //create new model as model_opt_on
                        JSONObject jObject;
                        try {
                            jObject = new JSONObject(opt_on);
                            MapsDataParser parser = new MapsDataParser(jObject);
                            waypoint_order = parser.get_waypoint_order();
                            Log.d("waypoints ",waypoint_order+"");
                            if(waypoint_order.size() == trip_data.keySet().size()){
                                boolean same = true;
                                ArrayList<Integer> trip_data_array = new ArrayList<>(waypoint_order.size());
                                for(int i = 0; i < waypoint_order.size(); i++){
                                    trip_data_array.add(i);
                                }
                                Log.d(TAG, "check toast " + trip_data_array + "----" + waypoint_order);
                                for(int i = 0; i < waypoint_order.size(); i++){
                                    if(waypoint_order.get(i) != trip_data_array.get(i)){
                                        same = false;
                                    }
                                }
                                if(same){
                                    Toast.makeText(CurrentTripActivity.this,
                                            "Trip Already Optimized", Toast.LENGTH_SHORT).show();
                                }else{
                                    applyModel_opt_on();
                                }
                            }


                        }catch (Exception e){e.printStackTrace();}
                    }else{
                        Toast.makeText(getBaseContext(),"No further optimization",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(trip_data.keySet().size() > 1) {
                        updateModel(0);
                    }
                }

    }
});

    }

    private void applyModel_opt_on() {
        // waypoint (0,3,1,2)    (0,1,2,3)
        model_opt_on = new ArrayList<>();
        for(int index:waypoint_order){
            model_opt_on.add(model_opt_off.get(index));
        }
        updateModel(0);
    }

    private void optimizeRoute() {

        StringBuffer waypoints_coordinates ;
        waypoints_coordinates = getWaypointsCoordinates();

        double hotel_lat = 28.651685;
        double hotel_lon = 77.217220;

        LatLng tmp_origin,tmp_destination;
        tmp_origin = new LatLng(hotel_lat,hotel_lon);
        tmp_destination = tmp_origin;

        //change this
        origin = tmp_origin;
        destination = tmp_destination;

        String url_opt_is_false = getUrl(tmp_origin,tmp_destination,false,waypoints_coordinates);
        String url_opt_is_true = getUrl(tmp_origin,tmp_destination,true,waypoints_coordinates);

        DownloadTask task_opt_is_false = new DownloadTask();
        task_opt_is_false.execute(url_opt_is_false);
        DownloadTask task_opt_is_true = new DownloadTask();
        task_opt_is_true.execute((url_opt_is_true));

        try {
            opt_off = task_opt_is_false.get();
            saveApiResult(false);

            opt_on = task_opt_is_true.get();
            saveApiResult(true);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveApiResult(Boolean opt){
        if(!opt){
            try {
                File file = new File(getDir("apiResponse", MODE_PRIVATE), "opt_false");
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(opt_off);
                outputStream.flush();
                outputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            try {
                File file = new File(getDir("apiResponse", MODE_PRIVATE), "opt_true");
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(opt_on);
                outputStream.flush();
                outputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
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

    private void saveTripData(){
        try {
            File file = new File(getDir("data", MODE_PRIVATE), "map");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(trip_data);
            outputStream.flush();
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }

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

    private void bottomNavigation() {
        loadTripData();
        navigation = findViewById(R.id.navigation_bar);
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

    private void setViewPagerBackground() {

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

    private HashMap<Integer, DocumentReference> CreateDocReference(String... cities) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference city_reference = db.collection("ts_data").document(cities[0]);
        HashMap<Integer, DocumentReference> tourist_places = new HashMap<>();

        for (int id : trip_data.keySet()) {
            String tourist_places_id = cities[0] + "::" + id;
            tourist_places.put(id,city_reference.collection(cities[0] + "_data").document(tourist_places_id));
        }
        return tourist_places;
    }

    private void createStorageReference(String... cities) {

        HashMap<Integer, DocumentReference> tp = CreateDocReference(cities);
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        for(final int id : trip_data.keySet()){
            Log.d("proper id is",id+"");

            tp.get(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

                @Override
                public void onSuccess(DocumentSnapshot document) {

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
                        spaceRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                addTo_Model_opt_off(uri,title,short_description,id);
                            }
                        });
                    }
                }
            });
        }
    }

    public void removeFromModel(boolean was_checked) {
        int curr_id;
        if(trip_data.isEmpty()){
            Toast.makeText(getBaseContext(),"Trip is Empty",Toast.LENGTH_SHORT).show();
        }else{

            int position = viewPager.getCurrentItem();
            if(!was_checked){
                curr_id = model_opt_off.get(position).getId();
                model_opt_off.remove(position);
            }else{
                curr_id = model_opt_on.get(position).getId();
                model_opt_on.remove(position);
                for(int i = 0; i < model_opt_off.size(); i++){
                    if(model_opt_off.get(i).getId() == curr_id){
                        model_opt_off.remove(i);
                    }
                }
            }

            Log.d("deleting:",""+viewPager.getCurrentItem());
            trip_data.remove(curr_id);
            updateModel(position);
        }

    }

    private void updateModel(int start_position) {
        if(!optimization)   adapter = new CurrentTripAdapter(model_opt_off, this);
        else                adapter = new CurrentTripAdapter(model_opt_on , this);

        viewPager.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        viewPager.setCurrentItem(start_position);
        viewPager.setPadding(100, 0, 100, 0);
        saveTripData();
    }

    private void addTo_Model_opt_off(Uri result, String title, String short_description, int id) {

        /*model_opt_off.set(index,new CurrentTripModel(result,title,short_description,id));

       */
        model_opt_off.add(new CurrentTripModel(result,title,short_description,id));

        if(model_opt_off.size() == trip_data.keySet().size()){
            int size = model_opt_off.size();
            ArrayList<Integer> ids = new ArrayList<>(trip_data.keySet());
            List<CurrentTripModel> proper_model = new ArrayList<>();
            for(int i=0;i<size;i++){
                proper_model.add(new CurrentTripModel());
            }
            for(int i=0;i<size;i++){
                CurrentTripModel curr_model = model_opt_off.get(i);
                int curr_id = model_opt_off.get(i).getId();
                Log.d("curr_id is ",curr_id+"");
                int index = ids.indexOf(curr_id);
                proper_model.set(index,curr_model);
            }
            model_opt_off = proper_model;
            updateModel(0);
        }

    }

    private String getUrl(LatLng origin, LatLng dest, Boolean opt,StringBuffer waypoints_coordinates) {

        //Directions API URL
        String directions_api = "https://maps.googleapis.com/maps/api/directions/";

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=true";

        String waypoints = waypoints = "waypoints=optimize:" + opt + waypoints_coordinates.toString();

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;

        // Output format
        String output = "json";

        //API KEY
        String apiKey ="key="+"***REMOVED***" ;

        //url
        String url = directions_api+output+"?"+parameters+"&"+apiKey+"\n";
        Log.d("url is ",url);
        return url;
    }

    private class DownloadTask extends AsyncTask<String,Void,String> {

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
            return "not possible";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    private StringBuffer getWaypointsCoordinates() {

        StringBuffer waypoints_coordinates = new StringBuffer("");

        for(int id:trip_data.keySet()){

            String lon = trip_data.get(id).get("lon");
            String lat = trip_data.get(id).get("lat");

            waypoints_coordinates.append("|").append(lat).append(",").append(lon);

        }
        Log.d("waypoint is ",waypoints_coordinates+"");
      return waypoints_coordinates;
    }

}



