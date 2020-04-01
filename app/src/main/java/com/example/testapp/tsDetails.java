package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.Serializable;

public class tsDetails extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore rootRef;
    TextView text;
    private static final String TAG = "tsDetails";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ts_spot_details);

        text = findViewById(R.id.yuHiname);

        Intent it = getIntent();
        String doc_id = it.getStringExtra("ID");
        explore_model obj = (explore_model) it.getSerializableExtra("snapshot");



        Toast.makeText(tsDetails.this, obj.getShort_description(),
                Toast.LENGTH_SHORT).show();

    }


}
