package com.example.testapp.utils;

import com.example.testapp.models.CustomImgUrlModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CustomImgUrlApi {

    @GET("images/custom")
    Call<CustomImgUrlModel.ResponseBase> getUrl(@Query("placeid") String placeid, @Query("photoref") String photoref, @Query("city") String city);
}