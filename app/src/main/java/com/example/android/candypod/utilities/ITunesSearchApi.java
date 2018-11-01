package com.example.android.candypod.utilities;

import com.example.android.candypod.model.ITunesResponse;
import com.example.android.candypod.model.LookupResponse;
import com.example.android.candypod.model.rss.RssFeed;

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

    @GET @Xml
    Call<RssFeed> getRssFeed(
            @Url String url
    );

    @Retention(RetentionPolicy.RUNTIME)
    @interface Json {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface Xml {}
}
