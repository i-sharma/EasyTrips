package com.example.testapp.models;

public class YourTripsModel {
    public String start_location, end_location, n_locations;

    public YourTripsModel(String start_location, String end_location, String n_locations) {
        this.start_location = start_location;
        this.end_location = end_location;
        this.n_locations = n_locations;
    }

    public String getStart_location() {
        return start_location;
    }

    public String getEnd_location() {
        return end_location;
    }

    public String getN_locations() {
        return n_locations;
    }
}
