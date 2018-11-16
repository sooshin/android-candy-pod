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

package com.example.android.candypod.ui.nowplaying;

import android.content.ComponentName;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;
import com.example.android.candypod.PodcastService;
import com.example.android.candypod.R;
import com.example.android.candypod.databinding.ActivityNowPlayingBinding;
import com.example.android.candypod.model.rss.Item;
import com.example.android.candypod.model.rss.ItemImage;

import timber.log.Timber;

import static com.example.android.candypod.utilities.Constants.EXTRA_ITEM;
import static com.example.android.candypod.utilities.Constants.EXTRA_PODCAST_IMAGE;
import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_NAME;

/**
 * Reference: @see "https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowser-client"
 */
public class NowPlayingActivity extends AppCompatActivity {

    /** The podcast episode */
    private Item mItem;

    /** The podcast image URL used when there is no the current episode's image */
    private String mPodcastImage;

    /** The MediaBrowser connects to a MediaBrowserService, and upon connecting it creates the
     * MediaController for the UI*/
    private MediaBrowserCompat mMediaBrowser;

    /** This field is used for data binding */
    private ActivityNowPlayingBinding mNowPlayingBinding;
    /** The podcast title */
    private String mPodcastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNowPlayingBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_now_playing);

        setupUI();

        Timber.d("enclosure url: " + mItem.getEnclosure().getUrl());

        // Create MediaBrowserCompat
        createMediaBrowserCompat();
    }

    /**
     * Setup UI
     */
    private void setupUI() {
        // Get the podcast episode, title and the podcast image URL
        getData();

        String title = mItem.getTitle();
        if (title != null) {
            mNowPlayingBinding.playingInfo.tvNowTitle.setText(title);
        }
        // Set podcast title
        if (mPodcastName != null) {
            mNowPlayingBinding.playingInfo.tvPodcastTitle.setText(mPodcastName);
        }

        // Not all episode has its image. If it exists, use the episode image. Otherwise,
        // use the podcast image.
        String itemImageUrl = mPodcastImage;
        ItemImage itemImage = mItem.getItemImage();
        if (itemImage != null) {
            itemImageUrl = itemImage.getItemImageHref();
        }
        // Use Glide library to upload the image
        Glide.with(this)
                .load(itemImageUrl)
                .into(mNowPlayingBinding.ivNowEpisode);
    }

    /**
     * Constructs a MediaBrowserCompat.
     */
    private void createMediaBrowserCompat() {
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, PodcastService.class),
                mConnectionCallbacks,
                null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the MediaBrowserService
        mMediaBrowser.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set the audio stream so the app responds to the volume control on the device
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect the MediaBrowser and unregister the MediaController.Callback when the
        // activity stops
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    /**
     * Get the podcast episode, title and the podcast image URL from the DetailActivity via Intent.
     */
    private void getData() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_ITEM)) {
                Bundle b = intent.getBundleExtra(EXTRA_ITEM);
                mItem = b.getParcelable(EXTRA_ITEM);
            }
            // Get podcast title
            if (intent.hasExtra(EXTRA_RESULT_NAME)) {
                mPodcastName = intent.getStringExtra(EXTRA_RESULT_NAME);
            }
            if (intent.hasExtra(EXTRA_PODCAST_IMAGE)) {
                mPodcastImage = intent.getStringExtra(EXTRA_PODCAST_IMAGE);
            }
        }
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {

                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                    // Create a MediaControllerCompat
                    MediaControllerCompat mediaController = null;
                    try {
                        mediaController = new MediaControllerCompat(NowPlayingActivity.this, token);
                    } catch (RemoteException e) {
                        Timber.e("Error creating media controller");
                    }

                    // Save the controller
                    MediaControllerCompat.setMediaController(NowPlayingActivity.this,
                            mediaController);

                    // Finish building the UI
                    buildTransportControls();
                }

                @Override
                public void onConnectionSuspended() {
                    super.onConnectionSuspended();
                    // The Service has crashed. Disable transport controls until it automatically
                    // reconnects.
                }

                @Override
                public void onConnectionFailed() {
                    super.onConnectionFailed();
                    // The Service has refused our connection
                }
            };

    void buildTransportControls() {
        // Attach a listener to the play/pause button
        mNowPlayingBinding.playingInfo.ibPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pbState = MediaControllerCompat.getMediaController(NowPlayingActivity.this)
                        .getPlaybackState().getState();

                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    MediaControllerCompat.getMediaController(NowPlayingActivity.this)
                            .getTransportControls().pause();

                } else {
                    MediaControllerCompat.getMediaController(NowPlayingActivity.this)
                            .getTransportControls().play();

                }
            }
        });

        // Attach a listener to the fast forward button
        mNowPlayingBinding.playingInfo.ibFastforward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat.getMediaController(NowPlayingActivity.this)
                        .getTransportControls().fastForward();
            }
        });

        // Attach a listener to the rewind button
        mNowPlayingBinding.playingInfo.ibRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat.getMediaController(NowPlayingActivity.this)
                        .getTransportControls().rewind();
            }
        });

        mNowPlayingBinding.playingInfo.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(NowPlayingActivity.this);

        // Display the initial state
        MediaMetadataCompat metadata = mediaController.getMetadata();
        PlaybackStateCompat pbState = mediaController.getPlaybackState();

        if (pbState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            mNowPlayingBinding.playingInfo.ibPlayPause.setImageResource(R.drawable.exo_controls_pause);
        } else {
            mNowPlayingBinding.playingInfo.ibPlayPause.setImageResource(R.drawable.exo_controls_play);
        }

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);
    }

    /**
     * Callback for receiving updates from the session.
     */
    MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                mNowPlayingBinding.playingInfo.ibPlayPause.setImageResource(R.drawable.exo_controls_pause);
            } else {
                mNowPlayingBinding.playingInfo.ibPlayPause.setImageResource(R.drawable.exo_controls_play);
            }
        }
    };
}
