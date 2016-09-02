package com.example.almaz.flatstackvk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by almaz on 02.09.2016.
 */
public class UsersResponse {
    @SerializedName("response")
    public Response[] responses;

    public static class Response{

        @SerializedName("id")
        public long id;

        @SerializedName("first_name")
        public String first_name;

        @SerializedName("last_name")
        public String last_name;

        @SerializedName("photo_50")
        public String photo_50;
    }
}
