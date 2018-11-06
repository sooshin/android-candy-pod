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

package com.example.android.candypod.ui.detail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.android.candypod.R;
import com.example.android.candypod.data.PodcastEntry;
import com.example.android.candypod.databinding.ActivityDetailBinding;
import com.example.android.candypod.ui.subscribe.PodcastEntryViewModel;
import com.example.android.candypod.ui.subscribe.PodcastEntryViewModelFactory;
import com.example.android.candypod.utilities.InjectorUtils;

import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_ID;

public class DetailActivity extends AppCompatActivity {

    /** This field is used for data binding */
    private ActivityDetailBinding mDetailBinding;

    /** The podcast ID */
    private String mResultId;

    /** Member variable for the PodcastEntryViewModel to store and manage LiveData PodcastEntry */
    private PodcastEntryViewModel mPodcastEntryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Get the podcast ID
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_RESULT_ID)) {
            mResultId = intent.getStringExtra(EXTRA_RESULT_ID);
        }

        // Setup the PodcastEntryViewModel
        setupViewModel();
    }

    /**
     * Observe the data and update the UI.
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
     * Show the details of the podcast.
     *
     * @param podcastEntry A single row from podcast table that has the data of the podcast
     */
    private void showPodcastEntryData(PodcastEntry podcastEntry) {
        // Get the data from the PodcastEntry
        String artworkImageUrl = podcastEntry.getArtworkImageUrl();
        String title = podcastEntry.getTitle();
        String author = podcastEntry.getAuthor();

        // Use Glide library to upload the artwork
        Glide.with(this)
                .load(artworkImageUrl)
                .into(mDetailBinding.ivArtwork);
        // Set text
        mDetailBinding.tvTitle.setText(title);
        mDetailBinding.tvAuthor.setText(author);
    }
}
