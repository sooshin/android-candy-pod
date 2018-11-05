package com.example.android.candypod.ui.subscribe;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.candypod.data.CandyPodRepository;
import com.example.android.candypod.data.PodcastEntry;

/**
 * PodcastEntryViewModel class is designed to store and manage {@link LiveData} PodcastEntry.
 */
public class PodcastEntryViewModel extends ViewModel {

    private final CandyPodRepository mRepository;
    private LiveData<PodcastEntry> mPodcastEntry;

    public PodcastEntryViewModel(CandyPodRepository repository, String podcastId) {
        mRepository = repository;
        mPodcastEntry = mRepository.getPodcastByPodcastId(podcastId);
    }

    public LiveData<PodcastEntry> getPodcastEntry() {
        return mPodcastEntry;
    }
}
