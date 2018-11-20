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

package com.example.android.candypod.service;

import android.app.Notification;
import android.support.annotation.Nullable;

import com.example.android.candypod.R;
import com.example.android.candypod.utilities.DownloadUtil;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationUtil;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.google.android.exoplayer2.util.Util;

import static com.example.android.candypod.utilities.Constants.DOWNLOAD_CHANNEL_ID;
import static com.example.android.candypod.utilities.Constants.DOWNLOAD_NOTIFICATION_ID;

/**
 * The PodcastDownloadService is to run the downloading operation.
 * Reference: @see "https://github.com/google/ExoPlayer/tree/io18"
 */
public class PodcastDownloadService extends DownloadService {

    public PodcastDownloadService() {
        super(
                DOWNLOAD_NOTIFICATION_ID,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                DOWNLOAD_CHANNEL_ID,
                R.string.download_channel_name);
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
        return null;
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
}
