package com.example.testapp;

public class explore_model {
    private String priority, title, short_description, image_name;

    public explore_model() {}

    public explore_model(String priority, String title, String short_description, String image_name) {
        this.priority = priority;
        this.title = title;
        this.short_description = short_description;
        this.image_name = image_name;
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
