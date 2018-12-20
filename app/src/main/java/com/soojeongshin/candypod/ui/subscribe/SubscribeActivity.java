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

package com.soojeongshin.candypod.ui.subscribe;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.soojeongshin.candypod.AppExecutors;
import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.data.CandyPodDatabase;
import com.soojeongshin.candypod.data.PodcastEntry;
import com.soojeongshin.candypod.databinding.ActivitySubscribeBinding;
import com.soojeongshin.candypod.model.LookupResponse;
import com.soojeongshin.candypod.model.LookupResult;
import com.soojeongshin.candypod.model.rss.ArtworkImage;
import com.soojeongshin.candypod.model.rss.Category;
import com.soojeongshin.candypod.model.rss.Channel;
import com.soojeongshin.candypod.model.rss.Item;
import com.soojeongshin.candypod.model.rss.RssFeed;
import com.soojeongshin.candypod.ui.MainActivity;
import com.soojeongshin.candypod.ui.detail.PodcastEntryViewModel;
import com.soojeongshin.candypod.ui.detail.PodcastEntryViewModelFactory;
import com.soojeongshin.candypod.utilities.CandyPodUtils;
import com.soojeongshin.candypod.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import timber.log.Timber;

import static com.soojeongshin.candypod.utilities.Constants.BLUR_RADIUS;
import static com.soojeongshin.candypod.utilities.Constants.BLUR_SAMPLING;
import static com.soojeongshin.candypod.utilities.Constants.DEF_VIBRANT_COLOR;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_ARTWORK_100;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_ID;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_NAME;
import static com.soojeongshin.candypod.utilities.Constants.I_TUNES_LOOKUP;
import static com.soojeongshin.candypod.utilities.Constants.MAX_COLOR_COUNT;

/**
 * The SubscribeActivity displays the detailed information of the podcast such as the title, the image,
 * subscribe button, descriptions, and the list of episodes. If the user clicks the subscribe button,
 * the PodcastsFragment will show this podcast with a grid arrangement of the image.
 */
public class SubscribeActivity extends AppCompatActivity {

    /** The podcast ID used to a lookup request */
    private String mResultId;
    /** The podcast title */
    private String mResultName;
    /** The podcast image used when there is no episode image */
    private String mPodcastImage;
    private String mResultArtwork100;

    /** ViewModel for SubscribeActivity */
    private SubscribeViewModel mSubscribeViewModel;
    /** ViewModel which stores and manages LiveData RssFeed */
    private RssFeedViewModel mRssFeedViewModel;
    /** Member variable for the PodcastEntryViewModel to store and manage LiveData PodcastEntry */
    private PodcastEntryViewModel mPodcastEntryViewModel;

    /** This field is used for data binding **/
    private ActivitySubscribeBinding mSubscribeBinding;

    /** Member variable for SubscribeAdapter */
    private SubscribeAdapter mSubscribeAdapter;

    /** Member variable for the list of {@link Item}s which is the episodes in the podcast */
    private List<Item> mItemList;

    /** Member variable for the Database */
    private CandyPodDatabase mDb;
    /** Member variable for the PodcastEntry */
    private PodcastEntry mPodcastEntry;
    /** True when the user subscribed the podcast, otherwise false */
    private boolean mIsSubscribed;

    /** The default value of vibrant color used if the Vibrant swatch is null */
    private int mVibrantColor = DEF_VIBRANT_COLOR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSubscribeBinding = DataBindingUtil.setContentView(this, R.layout.activity_subscribe);

        // Get the podcast ID and title
        getResultData();

       // Get the ViewModel from the factory
        setupViewModel();
        // Observe changes in the LookupResponse
        observeLookupResponse();

        // Create a LinearLayoutManager and SubscribeAdapter, and set them to the RecyclerView
        initAdapter();

        // Show the up button on Collapsing Toolbar
        showUpButton();
        // Show the title in the app bar when a CollapsingToolbarLayout is fully collapsed
        setCollapsingToolbarTitle();

        // Get the Database instance
        mDb = CandyPodDatabase.getInstance(getApplicationContext());
        // Check if the podcast is subscribed or not
        mIsSubscribed = isSubscribed();
    }

    /**
     * Creates a LinearLayoutManager and SubscribeAdapter, and set them to the RecyclerView.
     */
    private void initAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Set the layout manager to the RecyclerView
        mSubscribeBinding.rvItem.setLayoutManager(layoutManager);
        // Use this setting to improve performance if you know that changes in content do not
        // change the child layout size in the RecyclerView
        mSubscribeBinding.rvItem.setHasFixedSize(true);

        // Create an empty ArrayList
        mItemList = new ArrayList<>();
        // SubscribeAdapter is responsible for displaying each item in the list.
        mSubscribeAdapter = new SubscribeAdapter(mItemList, mPodcastImage);
        // Set adapter to the RecyclerView
        mSubscribeBinding.rvItem.setAdapter(mSubscribeAdapter);
    }

    /**
     * Gets the podcast ID which is used to create a lookup request to search for content.
     * Gets the podcast title used to set the title in the app bar.
     */
    private void getResultData() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_RESULT_ID)) {
            mResultId = intent.getStringExtra(EXTRA_RESULT_ID);
        }
        if(intent.hasExtra(EXTRA_RESULT_NAME)) {
            mResultName = intent.getStringExtra(EXTRA_RESULT_NAME);
        }
        if(intent.hasExtra(EXTRA_RESULT_ARTWORK_100)) {
            mResultArtwork100 = intent.getStringExtra(EXTRA_RESULT_ARTWORK_100);
        }
    }

    /**
     * Gets the ViewModel from the factory.
     */
    private void setupViewModel() {
        SubscribeViewModelFactory factory = InjectorUtils.provideSubscribeViewModelFactory(
                SubscribeActivity.this, I_TUNES_LOOKUP, mResultId);
        mSubscribeViewModel = ViewModelProviders.of(this, factory).get(SubscribeViewModel.class);
    }

    /**
     * Every time the LookupResponse data is updated, the onChanged callback will be invoked and
     * update the UI.
     */
    private void observeLookupResponse() {
        // When online, show a loading indicator. When offline, show offline message.
        showLoadingOrOffline();

        mSubscribeViewModel.getLookupResponse().observe(this, new Observer<LookupResponse>() {
            @Override
            public void onChanged(@Nullable LookupResponse lookupResponse) {
                if (lookupResponse != null) {
                    // Hide the loading indicator
                    mSubscribeBinding.setIsLoading(false);

                    List<LookupResult> lookupResults = lookupResponse.getLookupResults();
                    String feedUrl = lookupResults.get(0).getFeedUrl();
                    Timber.e(feedUrl);
                    // Check if the feedUrl exists
                    if (TextUtils.isEmpty(feedUrl)) {
                        // If the feedUrl is null, show a toast message
                        Toast.makeText(SubscribeActivity.this,
                                getString(R.string.toast_feed_url_null), Toast.LENGTH_SHORT).show();
                        // Hide the loading indicator
                        mSubscribeBinding.setIsLoading(false);
                    } else {
                        // Get the RssFeedViewModel from the factory
                        setupRssFeedViewModel(feedUrl);
                        // Observe changes in the RssFeed
                        observeRssFeed();
                    }
                }
            }
        });
        // Hide the loading indicator
        mSubscribeBinding.setIsLoading(false);
    }

    /**
     * Gets the RssFeedViewModel from the factory.
     * @param feedUrl The feed URL extracted from the list of LookupResults has the episode
     *                  metadata and stream URLs for the audio file.
     */
    private void setupRssFeedViewModel(String feedUrl) {
        RssFeedViewModelFactory rssFactory = InjectorUtils.provideRssViewModelFactory(
                SubscribeActivity.this, feedUrl);
        mRssFeedViewModel = ViewModelProviders.of(this, rssFactory).get(RssFeedViewModel.class);
    }

    /**
     * Observes changes in the RssFeed
     */
    private void observeRssFeed() {
        // When online, show a loading indicator.
        showLoadingOrOffline();

        mRssFeedViewModel.getRssFeed().observe(this, new Observer<RssFeed>() {
            @Override
            public void onChanged(@Nullable RssFeed rssFeed) {
                if (rssFeed != null) {
                    // Hide the loading indicator
                    mSubscribeBinding.setIsLoading(false);
                    // Show the subscribe button
                    mSubscribeBinding.btSubscribe.setVisibility(View.VISIBLE);

                    Channel channel = rssFeed.getChannel();

                    // Show the details of the podcast
                    showDetails(channel);
                    // Show the episodes of the podcast
                    showItems(channel);
                }
            }
        });
    }

    /**
     * Shows the details of the podcast and create the PodcastEntry based on the data.
     * @param channel Channel object that contains data, such as title, description, author,
     *                language, categories, image, items.
     */
    private void showDetails(Channel channel) {
        // Get the image URL. If the artworkImageUrl does not exist, use mResultArtwork100
        // which is received via Intent
        List<ArtworkImage> artworkImage = channel.getImages();
        String artworkImageUrl = mResultArtwork100;
        if (!artworkImage.isEmpty()) {
            ArtworkImage image = artworkImage.get(0);
            if (image != null) {
                artworkImageUrl = image.getImageHref();
                if (TextUtils.isEmpty(artworkImageUrl)) {
                    artworkImageUrl = mResultArtwork100;
                }
            }
        }

        // Set the podcast image in order to display it in the list of items when there are no
        // episode images.
        mPodcastImage = artworkImageUrl;
        mSubscribeAdapter.setPodcastImage(mPodcastImage);

        // Set the background color from a palette
        Glide.with(this)
                .asBitmap()
                .load(artworkImageUrl)
                .into(new ImageViewTarget<Bitmap>(mSubscribeBinding.ivPalette) {
                    @Override
                    protected void setResource(@Nullable Bitmap resource) {
                        if (resource != null) {
                            // Generate the palette asynchronously using an AsyncTask to gather
                            // the Palette swatch information from the bitmap
                            // Reference: @see "https://github.com/codepath/android_guides/wiki/Dynamic-Color-using-Palettes"
                            // @see "https://developer.android.com/training/material/palette-colors#java"
                            Palette.from(resource).maximumColorCount(MAX_COLOR_COUNT).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@Nullable Palette palette) {
                                    Palette.Swatch vibrant = palette.getVibrantSwatch();
                                    // Check that the Vibrant swatch is available
                                    if (vibrant != null) {
                                        mVibrantColor = vibrant.getRgb();
                                    }
                                    // Set the background color of an ImageView based on the vibrant color
                                    mSubscribeBinding.ivPalette.setBackgroundColor(mVibrantColor);
                                }
                            });
                        }

                    }
                });

        // Load blurry artwork using Glide Transformations library
        Glide.with(this)
                .load(artworkImageUrl)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(BLUR_RADIUS, BLUR_SAMPLING)))
                .into(mSubscribeBinding.ivBlur);

        // Use Glide library to load the artwork
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.candy_error);
        Glide.with(this)
                .load(artworkImageUrl)
                .apply(options)
                .into(mSubscribeBinding.ivArtwork);

        // Get the title and set the text
        String title = channel.getTitle();
        mSubscribeBinding.tvTitle.setText(title);

        // Get the author and set the text
        String author = channel.getITunesAuthor();
        mSubscribeBinding.tvAuthor.setText(author);

        // Get the categories and set the categories
        List<Category> categories = channel.getCategories();
        String categoryText;
        if(categories != null && !categories.isEmpty()) {
            for (Category category:categories) {
                categoryText = category.getText();
                if (categoryText != null) {
                    mSubscribeBinding.tvCategory.append(categoryText + getString(R.string.space));
                }
            }
        }

        // Get the language and set the text
        String language = channel.getLanguage();
        if (!TextUtils.isEmpty(language)) {
            // Convert language code to language name
            String languageName = CandyPodUtils.convertLanguageCode(language);
            mSubscribeBinding.tvLanguage.setText(languageName);
        }

        // Get the description
        String description = channel.getDescription();
        // Check if the description is empty
        if (!TextUtils.isEmpty(description)) {
            // Convert HTML to plain text and set the text
            // Reference: @see "https://stackoverflow.com/questions/22573319/how-to-convert-html-text-to-plain-text-in-android"
            mSubscribeBinding.tvDescription.setText(Html.fromHtml(Html.fromHtml(description).toString()));
        }

        // Get the list of items
        mItemList = channel.getItemList();
        if (mItemList != null && !mItemList.isEmpty()) {
            // Create the PodcastEntry based on the data
            mPodcastEntry = new PodcastEntry(mResultId, title, description, author,
                    artworkImageUrl, mItemList, new Date());
        }
    }

    /**
     * Shows the episodes of the podcast.
     * @param channel Channel object that includes the items data which is the podcast episodes.
     */
    private void showItems(Channel channel) {
        // Get the list of items
        mItemList = channel.getItemList();
        if (mItemList != null && !mItemList.isEmpty()) {
            // Update the data source and notify the adapter of any changes.
            mSubscribeAdapter.addAll(mItemList);
        }
    }

    /**
     * Returns true when the user subscribed the podcast otherwise, returns false.
     */
    private boolean isSubscribed() {
        // Get the PodcastEntryViewModel from the factory
        PodcastEntryViewModelFactory podcastEntryFactory = InjectorUtils.providePodcastEntryViewModelFactory(
                this, mResultId);
        mPodcastEntryViewModel = ViewModelProviders.of(this, podcastEntryFactory)
                .get(PodcastEntryViewModel.class);

        // Observe the PodcastEntry and changes the button text based on whether or not the podcast
        // exists
        mPodcastEntryViewModel.getPodcastEntry().observe(this, new Observer<PodcastEntry>() {
            @Override
            public void onChanged(@Nullable PodcastEntry podcastEntry) {
                if (mPodcastEntryViewModel.getPodcastEntry().getValue() == null) {
                    mSubscribeBinding.btSubscribe.setText(getString(R.string.subscribe));
                    mIsSubscribed = false;
                } else {
                    mSubscribeBinding.btSubscribe.setText(getString(R.string.unsubscribe));
                    mIsSubscribed = true;
                }
            }
        });
        return mIsSubscribed;
    }

    /**
     * Called when the subscribe button is clicked. If the podcast is not in the podcast table,
     * insert the data into the underlying database. Otherwise, delete the podcast data from the
     * database.
     */
    public void onSubscribeClick(View view) {
        // Check if the PodcastEntry is not null
        if (mPodcastEntry != null) {
            if (!mIsSubscribed) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        // Insert the podcast data into the database by using the podcastDao
                        mDb.podcastDao().insertPodcast(mPodcastEntry);
                    }
                });
            } else {
                mPodcastEntry = mPodcastEntryViewModel.getPodcastEntry().getValue();
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        // Delete the podcast from the database by using the podcastDao
                        mDb.podcastDao().deletePodcast(mPodcastEntry);
                    }
                });
            }
            // Show a snackbar message
            showSnackbar();
        }
    }

    /**
     * Shows a snackbar message when the user clicks subscribe or unsubscribe button.
     */
    private void showSnackbar() {
        String snackMessage;
        Snackbar snackbar;
        if (mIsSubscribed) {
            // Removed
            snackMessage = getString(R.string.snackbar_removed);
            snackbar = Snackbar.make(mSubscribeBinding.coordinator, snackMessage, Snackbar.LENGTH_LONG);
        } else {
            snackMessage = getString(R.string.snackbar_subscribed);
            snackbar = Snackbar.make(mSubscribeBinding.coordinator, snackMessage, Snackbar.LENGTH_LONG);
            // Add an action "Go to Podcasts"
            // Reference: @see "https://www.androidhive.info/2015/09/android-material-design-snackbar-example/"
            snackbar.setAction(getString(R.string.snackbar_action_go), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the MainActivity
                    Intent intent = new Intent(SubscribeActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
        }
        snackbar.show();
    }

    /**
     * Shows the up button on the Collapsing Toolbar.
     */
    private void showUpButton() {
        // Set the toolbar as the app bar
        setSupportActionBar(mSubscribeBinding.toolbar);

        ActionBar actionBar = getSupportActionBar();
        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * When the user press the up button in the app bar, finishes this SubscribeActivity.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
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
        mSubscribeBinding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int scrollRange = appBarLayout.getTotalScrollRange();
                if (verticalOffset == 0) {
                    // When a CollapsingToolbarLayout is expanded, hide the title
                    mSubscribeBinding.collapsingToolbar.setTitle(getString(R.string.space));
                } else if (Math.abs(verticalOffset) >= scrollRange) {
                    // When a CollapsingToolbarLayout is fully collapsed, show the title
                    if (mResultName != null) {
                        mSubscribeBinding.collapsingToolbar.setTitle(mResultName);
                    }
                } else {
                    // Otherwise, hide the title
                    mSubscribeBinding.collapsingToolbar.setTitle(getString(R.string.space));
                }
            }
        });
    }

    /**
     * When online, show a loading indicator. When offline, show offline message.
     */
    private void showLoadingOrOffline() {
        if (CandyPodUtils.isOnline(this)) {
            // Show the loading indicator
            mSubscribeBinding.setIsLoading(true);
            // Hide the subscribe button when loading data
            mSubscribeBinding.btSubscribe.setVisibility(View.GONE);
        } else {
            // Show a text that indicates there is no internet connectivity
            mSubscribeBinding.setIsOffline(true);
            // Hide the subscribe button when offline
            mSubscribeBinding.btSubscribe.setVisibility(View.GONE);
        }
    }
}
