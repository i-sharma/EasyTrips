package com.example.testapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

public class tsDetails extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore rootRef;
    TextView title, rating, description, address_heading, address_content, opening_hours_content;
    TextView opening_hours_heading, entry_fee_content, entry_fee_heading, tips_content, tips_heading;
    TextView airport_distance_content, airport_distance_heading, must_visit_content, must_visit_heading;
    Button add_to_trip;
    ImageView photo;
    private static final String TAG = "tsDetails";
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    final long ONE_MEGABYTE = 1024 * 1024;
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
        photo = findViewById(R.id.ts_details_photo);

        Intent it = getIntent();
        explore_model obj = (explore_model) it.getSerializableExtra("snapshot");


        set_content(obj);


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
        if(obj.getTip() != "Not found" && obj.getTip() != ""){
            tips_content.setVisibility(View.VISIBLE);
            tips_content.setText(obj.getTip());
            tips_heading.setVisibility(View.VISIBLE);
        }

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


}
