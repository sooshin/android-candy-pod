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

package com.soojeongshin.candypod.ui.favorites;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.data.FavoriteEntry;
import com.soojeongshin.candypod.databinding.FragmentFavoritesBinding;
import com.soojeongshin.candypod.model.rss.Enclosure;
import com.soojeongshin.candypod.model.rss.Item;
import com.soojeongshin.candypod.model.rss.ItemImage;
import com.soojeongshin.candypod.service.PodcastService;
import com.soojeongshin.candypod.ui.nowplaying.NowPlayingActivity;
import com.soojeongshin.candypod.utilities.CandyPodUtils;
import com.soojeongshin.candypod.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

import static com.soojeongshin.candypod.utilities.Constants.ACTION_RELEASE_OLD_PLAYER;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_ITEM;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_PODCAST_IMAGE;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_ID;
import static com.soojeongshin.candypod.utilities.Constants.EXTRA_RESULT_NAME;
import static com.soojeongshin.candypod.utilities.Constants.NO_DRAWABLES;

/**
 * The FavoritesFragment displays a list of the favorite episodes.
 */
public class FavoritesFragment extends Fragment implements FavoritesAdapter.FavoritesAdapterOnClickHandler {

    /** This field is used for data binding */
    private FragmentFavoritesBinding mFavoritesBinding;

    /** Member variable for FavoritesAdapter */
    private FavoritesAdapter mFavoritesAdapter;

    /***The ViewModel for FavoritesFragment */
    private FavViewModel mFavViewModel;
    /** Podcast Id, title, and image URL */
    private String mPodcastId;
    private String mPodcastTitle;
    private String mPodcastImage;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the data binding layout for this fragment
        mFavoritesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorites,
                container, false);
        View rootView = mFavoritesBinding.getRoot();

        // Change the title associated with this fragment
        getActivity().setTitle(getString(R.string.favorites));

        // Create and set the adapter to the RecyclerView
        initAdapter();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup ViewModel
        setupViewModel(this.getActivity());
    }

    /**
     * Creates a LinearLayoutManager and FavoritesAdapter, and set them to the RecyclerView.
     */
    private void initAdapter() {
        // A LinearLayoutManager is responsible for measuring and positioning item views within a
        // RecyclerView into a linear list.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        // Set the layout for the RecyclerView to be a linear layout
        mFavoritesBinding.rvFavorites.setLayoutManager(layoutManager);
        // Use this setting to improve performance if you know that changes in content do not
        // change the child layout size in the RecyclerView
        mFavoritesBinding.rvFavorites.setHasFixedSize(true);

        // Initialize the adapter and attach it to the RecyclerView
        mFavoritesAdapter = new FavoritesAdapter(getContext(), this);
        mFavoritesBinding.rvFavorites.setAdapter(mFavoritesAdapter);
    }

    /**
     * Every time the favorite episode data is updated, update the UI.
     * @param context Context we use to utility methods, app resources and layout inflaters
     */
    private void setupViewModel(Context context) {
        // Get the ViewModel from the factory
        FavViewModelFactory favFactory = InjectorUtils.provideFavViewModelFactory(context);
        mFavViewModel = ViewModelProviders.of(this, favFactory).get(FavViewModel.class);

        // Observe FavoriteEntry data
        mFavViewModel.getFavorites().observe(this, new Observer<List<FavoriteEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavoriteEntry> favoriteEntries) {
                if (favoriteEntries != null && favoriteEntries.size() != 0) {
                    // Make the view for favorites visible
                    mFavoritesBinding.setHasFavorites(true);

                    mFavoritesAdapter.setFavoriteEntries(favoriteEntries);
                } else {
                    // When the favorite list is empty, show an empty view.
                    showEmptyView();
                }
            }
        });
    }

    /**
     * This is where we receive our callback from {@link FavoritesAdapter.FavoritesAdapterOnClickHandler}.
     *
     * This callback is invoked when you click on an item in the list.
     * Once the Intent has been created, starts the NowPlayingActivity and the PodcastService.
     *
     * @param favoriteEntry FavoriteEntry the user clicked
     * @param imageView The shared element
     */
    @Override
    public void onFavoriteClick(FavoriteEntry favoriteEntry, ImageView imageView) {
        Item item = getItem(favoriteEntry);
        // Update the episode data using SharedPreferences each time the user selects the episode
        CandyPodUtils.updateSharedPreference(this.getContext(), item,
                mPodcastTitle, CandyPodUtils.getItemImageUrl(item, mPodcastImage));
        // Send an update broadcast message to the app widget
        CandyPodUtils.sendBroadcastToWidget(this.getContext());

        // Start the NowPlayingActivity
        Intent intent = new Intent(this.getActivity(), NowPlayingActivity.class);
        // Wrap the parcelable into a bundle
        Bundle b = new Bundle();
        b.putParcelable(EXTRA_ITEM, item);
        // Pass the bundle through intent
        intent.putExtra(EXTRA_ITEM, b);
        // Pass podcast id
        intent.putExtra(EXTRA_RESULT_ID, mPodcastId);
        // Pass podcast title
        intent.putExtra(EXTRA_RESULT_NAME, mPodcastTitle);
        // Pass the podcast image URL. If there is no item image, use this podcast image.
        intent.putExtra(EXTRA_PODCAST_IMAGE, mPodcastImage);

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
            // Once the Intent has been created, start the NowPlayingActivity
            startActivity(intent);
        }

        // Start the PodcastService
        Intent serviceIntent = new Intent(this.getActivity(), PodcastService.class);
        // Set the action to check if the old player should be released in PodcastService
        serviceIntent.setAction(ACTION_RELEASE_OLD_PLAYER);
        // Pass item that contains episode data
        serviceIntent.putExtra(EXTRA_ITEM, b);
        // Pass podcast title and podcast image
        serviceIntent.putExtra(EXTRA_RESULT_NAME, mPodcastTitle);
        serviceIntent.putExtra(EXTRA_PODCAST_IMAGE, mPodcastImage);
        getActivity().startService(serviceIntent);
    }

    /**
     * Returns item used to intent extra.
     * @param favoriteEntry favoriteEntry that the user clicks
     */
    private Item getItem(FavoriteEntry favoriteEntry) {
        // Extract the episode details
        mPodcastId = favoriteEntry.getPodcastId();
        mPodcastTitle = favoriteEntry.getTitle();
        mPodcastImage = favoriteEntry.getArtworkImageUrl();

        String itemTitle = favoriteEntry.getItemTitle();
        String itemDescription = favoriteEntry.getItemDescription();
        String iTunesSummary = favoriteEntry.getItemDescription();
        String pubDate = favoriteEntry.getItemPubDate();
        String duration = favoriteEntry.getItemDuration();

        String enclosureUrl = favoriteEntry.getItemEnclosureUrl();
        String enclosureType = favoriteEntry.getItemEnclosureType();
        String enclosureLength = favoriteEntry.getItemEnclosureLength();
        Enclosure enclosure = new Enclosure(enclosureUrl, enclosureType, enclosureLength);
        List<Enclosure> enclosures = new ArrayList<>();
        enclosures.add(enclosure);

        String itemImageUrl = favoriteEntry.getItemImageUrl();
        ItemImage itemImage = new ItemImage(itemImageUrl);
        List<ItemImage> itemImages = new ArrayList<>();
        itemImages.add(itemImage);

        return new Item(itemTitle, itemDescription, iTunesSummary, pubDate, duration, enclosures, itemImages);
    }

    /**
     * When the favorite list is empty, shows an empty view.
     */
    private void showEmptyView() {
        // Show an empty view
        mFavoritesBinding.setHasFavorites(false);
        // Set text programmatically in order to make text invisible when the user changes the menu
        // items in the navigation drawer
        mFavoritesBinding.tvEmptyFavorites.setText(getString(R.string.empty_favorites));
        // Set the favorite icon above the text
        setDrawable();
    }

    /**
     * Sets the favorite icon above the text.
     * Reference: @see "https://stackoverflow.com/questions/4919703/how-to-set-property-androiddrawabletop-of-a-button-at-runtime"
     */
    private void setDrawable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // API level 17
            mFavoritesBinding.tvEmptyFavorites.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    NO_DRAWABLES, R.drawable.favorite_large, NO_DRAWABLES, NO_DRAWABLES);
        } else {
            mFavoritesBinding.tvEmptyFavorites.setCompoundDrawablesWithIntrinsicBounds(
                    NO_DRAWABLES, R.drawable.favorite_large, NO_DRAWABLES, NO_DRAWABLES);
        }
    }
}
