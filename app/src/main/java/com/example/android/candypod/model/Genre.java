package com.example.android.candypod.model;

import com.google.gson.annotations.SerializedName;

public class Genre {

    @SerializedName("genreId")
    private String mGenreId;

    @SerializedName("name")
    private String mName;

    @SerializedName("url")
    private String mUrl;

    public String getGenreId() {
        return mGenreId;
    }

    public void setGenreId(String genreId) {
        mGenreId = genreId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}
