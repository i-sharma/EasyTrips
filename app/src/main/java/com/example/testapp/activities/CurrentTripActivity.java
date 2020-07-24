package com.example.testapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.testapp.R;
import com.example.testapp.adapters.CurrentTripAdapter;
import com.example.testapp.dragListView.DragListView;
import com.example.testapp.models.TourismSpotModel;
import com.example.testapp.pendant_popup.SweetAlertDialog;
import com.example.testapp.utils.MapsDataParser;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.suke.widget.SwitchButton;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class CurrentTripActivity extends Activity implements CurrentTripAdapter.ICurrTrip {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;
    private static final String TAG = "MainActivity";
    int current_position = 0, origin_index, destination_index;
    LatLng origin, destination;
    PlacesClient placesClient;
    DragListView dragListView;
    CurrentTripAdapter adapter;
    List<TourismSpotModel> model_opt_off = new ArrayList<>();
    List<TourismSpotModel> model_opt_on = new ArrayList<>();
    Integer[] colors = null;
    Button route, editBtn, doneBtn, customStopBtn, deleteCard, emptyTripBtn;
    com.suke.widget.SwitchButton switchButton;
    Boolean optimization = false;
    LinearLayout removeItem;
    ImageView empty_trip;
    BottomNavigationView navigation;
    String opt_off, opt_on, url_opt_is_false, url_opt_is_true;
    StringBuffer waypoints_coordinates, waypoints_coordinates_opt_on;
    LinkedHashMap<String, TourismSpotModel> data_models_map = new LinkedHashMap<>();
    ArrayList<Integer> waypoint_order = new ArrayList<>();
    ArrayList<String> saved_api_ids = new ArrayList<>();
    Boolean same, somethingSwapped = false, somethingDeleted = false, customStopAdded = false, viewDragged = false; //checks if waypoint_order is same for both non optimized and optimized state
    ProgressBar progressBar;
    RelativeLayout opt_layout;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_current_trip);

        /*if(!isInternetAvailable()){
            Intent intent = new Intent(getBaseContext(), ExploreActivity.class);
            Toast.makeText(getBaseContext(),"NO INTERNET CONNECTION",Toast.LENGTH_LONG).show();
            startActivity(intent);
        }*/

        loadTripData();

        Intent i = getIntent();
        origin = i.getParcelableExtra("origin");
        destination = i.getParcelableExtra("destination");
        sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.shared_pref_file_name), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        route = findViewById(R.id.showRoute);
        dragListView = (DragListView) findViewById(R.id.drag_list_view);
        removeItem = findViewById(R.id.removeItemFromTrip);
        editBtn = findViewById(R.id.editBtn);
        doneBtn = findViewById(R.id.doneBtn);
        emptyTripBtn = findViewById(R.id.emptyTripBtn);
        customStopBtn = findViewById(R.id.customStop);
        progressBar = findViewById(R.id.curr_trip_progress);
        deleteCard = findViewById(R.id.curr_trip_delete);
        switchButton = (com.suke.widget.SwitchButton) findViewById(R.id.optimize_switch);
        opt_layout = findViewById(R.id.optimize_switch_layout);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorDark),
                android.graphics.PorterDuff.Mode.MULTIPLY);
        setViewPagerBackground();

        initializePlaces();
        dragListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        dragListView.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {

            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y) {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                dragListView.getRecyclerView().smoothScrollToPosition(toPosition);
                new DragEndedAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                adapter.notifyDataSetChanged();
                updateCurrentPosition();
                loadOriginDestIdx();
                if(fromPosition < origin_index && toPosition >= origin_index){
                    origin_index -= 1;
                }else if(fromPosition > origin_index && toPosition <=origin_index){
                    origin_index += 1;
                }else if(fromPosition == origin_index){
                    origin_index = toPosition;
                }


                if(fromPosition < destination_index && toPosition >= destination_index){
                    destination_index -= 1;
                }else if(fromPosition > destination_index && toPosition <=destination_index){
                    destination_index += 1;
                }else if(fromPosition == destination_index){
                    destination_index = toPosition;
                }

                saveOriginDestIdx();

            }
        });
        dragListView.setDragEnabled(false);

        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(dragListView.getRecyclerView());


        //show empty_trip_notification and hide everything else
        if (data_models_map.keySet().size() == 0) {
            showEmptyTripUI();
            progressBar.setVisibility(View.GONE);
        } else {

            progressBar.setVisibility(View.VISIBLE);

            //this is essential for maps activity to work, not required when we access location in app
//            setTmpLocation();

            updateModel(0);

            loadOriginDestIdx();
            if(origin_index != -1 && destination_index != -1){
                Log.d(TAG, "onCreate: here");
                new OptimizeAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        bottomNavigation();

        createOnClickListeners();

    }
/*

    public boolean isInternetAvailable() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        } catch (UnknownHostException e) {
            // Log error
        }
        return false;
    }
*/

    @Override
    public void dragTopBottom(Boolean topmost, Boolean bottommost) {
        if(topmost!=null){
            dragListView.setCanNotDragAboveTopItem(topmost);
        }
        if(bottommost!=null){
            dragListView.setCanNotDragBelowBottomItem(bottommost);
        }



    }

    private class DragEndedAsync extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            viewDragged = true;

        }

        @Override
        protected Void doInBackground(Void... voids) {
            data_models_map = new LinkedHashMap<>();
            saved_api_ids = new ArrayList<>();
            for (TourismSpotModel model : model_opt_off) {
                data_models_map.put(model.getId(), model);
                saved_api_ids.add(model.getId());
            }
            editor.putString("saved_api_ids", saved_api_ids.toString());
            editor.commit();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            saveTripData();
        }
    }

    private void initializePlaces() {
        String apiKey = getResources().getString(R.string.autocomplete_api_key);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        placesClient = Places.createClient(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                String title = place.getName();
                String id = place.getId();
                LatLng customLoc = place.getLatLng();
                assert customLoc != null;
                String lat = String.valueOf(customLoc.latitude);
                String lon = String.valueOf(customLoc.longitude);
                String time = "NO ESTIMATE";
                optimization = false;
                customStopAdded = true;
                TourismSpotModel customModel = new TourismSpotModel(id, title, lat, lon, true, time);
                boolean was_checked = switchButton.isChecked();
                boolean was_empty = model_opt_off.isEmpty();
                switchButton.setChecked(false);
                int insert_pos;
                loadOriginDestIdx();
                if (!was_checked) {
                    if(current_position == 0 && origin_index == 0){
                        model_opt_off.add(1, customModel);
                        insert_pos = 1;
                    }else{
                        model_opt_off.add(current_position, customModel);
                        insert_pos = current_position;
                    }

                } else {
                    if(current_position == 0 && origin_index == 0){
                        model_opt_on.add(1, customModel);
                        model_opt_off.add(1, customModel);
                        insert_pos = 1;
                    }else{
                        model_opt_on.add(current_position, customModel);
                        model_opt_off.add(0, customModel);
                        insert_pos = current_position;
                    }

                }
                LinkedHashMap<String, TourismSpotModel> tmp = new LinkedHashMap<>();
                for (TourismSpotModel m : model_opt_off) {
                    tmp.put(m.getId(), m);
                }
                data_models_map.clear();
                data_models_map.putAll(tmp);
                if (data_models_map.size() > 1)
                    dragListView.setDragEnabled(true);
                saveTripData();
                if (was_empty) {
                    removeEmptyTripUI();
                    updateModel(0);
                } else {
                    adapter.notifyItemInserted(insert_pos);
                }
                loadOriginDestIdx();
                if(insert_pos <= origin_index && origin_index > 0)  origin_index += 1;
                if(insert_pos <= destination_index && destination_index > 0) destination_index += 1;
                Log.d(TAG, "onActivityResult: org " + origin_index + " dest " + destination_index);
                editor.putInt("origin_index", origin_index);
                editor.putInt("destination_index", destination_index);
                editor.commit();


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // Start the autocomplete intent.

        //these are bounds for delhi -> update these bounds in firestore database
        LatLng northEast = new LatLng(28.847711, 77.341943);
        LatLng southWest = new LatLng(28.476807, 76.958110);

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields).setCountry("IN")  //INDIA
                .setHint("Stops within Delhi")
                .setLocationRestriction(RectangularBounds.newInstance(
                        southWest, northEast))
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void popUpOnlyOneItem(final popup_switch_route code){
        new SweetAlertDialog(this)
                .setTitleText("There's only one place in your trip. Add more!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if(code == popup_switch_route.OPT_SWITCH){
                            switchButton.setChecked(false);

                        }
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createOnClickListeners() {

        customStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchCalled();
            }
        });

        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(adapter.getItemCount() == 1){
                    popUpOnlyOneItem(popup_switch_route.SHOW_ROUTE);
                    return;
                }
                if((origin_index == -1 || destination_index == -1)){
                    popUpOrgDstNotSet(popup_switch_route.SHOW_ROUTE);
                    return;
                }

                if (data_models_map.keySet().size() > 0) {

                    waypoints_coordinates_opt_on = getWaypoints_coordinates_opt_on();
                    waypoints_coordinates = getWaypointsCoordinates();
                    Log.d(TAG, "In CurrTrip wc_opt_on is " + waypoints_coordinates_opt_on);
                    Log.d(TAG, "In CurrTrip wc_opt_off is " + waypoints_coordinates);
                    Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                    intent.putExtra("optimization", optimization);
                    Log.d(TAG, "from CurrTrip To MapsActivity origin is "+origin);
                    intent.putExtra("origin", origin);
                    intent.putExtra("destination", destination);
                    intent.putExtra("waypoints", waypoint_order);
                    intent.putExtra("wc_opt_off", waypoints_coordinates.toString());
                    intent.putExtra("wc_opt_on", waypoints_coordinates_opt_on.toString());
                    startActivity(intent);

                }

            }
        });

        emptyTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (data_models_map.size() > 0) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(CurrentTripActivity.this);
                    builder1.setMessage("DO YOU WANT TO COMPLETELY REMOVE TRIP ITEMS?");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    data_models_map.clear();
                                    model_opt_on.clear();
                                    model_opt_off.clear();
                                    editor.putInt("origin_index", -1);
                                    editor.putInt("destination_index", -1);
                                    editor.commit();
                                    saveTripData();
                                    showEmptyTripUI();
                                    dialog.dismiss();
                                }
                            });

                    builder1.setNegativeButton(
                            "No, Thanks",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else {
                    Toast.makeText(getBaseContext(), "TRIP ALREADY EMPTY", Toast.LENGTH_LONG).show();
                }
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.setEdit_mode(true);
                adapter.notifyDataSetChanged();
                if (data_models_map.size() > 1)
                    dragListView.setDragEnabled(true);
                customStopBtn.setVisibility(View.VISIBLE);
                editBtn.setVisibility(View.GONE);
                doneBtn.setVisibility(View.VISIBLE);
                removeItem.setVisibility(View.VISIBLE);
                route.setVisibility(View.GONE);
                opt_layout.setVisibility(View.GONE);
                if(origin_index == 0){
                    dragListView.setCanNotDragAboveTopItem(true);
                }
                if(destination_index == adapter.getItemCount() - 1){
                    dragListView.setCanNotDragBelowBottomItem(true);
                }
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.setEdit_mode(false);
                adapter.notifyDataSetChanged();
                dragListView.setDragEnabled(false);
                customStopBtn.setVisibility(View.GONE);
                editBtn.setVisibility(View.VISIBLE);
                doneBtn.setVisibility(View.GONE);
                removeItem.setVisibility(View.GONE);
                route.setVisibility(View.VISIBLE);
                opt_layout.setVisibility(View.VISIBLE);
                loadOriginDestIdx();
                loadApiResult(false); loadApiResult(true);
                if(origin_index!=-1 || destination_index != -1){
                    swapAdapter();
                    swapDataModels();
                }
                if (data_models_map.keySet().size() >= 1 && (somethingDeleted || customStopAdded ||
                        viewDragged || somethingSwapped)){
                    switchButton.setChecked(false);
                }
                saveOriginDestIdx();
                if (data_models_map.keySet().size() >= 1 && (somethingDeleted || customStopAdded ||
                        viewDragged || somethingSwapped) && origin_index!=-1 && destination_index!=-1) {

                    Log.d(TAG, "done onClick: org " + origin_index + " dst " + destination_index);
                    OptimizeAsyncTask optimizeAsyncTask = new OptimizeAsyncTask();
                    optimizeAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    if(opt_off == "") {
                        Log.d(TAG, "doneBtn Pressed no api found");
                        //saveApiResult(false);   saveApiResult(true);
                        if(opt_off == "")
                            Toast.makeText(getBaseContext(), "NO INTERNET CONNECTION", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

            }
        });


        removeItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    removeItem.setBackgroundColor(getResources().getColor(R.color.light_red));
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                    removeItem.setBackgroundColor(0x00000000);
//                    loadTripData();
                    somethingDeleted = true;
                    boolean was_checked = switchButton.isChecked();
                    switchButton.setChecked(false); //because it may not be optimized.
                    removeItem.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    removeFromModel(was_checked);
                    return true;
                }
                return false;
            }
        });


        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if(adapter.getItemCount() == 1 && isChecked){
                    popUpOnlyOneItem(popup_switch_route.OPT_SWITCH);
                    return;
                }
                if((origin_index == -1  || destination_index == -1) && isChecked){
                    popUpOrgDstNotSet(popup_switch_route.OPT_SWITCH);
                    return;
                }
                Log.d(TAG, "onCheckedChanged: hello there");
                optimization = isChecked;
                if (optimization) {
                    if (data_models_map.keySet().size() > 3) {
                        loadApiResult(optimization);

                        if(opt_on == "" || opt_off == "") {
                            Log.d(TAG, "switchBtn Pressed no api found");
                            /*switchButton.setOnCheckedChangeListener (null);
                            switchButton.setChecked(false);
                            switchButton.setOnCheckedChangeListener (this);*/
                            new OptimizeAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            if(opt_on == "")
                                Toast.makeText(getBaseContext(), "NO INTERNET CONNECTION", Toast.LENGTH_LONG).show();
                            saveApiResult(false);   saveApiResult(true);
                            return;
                        }

                        //opt_on has response //create new model as model_opt_on
                        JSONObject jObject;
                        try {
                            jObject = new JSONObject(opt_on);
                            MapsDataParser parser = new MapsDataParser(jObject);
                            waypoint_order = parser.get_waypoint_order();

                            ArrayList<Integer> trip_data_array = new ArrayList<>(waypoint_order.size());
                            for (int i = 0; i < waypoint_order.size(); i++) {
                                trip_data_array.add(i);
                            }
                            Log.d(TAG, "onCheckedChanged: wpo " + waypoint_order);
                            Log.d(TAG, "onCheckedChanged: tda " + trip_data_array);
                            for (String key : data_models_map.keySet()) {
                                Log.d(TAG, "onCheckedChanged: dm " + data_models_map.get(key).getTitle());
                            }
                            if (waypoint_order.size() == data_models_map.keySet().size() - 1 ||
                                    waypoint_order.size() == data_models_map.keySet().size() - 2) {
                                same = true;
                                for (int i = 0; i < waypoint_order.size(); i++) {
                                    if (waypoint_order.get(i) != trip_data_array.get(i)) {
                                        same = false;
                                        Log.d(TAG, "onCheckedChanged: 3");
                                        break;
                                    }
                                }
                                if (same) {
                                    Log.d(TAG, "onCheckedChanged: 4");
                                    Toast.makeText(CurrentTripActivity.this,
                                            "Trip Already Optimized", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d(TAG, "onCheckedChanged: 5");
                                    Toast.makeText(CurrentTripActivity.this,
                                            "Optimized", Toast.LENGTH_SHORT).show();
                                    applyModel_opt_on();
                                }
                            }

                            Log.d(TAG, "onCheckedChanged: 6");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "onCheckedChanged: 7");
                        Toast.makeText(getBaseContext(), "No further optimization", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "onCheckedChanged: 8");
                    if (data_models_map.keySet().size() > 1) {
                        updateModel(current_position);
                    }
                }

            }
        });

    }


    private void swapAdapter() {
        loadOriginDestIdx();
        Boolean adap_swapped = false;
        int n = adapter.getItemCount();
        if (destination_index == 0 && origin_index == n - 1) {
            adapter.swapItems(n - 1, 0);
            adap_swapped = true;
        }else if(destination_index == origin_index){
            adapter.swapItems(origin_index, 0);
            adap_swapped = true;
        } else if (destination_index == 0) {
            Log.d(TAG, "swapAdapter: org " + origin_index + " dest " + destination_index);
            adapter.swapItems(destination_index, n - 1);
            if(origin_index != -1)
                adapter.swapItems(origin_index, 0);
            adap_swapped = true;
        } else if (origin_index == n - 1) {
            adapter.swapItems(origin_index, 0);
            if(destination_index != -1)
                adapter.swapItems(destination_index, n - 1);
            adap_swapped = true;
        } else {
            if(destination_index != -1){
                adapter.swapItems(destination_index, n - 1);
                adap_swapped = true;
            }
            if(origin_index != -1){
                adapter.swapItems(origin_index, 0);
                adap_swapped = true;
            }
        }
        if(adap_swapped){
            adapter.notifyDataSetChanged();

        }

    }

    private void swapDataModels() {
        LinkedHashMap<String, TourismSpotModel> temp = (LinkedHashMap)data_models_map.clone();
        ArrayList<String> keySetList = new ArrayList<>();
        keySetList.addAll(data_models_map.keySet());
        int n = adapter.getItemCount();
        loadOriginDestIdx();
        Boolean dest_at_zero = false, models_swapped = false;
        if (destination_index == 0 && origin_index == n - 1) {
            Collections.swap(keySetList, 0, n - 1);
            models_swapped = true;
        } else if(destination_index == origin_index){
            Collections.swap(keySetList, origin_index, 0);
            dest_at_zero = true;
            models_swapped = true;
        }else if (destination_index == 0) {
            Collections.swap(keySetList, destination_index, n - 1);
            if(origin_index != -1)
                Collections.swap(keySetList, origin_index, 0);
            models_swapped = true;
        } else if (origin_index == n - 1) {
            Collections.swap(keySetList, origin_index, 0);
            if(destination_index != -1)
                Collections.swap(keySetList, destination_index, n - 1);
            models_swapped = true;
        } else {
            if(destination_index != -1){
                Collections.swap(keySetList, destination_index, n - 1);
                models_swapped = true;
            }if(origin_index != -1){
                Collections.swap(keySetList, origin_index, 0);
                models_swapped = true;
            }
        }
        if(!models_swapped) return;
        somethingSwapped = true;
        if(dest_at_zero)    destination_index = 0;
        else if(destination_index != -1)    destination_index = n-1;
        if(origin_index!=-1)    origin_index = 0;
        data_models_map.clear();
        saved_api_ids.clear();

        for (String oldSwappedKey : keySetList) {
            data_models_map.put(oldSwappedKey, temp.get(oldSwappedKey));
            saved_api_ids.add(temp.get(oldSwappedKey).getId());
        }
        editor.putString("saved_api_ids", saved_api_ids.toString());
        editor.commit();
        saveTripData();
    }

    private void loadOriginDestIdx() {
        origin_index = sharedPref.getInt("origin_index", -1);
        destination_index = sharedPref.getInt("destination_index", -1);
    }

    private void saveOriginDestIdx() {
        editor.putInt("origin_index", origin_index);
        editor.putInt("destination_index", destination_index);
        editor.commit();
    }

    private void showEmptyTripUI() {
        progressBar.setVisibility(View.GONE);
        empty_trip = findViewById(R.id.empty_trip_notify);
        empty_trip.setVisibility(View.VISIBLE);
        customStopBtn.setVisibility(View.VISIBLE);
        dragListView.setVisibility(View.GONE);
        opt_layout.setVisibility(View.GONE);
        removeItem.setVisibility(View.GONE);
        route.setVisibility(View.GONE);
        emptyTripBtn.setVisibility(View.GONE);
        editBtn.setVisibility(View.GONE);
        doneBtn.setVisibility(View.GONE);
    }

    private void removeEmptyTripUI() {
        empty_trip = findViewById(R.id.empty_trip_notify);
        empty_trip.setVisibility(View.GONE);
        dragListView.setVisibility(View.VISIBLE);
        opt_layout.setVisibility(View.GONE);
        route.setVisibility(View.GONE);
        editBtn.setVisibility(View.GONE);
        removeItem.setVisibility(View.VISIBLE);
        doneBtn.setVisibility(View.VISIBLE);
    }

    private void applyModel_opt_on() {
        // waypoint (0,3,1,2)    (0,1,2,3)
        model_opt_on = new ArrayList<>();
//        int count = 0;
        if(0 == origin_index){
            model_opt_on.add(model_opt_off.get(0));
        }
        Log.d(TAG, "applyModel_opt_on: " + waypoint_order);
        for (int index : waypoint_order) {
            model_opt_on.add(model_opt_off.get(index+1));
        }
        if(destination_index == adapter.getItemCount() - 1){
            model_opt_on.add(model_opt_off.get(destination_index));
        }
        current_position = 0;
        updateModel(current_position);
    }

    private void optimizeRoute() {

        Log.d(TAG, "optimizeRoute: is being called");
        
        ArrayList<String> temp = new ArrayList<>();
        int count = 0;
        String origin_key = "", dest_key = "";
        for (String s : data_models_map.keySet()) {
            // Check if string contains only numbers
            temp.add(s);
            if(count == origin_index){
                origin_key = s;
            }if(count == destination_index){
                dest_key = s;
            }
            count += 1;
        }
        Collections.sort(temp);
        setLatLong(origin_key, dest_key);
        String shared_pref_ids = sharedPref.getString("saved_api_ids", "");

        if (temp.toString().equals(shared_pref_ids) && !customStopAdded &&
                !somethingDeleted && !viewDragged && !somethingSwapped) return;

        Log.d(TAG, "optimizeRoute api getting called");

        waypoints_coordinates = getWaypointsCoordinates();

        url_opt_is_false = getUrl(origin, destination, false, waypoints_coordinates);
        Log.d(TAG, "optimizeRoute: url off " + url_opt_is_false);
        DownloadTask task_opt_is_false,task_opt_is_true = null;
        task_opt_is_false = new DownloadTask();
        task_opt_is_false.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url_opt_is_false);

        if(data_models_map.size() > 3){
            url_opt_is_true = getUrl(origin, destination, true, waypoints_coordinates);
            Log.d(TAG, "optimizeRoute: url on " + url_opt_is_true);
            task_opt_is_true = new DownloadTask();
            task_opt_is_true.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url_opt_is_true);
        }

        try {
            opt_off = task_opt_is_false.get();
            Log.d(TAG, "optimizeRoute: opt_off is " + opt_off);

            if(data_models_map.size() > 3 && task_opt_is_true != null) {
                opt_on = task_opt_is_true.get();
                Log.d(TAG, "optimizeRoute: opt_on is " + opt_on);
            }else{
                opt_on = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        saveApiResult(false);   saveApiResult(true);

    }


    private class OptimizeAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            somethingDeleted = false;
            customStopAdded = false;
            viewDragged = false;
            somethingSwapped = false;
//                saveApiResult(optimization);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: we are here");
            optimizeRoute();
            return null;
        }
    }

    private int getCurrentItem() {
        int pos = ((LinearLayoutManager) (dragListView.getRecyclerView().getLayoutManager())).findFirstCompletelyVisibleItemPosition();
        if (pos >= 0)
            return pos;
        return -1;
    }

    private void updateCurrentPosition() {
        int position = getCurrentItem();
        if (position != -1) {
            current_position = position;
        }
        if (current_position > (data_models_map.size() - 1))
            current_position = Math.max(0, data_models_map.size() - 1);
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


        dragListView.getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {

//            ScrollDirection scrollDirection;
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if(dx > 0)  scrollDirection = ScrollDirection.RIGHT;
//                else if(dx < 0)  scrollDirection = ScrollDirection.LEFT;
////                Log.d(TAG, "onScrolled: dx " + dx + " dy " + dy);
//            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateCurrentPosition();
                    if (adapter != null) {
                        int idx = current_position % (colors.length - 1);
                        if (idx < 0) idx = (-1 * idx) % (colors.length - 1);
                        dragListView.setBackgroundColor(colors[idx]);
                    }
                }
            }
        });
    }

    private enum popup_remove {
        ONLY_ONE_ELEM,
        POS_ZERO_ISORIGIN,
        POS_LAST_ISDEST,
        POS_ZERO_ISORIGIN_AND_ISDEST
    }

    private enum popup_switch_route {
        SHOW_ROUTE,
        OPT_SWITCH
    }

    private void change_origin_dest_index() {
        loadOriginDestIdx();
        if(current_position < origin_index){
            origin_index -= 1;
        }else if(current_position == origin_index){
            origin_index = -1;
        }

        if(current_position < destination_index){
            destination_index -= 1;
        }else if(current_position == destination_index){
            destination_index = -1;
        }
        editor.putInt("origin_index", origin_index);
        editor.putInt("destination_index", destination_index);
        editor.commit();
    }

    private void popUpOrgDstNotSet(final popup_switch_route code){

        loadOriginDestIdx();
        String title = "", content = "";
        if(origin_index == -1 && destination_index == -1){
            title = "Set Origin and Destination!";
            content = "Hey, we need a place to start and one to end. " +
                    "Please click on Edit Trip button and select where you would like to begin and end. Cheers!";
        }else if(origin_index == -1){
            title = "Please Set Origin";
            content = "Hey, seems like you forgot to tell us where to start your trip. " +
                    "Please click on Edit Trip button and indicate where you would like to begin. Cheers!";
        }else if(destination_index == -1){
            title = "Please Set Destination!";
            content = "Hey, seems like you forgot to tell us where to end your trip. " +
                    "Please click on Edit Trip button and indicate where you would like to end. Cheers!";
        }

        new SweetAlertDialog(CurrentTripActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(title)
                .setContentText(content)
                .setCustomImage(R.drawable.how_to_set_org_dest)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        switch(code){
                            case OPT_SWITCH:
                                switchButton.setChecked(false);
                                break;
                        }
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    private void popUpOnRemove(final popup_remove code, final Boolean was_checked) {
        String title = "", content = "", button_message = "Yes, Please!";
        switch (code) {
            case ONLY_ONE_ELEM:
                title = "Empty The Trip?";
                content = "There's only one element in the trip. Do you want to empty it?";
                button_message = "Yes,remove it!";
                break;
            case POS_ZERO_ISORIGIN:
                title = "Remove Origin?";
                content = "Are You Sure? Don't forget to set a new Origin!";
                break;
            case POS_LAST_ISDEST:
                title = "Remove Destination?";
                content = "Are You Sure? Don't forget to set a new Destination!";
                break;
            case POS_ZERO_ISORIGIN_AND_ISDEST:
                title = "Remove Origin and Destination?";
                content = "Sure? Don't forget to set a new Origin and Destination!";
                break;
        }
        new SweetAlertDialog(CurrentTripActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(content)
                .setConfirmText(button_message)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        change_origin_dest_index();
                        reallyRemoveFromModel(was_checked);
                        sDialog.dismissWithAnimation();
                    }
                })
                .setCancelText("No way!")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        removeItem.setVisibility(View.VISIBLE);
                    }
                })
                .show();
    }

    public void removeFromModel(boolean was_checked){
//        String curr_id;
        Boolean already_removed = false;
        if (data_models_map.isEmpty()) {
            Toast.makeText(getBaseContext(), "Trip is Empty", Toast.LENGTH_SHORT).show();
            showEmptyTripUI();
        } else {
            if (adapter.getItemCount() == 1) {
                popUpOnRemove(popup_remove.ONLY_ONE_ELEM, was_checked);
            } else {
                loadOriginDestIdx();
                if(current_position == origin_index && destination_index == current_position){
                    popUpOnRemove(popup_remove.POS_ZERO_ISORIGIN_AND_ISDEST, was_checked);
                }
                else if (current_position == origin_index) {
                    popUpOnRemove(popup_remove.POS_ZERO_ISORIGIN, was_checked);

                } else if (current_position == destination_index) {
                    popUpOnRemove(popup_remove.POS_LAST_ISDEST, was_checked);
                }
                else {
                    change_origin_dest_index();
                    reallyRemoveFromModel(was_checked);

                }
            }
        }
    }

    private void reallyRemoveFromModel(Boolean was_checked) {
        String curr_id;
        if (!was_checked) {
            curr_id = model_opt_off.get(current_position).getId();
            model_opt_off.remove(current_position);
        } else {
            curr_id = model_opt_on.get(current_position).getId();
            model_opt_on.remove(current_position);
            for (int i = 0; i < model_opt_off.size(); i++) {
                if (model_opt_off.get(i).getId() == curr_id) {
                    model_opt_off.remove(i);
                    break;
                }
            }
        }
        data_models_map.remove(curr_id);
        saved_api_ids.clear();
        for (String s : data_models_map.keySet())
            saved_api_ids.add(s);

        editor.putString("saved_api_ids", saved_api_ids.toString());
        editor.commit();
        adapter.notifyItemRemoved(current_position);
        adapter.notifyItemRangeChanged(current_position, model_opt_off.size() - current_position);
        updateCurrentPosition();
        progressBar.setVisibility(View.GONE);
        removeItem.setVisibility(View.VISIBLE);
        optimization = false;
        saveTripData();
        if (data_models_map.keySet().size() == 0) {
            showEmptyTripUI();
        }
    }






    private DisplayMetrics getDisplayMetrics() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }


    private void updateModel(int start_position) {
        if (!optimization)
            adapter = new CurrentTripAdapter(model_opt_off, this, getDisplayMetrics(), true);
        else adapter = new CurrentTripAdapter(model_opt_on, this, getDisplayMetrics(), true);
        adapter.setItemMargin((int) (getResources().getDimension(R.dimen.pager_margin)));
        adapter.updateDisplayMetrics();
        dragListView.setAdapter(adapter, true);
        dragListView.setCanDragHorizontally(true);
        dragListView.getRecyclerView().smoothScrollToPosition(start_position);
        progressBar.setVisibility(View.GONE);

    }

    private String getUrl(LatLng origin, LatLng dest, Boolean opt, StringBuffer waypoints_coordinates) {

        //Directions API URL
        String directions_api = "https://maps.googleapis.com/maps/api/directions/";

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=true";

        String waypoints = "waypoints=optimize:" + opt + waypoints_coordinates.toString();

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        if(data_models_map.size() > 2)
            parameters +=  "&" + waypoints;

        // Output format
        String output = "json";
        //API KEY
        String apiKey = "key=" + getResources().getString(R.string.directions_api_key);

        //url
        String url = directions_api + output + "?" + parameters + "&" + apiKey + "\n";
        return url;
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... urls) {
            Log.d(TAG, "doInBackground: down");
            URL url;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(urls[0]);
                InputStream in = url.openStream();
                InputStreamReader reader = new InputStreamReader(in);
                char[] buffer = new char[1024];
                int bytesRead = reader.read(buffer);
                while (bytesRead != -1) {

                    result.append(buffer, 0, bytesRead);
                    bytesRead = reader.read(buffer);

                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    private StringBuffer getWaypoints_coordinates_opt_on(){
        StringBuffer wp = new StringBuffer("");
        if(data_models_map.size() <= 3) return wp;
        ArrayList<String> data_models_lat = new ArrayList<>();
        ArrayList<String> data_models_lon = new ArrayList<>();
        int count = 0;
        for (String id : data_models_map.keySet()) {
            if(count != origin_index && count != destination_index){
                String lat = data_models_map.get(id).getLat();
                String lon = data_models_map.get(id).getLon();
                data_models_lat.add(lat);
                data_models_lon.add(lon);
            }
            count += 1;
        }
        if(waypoint_order.isEmpty()){
            try {
                loadApiResult(true);
                JSONObject jObject = new JSONObject(opt_on);
                MapsDataParser parser = new MapsDataParser(jObject);
                waypoint_order = parser.get_waypoint_order();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        for (int index : waypoint_order) {
            wp.append("|").append(data_models_lat.get(index)).append(",").append(data_models_lon.get(index));
        }
        Log.d(TAG, "getWaypoints_coordinates_opt_on() called returns " + wp);
        return wp;
    }

    private StringBuffer getWaypointsCoordinates() {

        StringBuffer waypoints_coordinates = new StringBuffer("");

        int count = 0;
        for (String id : data_models_map.keySet()) {
            if(count!=origin_index && count!=destination_index){
                String lon = data_models_map.get(id).getLon();
                String lat = data_models_map.get(id).getLat();
                waypoints_coordinates.append("|").append(lat).append(",").append(lon);
            }
            count += 1;
        }
        return waypoints_coordinates;
    }

    private void setLatLong(String origin_key, String dest_key) {

        loadOriginDestIdx();

        LatLng tmp_origin, tmp_destination;
        origin = new LatLng(Double.valueOf(data_models_map.get(origin_key).getLat()),
                Double.valueOf(data_models_map.get(origin_key).getLon()));
        destination = new LatLng(Double.valueOf(data_models_map.get(dest_key).getLat()),
                Double.valueOf(data_models_map.get(dest_key).getLon()));

        //change this
//        origin = tmp_origin;
//        destination = tmp_destination;
    }

    private void saveApiResult(Boolean opt) {
        if (!opt) {
            try {
                File file = new File(getDir("apiResponse", MODE_PRIVATE), "opt_false");
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(opt_off);
                outputStream.flush();
                outputStream.close();
                for (String i : data_models_map.keySet()) {
                    saved_api_ids.add(i);
                }
                Collections.sort(saved_api_ids);
                editor.putString("saved_api_ids", saved_api_ids.toString());
                editor.commit();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                File file = new File(getDir("apiResponse", MODE_PRIVATE), "opt_true");
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(opt_on);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            JSONObject jObject = new JSONObject(opt_on);
            MapsDataParser parser = new MapsDataParser(jObject);
            waypoint_order = parser.get_waypoint_order();
        }catch (Exception e){
            e.printStackTrace();
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
        } else {
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
        currentUser = mAuth.getCurrentUser();
        loadTripData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTripData();
    }

    public void saveTripData() {
        try {
            File file = new File(getDir("data", MODE_PRIVATE), "data_models_map");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(data_models_map);
            outputStream.flush();
            outputStream.close();
            
            Log.d(TAG, "saveTripData: called");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadTripData() {
        try {
            File file = new File(getDir("data", MODE_PRIVATE), "data_models_map");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            data_models_map = (LinkedHashMap) ois.readObject();

            model_opt_off.clear();

            for (String s : data_models_map.keySet()) {
                model_opt_off.add(data_models_map.get(s));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            data_models_map = new LinkedHashMap<>();
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
                        Intent a = new Intent(CurrentTripActivity.this, ExploreActivity.class);
                        startActivity(a);
                        break;
                    case R.id.menu_item1:
                        break;
                    case R.id.menu_item2:
                        if (currentUser != null) {
                            Intent b = new Intent(CurrentTripActivity.this, AccountActivity.class);
                            startActivity(b);
                        } else {
                            Intent b = new Intent(CurrentTripActivity.this, LoginActivity.class);
                            startActivity(b);
                        }
                        break;
                }
                return false;
            }
        });
    }
}



