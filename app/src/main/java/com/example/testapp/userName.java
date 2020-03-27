package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class userName extends AppCompatActivity implements View.OnClickListener {

    EditText name;
    TextView skip_button, start_explore_button;
    FirebaseAuth mAuth;
    private static final String TAG = "userName";
    private FirebaseFirestore rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);

        name = findViewById(R.id.name);
        skip_button = findViewById(R.id.username_skip);
        start_explore_button = findViewById(R.id.button_start_explore);

        skip_button.setOnClickListener(this);
        start_explore_button.setOnClickListener(this);

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
                it1 = new Intent(userName.this, Explore.class);
                startActivity(it1);
                break;
            case R.id.button_start_explore:
                Map<String, Object> data = new HashMap<>();
                data.put("name", name.getText().toString());
                rootRef.collection("users")
                        .document(user.getUid())
                        .set(data, SetOptions.merge());
                it2 = new Intent(userName.this, Explore.class);
                startActivity(it2);
        }
    }
}
