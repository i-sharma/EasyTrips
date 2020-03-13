package com.example.testapp;

import android.graphics.Bitmap;
import android.content.Intent;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

public class Explore extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.item_layout);
//        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                R.drawable.delhi);
//        ImageView imageView = findViewById(R.id.location_pic);
//        RoundedBitmapDrawable img = RoundedBitmapDrawableFactory.create(getResources(),bitmap);
//        img.setCornerRadius(150);
//        imageView.setImageDrawable(img);
    }
}
