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

package com.soojeongshin.candypod.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;
import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.ui.MainActivity;

import static com.soojeongshin.candypod.utilities.Constants.PREF_DEF_VALUE;
import static com.soojeongshin.candypod.utilities.Constants.SIZE_BITMAP;
import static com.soojeongshin.candypod.utilities.Constants.WIDGET_PENDING_INTENT_ID;

/**
 * Implementation of App Widget functionality.
 */
public class PodcastWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Get the updated strings from shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String episodeTitle = sharedPreferences.getString(
                context.getString(R.string.pref_episode_title_key), PREF_DEF_VALUE);
        String podcastTitle = sharedPreferences.getString(
                context.getString(R.string.pref_podcast_title_key), PREF_DEF_VALUE);
        String episodeImage = sharedPreferences.getString(
                context.getString(R.string.pref_episode_image_key), PREF_DEF_VALUE);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.podcast_widget);
        views.setTextViewText(R.id.widget_episode_title, episodeTitle);
        views.setTextViewText(R.id.widget_podcast_title, podcastTitle);
        loadImage(context, views, appWidgetId, episodeImage);

        // Create a pending intent that relaunches the MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, WIDGET_PENDING_INTENT_ID,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_artwork, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Loads episode image from URL into RemoteViews.
     * Reference: @see "https://stackoverflow.com/questions/47993270/widget-load-image-from-url-into-remote-view"
     */
    private static void loadImage(Context context, RemoteViews remoteViews, int appWidgetId, String imageUrl) {
        AppWidgetTarget target = new AppWidgetTarget(context, R.id.widget_artwork, remoteViews, appWidgetId) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
            }
        };

        RequestOptions options = new RequestOptions().
                override(SIZE_BITMAP, SIZE_BITMAP);

        Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(imageUrl)
                .apply(options)
                .into(target);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // Receive a broadcast message and update all widget instances given the widget Ids
        String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            for (int appWidgetId :appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

