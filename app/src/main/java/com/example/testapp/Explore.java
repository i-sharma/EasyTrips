package com.example.testapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedList;

public class Explore extends AppCompatActivity {
    private final LinkedList<String> mImages = new LinkedList<>();
    private final LinkedList<String> mImageNames = new LinkedList<>();
    private final LinkedList<String> mImageDescriptions = new LinkedList<>();
    private RecyclerView mRecyclerView;
    private WordListAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        for (int i = 0; i < 5; i++) {
            mImages.addLast("https://i.picsum.photos/id/638/300/200.jpg");
            mImageNames.addLast("hello");
            mImageDescriptions.addLast("jaslkdfjlkfjlsdkfj sldkjf sdkjflsdkjdfldkdf ljf lsdjkf sldjfksdlfj ");

        }

        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.recycler_view);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new WordListAdapter(this, mImageNames, mImageDescriptions, mImages);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
