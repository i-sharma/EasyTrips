package com.example.testapp;

import java.io.Serializable;

public class explore_model implements Serializable {
    public String title, short_description, image_name, opening_hours;
    public String description, distance_from_delhi_airport, fb_img_url, entry_fee, few_essential_tips;
    public String formatted_address, must_visit, n_ratings, nearest_metro_station, rating;

    public explore_model() {}




    public explore_model(String title, String short_description,
                         String image_name, String opening_hours, String fb_img_url,
                         String description, String distance_from_delhi_airport, String entry_fee,
                         String few_essential_tips, String formatted_address, String must_visit,
                         String n_ratings, String nearest_metro_station, String rating) {
        this.title = title;
//        this.Tip = Tip;
        this.description = description;
        this.distance_from_delhi_airport = distance_from_delhi_airport;
        this.short_description = short_description;
        this.image_name = image_name;
        this.opening_hours = opening_hours;
        this.entry_fee = entry_fee;
        this.few_essential_tips = few_essential_tips;
        this.formatted_address = formatted_address;
        this.must_visit = must_visit;
        this.n_ratings = n_ratings;
        this.nearest_metro_station = nearest_metro_station;
        this.rating = rating;
        this.fb_img_url = fb_img_url;
    }

    public String getFb_image_url() {
        return fb_img_url;
    }

    public String getOpening_hours() {
        return opening_hours;
    }

//    public String getTip() {
//        return Tip;
//    }

    public String getDescription() {
        return description;
    }

    public String getDistance_from_delhi_airport() {
        return distance_from_delhi_airport;
    }

    public String getEntry_fee() {
        return entry_fee;
    }

    public String getFew_essential_tips() {
        return few_essential_tips;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public String getMust_visit() {
        return must_visit;
    }

    public String getN_ratings() {
        return n_ratings;
    }

    public String getNearest_metro_station() {
        return nearest_metro_station;
    }

    public String getRating() {
        return rating;
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
