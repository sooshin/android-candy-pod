package com.example.android.candypod.ui.subscribe;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.example.android.candypod.data.CandyPodRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link CandyPodRepository} and String podcast ID.
 */
public class PodcastEntryViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final CandyPodRepository mRepository;
    private final String mPodcastId;

    public PodcastEntryViewModelFactory(CandyPodRepository repository, String podcastId) {
        mRepository = repository;
        mPodcastId = podcastId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // noinspection unchecked
        return (T) new PodcastEntryViewModel(mRepository, mPodcastId);
    }
}
