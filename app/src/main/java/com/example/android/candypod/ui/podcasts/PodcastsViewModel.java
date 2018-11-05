package com.example.android.candypod.ui.podcasts;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.candypod.data.CandyPodRepository;
import com.example.android.candypod.data.PodcastEntry;

import java.util.List;

/**
 * PodcastsViewModel class is designed to store and manage {@link LiveData} the list of PodcastEntries.
 */
public class PodcastsViewModel extends ViewModel {

    private final CandyPodRepository mRepository;
    private final LiveData<List<PodcastEntry>> mPodcastEntries;

    public PodcastsViewModel(CandyPodRepository repository) {
        mRepository = repository;
        mPodcastEntries = mRepository.getPodcasts();
    }

    public LiveData<List<PodcastEntry>> getPodcasts() {
        return mPodcastEntries;
    }
}
