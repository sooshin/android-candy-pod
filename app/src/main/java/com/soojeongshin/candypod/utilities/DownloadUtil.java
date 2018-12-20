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

import android.content.Context;

import com.soojeongshin.candypod.R;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import static com.soojeongshin.candypod.utilities.Constants.FILE_ACTIONS;
import static com.soojeongshin.candypod.utilities.Constants.FILE_DOWNLOADS;

/**
 * Reference: @see "https://github.com/google/ExoPlayer/tree/io18"
 */
public class DownloadUtil {

    private static Cache sCache;
    private static DownloadManager sDownloadManager;

    /**
     * As we're sharing the same cache for playback and downloading, we have a singleton for our
     * whole process.
     */
    public static synchronized Cache getCache(Context context) {
        if (sCache == null) {
            // Create a file which points to a directory where we're going to store the downloads
            File cacheDirectory = new File(context.getExternalFilesDir(null), FILE_DOWNLOADS);
            // Instantiate a simple cache
            sCache = new SimpleCache(cacheDirectory, new NoOpCacheEvictor());
        }
        return sCache;
    }

    /**
     * Creates a singleton instance of a DownloadManager for our whole process.
     */
    public static synchronized DownloadManager getDownloadManager(Context context) {
        if (sDownloadManager == null) {
            // Create a file, because it needs to persist some information about downloads that are
            // in progress
            File actionFile = new File(context.getExternalCacheDir(), FILE_ACTIONS);
            // Instantiate a DownloadManager
            sDownloadManager =
                    new DownloadManager(
                            // The same cache as the one we're using for the cache during playback
                            getCache(context),
                            // DataSourceFactory for loading data to populate the cache
                            new DefaultDataSourceFactory(
                                    context,
                                    Util.getUserAgent(context, context.getString(R.string.app_name))),
                            actionFile,
                            // Deserializer for progressive download action, because we're downloading
                            // progressive media
                            ProgressiveDownloadAction.DESERIALIZER);
        }
        return sDownloadManager;
    }
}
