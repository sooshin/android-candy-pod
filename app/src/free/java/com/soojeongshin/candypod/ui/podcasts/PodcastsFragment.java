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

package com.soojeongshin.candypod.ui.podcasts;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.data.PodcastEntry;
import com.soojeongshin.candypod.databinding.FragmentPodcastsBinding;
import com.soojeongshin.candypod.ui.GridAutofitLayoutManager;
import com.soojeongshin.candypod.ui.add.AddPodcastActivity;
import com.soojeongshin.candypod.ui.detail.DetailActivity;
import com.soojeongshin.candypod.utilities.InjectorUtils;

import java.util.List;

import static com.soojeongshin.candypod.utilities.Constants.EXTRA_PODCAST_IMAGE;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_ID;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_NAME;
import static com.soojeongshin.candypod.utilities.Constants.GRID_AUTO_FIT_COLUMN_WIDTH;

/**
 * The PodcastsFragment displays the list of podcasts that the user subscribed.
 */
public class PodcastsFragment extends Fragment
        implements PodcastsAdapter.PodcastsAdapterOnClickHandler {

    /** This field is used for data binding */
    private FragmentPodcastsBinding mPodcastsBinding;

    /** Member variable for PodcastsAdapter */
    private PodcastsAdapter mPodcastsAdapter;

    /** PodcastsViewModel which stores and manages LiveData the list of PodcastEntries */
    private PodcastsViewModel mPodcastsViewModel;

    public PodcastsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mPodcastsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_podcasts,
                container, false);
        View rootView = mPodcastsBinding.getRoot();

        // Change the title associated with this fragment
        getActivity().setTitle(getString(R.string.app_name));

        // When a FAB is clicked, start the AddPodcastActivity
        startAddPodcastActivity();

        // Create a GridAutofitLayoutManager and PodcastsAdapter, and set them to the RecyclerView
        initAdapter();

        // Hide FAB when scrolling
        hideShowFab();

        // Enable test ads
        setupTestAds();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup PodcatsViewModel
        setupViewModel(this.getActivity());
    }

    /**
     * When the user clicks a FAB, start the AddPodcastActivity.
     */
    private void startAddPodcastActivity() {
        mPodcastsBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the Intent that will start the AddPodcastActivity
                Intent intent = new Intent(getContext(), AddPodcastActivity.class);
                // Once the Intent has been created, start the AddPodcastActivity
                startActivity(intent);
            }
        });
    }

    /**
     * Creates a GridAutofitLayoutManager and PodcastsAdapter, and set them to the RecyclerView.
     */
    private void initAdapter() {
        // A GridAutofitLayoutManager is responsible for calculating the amount of GridView columns
        // based on screen size and positioning item views within a RecyclerView into a grid layout.
        // Reference: @see "https://codentrick.com/part-4-android-recyclerview-grid/"
        GridAutofitLayoutManager layoutManager = new GridAutofitLayoutManager(
                getContext(), GRID_AUTO_FIT_COLUMN_WIDTH);
        // Set the layout for the RecyclerView to be a grid layout
        mPodcastsBinding.rvPodcasts.setLayoutManager(layoutManager);
        // Use this setting to improve performance if you know that changes in content do not
        // change the child layout size in the RecyclerView
        mPodcastsBinding.rvPodcasts.setHasFixedSize(true);

        // Initialize the adapter and attach it to the RecyclerView
        mPodcastsAdapter = new PodcastsAdapter(getContext(), this);
        mPodcastsBinding.rvPodcasts.setAdapter(mPodcastsAdapter);
    }

    /**
     * Every time the podcast data is updated, updates the UI.
     */
    private void setupViewModel(Context context) {
        // Get the ViewModel from the factory
        PodcastsViewModelFactory podcastsFactory = InjectorUtils.providePodcastsViewModelFactory(context);
        mPodcastsViewModel = ViewModelProviders.of(this, podcastsFactory)
                .get(PodcastsViewModel.class);

        // Observe the list of all {@link PodcastEntry}
        mPodcastsViewModel.getPodcasts().observe(this, new Observer<List<PodcastEntry>>() {
            @Override
            public void onChanged(@Nullable List<PodcastEntry> podcastEntries) {
                // When the podcasts list is empty, show an empty view, otherwise, show podcasts.
                if (podcastEntries != null && podcastEntries.size() != 0) {
                   // Make the view for podcasts visible.
                    mPodcastsBinding.setHasPodcasts(true);

                    // Update the list of PodcastEntries and notify the adapter of any changes
                    mPodcastsAdapter.setPodcastEntries(podcastEntries);
                } else {
                    // When the podcasts list is empty, show an empty view.
                    showEmptyView();
                }
            }
        });
    }

    /**
     * This is where we receive our callback from
     * {@link PodcastsAdapter.PodcastsAdapterOnClickHandler}.
     *
     * This callback is invoked when the user clicks on a podcast in the list. When the user clicks
     * the podcast, start the DetailActivity.
     *
     * @param podcastEntry A single row from podcast table that has the data of the podcast.
     *             When the user subscribes to the podcast, the podcast data is added to the database.
     * @param imageView The shared element
     */
    @Override
    public void onPodcastClick(PodcastEntry podcastEntry, ImageView imageView) {
        // Create the Intent that will start the DetailActivity
        Intent intent = new Intent(getActivity(), DetailActivity.class);

        // Get the podcast ID, title, and the image from the podcastEntry and pass them via Intent
        String podcastId = podcastEntry.getPodcastId();
        String podcastName = podcastEntry.getTitle();
        String podcastImage = podcastEntry.getArtworkImageUrl();
        intent.putExtra(EXTRA_RESULT_ID, podcastId);
        intent.putExtra(EXTRA_RESULT_NAME, podcastName);
        intent.putExtra(EXTRA_PODCAST_IMAGE, podcastImage);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Apply the shared element transition to the podcast image
            String transitionName = imageView.getTransitionName();
            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this.getActivity(),
                    imageView,
                    transitionName
            ).toBundle();
            startActivity(intent, bundle);
        } else {
            // Once the Intent has been created, start the DetailActivity
            startActivity(intent);
        }
    }

    /**
     * When the podcasts list is empty, show an empty view.
     */
    private void showEmptyView() {
        // Show an empty view
        mPodcastsBinding.setHasPodcasts(false);
        // Set text programmatically in order to make text invisible when the user changes the menu
        // items in the navigation drawer
        mPodcastsBinding.tvEmptyPodcasts.setText(getString(R.string.empty_podcasts));
    }

    /**
     * Hides FAB when scrolling.
     * Reference: @see "https://stackoverflow.com/questions/33208613/hide-floatingactionbutton-on-scroll-of-recyclerview"
     */
    private void hideShowFab() {
        // Add a listener to be notified of any changes in scroll state
        mPodcastsBinding.rvPodcasts.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Hide FAB when the RecyclerView is scrolling
                if (dy > 0 || dy < 0 && mPodcastsBinding.fab.isShown()) {
                    mPodcastsBinding.fab.hide();
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // Show FAB when the RecyclerView is not currently scrolling
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mPodcastsBinding.fab.show();
                }
            }
        });
    }

    /**
     * Enables test ads.
     */
    private void setupTestAds() {
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mPodcastsBinding.adView.loadAd(adRequest);
    }
}
