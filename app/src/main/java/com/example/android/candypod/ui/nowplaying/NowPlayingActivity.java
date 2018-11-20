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

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.candypod.AppExecutors;
import com.example.android.candypod.PodcastService;
import com.example.android.candypod.R;
import com.example.android.candypod.data.CandyPodDatabase;
import com.example.android.candypod.data.FavoriteEntry;
import com.example.android.candypod.databinding.ActivityNowPlayingBinding;
import com.example.android.candypod.model.rss.Item;
import com.example.android.candypod.model.rss.ItemImage;
import com.example.android.candypod.utilities.InjectorUtils;

import timber.log.Timber;

import static com.example.android.candypod.utilities.Constants.EXTRA_ITEM;
import static com.example.android.candypod.utilities.Constants.EXTRA_PODCAST_IMAGE;
import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_ID;
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

    /** The podcast id */
    private String mPodcastId;

    /** Member variable for FavoriteEntry which represents a single favorite episode */
    private FavoriteEntry mFavoriteEntry;

    /** Member variable for the CandyPodDatabase */
    private CandyPodDatabase mDb;

    /** True when the current episode is in the favorites, otherwise false */
    private boolean mIsFavorite;

    /** Member variable for the FavoriteEntryViewModel to store and manage LiveData FavoriteEntry */
    private FavoriteEntryViewModel mFavoriteEntryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNowPlayingBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_now_playing);

        setupUI();

        // Get the database instance
        mDb = CandyPodDatabase.getInstance(getApplicationContext());

        // Check if the episode is in the favorites or not
        mIsFavorite = isFavorite();

        Timber.d("enclosure url: " + mItem.getEnclosure().getUrl());

        // Create MediaBrowserCompat
        createMediaBrowserCompat();

        // Hide title on the Toolbar
        setTitle(getString(R.string.space));
        // Show the up button on the Toolbar
        showUpButton();
    }

    /**
     * Setup UI
     */
    private void setupUI() {
        // Get the podcast episode, title and the podcast image URL
        getData();

        // Set episode title
        String episodeTitle = mItem.getTitle();
        if (episodeTitle != null) {
            mNowPlayingBinding.playingInfo.tvEpisodeTitle.setText(episodeTitle);
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
            // Get podcast id
            if (intent.hasExtra(EXTRA_RESULT_ID)) {
                mPodcastId = intent.getStringExtra(EXTRA_RESULT_ID);
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

    /**
     * Show the up button on the tool bar.
     */
    private void showUpButton() {
        // Set the toolbar as the app bar
        setSupportActionBar(mNowPlayingBinding.toolbar);

        ActionBar actionBar = getSupportActionBar();
        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Modify the options menu based on events. If the current episode is not in the favorites,
     * the heart button icon image will be border heart image, otherwise full heart image.
     * When an event occurs and you want to perform a menu update, you must call
     * invalidateOptionsMenu() to request that the system call onPrepareOptionsMenu().
     *
     * References: @see "https://stackoverflow.com/questions/11006749/change-icons-in-actionbar-dynamically?rq=1"
     * "https://developer.android.com/guide/topics/ui/menus#ChangingTheMenu"
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Change the hear button icon based on whether or not the episode exists in the favorites
        changeFavIcon(mIsFavorite, menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.now_playing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                // When the user press the up button, finishes this NowPlayingActivity
                onBackPressed();
                return true;
            case R.id.action_favorite:
                //
                addOrRemoveFavorite();
                return true;
            case R.id.action_share:
                //
                Toast.makeText(this, "share", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isFavorite() {
        // Get the FavoriteEntryViewModel from the factory
        FavoriteEntryViewModelFactory favEntryFactory =
                InjectorUtils.provideFavoriteEntryViewModelFactory(this, mItem.getTitle());
        mFavoriteEntryViewModel = ViewModelProviders.of(this, favEntryFactory)
                .get(FavoriteEntryViewModel.class);

        // Observe the FavoriteEntry data
        mFavoriteEntryViewModel.getFavoriteEntry().observe(this, new Observer<FavoriteEntry>() {
            @Override
            public void onChanged(@Nullable FavoriteEntry favoriteEntry) {
                mIsFavorite = mFavoriteEntryViewModel.getFavoriteEntry().getValue() != null;
                // Update the menu
                invalidateOptionsMenu();
            }
        });
        return mIsFavorite;
    }

    /**
     * Change the hear button icon based on whether or not the episode exists in the favorites.
     * @param isFavorite True when the current episode is in the favorites, otherwise false
     * @param menu The menu object
     */
    private void changeFavIcon(boolean isFavorite, Menu menu) {
        if (isFavorite) {
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_menu_favorite);
        } else {
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_favorite_border);
        }
    }

    /**
     * Called when the favorite menuItem is clicked. If the current episode is not in the favorites,
     * insert the episode data into the database. Otherwise, delete the episode data from the database.
     */
    private void addOrRemoveFavorite() {
        // Not all episode have the image URL, so we should check if it is null.
        // If the episode image does not exist, use the podcast image instead.
        String itemImageUrl;
        ItemImage itemImage = mItem.getItemImage();
        if (itemImage == null) {
            itemImageUrl = mPodcastImage;
        } else {
            itemImageUrl = itemImage.getItemImageHref();
        }

        // Create a FavoriteEntry
        mFavoriteEntry = new FavoriteEntry(mPodcastId, mPodcastName, mPodcastImage,
                mItem.getTitle(), mItem.getDescription(), mItem.getPubDate(),
                mItem.getITunesDuration(), mItem.getEnclosure().getUrl(),
                mItem.getEnclosure().getType(), mItem.getEnclosure().getLength(),
                itemImageUrl);

        if (!mIsFavorite) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    // Insert a episode to the database by using the podcastDao
                    mDb.podcastDao().insertFavoriteEpisode(mFavoriteEntry);
                }
            });
            Toast.makeText(this, "inserted", Toast.LENGTH_SHORT).show();
        } else {
            mFavoriteEntry = mFavoriteEntryViewModel.getFavoriteEntry().getValue();
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    // Delete the episode from the database by using the podcastDao
                    mDb.podcastDao().deleteFavoriteEpisode(mFavoriteEntry);
                }
            });
            Toast.makeText(this, "deleted", Toast.LENGTH_SHORT).show();
        }
    }
}
