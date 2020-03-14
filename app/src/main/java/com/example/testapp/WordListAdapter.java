package com.example.testapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
//        Log.d(TAG, "here we go again " + Arrays.toString(Images.toArray()));
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
//        holder.imgDescription = (TextView) mItemView.findViewById(R.id.ts_description);
//        holder.imgName = (TextView)mItemView.findViewById(R.id.ts_name);
//        holder.imgView =(ImageView) mItemView.findViewById(R.id.ts_image);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull WordListAdapter.WordViewHolder holder, int position) {
        if (holder.imgName != null){
            holder.imgName.setText(mImageNames.get(position));
        }
        if (holder.imgDescription != null) {
            holder.imgDescription.setText(mImageDescriptions.get(position));
        }
        if (holder.imgView != null) {
            Glide.with(mContext)
                    .asBitmap()
                    .load(mImages.get(position))
                    .into(holder.imgView);
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
            imgDescription = (TextView)itemView.findViewById(R.id.ts_description);
            imgName = (TextView)itemView.findViewById(R.id.ts_name);
            imgView =(ImageView)itemView.findViewById(R.id.ts_image);
            this.mAdapter = adapter;

        }
    }

}
