package com.example.android.candypod.model;

import com.google.gson.annotations.SerializedName;

public class SearchResult {

    @SerializedName("feedUrl")
    private String mFeedUrl;

    @SerializedName("collectionId")
    private int mCollectionId;

    @SerializedName("artistName")
    private String mArtistName;

    @SerializedName("collectionName")
    private String mCollectionName;

    @SerializedName("artworkUrl600")
    private String mArtworkUrl600;

    public String getFeedUrl() {
        return mFeedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        mFeedUrl = feedUrl;
    }

    public int getCollectionId() {
        return mCollectionId;
    }

    public void setCollectionId(int collectionId) {
        mCollectionId = collectionId;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String artistName) {
        mArtistName = artistName;
    }

    public String getCollectionName() {
        return mCollectionName;
    }

    public void setCollectionName(String collectionName) {
        mCollectionName = collectionName;
    }

    public String getArtworkUrl600() {
        return mArtworkUrl600;
    }

    public void setArtworkUrl600(String artworkUrl600) {
        mArtworkUrl600 = artworkUrl600;
    }
}
