package com.example.testapp;

import android.net.Uri;

public class CurrentTripModel {

    private Uri image;
    private String title;
    private String time_to_cover;
    private int id;

    public CurrentTripModel(){};

    public CurrentTripModel(Uri image, String title, String time_to_cover,int id) {
        this.image = image;
        this.title = title;
        this.time_to_cover= time_to_cover;
        this.id = id;
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

    public String getTime_to_cover() {
        return time_to_cover;
    }

    public void setTime_to_cover(String time_to_cover) {
        this.time_to_cover = time_to_cover;
    }

    public int getId(){return id;}
}
