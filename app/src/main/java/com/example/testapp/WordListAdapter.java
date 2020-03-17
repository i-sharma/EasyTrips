package com.example.testapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.Arrays;
import java.util.LinkedList;

public class WordListAdapter extends
        RecyclerView.Adapter<WordListAdapter.WordViewHolder>  {
    private static final String TAG = "WordListAdapter";
    private final LinkedList<String> mImages, mImageNames, mImageDescriptions;
    private LayoutInflater mInflater;
    private Context mContext;
    public WordListAdapter(Context context, LinkedList<String> ImageNames,
                           LinkedList<String> ImageDescriptions, LinkedList<String> Images) {
        mInflater = LayoutInflater.from(context);
        this.mImages = Images;
        this.mImageDescriptions = ImageDescriptions;
        this.mImageNames = ImageNames;
        mContext = context;
    }

    @NonNull
    @Override
    public WordListAdapter.WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.layout_explore_item,
                parent, false);
        WordViewHolder holder =  new WordViewHolder(mItemView, this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final WordListAdapter.WordViewHolder holder, int position) {
        Bitmap bitmap;
        if (holder.imgName != null){
            holder.imgName.setText(mImageNames.get(position));
        }

        if (holder.imgDescription != null) {
            holder.imgDescription.setText(mImageDescriptions.get(position));
        }
        if (holder.imgView != null) {
            Log.d(TAG, "here here");
            try {
                Glide.with(mContext)
                        .asBitmap()
                        .load(mImages.get(position))
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {


//                                Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                                        R.drawable.lion);
//                                RoundedBitmapDrawable img = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
//                                img.setCornerRadius(150);
                                holder.imgView.setImageBitmap(resource);
//                                imageView.setImageDrawable(img);
                            }

                            @Override
                            public void onLoadCleared(Drawable placeholder) {
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }

//            Glide.with(mContext)
//                    .asBitmap()
//                    .load(mImages.get(position))
//                    .into(holder.imgView);

        }

    }

    @Override
    public int getItemCount() {
        return mImageNames.size();
    }

    class WordViewHolder extends RecyclerView.ViewHolder {
        public final TextView imgName, imgDescription;
        public final ImageView imgView;
        final WordListAdapter mAdapter;
        public WordViewHolder(@NonNull View itemView, WordListAdapter adapter) {
            super(itemView);
            this.imgDescription = (TextView)itemView.findViewById(R.id.ts_description);
            this.imgName = (TextView)itemView.findViewById(R.id.ts_name);
            this.imgView =(ImageView)itemView.findViewById(R.id.ts_image);
            this.mAdapter = adapter;

        }
    }

}
