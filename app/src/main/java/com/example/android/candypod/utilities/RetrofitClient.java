package com.example.android.candypod.utilities;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.android.candypod.utilities.Constants.I_TUNES_BASE_URL;

/**
 * Create a singleton of Retrofit.
 */
public class RetrofitClient {

    /** Static variable for Retrofit */
    private static Retrofit sRetrofit = null;

    public static Retrofit getClient() {
        if (sRetrofit == null) {
            // Create a Retrofit instance using the builder
            sRetrofit = new Retrofit.Builder()
                    // Set the API base URL
                    .baseUrl(I_TUNES_BASE_URL)
                    // Use the GsonConverterFactory class to generate an implementation of the
                    // ITunesSearchApi interface which uses Gson for its deserialization
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return sRetrofit;
    }
}
