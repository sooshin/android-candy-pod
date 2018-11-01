package com.example.android.candypod.utilities;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.android.candypod.utilities.Constants.I_TUNES_BASE_URL;

/**
 * Create a Retrofit object.
 */
public class RetrofitClient {

    /** Static variable for Retrofit */
    private static Retrofit sRetrofit = null;

    public static Retrofit getClient() {
        if (sRetrofit == null) {
            // OkHttp interceptor which logs HTTP request and response data.
            // Reference: @see "https://stackoverflow.com/questions/32514410/logging-with-retrofit-2"
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            // Create a Retrofit instance using the builder
            sRetrofit = new Retrofit.Builder()
                    // Set the API base URL
                    .baseUrl(I_TUNES_BASE_URL)
                    .client(client)
                    // Use the GsonConverterFactory class to generate an implementation of the
                    // ITunesSearchApi interface which uses Gson for its deserialization
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return sRetrofit;
    }
}
