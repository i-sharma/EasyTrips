package com.example.testapp;

import java.io.Serializable;

public class explore_model implements Serializable {
    private String priority, title, short_description, image_name, opening_hours ;


    public explore_model() {}


    public explore_model(String priority, String title,
                         String short_description, String image_name, String openingHours) {
        this.priority = priority;
        this.title = title;
        this.short_description = short_description;
        this.image_name = image_name;
        this.opening_hours = openingHours;
    }

    public String getOpeningHours() {
        return opening_hours;
    }


    public String getPriority() {
        return priority;
    }

    public String getShort_description() {
        return short_description;
    }

    public String getTitle() {
        return title;
    }

    public String getImage_name() {
        return image_name;
    }
}
