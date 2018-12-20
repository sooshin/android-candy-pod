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

package com.soojeongshin.candypod.ui.detail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.analytics.Analytics;
import com.soojeongshin.candypod.data.PodcastEntry;
import com.soojeongshin.candypod.databinding.ActivityDetailBinding;
import com.soojeongshin.candypod.model.rss.Item;
import com.soojeongshin.candypod.service.PodcastService;
import com.soojeongshin.candypod.ui.nowplaying.NowPlayingActivity;
import com.soojeongshin.candypod.utilities.CandyPodUtils;
import com.soojeongshin.candypod.utilities.InjectorUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.soojeongshin.candypod.utilities.Constants.ACTION_RELEASE_OLD_PLAYER;
import static com.soojeongshin.candypod.utilities.Constants.BLUR_RADIUS;
import static com.soojeongshin.candypod.utilities.Constants.BLUR_SAMPLING;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_ITEM;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_PODCAST_IMAGE;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_ID;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_NAME;

/**
 * The DetailActivity displays the detail of the podcast and the list of episodes.
 */
public class DetailActivity extends AppCompatActivity
        implements DetailAdapter.DetailAdapterOnClickHandler {

    /** This field is used for data binding */
    private ActivityDetailBinding mDetailBinding;

    /** The podcast ID */
    private String mResultId;
    /** The podcast title */
    private String mResultName;

    /** Member variable for the PodcastEntryViewModel to store and manage LiveData PodcastEntry */
    private PodcastEntryViewModel mPodcastEntryViewModel;

    /** Member variable for the DetailAdapter */
    private DetailAdapter mDetailAdapter;

    /** Member variable for the list of {@link Item}s which is the episodes in the podcast */
    private List<Item> mItemList;

    /** The Podcast image URL received from the PodcastsFragment will be passed to the
     * NowPlayingActivity via Intent */
    private String mPodcastImage;

    /** Member variable for FirebaseAnalytics */
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Get the podcast ID and title
        getResultData();

        // Setup the PodcastEntryViewModel
        setupViewModel();

        // Create a LinearLayoutManager and DetailAdapter, and set them to the RecyclerView
        initAdapter();

        // Show the up button on Collapsing Toolbar
        showUpButton();
        // Show the title in the app bar when a CollapsingToolbarLayout is fully collapsed
        setCollapsingToolbarTitle();

        // Get the FirebaseAnalytics instance
        mFirebaseAnalytics = Analytics.getInstance(this);
    }

    /**
     * Gets the podcast ID which is used to retrieve the PodcastEntry from the podcast table.
     * Gets the podcast title used to set the title in the app bar.
     * Gets the podcast image URL from the PodcastFragment, which is used when there is no episode image.
     */
    private void getResultData() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_RESULT_ID)) {
            mResultId = intent.getStringExtra(EXTRA_RESULT_ID);
        }
        if(intent.hasExtra(EXTRA_RESULT_NAME)) {
            mResultName = intent.getStringExtra(EXTRA_RESULT_NAME);
        }
        if (intent.hasExtra(EXTRA_PODCAST_IMAGE)) {
            mPodcastImage = intent.getStringExtra(EXTRA_PODCAST_IMAGE);
        }
    }

    /**
     * Observes the data and update the UI.
     */
    private void setupViewModel() {
        // Get the PodcastEntryViewModel from the factory
        PodcastEntryViewModelFactory podcastEntryFactory = InjectorUtils.providePodcastEntryViewModelFactory(
                this, mResultId);
        mPodcastEntryViewModel = ViewModelProviders.of(this, podcastEntryFactory)
                .get(PodcastEntryViewModel.class);

        // Observe the PodcastEntry
        mPodcastEntryViewModel.getPodcastEntry().observe(this, new Observer<PodcastEntry>() {
            @Override
            public void onChanged(@Nullable PodcastEntry podcastEntry) {
                if (podcastEntry != null) {
                    showPodcastEntryData(podcastEntry);
                }
            }
        });
    }

    /**
     * Creates a LinearLayoutManager and DetailAdapter, and set them to the RecyclerView.
     */
    private void initAdapter() {
        // A LinearLayoutManager is responsible for measuring and positioning item views within a
        // RecyclerView into a linear list.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDetailBinding.rvDetail.setLayoutManager(layoutManager);
        mDetailBinding.rvDetail.setHasFixedSize(true);

        // Create an empty ArrayList
        mItemList = new ArrayList<>();
        // The DetailAdapter is responsible for linking our item data with the Views that will
        // end up displaying our episode data.
        mDetailAdapter = new DetailAdapter(mItemList, this, mPodcastImage);
        // Setting the adapter attaches it to the RecyclerView in our layout
        mDetailBinding.rvDetail.setAdapter(mDetailAdapter);
    }

    /**
     * Shows the details of the podcast.
     *
     * @param podcastEntry A single row from podcast table that has the data of the podcast
     */
    private void showPodcastEntryData(PodcastEntry podcastEntry) {
        // Get the data from the PodcastEntry
        String artworkImageUrl = podcastEntry.getArtworkImageUrl();
        String title = podcastEntry.getTitle();
        String author = podcastEntry.getAuthor();

        // Load blurry artwork using Glide Transformations library
        Glide.with(this)
                .load(artworkImageUrl)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(BLUR_RADIUS, BLUR_SAMPLING)))
                .into(mDetailBinding.ivBlur);

        // Use Glide library to upload the artwork
        Glide.with(this)
                .load(artworkImageUrl)
                .into(mDetailBinding.ivArtwork);
        // Set text
        mDetailBinding.tvTitle.setText(title);
        mDetailBinding.tvAuthor.setText(author);

        // Read episodes from the PodcastEntry
        mItemList = podcastEntry.getItems();
        // Update the list of items and notify the adapter of any changes
        mDetailAdapter.addAll(mItemList);
    }

    /**
     * Shows the up button on the Collapsing Toolbar.
     */
    private void showUpButton() {
        // Set the toolbar as the app bar
        setSupportActionBar(mDetailBinding.toolbar);

        ActionBar actionBar = getSupportActionBar();
        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * When the user press the up button in the app bar, finishes this DetailActivity.
     */
    @Override
    public boolean onSupportNavigateUp() {
        // Allow the animation to be reversed
        supportFinishAfterTransition();
        return true;
    }

    /**
     * Shows the title in the app bar when a CollapsingToolbarLayout is fully collapsed, otherwise
     * hide the title.
     *
     * References: @see "https://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed"
     * @see "https://stackoverflow.com/questions/31872653/how-can-i-determine-that-collapsingtoolbar-is-collapsed"
     */
    private void setCollapsingToolbarTitle() {
        // Set onOffsetChangedListener to determine when the CollapsingToolbar is collapsed
        mDetailBinding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int scrollRange = appBarLayout.getTotalScrollRange();
                if (verticalOffset == 0) {
                    // When a CollapsingToolbarLayout is expanded, hide the title
                    mDetailBinding.collapsingToolbar.setTitle(getString(R.string.space));
                } else if (Math.abs(verticalOffset) >= scrollRange) {
                    // When a CollapsingToolbarLayout is fully collapsed, show the title
                    if (mResultName != null) {
                        mDetailBinding.collapsingToolbar.setTitle(mResultName);
                    }
                } else {
                    // Otherwise, hide the title
                    mDetailBinding.collapsingToolbar.setTitle(getString(R.string.space));
                }
            }
        });
    }

    /**
     * When an episode is selected, starts the NowPlayingActivity.
     * @param item Item object which contains an episode data
     * @param imageView The shared element
     */
    @Override
    public void onItemClick(Item item, ImageView imageView) {
        // Update the episode data using SharedPreferences each time the user selects the episode.
        CandyPodUtils.updateSharedPreference(this, item, mResultName,
                CandyPodUtils.getItemImageUrl(item, mPodcastImage));
        // Send an update broadcast message to the app widget
        CandyPodUtils.sendBroadcastToWidget(this);

        Intent intent = new Intent(this, NowPlayingActivity.class);
        // Wrap the parcelable into a bundle
        Bundle b = new Bundle();
        b.putParcelable(EXTRA_ITEM, item);
        // Pass the bundle through intent
        intent.putExtra(EXTRA_ITEM, b);
        // Pass podcast id
        intent.putExtra(EXTRA_RESULT_ID, mResultId);
        // Pass podcast title
        intent.putExtra(EXTRA_RESULT_NAME, mResultName);
        // Pass the podcast image URL. If there is no item image, use this podcast image.
        intent.putExtra(EXTRA_PODCAST_IMAGE, mPodcastImage);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Apply the shared element transition to the podcast image
            String transitionName = imageView.getTransitionName();
            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    imageView,
                    transitionName
            ).toBundle();
            startActivity(intent, bundle);
        } else {
            // Once the Intent has been created, start the NowPlayingActivity
            startActivity(intent);
        }


        Intent serviceIntent = new Intent(this, PodcastService.class);
        // Set the action to check if the old player should be released in PodcastService
        serviceIntent.setAction(ACTION_RELEASE_OLD_PLAYER);
        // Pass an Item object
        serviceIntent.putExtra(EXTRA_ITEM, b);
        // Pass podcast title and podcast image
        serviceIntent.putExtra(EXTRA_RESULT_NAME, mResultName);
        serviceIntent.putExtra(EXTRA_PODCAST_IMAGE, mPodcastImage);
        // Start the PodcastService
        startService(serviceIntent);

        // Log select content event when a user has selected content in an app
        Analytics.logEventSelectContent(mResultName, item.getTitle());
    }
}
