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
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback;
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.model.rss.Item;
import com.soojeongshin.candypod.ui.nowplaying.NowPlayingActivity;
import com.soojeongshin.candypod.utilities.CandyPodUtils;
import com.soojeongshin.candypod.utilities.DownloadUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.soojeongshin.candypod.utilities.Constants.ACTION_RELEASE_OLD_PLAYER;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_ITEM;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_PODCAST_IMAGE;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_NAME;
import static com.soojeongshin.candypod.utilities.Constants.FAST_FORWARD_INCREMENT;
import static com.soojeongshin.candypod.utilities.Constants.NOTIFICATION_PENDING_INTENT_ID;
import static com.soojeongshin.candypod.utilities.Constants.PLAYBACK_CHANNEL_ID;
import static com.soojeongshin.candypod.utilities.Constants.PLAYBACK_NOTIFICATION_ID;
import static com.soojeongshin.candypod.utilities.Constants.REWIND_INCREMENT;

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

    /** A notification manager to start, update and cancel a media style notification reflecting
     * the player state */
    private PlayerNotificationManager mPlayerNotificationManager;

    /** Attributes for audio playback, which configure the underlying platform AudioTrack */
    private AudioAttributes mAudioAttributes;

    /** Tag for a MediaSessionCompat */
    private static final String TAG = PodcastService.class.getSimpleName();

    /** The enclosure URL for the episode's audio file */
    private String mUrl;
    /** The current episode item*/
    private Item mItem;
    /** The podcast title */
    private String mPodcastName;
    /** The podcast image URL */
    private String mPodcastImage;
    /** The podcast bitmap image */
    private Bitmap mBitmap;

    private boolean mAudioNoisyReceiverRegistered;
    private final IntentFilter mAudioNoisyIntentFilter =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    /**
     * A BroadcastReceiver that listens for an ACTION_AUDIO_BECOMING_NOISY intent whenever you're
     * playing audio. This handles changes in audio output to avoid suddenly playing out loud if a
     * peripheral like headphones is disconnected while in use.
     * Reference: @see "https://developer.android.com/guide/topics/media-apps/volume-and-earphones"
     */
    private final BroadcastReceiver mAudioNoisyReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                        Timber.d("Headphones disconnected.");
                        // Pause the playback
                        if (mExoPlayer != null && mExoPlayer.getPlayWhenReady()) {
                            mExoPlayer.setPlayWhenReady(false);
                        }
                    }
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the media session
        initializeMediaSession();

        // Create an instance of com.google.android.exoplayer2.audio.AudioAttributes
        initAudioAttributes();
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

            // Set the attributes for audio playback. ExoPlayer manages audio focus automatically.
            mExoPlayer.setAudioAttributes(mAudioAttributes, /* handleAudioFocus= */ true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If there are not any pending start commands to be delivered to the service, it will
        // be called with a null intent object, so you must take care to check for this.
        // Reference: @see "https://developer.android.com/reference/android/app/Service.html#START_STICKY"
        // "https://stackoverflow.com/questions/8421430/reasons-that-the-passed-intent-would-be-null-in-onstartcommand"
        if (intent == null || intent.getAction() == null) {
            Timber.e("intent in onStartCommand is null");
            return START_STICKY;
        }
        // Check if the old player should be released
        if (intent.getAction() != null && intent.getAction().equals(ACTION_RELEASE_OLD_PLAYER)) {
            if (mExoPlayer != null) {
                mExoPlayer.stop();
                releasePlayer();
            }
        }
        Bundle b = intent.getBundleExtra(EXTRA_ITEM);
        if (b != null) {
            mItem = b.getParcelable(EXTRA_ITEM);
            String itemTitle = mItem.getTitle();
            mUrl = mItem.getEnclosures().get(0).getUrl();
            Timber.d("onStartCommand: " + itemTitle + " Url: " + mUrl);
        }
        // Get the podcast title
        if (intent.hasExtra(EXTRA_RESULT_NAME)) {
            mPodcastName = intent.getStringExtra(EXTRA_RESULT_NAME);
        }
        // Get the podcast image
        if (intent.hasExtra(EXTRA_PODCAST_IMAGE)) {
            mPodcastImage = intent.getStringExtra(EXTRA_PODCAST_IMAGE);
        }
        // Initialize ExoPlayer
        initializePlayer();

        // Convert hh:mm:ss string to seconds to put it into the metadata
//        long duration = CandyPodUtils.getDurationInMilliSeconds(mItem);
//        long duration = Long.parseLong(mItem.getITunesDuration());
//        Timber.e("duration: " + duration);
//        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
//                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration).build();
//        mMediaSession.setMetadata(metadata);

        // Initialize PlayerNotificationManager
        initializeNotificationManager(mItem);

        // If an episode image exists, use it. Otherwise, use the podcast image.
        String itemImageUrl = CandyPodUtils.getItemImageUrl(mItem, mPodcastImage);
        // Load a bitmap image from the URL using asyncTask
        new BitmapTask().execute(itemImageUrl);

        // The service is not immediately destroyed, and we can explicitly terminate our service
        // when finished audio playback.
        return START_STICKY;
    }

    /**
     * Initialize PlayerNotificationManager.
     * References: @see "https://medium.com/google-exoplayer/playback-notifications-with-exoplayer-a2f1a18cf93b"
     * "https://www.youtube.com/watch?v=svdq1BWl4r8" "https://github.com/google/ExoPlayer/tree/io18"
     * "https://google.github.io/ExoPlayer/doc/reference/com/google/android/exoplayer2/ui/PlayerNotificationManager.html"
     *  @param item The current episode item
     */
    private void initializeNotificationManager(Item item) {
        // Create a notification manager and a low-priority notification channel with the channel ID
        // and channel name
        mPlayerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                this,
                PLAYBACK_CHANNEL_ID,
                R.string.playback_channel_name,
                PLAYBACK_NOTIFICATION_ID,
                // An adapter to provide descriptive data about the current playing item
                new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public String getCurrentContentTitle(Player player) {
                        return item.getTitle();
                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {
                        // Create a pending intent that relaunches the NowPlayingActivity
                        return createContentPendingIntent(item);
                    }

                    @Nullable
                    @Override
                    public String getCurrentContentText(Player player) {
                        return mPodcastName;
                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, BitmapCallback callback) {
                        // Return a podcast bitmap image using AsyncTask
                        if (mBitmap != null) {
                            return mBitmap;
                        }
                        return null;
                    }
                }
        );

        // A listener for start and cancellation of the notification
        mPlayerNotificationManager.setNotificationListener(new NotificationListener() {
            // Called when the notification is initially created
            @Override
            public void onNotificationStarted(int notificationId, Notification notification) {
                startForeground(notificationId, notification);
            }

            // Called when the notification is cancelled
            @Override
            public void onNotificationCancelled(int notificationId) {
                stopSelf();
            }
        });

        // Once the notification manager is created, attach the player
        mPlayerNotificationManager.setPlayer(mExoPlayer);
        // Set the MediaSessionToken
        mPlayerNotificationManager.setMediaSessionToken(mMediaSession.getSessionToken());

        // Customize the notification
        // Set the small icon of the notification
        mPlayerNotificationManager.setSmallIcon(R.drawable.ic_candy);
        // Set skip previous and next actions
        mPlayerNotificationManager.setUseNavigationActions(true);
        // Set the fast forward increment by 30 sec
        mPlayerNotificationManager.setFastForwardIncrementMs(FAST_FORWARD_INCREMENT);
        // Set the rewind increment by 10sec
        mPlayerNotificationManager.setRewindIncrementMs(REWIND_INCREMENT);
        // Omit the stop action
        mPlayerNotificationManager.setStopAction(null);
        // Make the notification not ongoing
        mPlayerNotificationManager.setOngoing(false);
    }

    /**
     * Create a content pending intent that relaunches the NowPlayingActivity.
     */
    private PendingIntent createContentPendingIntent(Item item) {
        Intent intent = new Intent(PodcastService.this, NowPlayingActivity.class);
        // Pass data via intent
        Bundle b = new Bundle();
        b.putParcelable(EXTRA_ITEM, item); // Podcast episode
        intent.putExtra(EXTRA_ITEM, b);
        intent.putExtra(EXTRA_RESULT_NAME, mPodcastName); // Podcast title
        intent.putExtra(EXTRA_PODCAST_IMAGE, mPodcastImage); // Podcast Image

        return PendingIntent.getActivity(
                PodcastService.this, NOTIFICATION_PENDING_INTENT_ID,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Create an instance of AudioAttributes.
     * References: @see "https://medium.com/google-exoplayer/easy-audio-focus-with-exoplayer-a2dcbbe4640e"
     * "https://google.github.io/ExoPlayer/doc/reference/com/google/android/exoplayer2/audio/AudioAttributes.html"
     */
    private void initAudioAttributes() {
        mAudioAttributes = new AudioAttributes.Builder()
                // If audio focus should be handled, the AudioAttributes.usage must be C.USAGE_MEDIA
                // or C.USAGE_GAME. Other usages will throw an IllegalArgumentException.
                .setUsage(C.USAGE_MEDIA)
                // Since the app is playing a podcast, set contentType to CONTENT_TYPE_SPEECH.
                // SimpleExoPlayer will pause while the notification, such as when a message arrives,
                // is playing and will automatically resume afterwards.
                .setContentType(C.CONTENT_TYPE_SPEECH)
                .build();
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
        if (mExoPlayer != null) {
            mExoPlayer.stop(true);
        }
        stopSelf();
        // Allow the notification to be swipe dismissed when paused
        // Reference: @see "https://stackoverflow.com/questions/26496670/dismissing-mediastyle-notifications"
        mPlayerNotificationManager.setOngoing(false);
    }


    @Override
    public void onDestroy() {
        mMediaSession.release();
        // If the player is released it must be removed from the manager by calling setPlayer(null)
        // which will cancel the notification
        if (mPlayerNotificationManager != null) {
            mPlayerNotificationManager.setPlayer(null);
        }
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

        // Add support for caching to the player. The CacheDataSourceFactory sits between our
        // DataSourceFactory for loading from the network and the MediaSource which extracts the media.
        // Reference: @see "https://github.com/google/ExoPlayer/tree/io18"
        // "https://www.youtube.com/watch?v=svdq1BWl4r8"
        CacheDataSourceFactory cacheDataSourceFactory =
                new CacheDataSourceFactory(
                        DownloadUtil.getCache(this),
                        dataSourceFactory, // The upstream DataSourceFactory for loading data from the network
                        CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
        return new ExtractorMediaSource.Factory(cacheDataSourceFactory).createMediaSource(mediaUri);
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
            if (mExoPlayer != null) {
                mExoPlayer.setPlayWhenReady(true);
            }

            // Register the receiver when you begin playback
            registerAudioNoisyReceiver();
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);

            stopForeground(false);
        }

        @Override
        public void onRewind() {
            mExoPlayer.seekTo(Math.max(mExoPlayer.getCurrentPosition() - REWIND_INCREMENT, 0));
        }

        @Override
        public void onFastForward() {
            long duration = mExoPlayer.getDuration();
            mExoPlayer.seekTo(Math.min(mExoPlayer.getCurrentPosition() +
                    FAST_FORWARD_INCREMENT, duration));
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

            // Unregister the receiver when you stop
            unregisterAudioNoisyReceiver();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            if (mExoPlayer != null) {
                mExoPlayer.seekTo((int) pos);
            }
        }
    }

    // Player Event Listeners

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Timber.e("onPlayerError: " + error.getMessage());
        Toast.makeText(this, getString(R.string.toast_source_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_IDLE) {
            // When there is nothing to play, update the state to paused
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
        } else if (playbackState == Player.STATE_BUFFERING) {
            // When ExoPlayer is buffering, not being able to play immediately,
            // update the state to buffering.
            mStateBuilder.setState(PlaybackStateCompat.STATE_BUFFERING,
                    mExoPlayer.getCurrentPosition(), 1f);
        } else if (playbackState == Player.STATE_READY && playWhenReady) {
            // When ExoPlayer is playing, update the state to playing that indicates this item is
            // currently playing.
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);
            // Register the receiver when you begin playback
            registerAudioNoisyReceiver();
            Timber.d("onPlayerStateChanged: we are playing");
        } else if (playbackState == Player.STATE_READY) {
            // When ExoPlayer is paused, update the state to paused that indicates this item is
            // currently paused.
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
            // Unregister the receiver when you stop
            unregisterAudioNoisyReceiver();
            Timber.d("onPlayerStateChanged: we are paused");
        } else if (playbackState == Player.STATE_ENDED) {
            // When ExoPlayer finished playing, update the state to paused
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
        } else {
            // Update the state to the default state that indicates that the performer has no content to play.
            mStateBuilder.setState(PlaybackStateCompat.STATE_NONE,
                    mExoPlayer.getCurrentPosition(), 1f);
        }
        mMediaSession.setPlaybackState(mStateBuilder.build());
    }

    /**
     * Register the AudioNoisyReceiver when you begin playback.
     */
    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    /**
     * Unregister the AudioNoisyReceiver when you stop.
     */
    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }

    // Use AsyncTask to load the image from the URL

    /**
     * Loads a bitmap image for a notification
     */
    public class BitmapTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                bitmap = CandyPodUtils.loadImage(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mBitmap = bitmap;
        }
    }
}
