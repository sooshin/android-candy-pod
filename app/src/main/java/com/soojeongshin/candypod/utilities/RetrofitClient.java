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

import com.soojeongshin.candypod.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

import static com.soojeongshin.candypod.utilities.Constants.I_TUNES_BASE_URL;

/**
 * Creates a Retrofit object.
 */
public class RetrofitClient {

    /** Static variable for Retrofit */
    private static Retrofit sRetrofit = null;

    public static Retrofit getClient() {
        if (sRetrofit == null) {
            // Add OkHttp interceptor which logs HTTP request and response data only when the debug mode is true.
            // Reference: @see "https://stackoverflow.com/questions/32514410/logging-with-retrofit-2"
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            if (BuildConfig.DEBUG_MODE) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                okHttpClientBuilder.addInterceptor(interceptor);
            }

            // Create a Retrofit instance using the builder
            sRetrofit = new Retrofit.Builder()
                    // Set the API base URL
                    .baseUrl(I_TUNES_BASE_URL)
                    .client(okHttpClientBuilder.build())
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
