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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.model.rss.Item;
import com.soojeongshin.candypod.model.rss.ItemImage;
import com.soojeongshin.candypod.widget.PodcastWidgetProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.soojeongshin.candypod.utilities.Constants.FORMATTED_PATTERN;
import static com.soojeongshin.candypod.utilities.Constants.PUB_DATE_PATTERN;
import static com.soojeongshin.candypod.utilities.Constants.PUB_DATE_PATTERN_TIME_ZONE;
import static com.soojeongshin.candypod.utilities.Constants.REQUEST_METHOD_GET;
import static com.soojeongshin.candypod.utilities.Constants.SPLIT_COLON;
import static com.soojeongshin.candypod.utilities.Constants.SPLIT_INDEX_ONE;
import static com.soojeongshin.candypod.utilities.Constants.SPLIT_INDEX_TWO;
import static com.soojeongshin.candypod.utilities.Constants.SPLIT_INDEX_ZERO;

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
            // If the pubDate pattern is different from "Tue, 25 Nov 2018 05:00:00 -0000",
            // use getFormattedDateStringFromGmt() method.
            return getFormattedDateStringFromGmt(pubDate);
        }

        SimpleDateFormat formatter = new SimpleDateFormat(FORMATTED_PATTERN, Locale.US);
        return formatter.format(currentTime);
    }

    /**
     * Converts the publication date into something to display to users.
     *
     * @param pubDate The publication date (i.e. Tue, 25 Nov 2018 05:00 GMT)
     * @return The formatted date (i.e. Nov 25, 2018)
     */
    private static String getFormattedDateStringFromGmt(String pubDate) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(PUB_DATE_PATTERN_TIME_ZONE, Locale.US);
        Date currentTime = null;
        try {
            currentTime = simpleDateFormat.parse(pubDate);
        } catch (ParseException e) {
            Timber.e("Error formatting date: " + e.getMessage());
            // If failed converting a date, use pubDate.
            return pubDate;
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
            // If the URL is null, then return early
            if (url == null) {
                return null;
            }
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

    // Update App widgets

    /**
     * Updates the episode data using SharedPreferences each time the user selects the episode.
     * @param context Context we use to utility methods, app resources and layout inflaters
     * @param item Item object which contains an episode data
     * @param podcastTitle The podcast title
     * @param imageUrl The episode image URL. If the episode image does not exist, use the podcast image instead.
     */
    public static void updateSharedPreference(Context context, Item item, String podcastTitle, String imageUrl) {
        // Get an instance of SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // Get the editor object
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the string used for displaying in the app widget
        editor.putString(context.getString(R.string.pref_podcast_title_key), podcastTitle);
        editor.putString(context.getString(R.string.pref_episode_title_key), item.getTitle());
        editor.putString(context.getString(R.string.pref_episode_image_key), imageUrl);

        // Save results
        editor.apply();
    }

    /**
     * Sends the update broadcast message to the app widget.
     * @param context Context we use to utility methods, app resources and layout inflaters
     *
     * Reference: @see "https://stackoverflow.com/questions/10663800/sending-an-update-broadcast
     * -to-an-app-widget"
     */
    public static void sendBroadcastToWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, PodcastWidgetProvider.class));

        Intent updateAppWidgetIntent = new Intent();
        updateAppWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateAppWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(updateAppWidgetIntent);
    }

    /**
     * Not all episodes have the image URL, so we should check if it is null.
     * Returns the episode image URL. If the episode image does not exist, returns the podcast image URL.
     * @param item Item object which contains an episode data
     * @param podcastImage The podcast image URL
     */
    public static String getItemImageUrl(Item item, String podcastImage) {
        List<ItemImage> itemImages = item.getItemImages();
        String itemImageUrl = null;
        if (itemImages != null) {
            itemImageUrl = itemImages.get(0).getItemImageHref();
        }
        if (TextUtils.isEmpty(itemImageUrl)) {
            itemImageUrl = podcastImage;
        }
        return itemImageUrl;
    }

    /**
     * Check if there is the network connectivity.
     * @return True if connected to the network
     */
    public static boolean isOnline(Context context) {
        // Get a reference to the ConnectivityManager to check the state of network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Runs the layout animation for RecyclerView.
     * Reference: @see "https://proandroiddev.com/enter-animation-using-recyclerview-and-layoutanimation-part-1-list-75a874a5d213"
     */
    public static void runLayoutAnimation(RecyclerView recyclerView) {
        Context context = recyclerView.getContext();
        LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_from_bottom);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    /**
     * Converts language code to language name.
     * @param languageCode The language code (i.e. en, ko)
     * @return The language name (i.e. English, Korean)
     *
     * Reference: @see "https://stackoverflow.com/questions/36061116/get-language-name-in-that-language-from-language-code"
     * "https://stackoverflow.com/questions/39583040/how-to-display-the-language-name-in-english-from-the-language-code"
     */
    public static String convertLanguageCode(String languageCode) {
        Locale locale = new Locale(languageCode);
        return locale.getDisplayLanguage(Locale.ENGLISH);
    }

    /**
     * Converts hh:mm:ss string to seconds.
     * @param item Item object that holds the current episode data
     * Reference: @see "https://stackoverflow.com/questions/8257641/java-how-to-convert-a-string-hhmmss-to-a-duration"
     */
    public static long getDurationInMilliSeconds(Item item) {
        String timestampStr = item.getITunesDuration();
        String[] tokens = timestampStr.split(SPLIT_COLON);
        int hours = Integer.parseInt(tokens[SPLIT_INDEX_ZERO]);
        int minutes = Integer.parseInt(tokens[SPLIT_INDEX_ONE]);
        int seconds = Integer.parseInt(tokens[SPLIT_INDEX_TWO]);
        return TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(minutes) + seconds;
    }
}