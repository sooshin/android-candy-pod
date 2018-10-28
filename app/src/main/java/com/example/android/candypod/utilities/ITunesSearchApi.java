package com.example.android.candypod.utilities;

import com.example.android.candypod.model.ITunesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit turns HTTP API into a Java interface.
 */
public interface ITunesSearchApi {

    @GET("{country}/podcasts/top-podcasts/all/25/explicit.json")
    Call<ITunesResponse> getTopPodcasts(
            @Path("country") String country
    );
}
