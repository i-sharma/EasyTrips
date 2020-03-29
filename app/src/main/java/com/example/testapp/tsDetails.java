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

public class tsDetails extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore rootRef;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ts_spot_details);

        text = findViewById(R.id.yuHiname);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            Toast.makeText(this, "here " + user.getUid(), Toast.LENGTH_SHORT).show();
            setName(user.getUid());
        }


    }

    private void setName(String uid) {
        String result = "Hey man";
        rootRef.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Intent i;
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                text.setText(document.get("name").toString());
                            }
                        }
                    }
                });
    }
}
