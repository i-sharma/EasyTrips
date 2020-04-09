package com.example.testapp;

import android.net.Uri;

public class CurrentTripModel {

    private Uri image;
    private String title;
    private String desc;

    public CurrentTripModel(){};

    public CurrentTripModel(Uri image, String title, String desc) {
        this.image = image;
        this.title = title;
        this.desc = desc;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
