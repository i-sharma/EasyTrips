package com.example.testapp;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class CurrentTripActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    ViewPager viewPager;
    CurrentTripAdapter adapter;
    List<CurrentTripModel> models = new ArrayList<>();
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    Button route;
    LinearLayout removeItem;
    BottomNavigationView navigation;

    LinkedHashMap<Integer, HashMap<String,String>> trip_data = new LinkedHashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_current_trip);
        loadTripData();

        route = findViewById(R.id.showRoute);
        viewPager = findViewById(R.id.viewPager);
        removeItem = findViewById(R.id.removeItemFromTrip);

        setViewPagerBackground();

        createStorageReference("delhi");

        bottomNavigation();

        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadTripData();
                Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

        removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadTripData();
                removeFromModel();
                saveTripData();
            }
        });

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

    private void createStorageReference(String... cities) {

        HashMap<Integer, DocumentReference> tp = CreateDocReference(cities);
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        for(final int id : trip_data.keySet()){

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

        if(trip_data.isEmpty()){
            Toast.makeText(getBaseContext(),"Trip size 0",Toast.LENGTH_SHORT).show();
        }else{
            Log.d("deleting:",""+viewPager.getCurrentItem());
            int position = viewPager.getCurrentItem();
            int curr_id = models.get(position).getId();
            trip_data.remove(Integer.valueOf(curr_id));
            models.remove(position);

            adapter = new CurrentTripAdapter(models, this);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(position);
            viewPager.setPadding(100, 0, 100, 0);
        }

    }

    private void addToModel(Uri result, String title, String short_description, int id) {
        models.add(new CurrentTripModel(result,title,short_description,id));
        Log.d("model contains ",""+title);

        adapter = new CurrentTripAdapter(models, this);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(130, 0, 130, 0);

    }

    private String getUrl(LatLng origin, LatLng dest, Boolean opt) {

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
        String apiKey ="key="+"***REMOVED***" ;

        return directions_api+output+"?"+parameters+"&"+apiKey+"\n";
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
            return "not posibble";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("json response is ",result);

    }
    }


}
