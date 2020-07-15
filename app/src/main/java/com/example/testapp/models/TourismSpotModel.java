package com.example.testapp.models;

import java.io.Serializable;

public class TourismSpotModel implements Serializable {
    public String title, short_description, image_name, opening_hours,lat;
    public String description, distance_from_delhi_airport, fb_img_url, entry_fee, few_essential_tips;
    public String formatted_address;
    public String must_visit;
    public String n_ratings;
    public String nearest_metro_station;
    public String duration_required_to_visit;
    public String id;

    public String rating;
    public String tip;
    public String lon;

    Boolean isCustom = false;

    public void setOrigin(Boolean origin) {
        isOrigin = origin;
    }

    public void setDestination(Boolean destination) {
        isDestination = destination;
    }

    public Boolean getOrigin() {
        return isOrigin;
    }

    public Boolean getDestination() {
        return isDestination;
    }

    Boolean isOrigin = false;
    Boolean isDestination = false;

    public TourismSpotModel() {}

    public TourismSpotModel(String id, String title, String lat, String lon, Boolean isCustom, String time) {
        this.lat = lat;
        this.lon = lon;
        this.title = title;
        this.isCustom = isCustom;
        this.duration_required_to_visit = time;
        this.id = id;
    }

    public TourismSpotModel(String title, String short_description, String lat, String duration_required_to_visit,
                            String image_name, String opening_hours, String fb_img_url, String id,
                            String description, String distance_from_delhi_airport, String entry_fee,
                            String few_essential_tips, String formatted_address, String must_visit,
                            String n_ratings, String nearest_metro_station, String rating, String tip, String lon) {
        this.title = title;
        this.tip = tip;
        this.lon = lon;
        this.lat = lat;
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
        this.duration_required_to_visit = duration_required_to_visit;
        this.id = id;
    }

    public String getLon() {
        return lon;
    }

    public String getDuration_required_to_visit() {
        return duration_required_to_visit;
    }

    public String getLat() {
        return lat;
    }


    public String getFb_image_url() {
        return fb_img_url;
    }

    public String getOpening_hours() {
        return opening_hours;
    }

    public String getTip() {
        return tip;
    }

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

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Boolean getIsCustom() {return isCustom;}
}
