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

package com.example.android.candypod.ui.downloads;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.candypod.R;
import com.example.android.candypod.data.DownloadEntry;
import com.example.android.candypod.databinding.FragmentDownloadsBinding;
import com.example.android.candypod.model.rss.Enclosure;
import com.example.android.candypod.model.rss.Item;
import com.example.android.candypod.model.rss.ItemImage;
import com.example.android.candypod.service.PodcastService;
import com.example.android.candypod.ui.nowplaying.NowPlayingActivity;
import com.example.android.candypod.utilities.CandyPodUtils;
import com.example.android.candypod.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.candypod.utilities.Constants.ACTION_RELEASE_OLD_PLAYER;
import static com.example.android.candypod.utilities.Constants.EXTRA_ITEM;
import static com.example.android.candypod.utilities.Constants.EXTRA_PODCAST_IMAGE;
import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_ID;
import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_NAME;

/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadsFragment extends Fragment implements DownloadsAdapter.DownloadsAdapterOnClickHandler {

    /** This field is used for data binding */
    private FragmentDownloadsBinding mDownloadsBinding;

    /** Member variable for DownloadsAdapter */
    private DownloadsAdapter mDownloadsAdapter;

    /** Member variable for DownloadsViewModel */
    private DownloadsViewModel mDownloadsViewModel;

    /** Podcast Id, title, and image URL */
    private String mPodcastId;
    private String mPodcastTitle;
    private String mPodcastImage;

    public DownloadsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the data binding layout for this fragment
        mDownloadsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_downloads,
                container, false);
        View rootView = mDownloadsBinding.getRoot();

        // Change the title associated with this fragment
        getActivity().setTitle(getString(R.string.downloads));

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
     * Create a LinearLayoutManager and DownloadsAdapter, and set them to the RecyclerView.
     */
    private void initAdapter() {
        // A LinearLayoutManager is responsible for measuring and positioning item views within a
        // RecyclerView into a linear list.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        // Set the layout for the RecyclerView to be a linear layout
        mDownloadsBinding.rvDownloads.setLayoutManager(layoutManager);
        // Use this setting to improve performance if you know that changes in content do not
        // change the child layout size in the RecyclerView
        mDownloadsBinding.rvDownloads.setHasFixedSize(true);

        // Initialize the adapter and attach it to the RecyclerView
        mDownloadsAdapter = new DownloadsAdapter(getContext(), this);
        mDownloadsBinding.rvDownloads.setAdapter(mDownloadsAdapter);
    }

    /**
     * Every time the downloaded episode data is updated, update the UI.
     */
    private void setupViewModel(Context context) {
        // Get the ViewModel from the factory
        DownloadsViewModelFactory downloadsFactory =
                InjectorUtils.provideDownloadsViewModelFactory(context);
        mDownloadsViewModel = ViewModelProviders.of(this, downloadsFactory)
                .get(DownloadsViewModel.class);

        // Observe changes in the list of DownloadEntries
        mDownloadsViewModel.getDownloads().observe(this, new Observer<List<DownloadEntry>>() {
            @Override
            public void onChanged(@Nullable List<DownloadEntry> downloadEntries) {
                if (downloadEntries != null && downloadEntries.size() != 0) {
                    // Make the view for downloads visible
                    mDownloadsBinding.setHasDownloads(true);

                    mDownloadsAdapter.setDownloadEntries(downloadEntries);
                } else {
                    // When the download list is empty, show an empty view.
                    showEmptyView();
                }
            }
        });
    }

    @Override
    public void onItemClick(DownloadEntry downloadEntry) {
        Item item = getItem(downloadEntry);
        // Update the episode data using SharedPreferences each time the user selects the episode
        CandyPodUtils.updateSharedPreference(this.getContext(), item,
                mPodcastTitle, CandyPodUtils.getItemImageUrl(item, mPodcastImage));
        // Send an update broadcast message to the app widget
        CandyPodUtils.sendBroadcastToWidget(this.getContext());

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
        startActivity(intent);


        Intent serviceIntent = new Intent(this.getActivity(), PodcastService.class);
        // Set the action to check if the old player should be released in PodcastService
        serviceIntent.setAction(ACTION_RELEASE_OLD_PLAYER);
        serviceIntent.putExtra(EXTRA_ITEM, b);
        // Pass podcast title and podcast image
        serviceIntent.putExtra(EXTRA_RESULT_NAME, mPodcastTitle);
        serviceIntent.putExtra(EXTRA_PODCAST_IMAGE, mPodcastImage);
        getActivity().startService(serviceIntent);
    }

    /**
     * Returns item used to intent extra.
     * @param downloadEntry DownloadEntry that the user clicks
     */
    private Item getItem(DownloadEntry downloadEntry) {
        mPodcastId = downloadEntry.getPodcastId();
        mPodcastTitle = downloadEntry.getTitle();
        mPodcastImage = downloadEntry.getArtworkImageUrl();

        String itemTitle = downloadEntry.getItemTitle();
        String itemDescription = downloadEntry.getItemDescription();
        String iTunesSummary = downloadEntry.getItemDescription();
        String pubDate = downloadEntry.getItemPubDate();
        String duration = downloadEntry.getItemDuration();

        String enclosureUrl = downloadEntry.getItemEnclosureUrl();
        String enclosureType = downloadEntry.getItemEnclosureType();
        String enclosureLength = downloadEntry.getItemEnclosureLength();
        Enclosure enclosure = new Enclosure(enclosureUrl, enclosureType, enclosureLength);
        List<Enclosure> enclosures = new ArrayList<>();
        enclosures.add(enclosure);

        String itemImageUrl = downloadEntry.getItemImageUrl();
        ItemImage itemImage = new ItemImage(itemImageUrl);
        List<ItemImage> itemImages = new ArrayList<>();
        itemImages.add(itemImage);

        return new Item(itemTitle, itemDescription, iTunesSummary, pubDate, duration, enclosures, itemImages);
    }

    /**
     * When the favorite list is empty, show an empty view.
     */
    private void showEmptyView() {
        // Show an empty view
        mDownloadsBinding.setHasDownloads(false);
        // Set text programmatically in order to make text invisible when the user changes the menu
        // items in the navigation drawer
        mDownloadsBinding.tvEmptyDownloads.setText(getString(R.string.empty_downloads));
    }
}
