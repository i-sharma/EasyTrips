package com.example.testapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.testapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class UserNameActivity extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener {

    EditText name;
    TextView skip_button, start_explore_button, ask_name;
    FirebaseAuth mAuth;
    private static final String TAG = "userName";
    private FirebaseFirestore rootRef;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/LatoLight.ttf");
        Typeface custom_font1 = Typeface.createFromAsset(getAssets(), "fonts/LatoRegular.ttf");


        name = findViewById(R.id.name);
        skip_button = findViewById(R.id.username_skip);
        start_explore_button = findViewById(R.id.button_start_explore);
        ask_name = findViewById(R.id.ask_for_name);
        progressBar = findViewById(R.id.username_progress);
        name.setTypeface(custom_font);
        skip_button.setTypeface(custom_font);
        start_explore_button.setTypeface(custom_font1);
        ask_name.setTypeface(custom_font);


        skip_button.setOnClickListener(this);
        start_explore_button.setOnTouchListener(this);

        mAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseFirestore.getInstance();

    }

    @Override
    public void onClick(View v) {
        FirebaseUser user = mAuth.getCurrentUser();
        Intent it1, it2;
        switch (v.getId()){
            case R.id.username_skip:
                Log.d(TAG, "here we are");
                it1 = new Intent(UserNameActivity.this, ExploreActivity.class);
                startActivity(it1);
                finish();
                LoginActivity.activity.finish();
                PhoneAuthActivity.activity.finish();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        FirebaseUser user = mAuth.getCurrentUser();
        Intent it2;
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            v.setBackgroundColor(getResources().getColor(R.color.auth_button_light));
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            v.setBackgroundColor(getResources().getColor(R.color.auth_button));
            progressBar.setVisibility(View.VISIBLE);
            switch (v.getId()){
                case R.id.button_start_explore:
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name.getText().toString());
                    rootRef.collection("users")
                            .document(user.getUid())
                            .set(data, SetOptions.merge());
                    progressBar.setVisibility(View.GONE);
                    it2 = new Intent(UserNameActivity.this, ExploreActivity.class);
                    startActivity(it2);
                    finish();
                    LoginActivity.activity.finish();
                    PhoneAuthActivity.activity.finish();
                    break;
            }
        }
        return true;
    }
}
