package com.example.testapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.testapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "AccountActivity";

    ImageView clear_button, profile_picture;
    EditText edit_name;
    RelativeLayout user_name_edit_layout;
    TextView user_name,edit_name_button, logout;
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
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_account);

        currentUser = mAuth.getCurrentUser();

        edit_name_button = findViewById(R.id.edit_name_button);
        edit_name = findViewById(R.id.user_name_edit);
        user_name = findViewById(R.id.user_name);
        logout = findViewById(R.id.logout_button);
        clear_button = findViewById(R.id.clear_button);
        save = findViewById(R.id.save_user_name_button);
        user_name_edit_layout = findViewById(R.id.user_name_edit_layout);
        profile_picture = findViewById(R.id.profile_picture);
        cancel = findViewById(R.id.cancel_user_name_button);

        if(currentUser != null) {
            if (currentUser.getPhotoUrl() != null){
                Log.d(TAG, "onCreate: " + currentUser.getPhotoUrl().toString());
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()
                        .into(profile_picture);
            }
            else{
                Glide.with(this)
                        .load(R.drawable.default_avatar)
                        .circleCrop()
                        .into(profile_picture);
            }
            handle_user_name(DISPLAY_NAME);

        }

        edit_name_button.setOnClickListener(this);
        logout.setOnClickListener(this);
        clear_button.setOnClickListener(this);
        cancel.setOnClickListener(this);
        save.setOnClickListener(this);
        bottomNavigation();
    }

    private void updateUI(int TASK){
        switch (TASK){
            case DISPLAY_NAME:
                user_name.setText(user_name_string);
                break;
            case EDIT_TEXT_SET_NAME:
                edit_name.setText(user_name_string);
                user_name.setVisibility(View.GONE);
                user_name_edit_layout.setVisibility(View.VISIBLE);
                break;
            case NEW_NAME_SAVED:
                user_name.setText(user_name_string);
                user_name.setVisibility(View.VISIBLE);
                user_name_edit_layout.setVisibility(View.GONE);
                break;
            case SIGNED_OUT:
                Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;

        }
    }

    private void handle_user_name(final int TASK) {
        if(user_name_string != null){
            updateUI(TASK);
            return;
        }
        rootRef.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
//                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                user_name_string = document.get("name").toString();
                                updateUI(TASK);
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void bottomNavigation() {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation_bar);
        navigation.getMenu().findItem(R.id.menu_item2).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item0:
                        Intent a = new Intent(AccountActivity.this, ExploreActivity.class);
                        startActivity(a);
                        break;
                    case R.id.menu_item1:
                        Intent b = new Intent(AccountActivity.this, CurrentTripActivity.class);
                        startActivity(b);
                        break;
                    case R.id.menu_item2:
                        break;
                }
                return false;
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.edit_name_button:
                handle_user_name(EDIT_TEXT_SET_NAME);
                break;
            case R.id.logout_button:
                mAuth.signOut();
                updateUI(SIGNED_OUT);
                break;
            case R.id.save_user_name_button:
                user_name_string = edit_name.getText().toString();
                save_user_name();
                updateUI(NEW_NAME_SAVED);
                break;
            case R.id.clear_button:
                edit_name.setText("");
                break;
            case R.id.cancel_user_name_button:
                updateUI(NEW_NAME_SAVED);
                break;
        }
    }

    private void save_user_name() {
        if(user_name_string != null && currentUser!=null){
            rootRef.collection("users")
                    .document(currentUser.getUid())
                    .update("name",user_name_string);
        }
    }
}
