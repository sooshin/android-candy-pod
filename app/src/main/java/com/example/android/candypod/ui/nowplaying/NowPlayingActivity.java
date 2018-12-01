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
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.candypod.AppExecutors;
import com.example.android.candypod.R;
import com.example.android.candypod.data.CandyPodDatabase;
import com.example.android.candypod.data.DownloadEntry;
import com.example.android.candypod.data.FavoriteEntry;
import com.example.android.candypod.databinding.ActivityNowPlayingBinding;
import com.example.android.candypod.model.rss.Item;
import com.example.android.candypod.service.PodcastDownloadService;
import com.example.android.candypod.service.PodcastService;
import com.example.android.candypod.utilities.CandyPodUtils;
import com.example.android.candypod.utilities.DownloadUtil;
import com.example.android.candypod.utilities.InjectorUtils;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction;

import jp.wasabeef.glide.transformations.BlurTransformation;
import timber.log.Timber;

import static com.example.android.candypod.utilities.Constants.BLUR_RADIUS;
import static com.example.android.candypod.utilities.Constants.BLUR_SAMPLING;
import static com.example.android.candypod.utilities.Constants.EXTRA_DOWNLOAD_ENTRY;
import static com.example.android.candypod.utilities.Constants.EXTRA_ITEM;
import static com.example.android.candypod.utilities.Constants.EXTRA_PODCAST_IMAGE;
import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_ID;
import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_NAME;
import static com.example.android.candypod.utilities.Constants.SHARE_INTENT_TYPE_TEXT;

/**
 * Reference: @see "https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowser-client"
 */
public class NowPlayingActivity extends AppCompatActivity implements DownloadManager.Listener {

    /** The podcast episode */
    private Item mItem;
    /** The podcast image URL used when there is no the current episode's image */
    private String mPodcastImage;
    /** The episode image URL */
    private String mItemImageUrl;
    /** The podcast title */
    private String mPodcastName;
    /** The podcast id */
    private String mPodcastId;
    /** The enclosure URL which is stream URL for the audio file */
    private String mEnclosureUrl;

    /** The MediaBrowser connects to a MediaBrowserService, and upon connecting it creates the
     * MediaController for the UI*/
    private MediaBrowserCompat mMediaBrowser;

    /** This field is used for data binding */
    private ActivityNowPlayingBinding mNowPlayingBinding;

    /** Member variable for FavoriteEntry which represents a single favorite episode */
    private FavoriteEntry mFavoriteEntry;

    /** Member variable for the CandyPodDatabase */
    private CandyPodDatabase mDb;

    /** True when the current episode is in the favorites, otherwise false */
    private boolean mIsFavorite;
    /** True when the user downloaded the current episode */
    private boolean mIsDownloaded;
    /** True when the download action is currently started */
    private boolean mStateStarted;

    /** Member variable for the FavoriteEntryViewModel to store and manage LiveData FavoriteEntry */
    private FavoriteEntryViewModel mFavoriteEntryViewModel;
    /** Member variable for the DownloadEntryViewModel to store and manage LiveData DownloadEntry */
    private DownloadEntryViewModel mDownloadEntryViewModel;

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

        // Check if the episode is downloaded or not
        mIsDownloaded = isDownloaded();

        Timber.d("enclosure url: " + mEnclosureUrl);

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

        // If an episode image exists, use it. Otherwise, use the podcast image.
        mItemImageUrl = CandyPodUtils.getItemImageUrl(mItem, mPodcastImage);
        // Use Glide library to upload the image
        Glide.with(this)
                .load(mItemImageUrl)
                .into(mNowPlayingBinding.ivNowEpisode);

        // Load blurry artwork using Glide Transformations library
        Glide.with(this)
                .load(mItemImageUrl)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(BLUR_RADIUS, BLUR_SAMPLING)))
                .into(mNowPlayingBinding.ivBlur);

        // Extract the enclosure URL
        mEnclosureUrl = mItem.getEnclosures().get(0).getUrl();
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
        // Add a DownloadManager.Listener. Please note that we must remove the listener in onStop()
        DownloadUtil.getDownloadManager(this).addListener(this);
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
        // Remove a DownloadManager.Listener
        DownloadUtil.getDownloadManager(this).removeListener(this);
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
        // Change the download button icon based on whether or not the user downloaded the episode
        changeDownloadIcon(mIsDownloaded, menu);
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
            case R.id.action_download:
                // When the user clicks download button, download or remove episode
                addOrRemoveDownloadedEpisode();
                return true;
            case R.id.action_favorite:
                //
                addOrRemoveFavorite();
                return true;
            case R.id.action_share:
                // Share the episode data
                shareEpisode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Uses the ShareCompat Intent builder to create our share intent for sharing.
     */
    private void shareEpisode() {
        // Text to share
        String shareText = getString(R.string.check_out) + mPodcastName + getString(R.string.space) +
                mItem.getTitle() + getString(R.string.space) +
                mEnclosureUrl;
        // Create a share intent
        Intent shareIntent = ShareCompat.IntentBuilder.from(NowPlayingActivity.this)
                .setType(SHARE_INTENT_TYPE_TEXT)
                .setText(shareText)
                .setChooserTitle(getString(R.string.chooser_title))
                .createChooserIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        startActivity(shareIntent);
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
     * Returns true when the user downloaded the current episode.
     */
    private boolean isDownloaded() {
        // Get the DownloadEntryViewModel from the factory
        DownloadEntryViewModelFactory downloadEntryFactory =
                InjectorUtils.provideDownloadEntryViewModelFactory(this, mEnclosureUrl);
        mDownloadEntryViewModel = ViewModelProviders.of(this, downloadEntryFactory)
                .get(DownloadEntryViewModel.class);

        // Observe the DownloadEntry data
        mDownloadEntryViewModel.getDownloadEntry().observe(this, new Observer<DownloadEntry>() {
            @Override
            public void onChanged(@Nullable DownloadEntry downloadEntry) {
                mIsDownloaded = mDownloadEntryViewModel.getDownloadEntry().getValue() != null;
                // Update the menu
                invalidateOptionsMenu();
            }
        });
        return mIsDownloaded;
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
     * Changes the download button icon based on whether or not the user downloaded the episode.
     * @param isDownloaded True when the user downloaded the current episode
     * @param menu The menu object
     */
    private void changeDownloadIcon(boolean isDownloaded, Menu menu) {
        if (isDownloaded) {
            menu.findItem(R.id.action_download).setIcon(R.drawable.ic_menu_download);
        } else {
            menu.findItem(R.id.action_download).setIcon(R.drawable.ic_download_outline);
        }
    }

    /**
     * Called when the favorite menuItem is clicked. If the current episode is not in the favorites,
     * insert the episode data into the database. Otherwise, delete the episode data from the database.
     */
    private void addOrRemoveFavorite() {
        if (!mIsFavorite) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    // Insert a episode to the database by using the podcastDao
                    mDb.podcastDao().insertFavoriteEpisode(getFavoriteEntry());
                }
            });
        } else {
            mFavoriteEntry = mFavoriteEntryViewModel.getFavoriteEntry().getValue();
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    // Delete the episode from the database by using the podcastDao
                    mDb.podcastDao().deleteFavoriteEpisode(mFavoriteEntry);
                }
            });
        }
        // Show a snackbar message
        showSnackbar();
    }

    /**
     * Shows a snackbar message when the user clicks a favorite button.
     * Reference: @see "https://stackoverflow.com/questions/34020891/how-to-change-background-color-of-the-snackbar"
     */
    private void showSnackbar() {
        String snackMessage;
        Snackbar snackbar;
        if (mIsFavorite) {
            snackMessage = getString(R.string.snackbar_removed_from_fav);
        } else {
            snackMessage = getString(R.string.snackbar_added_to_fav);
        }
        snackbar = Snackbar.make(mNowPlayingBinding.coordinator, snackMessage, Snackbar.LENGTH_SHORT);
        // Set the background color of the snackbar
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(Color.GREEN);
        // Set the text color of the snackbar
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.BLACK);
        snackbar.show();
    }

    /**
     * Returns FavoriteEntry which holds the current episode data.
     */
    private FavoriteEntry getFavoriteEntry() {
        return new FavoriteEntry(mPodcastId, mPodcastName, mPodcastImage,
                mItem.getTitle(), mItem.getDescription(), mItem.getPubDate(),
                mItem.getITunesDuration(), mEnclosureUrl,
                mItem.getEnclosures().get(0).getType(), mItem.getEnclosures().get(0).getLength(),
                mItemImageUrl);
    }

    /**
     * Returns DownloadEntry which holds the current episode data.
     */
    private DownloadEntry getDownloadEntry() {
        return new DownloadEntry(mPodcastId, mPodcastName, mPodcastImage,
                mItem.getTitle(), mItem.getDescription(), mItem.getPubDate(),
                mItem.getITunesDuration(), mEnclosureUrl,
                mItem.getEnclosures().get(0).getType(), mItem.getEnclosures().get(0).getLength(),
                mItemImageUrl);
    }

    /**
     * Called when the download menu item clicked.
     */
    private void addOrRemoveDownloadedEpisode() {
        // Check if is not downloaded episode and download action is currently started.
        // mStateStarted is to avoid starting the download every time the button is pressed.
        if (!mIsDownloaded && !mStateStarted) {
            // Show a toast message that indicates start downloading
            Toast.makeText(this, getString(R.string.toast_start_downloading),
                    Toast.LENGTH_SHORT).show();
            // Trigger the download to start from our activity
            startServiceWithDownloadAction();

        } else if (mIsDownloaded){
            // Remove the downloaded episode
            startServiceWithRemoveAction();
        }
    }

    /**
     * When the user clicks the download button, triggers the download to start from our activity.
     */
    private void startServiceWithDownloadAction() {
        Uri uri = Uri.parse(mEnclosureUrl);
        // The episode title to display in a notification for a completed download
        String itemTitle = mItem.getTitle();
        byte[] itemTitleBytes = itemTitle.getBytes();
        // Create a progressive stream download action
        ProgressiveDownloadAction downloadAction = ProgressiveDownloadAction.createDownloadAction(
                uri, itemTitleBytes,
                null);
        // Start the service with that action
        PodcastDownloadService.startWithAction(
                NowPlayingActivity.this,
                PodcastDownloadService.class,
                downloadAction,
                false);

        Intent intent = new Intent(NowPlayingActivity.this, PodcastDownloadService.class);
        Bundle b = new Bundle();
        b.putParcelable(EXTRA_DOWNLOAD_ENTRY, getDownloadEntry());
        intent.putExtra(EXTRA_DOWNLOAD_ENTRY, b);
        startService(intent);
    }

    /**
     * Removes downloaded episode.
     */
    private void startServiceWithRemoveAction() {
        Uri uri = Uri.parse(mEnclosureUrl);
        // Create a progressive stream remove action
        ProgressiveDownloadAction removeAction = ProgressiveDownloadAction.createRemoveAction(
                uri, null, null);
        // Start the service with that action
        PodcastDownloadService.startWithAction(
                NowPlayingActivity.this,
                PodcastDownloadService.class,
                removeAction,
                false);
    }

    // DownloadManager.Listener

    @Override
    public void onInitialized(DownloadManager downloadManager) {

    }

    @Override
    public void onTaskStateChanged(DownloadManager downloadManager, DownloadManager.TaskState taskState) {
        // Please note that inserting a downloaded episode after the 'download' action completed
        // is done in the onTaskStateChanged() of PodcastDownloadService. Even if the user leaves
        // NowPlayingActivity, the episode can be inserted into the database after the download completed.
        if (taskState.state == TaskState.STATE_COMPLETED && taskState.action.isRemoveAction) {
            // When the 'remove' action is completed, delete the downloaded episode from the database.
            deleteDownloadedEpisode();
            mStateStarted = false;
        } else if (taskState.state == TaskState.STATE_FAILED) {
            mStateStarted = false;
            // Show a toast message that indicates download failed
            Toast.makeText(this, getString(R.string.toast_download_failed),
                    Toast.LENGTH_SHORT).show();
        } else if (taskState.state == TaskState.STATE_STARTED && !taskState.action.isRemoveAction) {
            // Set mStateStarted to true
            mStateStarted = true;
        }
    }

    /**
     * Deletes the downloaded episode from the database.
     */
    private void deleteDownloadedEpisode() {
        // Get downloaded episode by enclosure url
        DownloadEntry downloadEntry = mDownloadEntryViewModel.getDownloadEntry().getValue();
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                // Delete the downloaded episode from the database by using the podcastDao
                mDb.podcastDao().deleteDownloadedEpisode(downloadEntry);
            }
        });
        // Show a toast message that indicates remove the downloaded episode
        Toast.makeText(this, getString(R.string.toast_remove_downloaded_episode),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onIdle(DownloadManager downloadManager) {

    }
}
