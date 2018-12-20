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

import java.util.List;

public class Result {

    @SerializedName("artistName")
    private String mArtistName;

    @SerializedName("id")
    private String mId;

    @SerializedName("releaseDate")
    private String mReleaseDate;

    @SerializedName("name")
    private String mName;

    @SerializedName("kind")
    private String mKind;

    @SerializedName("copyright")
    private String mCopyright;

    @SerializedName("artistId")
    private String mArtistId;

    @SerializedName("contentAdvisoryRating")
    private String mContentAdvisoryRating;

    @SerializedName("artistUrl")
    private String mArtistUrl;

    @SerializedName("artworkUrl100")
    private String mArtworkUrl;

    @SerializedName("genres")
    private List<Genre> mGenres;

    @SerializedName("url")
    private String mUrl;

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String artistName) {
        mArtistName = artistName;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getKind() {
        return mKind;
    }

    public void setKind(String kind) {
        mKind = kind;
    }

    public String getCopyright() {
        return mCopyright;
    }

    public void setCopyright(String copyright) {
        mCopyright = copyright;
    }

    public String getArtistId() {
        return mArtistId;
    }

    public void setArtistId(String artistId) {
        mArtistId = artistId;
    }

    public String getContentAdvisoryRating() {
        return mContentAdvisoryRating;
    }

    public void setContentAdvisoryRating(String contentAdvisoryRating) {
        mContentAdvisoryRating = contentAdvisoryRating;
    }

    public String getArtistUrl() {
        return mArtistUrl;
    }

    public void setArtistUrl(String artistUrl) {
        mArtistUrl = artistUrl;
    }

    public String getArtworkUrl() {
        return mArtworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        mArtworkUrl = artworkUrl;
    }

    public List<Genre> getGenres() {
        return mGenres;
    }

    public void setGenres(List<Genre> genres) {
        mGenres = genres;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}
