package com.example.testapp;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.LinkedList;

public class Explore extends AppCompatActivity {
    private static final String TAG = "Explore";
//    private final LinkedList<String> mImages = new LinkedList<>();
//    private final LinkedList<String> mImageNames = new LinkedList<>();
//    private final LinkedList<String> mImageDescriptions = new LinkedList<>();
//    private RecyclerView mRecyclerView;
//    private WordListAdapter mAdapter;

    private FirestoreRecyclerAdapter<explore_model, ExploreViewHolder> adapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();



        Query query = rootRef.collection("ts_data")
                .document("delhi")
                .collection("delhi_data")
                .orderBy("priority", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<explore_model> options = new FirestoreRecyclerOptions.Builder<explore_model>()
                .setQuery(query, explore_model.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<explore_model, ExploreViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ExploreViewHolder holder , int position, @NonNull explore_model productModel) {
                holder.setTitle(productModel.getTitle());
                holder.setShortDescription(productModel.getShort_description());

                StorageReference spaceRef = storageRef.child("photos_delhi/" + productModel.getImage_name());
                final long ONE_MEGABYTE = 1024 * 1024;
                spaceRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed
                        holder.setImage(bytes);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }

            @NonNull
            @Override
            public ExploreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_explore_item, parent, false);
                return new ExploreViewHolder(view);
            }
        };
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

    private class ExploreViewHolder extends RecyclerView.ViewHolder {
        private View view;

        ExploreViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        void setTitle(String title) {
            TextView textView = view.findViewById(R.id.ts_name);
            textView.setText(title);
        }

        void setShortDescription(String short_descr){
            TextView textView = view.findViewById(R.id.ts_description);
            textView.setText(short_descr);
        }

        void setImage(byte[] data){
            ImageView imageView = view.findViewById(R.id.ts_image);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
            drawable.setCornerRadius(100);
            imageView.setImageDrawable(drawable);
        }
    }

//    public void old_queries(){
//        for (int i = 0; i < 20; i++) {
//            mImages.addLast("https://cdn.pixabay.com/photo/2020/03/07/11/54/the-fog-4909513_1280.jpg");
//            mImageNames.addLast("hello");
//            mImageDescriptions.addLast("Also known as Lal Qila, it is a monument built in 1638 that rises 33 meters (108 ft) above Old Delhi. It was built by the Mughal Emperor Shah Jahan.");
//
//        }
//        mImages.addLast("https://i.picsum.photos/id/638/300/200.jpg");
//        mImageNames.addLast("hello");
//        mImageDescriptions.addLast("jaslkdfjlkfjlsdkfj sldkjf sdkjflsdkjdfldkdf ljf lsdjkf sldjfksdlfj ");
//
//        mRecyclerView = findViewById(R.id.recycler_view);
//        mAdapter = new WordListAdapter(this, mImageNames, mImageDescriptions, mImages);
//        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//    }

    public void clicked_item(View view) {
        Log.d(TAG, "we are here");
        Intent it = new Intent(Explore.this, tsDetails.class);
        startActivity(it);
    }
}
