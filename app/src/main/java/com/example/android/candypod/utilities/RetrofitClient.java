package com.example.android.candypod.utilities;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

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
                    // Use custom ConverterFactory where you delegate to either GsonConverterFactory
                    // or SimpleXmlConverterFactory
                    // Reference: @see "https://stackoverflow.com/questions/40824122/android-retrofit-2-multiple-converters-gson-simplexml-error"
                    // @see "https://speakerdeck.com/jakewharton/making-retrofit-work-for-you-ohio-devfest-2016?slide=86"
                    .addConverterFactory(new XmlOrJsonConverterFactory())
                    .build();
        }
        return sRetrofit;
    }
}
