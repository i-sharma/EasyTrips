package com.example.testapp;

public class explore_model {
    private int born;
    private String first, last;

    public explore_model() {}

    public explore_model(int born, String first, String last) {
        this.first = first;
        this.last = last;
        this.born = born;
    }

    public String getFirst() {
        return first;
    }

    public String getLast() {
        return last;
    }

    public int getBorn() {
        return born;
    }
}
