package com.example.wop.model;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {
    @SerializedName("result")
    public Message message;
    @SerializedName("url")
    public String url;

    @SerializedName("success")
    public boolean success;
}
