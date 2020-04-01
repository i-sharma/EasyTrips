package com.example.testapp;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ExploreAdapter extends FirestoreRecyclerAdapter<explore_model, ExploreAdapter.ExploreViewHolder> {

    FirebaseStorage storage;
    final StorageReference storageRef;
    final long ONE_MEGABYTE = 1024 * 1024;
    Resources resources;


    public ExploreAdapter(@NonNull FirestoreRecyclerOptions<explore_model> options, Resources res) {
        super(options);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        this.resources = res;
    }

    @Override
    protected void onBindViewHolder(@NonNull final ExploreViewHolder holder, int position, @NonNull explore_model model) {
        holder.setTitle(model.getTitle());
        holder.setShortDescription(model.getShort_description());

//        String doc_id = adapter.getSnapshots().getSnapshot(position).getId();
//        Log.d(TAG, "The id is: " + doc_id);

        StorageReference spaceRef = storageRef.child("photos_delhi/" + model.getImage_name());

        spaceRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
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

    class ExploreViewHolder extends RecyclerView.ViewHolder {
        private View view;
        String id;

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
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(resources,bitmap);
            drawable.setCornerRadius(100);
            imageView.setImageDrawable(drawable);
        }
    }
}
