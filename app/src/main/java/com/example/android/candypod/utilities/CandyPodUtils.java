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

package com.example.android.candypod.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

import static com.example.android.candypod.utilities.Constants.FORMATTED_PATTERN;
import static com.example.android.candypod.utilities.Constants.PUB_DATE_PATTERN;
import static com.example.android.candypod.utilities.Constants.REQUEST_METHOD_GET;

public class CandyPodUtils {

    /**
     * Converts the publication date into something to display to users.
     * @param pubDate The publication date (i.e. Tue, 25 Nov 2018 05:00:00 -0000)
     * @return The formatted date (i.e. Nov 25, 2018)
     */
    public static String getFormattedDateString(String pubDate) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(PUB_DATE_PATTERN, Locale.US);
        Date currentTime = null;
        try {
            currentTime = simpleDateFormat.parse(pubDate);
        } catch (ParseException e) {
            Timber.e("Error formatting date: " + e.getMessage());
        }

        SimpleDateFormat formatter = new SimpleDateFormat(FORMATTED_PATTERN, Locale.US);
        return formatter.format(currentTime);
    }

    /**
     * Makes a HTTP request and returns Bitmap from the given image URL.
     * @param urlString The podcast image URL
     *
     * Reference: @see "https://stackoverflow.com/questions/8992964/android-load-from-url-to-bitmap"
     *                 "https://developer.android.com/reference/java/net/HttpURLConnection"
     */
    public static Bitmap loadImage(String urlString) throws IOException {
        HttpURLConnection connection = null;
        InputStream stream = null;
        URL url = createUrl(urlString);
        Bitmap bitmap = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(REQUEST_METHOD_GET);
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
            } else {
                Timber.e("Error response code: " + connection.getResponseCode());
            }

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;
            bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (stream != null) {
                stream.close();
            }
        }
        return bitmap;
    }

    /**
     * Builds the URL from the given string URL.
     * @param urlString The podcast image URL
     */
    private static URL createUrl(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

}
