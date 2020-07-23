package com.example.testapp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CustomImgUrlModel {
    public class ResponseBase{
        public String getStatus() {
            return status;
        }

        public Object getResponse() {
            return response;
        }

        @SerializedName("status") @Expose private String status;
        @SerializedName("response") @Expose private Object response = null;
    }

    public class SuccessResponse extends ResponseBase{
        public String getAction_taken() {
            return action_taken;
        }

        public String getPlaceid() {
            return placeid;
        }

        public String getImage_url() {
            return image_url;
        }

        public String getPhotoref() {
            return photoref;
        }

        public String getCity() {
            return city;
        }

        public int getN_requests() {
            return n_requests;
        }

        @SerializedName("action_taken") @Expose private String action_taken;
        @SerializedName("placeid") @Expose private String placeid;
        @SerializedName("image_url") @Expose private String image_url;
        @SerializedName("photoref") @Expose private String photoref;
        @SerializedName("city") @Expose private String city;
        @SerializedName("n_requests") @Expose private int n_requests;
    }

    public class FailureResponse extends ResponseBase {
        public String getMessage() {
            return message;
        }

        @SerializedName("message") @Expose private String message;
    }


    public class InvalidQueryResponse extends ResponseBase {
        public String getMessage() {
            return message;
        }

        public String getExample_correct_request() {
            return example_correct_request;
        }

        @SerializedName("message") @Expose private String message;
        @SerializedName("example_correct_request") @Expose private String example_correct_request;
    }
}