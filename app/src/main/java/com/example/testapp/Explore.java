package com.example.testapp;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class Explore extends AppCompatActivity {
    private static final String TAG = "Explore";

    private FirebaseFirestore rootRef;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;
//    FirebaseStorage storage;

    private ExploreAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        rootRef = FirebaseFirestore.getInstance();

        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        Query query = rootRef.collection("ts_data")
                .document("delhi")
                .collection("delhi_data")
                .orderBy("priority", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<explore_model> options = new FirestoreRecyclerOptions
                .Builder<explore_model>()
                .setQuery(query, explore_model.class)
                .build();

        adapter = new ExploreAdapter(options, getResources());

        recyclerView = findViewById(R.id.recycler_view);
//        recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    public void clicked_item(View view) {
        Log.d(TAG, "we are here");
        Intent it = new Intent(Explore.this, tsDetails.class);
        startActivity(it);
    }
}
