package com.example.testapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class tsDetails extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private FirebaseFirestore rootRef;
    TextView title, rating, description, address_heading, address_content, opening_hours_content;
    TextView opening_hours_heading, entry_fee_content, entry_fee_heading, tips_content, tips_heading;
    TextView airport_distance_content, airport_distance_heading, must_visit_content, must_visit_heading;
    Button add_to_trip, added_to_trip;
    ImageView photo, delete_button;
    LinearLayout added_linear_layout;
    private static final String TAG = "tsDetails";
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    final long ONE_MEGABYTE = 1024 * 1024;

    int add_to_trip_value = 0;
    int already_present_in_trip;
    explore_model obj;
    int click_position;

    private ArrayList<Integer> trip_indices;

    private final int START_NOT_ALREADY_ADDED = 0;
    private final int START_ALREADY_ADDED = 1;
    private final int IN_ACTIVITY_ADD_BUTTON_CLICKED = 2;
    private final int IN_ACTIVITY_DELETE_BUTTON_CLICKED = 3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ts_spot_details);

        title = findViewById(R.id.ts_details_title);
        rating = findViewById(R.id.ts_details_rating);
        description = findViewById(R.id.ts_details_description);
        address_heading = findViewById(R.id.ts_details_address_heading);
        address_content = findViewById(R.id.ts_details_address_content);
        opening_hours_content = findViewById(R.id.ts_details_opening_hours_content);
        opening_hours_heading = findViewById(R.id.ts_details_opening_hours_heading);
        entry_fee_content = findViewById(R.id.ts_details_entry_fee_content);
        entry_fee_heading = findViewById(R.id.ts_details_entry_fee_heading);
        tips_content = findViewById(R.id.ts_details_tips_content);
        tips_heading = findViewById(R.id.ts_details_tips_heading);
        airport_distance_content = findViewById(R.id.ts_details_distance_from_airport_content);
        airport_distance_heading = findViewById(R.id.ts_details_distance_from_airport_heading);
        must_visit_content = findViewById(R.id.ts_details_must_visit_content);
        must_visit_heading = findViewById(R.id.ts_details_must_visit_heading);
        add_to_trip = findViewById(R.id.ts_details_button_add_to_trip);
        added_to_trip = findViewById(R.id.ts_details_button_already_added);
        photo = findViewById(R.id.ts_details_photo);
        added_linear_layout = findViewById(R.id.ts_details_added_button_layout);
        delete_button = findViewById(R.id.ts_details_delete_button);

        Intent it = getIntent();
        obj = (explore_model) it.getSerializableExtra("snapshot");
        already_present_in_trip = it.getIntExtra("already_present_in_trip", 0);
        click_position = it.getIntExtra("click_position", -1);
        updateButtonUI(already_present_in_trip);

        loadData();

        set_content(obj);
        add_to_trip.setOnClickListener(this);
        delete_button.setOnClickListener(this);


    }



    private void updateButtonUI(int already_present_in_trip) {
        switch (already_present_in_trip){
            case START_ALREADY_ADDED:
                added_linear_layout.setVisibility(View.VISIBLE);
                break;
            case START_NOT_ALREADY_ADDED:
                add_to_trip.setVisibility(View.VISIBLE);
                break;
            case IN_ACTIVITY_ADD_BUTTON_CLICKED:
                add_to_trip.setVisibility(View.GONE);
                added_linear_layout.setVisibility(View.VISIBLE);
                break;
            case IN_ACTIVITY_DELETE_BUTTON_CLICKED:
                added_linear_layout.setVisibility(View.GONE);
                add_to_trip.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(trip_indices);
        editor.putString("trip_indices", json);
        editor.apply();
    }

    private void loadData(){
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("add_to_trip_value",add_to_trip_value);
//        returnIntent.putExtra("remove_from_trip",remove_from_trip);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
        return super.onKeyDown(keyCode, event);
    }

    private void set_content(explore_model obj) {
        if(obj.getTitle() != "Not found" && obj.getTitle() != ""){
            title.setVisibility(View.VISIBLE);
            title.setText(obj.getTitle());
        }

        if(obj.getRating() != "Not found" && obj.getRating() != ""){
            rating.setVisibility(View.VISIBLE);
            rating.setText(obj.getRating());
        }

        if(obj.getDescription() != "Not found" && obj.getDescription() != ""){
            description.setVisibility(View.VISIBLE);
            description.setText(obj.getDescription());
        }

        if(obj.getFormatted_address() != "Not found" && obj.getFormatted_address() != ""){
            address_content.setVisibility(View.VISIBLE);
            address_content.setText(obj.getFormatted_address());
            address_heading.setVisibility(View.VISIBLE);
        }

        if(obj.getOpening_hours() != "Not found" && obj.getOpening_hours() != ""){
            opening_hours_content.setVisibility(View.VISIBLE);
            opening_hours_content.setText(obj.getOpening_hours());
            opening_hours_heading.setVisibility(View.VISIBLE);
        }

        if(obj.getEntry_fee() != "Not found" && obj.getEntry_fee() != ""){
            entry_fee_content.setVisibility(View.VISIBLE);
            entry_fee_content.setText(obj.getEntry_fee());
            entry_fee_heading.setVisibility(View.VISIBLE);
        }
//        if(obj.getTip() != "Not found" && obj.getTip() != ""){
//            tips_content.setVisibility(View.VISIBLE);
//            tips_content.setText(obj.getTip());
//            tips_heading.setVisibility(View.VISIBLE);
//        }

        if(obj.getDistance_from_delhi_airport() != "Not found" && obj.getDistance_from_delhi_airport() != ""){
            airport_distance_content.setVisibility(View.VISIBLE);
            airport_distance_content.setText(obj.getDistance_from_delhi_airport());
            airport_distance_heading.setVisibility(View.VISIBLE);
        }

        if(obj.getMust_visit() != "Not found" && obj.getMust_visit() != ""){
            must_visit_content.setVisibility(View.VISIBLE);
            must_visit_content.setText(obj.getMust_visit());
            must_visit_heading.setVisibility(View.VISIBLE);
        }

        if(obj.getImage_name()!="" && obj.getImage_name() != "Not found"){
            StorageReference spaceRef = storageRef.child("photos_delhi/" + obj.getImage_name());

            spaceRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
                    drawable.setCornerRadius(50);
                    photo.setImageDrawable(drawable);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ts_details_button_add_to_trip:
                add_to_trip_value = 1;
                updateButtonUI(IN_ACTIVITY_ADD_BUTTON_CLICKED);
                break;
            case R.id.ts_details_delete_button:
                add_to_trip_value = 0;
                trip_indices.remove(Integer.valueOf(click_position + 1));
                saveData();
                updateButtonUI(IN_ACTIVITY_DELETE_BUTTON_CLICKED);
                break;
        }
    }
}
