package com.example.almaz.flatstackvk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by almaz on 02.09.2016.
 */
public class GroupsResponse {

    @SerializedName("response")
    public Response[] responses;

    public static class Response{

        @SerializedName("id")
        public long id;

        @SerializedName("name")
        public String name;

        @SerializedName("photo_50")
        public String photo_50;
    }
}
