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

package com.example.android.candypod;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.example.android.candypod.model.rss.Item;
import com.example.android.candypod.ui.nowplaying.NowPlayingActivity;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.example.android.candypod.utilities.Constants.ACTION_RELEASE_OLD_PLAYER;
import static com.example.android.candypod.utilities.Constants.EXTRA_ITEM;

/**
 * Reference: @see "https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice"
 * "https://github.com/googlesamples/android-UniversalMusicPlayer"
 */
public class PodcastService extends MediaBrowserServiceCompat implements Player.EventListener {

    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    private static final String CANDY_POD_ROOT_ID = "pod_root_id";
    private static final String CANDY_POD_EMPTY_ROOT_ID = "empty_root_id";

    /** Member variable for the ExoPlayer */
    private SimpleExoPlayer mExoPlayer;

    /** Tag for a MediaSessionCompat */
    private static final String TAG = PodcastService.class.getSimpleName();

    /** The enclosure URL for the episode's audio file */
    private String mUrl;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the media session
        initializeMediaSession();
    }

    /**
     * Initialize the media session.
     */
    private void initializeMediaSession() {
        // Create a MediaSessionCompat
        mMediaSession = new MediaSessionCompat(PodcastService.this, TAG);

        // Enable callbacks from MediaButtons and TransportControls
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_REWIND |
                                PlaybackStateCompat.ACTION_FAST_FORWARD |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(mStateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller
        mMediaSession.setCallback(new MySessionCallback());

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mMediaSession.getSessionToken());

        mMediaSession.setSessionActivity(PendingIntent.getActivity(this,
                11,
                new Intent(this, NowPlayingActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT));
    }

    /**
     * Initialize ExoPlayer.
     */
    private void initializePlayer() {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer
            DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(this);
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, defaultRenderersFactory,
                    trackSelector, loadControl);

            // Set the Player.EventListener
            mExoPlayer.addListener(this);

            // Prepare the MediaSource
            Uri mediaUri = Uri.parse(mUrl);
            MediaSource mediaSource = buildMediaSource(mediaUri);
            mExoPlayer.prepare(mediaSource);

            mExoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Check if the old player should be released
        if (intent.getAction() != null && intent.getAction().equals(ACTION_RELEASE_OLD_PLAYER)) {
            if (mExoPlayer != null) {
                mExoPlayer.stop();
                releasePlayer();
            }
        }
        Bundle b = intent.getBundleExtra(EXTRA_ITEM);
        if (b != null) {
            Item item = b.getParcelable(EXTRA_ITEM);
            String itemTitle = item.getTitle();
            mUrl = item.getEnclosure().getUrl();
            Timber.d("onStartCommand: " + itemTitle + " Url: " + mUrl);
        }
        // Initialize ExoPlayer
        initializePlayer();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Release ExoPlayer.
     */
    private void releasePlayer() {
        mExoPlayer.release();
        mExoPlayer = null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        mExoPlayer.stop(true);
        stopSelf();
    }


    @Override
    public void onDestroy() {
        mMediaSession.release();
        releasePlayer();

        super.onDestroy();
    }

    /**
     * Create a MediaSource.
     * @param mediaUri
     */
    private MediaSource buildMediaSource(Uri mediaUri) {
        String userAgent = Util.getUserAgent(this, getString(R.string.app_name));
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                this, userAgent);
        return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(mediaUri);
    }

    /**
     * Returns the root node of the content hierarchy. This method controls access to the service.
     */
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid,
                                 @Nullable Bundle rootHints) {
        return new BrowserRoot(CANDY_POD_ROOT_ID, null);
    }

    /**
     * This method provides the ability for a client to build and display a menu of the
     * MediaBrowserService's content hierarchy.
     */
    @Override
    public void onLoadChildren(@NonNull String parentMediaId,
                               @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

        // Browsing not allowed
        if (TextUtils.equals(CANDY_POD_EMPTY_ROOT_ID, parentMediaId)) {
            result.sendResult(null);
            return;
        }

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        // Check if this is the root menu:
        if (CANDY_POD_ROOT_ID.equals(parentMediaId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...

        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems);
    }

    /**
     * Media Session Callbacks, where all external clients control the player.
     */
    private class MySessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            // onPlay() callback should include code that calls startService().
            startService(new Intent(getApplicationContext(), PodcastService.class));

            // Set the session active
            mMediaSession.setActive(true);

            // Start the player
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);

            stopForeground(false);
        }

        @Override
        public void onRewind() {
            mExoPlayer.seekTo(Math.max(mExoPlayer.getCurrentPosition() - 10000, 0));
            Timber.e("onRewind:");
        }

        @Override
        public void onFastForward() {
            long duration = mExoPlayer.getDuration();
            mExoPlayer.seekTo(Math.min(mExoPlayer.getCurrentPosition() + 30000, duration));
        }

        @Override
        public void onStop() {
            // onStop() callback should call stopSelf().
            stopSelf();

            // Set the session inactive
            mMediaSession.setActive(false);

            // Stop the player
            mExoPlayer.stop();

            // Take the service out of the foreground
            stopForeground(true);
        }
    }

    // Player Event Listeners

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_READY && playWhenReady) {
            // When ExoPlayer is playing, update the PlaybackState
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);

            Timber.d("onPlayerStateChanged: we are playing");
        } else if (playbackState == Player.STATE_READY) {
            // When ExoPlayer is paused, update the PlaybackState
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);

            Timber.d("onPlayerStateChanged: we are paused");
        }
        mMediaSession.setPlaybackState(mStateBuilder.build());
    }

//    /**
//     * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients.
//     */
//    public static class MediaReceiver extends BroadcastReceiver {
//
//        public MediaReceiver() {
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            MediaButtonReceiver.handleIntent(mMediaSession, intent);
//        }
//    }
}
