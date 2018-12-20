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

package com.soojeongshin.candypod.ui.search;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.databinding.ActivitySearchResultsBinding;
import com.soojeongshin.candypod.model.SearchResponse;
import com.soojeongshin.candypod.model.SearchResult;
import com.soojeongshin.candypod.ui.GridAutofitLayoutManager;
import com.soojeongshin.candypod.ui.subscribe.SubscribeActivity;
import com.soojeongshin.candypod.utilities.CandyPodUtils;
import com.soojeongshin.candypod.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_ARTWORK_100;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_ID;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_NAME;
import static com.soojeongshin.candypod.utilities.Constants.GRID_AUTO_FIT_COLUMN_WIDTH;
import static com.soojeongshin.candypod.utilities.Constants.I_TUNES_SEARCH;
import static com.soojeongshin.candypod.utilities.Constants.SEARCH_MEDIA_PODCAST;

/**
 * Reference: @see "https://developer.android.com/training/search/setup#create-sa"
 */
public class SearchResultsActivity extends AppCompatActivity implements SearchAdapter.SearchAdapterOnClickHandler {

    /** This field is used for data binding */
    private ActivitySearchResultsBinding mSearchBinding;

    /** ViewModel for SearchResultsActivity */
    private SearchViewModel mSearchViewModel;

    /** Member variable for SearchAdapter */
    private SearchAdapter mSearchAdapter;

    /** Member variable for a list of {@link SearchResult}s which is the search results to display */
    private List<SearchResult> mSearchResults;

    /** The user's search query from the SearchView in the AddPodcastActivity */
    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_search_results);

        handleIntent(getIntent());

        // Initialize the adapter and attach it to the RecyclerView
        initAdapter();

        // Run the layout animation for RecyclerView.
        CandyPodUtils.runLayoutAnimation(mSearchBinding.rvSearchResults);

        // Get the ViewModel from the factory
        setupViewModel();

        // Observe changes in the SearchResponse
        observeSearchResponse();

        // Show the up button in the action bar
        showUpButton();
    }

    /**
     * Since this searchable activity launches in single top mode (android:launchMode="singleTop"),
     * also handle the ACTION_SEARCH intent in the onNewIntent() method.
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mQuery = intent.getStringExtra(SearchManager.QUERY);
            // Show a toast message to display the query string
            Toast.makeText(this, getString(R.string.toast_query) + mQuery, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates a LinearLayoutManager and SearchAdapter, and set them to the RecyclerView.
     */
    private void initAdapter() {
        // A GridAutofitLayoutManager is responsible for calculating the amount of GridView columns
        // based on screen size and positioning item views within a RecyclerView into a grid layout.
        // Reference: @see "https://codentrick.com/part-4-android-recyclerview-grid/"
        GridAutofitLayoutManager layoutManager = new GridAutofitLayoutManager(
                this, GRID_AUTO_FIT_COLUMN_WIDTH);
        mSearchBinding.rvSearchResults.setLayoutManager(layoutManager);
        mSearchBinding.rvSearchResults.setHasFixedSize(true);

        // Create an empty ArrayList
        mSearchResults = new ArrayList<>();
        // A SearchAdapter is responsible for displaying each item in the list.
        mSearchAdapter = new SearchAdapter(mSearchResults, this);
        // Set adapter to the RecyclerView
        mSearchBinding.rvSearchResults.setAdapter(mSearchAdapter);
    }

    /**
     * Gets the ViewModel from the factory.
     */
    private void setupViewModel() {
        if (mQuery != null) {
            SearchViewModelFactory searchFactory = InjectorUtils.provideSearchViewModelFactory(
                    this, I_TUNES_SEARCH, getString(R.string.pref_country_default),
                    SEARCH_MEDIA_PODCAST, mQuery);
            mSearchViewModel = ViewModelProviders.of(this, searchFactory).get(SearchViewModel.class);
        }
    }

    /**
     * Observes changes in the SearchResponse.
     */
    private void observeSearchResponse() {
        // When online, show a loading indicator. When offline, show offline message.
        showLoadingOrOffline();

        mSearchViewModel.getSearchResponse().observe(this, new Observer<SearchResponse>() {
            @Override
            public void onChanged(@Nullable SearchResponse searchResponse) {
                if (searchResponse != null) {
                    // Hide the loading indicator
                    mSearchBinding.setIsLoading(false);

                    mSearchResults = searchResponse.getSearchResults();

                    // Check if the search results are empty
                    if (mSearchResults.isEmpty()) {
                        // Display "No results found" text when there are no search results.
                        mSearchBinding.setIsEmpty(true);
                    } else {
                        // Update a list of search results and notify the adapter of any changes.
                        mSearchAdapter.addAll(mSearchResults);
                    }
                }
            }
        });
    }

    /**
     * This is where we receive our callback from {@link SearchAdapter.SearchAdapterOnClickHandler}
     *
     * This callback is invoked when you click on an item in the list.
     * Once the Intent has been created, starts the {@link SubscribeActivity}
     * @param searchResult The {@link SearchResult} that was clicked
     * @param imageView The shared element
     */
    @Override
    public void onItemClick(SearchResult searchResult, ImageView imageView) {
        // Get the podcast ID and name
        String podcastId = String.valueOf(searchResult.getCollectionId());
        String podcastName = searchResult.getCollectionName();
        String artwork600 = searchResult.getArtworkUrl600();

        Intent intent = new Intent(this, SubscribeActivity.class);
        // Pass the podcast ID which will be used to a lookup request to search for the podcast
        intent.putExtra(EXTRA_RESULT_ID, podcastId);
        // Pass the podcast title which will be used to set the title in the app bar
        intent.putExtra(EXTRA_RESULT_NAME, podcastName);
        // Pass the podcast image
        intent.putExtra(EXTRA_RESULT_ARTWORK_100, artwork600);

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
            // Once the Intent has been created, start the SubscribeActivity
            startActivity(intent);
        }
    }

    /**
     * Shows an up button on the action bar.
     */
    private void showUpButton() {
        ActionBar actionBar = getSupportActionBar();
        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                // Navigate back to the AddPodcast activity when the home button is pressed
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * When online, show a loading indicator. When offline, show offline message.
     */
    private void showLoadingOrOffline() {
        if (CandyPodUtils.isOnline(this)) {
            // Show the loading indicator
            mSearchBinding.setIsLoading(true);
        } else {
            // Show a text that indicates there is no internet connectivity
            mSearchBinding.setIsOffline(true);
        }
    }
}
