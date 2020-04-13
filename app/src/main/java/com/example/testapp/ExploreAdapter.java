package com.example.testapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.ExecutionException;

public class ExploreAdapter extends FirestoreRecyclerAdapter<explore_model, ExploreAdapter.ExploreViewHolder> {

    private static final String TAG = "ExploreAdapter";
    FirebaseStorage storage;
    final StorageReference storageRef;
    final long ONE_MEGABYTE = 1024 * 1024;
    Resources resources;

    Context context;

    private OnItemClickListener listener;


    public ExploreAdapter(@NonNull FirestoreRecyclerOptions<explore_model> options, Resources res) {
        super(options);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        this.resources = res;
    }

    @Override
    protected void onBindViewHolder(@NonNull final ExploreViewHolder holder, int position,
                                    @NonNull explore_model model) {
        holder.setTitle(model.getTitle());
        holder.setShortDescription(model.getShort_description());

        Glide.with(context)
                .load(model.getFb_image_url())
                .placeholder(R.drawable.wait)
                .into(holder.imageView);

    }

    @NonNull
    @Override
    public ExploreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.layout_explore_item, parent, false);
        return new ExploreViewHolder(view);
    }

    class ExploreViewHolder extends RecyclerView.ViewHolder {
        private View view;
//        String id;
//        Button bt;
        ImageView imageView;

        ExploreViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView = view.findViewById(R.id.ts_image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position!=RecyclerView.NO_POSITION && listener != null){
                        listener.onViewClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });


//            bt = view.findViewById(R.id.btn);
//            imageView = view.findViewById(R.id.done);
//            bt.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    int position = getAdapterPosition();
//                    if(position!=RecyclerView.NO_POSITION && listener != null){
//                        listener.onButtonClick(position, imageView);
//                    }
//                }
//            });


        }



        void setTitle(String title) {
            TextView textView = view.findViewById(R.id.ts_name);
            textView.setText(title);
        }

        void setShortDescription(String short_descr){
            TextView textView = view.findViewById(R.id.ts_description);
            textView.setText(short_descr);
        }

//        void setImage(Bitmap bitmap){
//            ImageView imageView = view.findViewById(R.id.ts_image);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(resources,bitmap);
//            drawable.setCornerRadius(100);
//            imageView.setImageDrawable(drawable);
//        }
    }

    public interface OnItemClickListener{
        void onViewClick(DocumentSnapshot documentSnapshot, int position);

//        void onButtonClick(int position, ImageView done);
    }

    public void setOnClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
