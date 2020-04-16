package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity{

    ImageView imageView;
    Button edit_name_button;

    ImageView edit_image,clear_button, profile_picture;
    EditText edit_name;
    RelativeLayout user_name_layout, user_name_edit_layout;
    TextView user_name, logout;
    Button save,cancel;


    String user_name_string;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;
    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();



    final static int DISPLAY_NAME = 1;
    final static int EDIT_TEXT_SET_NAME = 2;
    final static int SIGNED_OUT = 3;
    final static int NEW_NAME_SAVED = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_new);

        imageView = findViewById(R.id.profile_picture);
        edit_name_button = findViewById(R.id.edit_name_button);

        Glide.with(this)
                .load(R.drawable.default_avatar)
                .circleCrop()
                .into(imageView);

    }


    }




