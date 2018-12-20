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

package com.soojeongshin.candypod.utilities;

import com.soojeongshin.candypod.model.ITunesResponse;
import com.soojeongshin.candypod.model.LookupResponse;
import com.soojeongshin.candypod.model.SearchResponse;
import com.soojeongshin.candypod.model.rss.RssFeed;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Retrofit turns HTTP API into a Java interface.
 */
public interface ITunesSearchApi {

    @GET("{country}/podcasts/top-podcasts/all/25/explicit.json") @Json
    Call<ITunesResponse> getTopPodcasts(
            @Path("country") String country
    );

    /**
     * Reference: @see "https://stackoverflow.com/questions/32559333/retrofit-2-dynamic-url"
     * @param url A complete URL for an endpoint
     * @param id The id is used to create a lookup request to search for a specific podcast
     */
    @GET @Json
    Call<LookupResponse> getLookupResponse(
            @Url String url,
            @Query("id") String id
    );

    @GET @Json
    Call<SearchResponse> getSearchResponse(
            @Url String searchUrl,
            @Query("country") String country,
            @Query("media") String media,
            @Query("term") String term
    );

    @GET @Xml
    Call<RssFeed> getRssFeed(
            @Url String url
    );

    @Retention(RetentionPolicy.RUNTIME)
    @interface Json {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface Xml {}
}
