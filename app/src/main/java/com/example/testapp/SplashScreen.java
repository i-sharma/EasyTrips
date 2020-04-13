package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Timer timer;
    private static final String TAG = "SplashScreen";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        final FirebaseUser currentUser = mAuth.getCurrentUser();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(currentUser!=null){
                    Intent intent = new Intent(SplashScreen.this, Explore.class);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(SplashScreen.this, login.class);
                    startActivity(intent);
                    finish();
                }
            }
        },500);

    }
}
