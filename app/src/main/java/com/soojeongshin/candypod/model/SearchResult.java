/*
 * Copyright 2018 Soojeong Shin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soojeongshin.candypod.model;

import com.google.gson.annotations.SerializedName;

public class SearchResult {

    @SerializedName("feedUrl")
    private String mFeedUrl;

    /** The Podcast ID */
    @SerializedName("collectionId")
    private int mCollectionId;

    @SerializedName("artistName")
    private String mArtistName;

    /** The Podcast Name */
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
