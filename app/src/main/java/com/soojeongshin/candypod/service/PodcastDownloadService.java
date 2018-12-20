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

package com.soojeongshin.candypod.service;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.soojeongshin.candypod.AppExecutors;
import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.data.CandyPodDatabase;
import com.soojeongshin.candypod.data.DownloadEntry;
import com.soojeongshin.candypod.utilities.DownloadUtil;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationUtil;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.google.android.exoplayer2.util.Util;

import timber.log.Timber;

import static com.soojeongshin.candypod.utilities.Constants.DOWNLOAD_CHANNEL_ID;
import static com.soojeongshin.candypod.utilities.Constants.DOWNLOAD_NOTIFICATION_ID;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_DOWNLOAD_ENTRY;
import static com.soojeongshin.candypod.utilities.Constants.JOB_ID;

/**
 * The PodcastDownloadService is to run the downloading operation.
 * Reference: @see "https://github.com/google/ExoPlayer/tree/io18"
 */
public class PodcastDownloadService extends DownloadService {

    private DownloadEntry mDownloadEntry;
    private CandyPodDatabase mDb;

    public PodcastDownloadService() {
        super(
                DOWNLOAD_NOTIFICATION_ID,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                DOWNLOAD_CHANNEL_ID,
                R.string.download_channel_name);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Get the database instance
        mDb = CandyPodDatabase.getInstance(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Timber.e("intent in onStartCommand is null");
        } else {
            Bundle b = intent.getBundleExtra(EXTRA_DOWNLOAD_ENTRY);
            if (b != null) {
                mDownloadEntry = b.getParcelable(EXTRA_DOWNLOAD_ENTRY);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Returns a DownloadManager to be used to downloaded content.
     */
    @Override
    protected DownloadManager getDownloadManager() {
        return DownloadUtil.getDownloadManager(this);
    }

    @Nullable
    @Override
    protected Scheduler getScheduler() {
        return new PlatformScheduler(this, JOB_ID);
    }

    /**
     * Returns a progress notification to be displayed when the download service is running
     * in the foreground.
     */
    @Override
    protected Notification getForegroundNotification(TaskState[] taskStates) {
        return DownloadNotificationUtil.buildProgressNotification(
                this,
                R.drawable.ic_menu_download,
                DOWNLOAD_CHANNEL_ID,
                null,
                null,
                taskStates);
    }

    /**
     * Called when the state of a task changes.
     * Reference: @see "https://github.com/google/ExoPlayer/tree/release-v2/demos/main"
     * @param taskState The state of the task
     */
    @Override
    protected void onTaskStateChanged(TaskState taskState) {
        if (taskState.action.isRemoveAction) {
            return;
        }
        Notification notification = null;
        if (taskState.state == TaskState.STATE_COMPLETED) {
            // A notification for a completed download
            notification =
                    DownloadNotificationUtil.buildDownloadCompletedNotification(
                            this,
                            R.drawable.ic_candy,
                            DOWNLOAD_CHANNEL_ID,
                            null,
                            Util.fromUtf8Bytes(taskState.action.data));

            // After the download completed, inserts a downloaded episode into the database
            insertDownloadedEpisode();

        } else if (taskState.state == TaskState.STATE_FAILED) {
            // A notification for a failed download
            notification =
                    DownloadNotificationUtil.buildDownloadFailedNotification(
                            this,
                            R.drawable.ic_candy,
                            DOWNLOAD_CHANNEL_ID,
                            null,
                            Util.fromUtf8Bytes(taskState.action.data));
        }
        int notificationId = DOWNLOAD_NOTIFICATION_ID + 1 + taskState.taskId;
        NotificationUtil.setNotification(this, notificationId, notification);
    }

    /**
     * Inserts a downloaded episode into the downloads database after download complete.
     */
    private void insertDownloadedEpisode() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mDownloadEntry != null) {
                    // Check if this episode does not exist in the downloads database
                    // to avoid inserting the same one twice.
                    if (mDb.podcastDao().syncLoadDownload(mDownloadEntry.getItemEnclosureUrl()) == null) {
                        // Insert a downloaded episode to the database by using the podcastDao
                        mDb.podcastDao().insertDownloadedEpisode(mDownloadEntry);
                    }
                }
            }
        });

        // Show a toast message that indicates download completed
        Toast.makeText(this, getString(R.string.toast_download_completed),
                Toast.LENGTH_SHORT).show();
    }
}
