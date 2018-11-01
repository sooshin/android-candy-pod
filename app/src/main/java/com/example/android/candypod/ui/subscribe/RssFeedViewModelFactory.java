package com.example.android.candypod.ui.subscribe;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.example.android.candypod.data.CandyPodRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link CandyPodRepository} and String feedUrl.
 */
public class RssFeedViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final CandyPodRepository mRepository;
    private final String mFeedUrl;

    public RssFeedViewModelFactory(CandyPodRepository repository, String feedUrl) {
        mRepository = repository;
        mFeedUrl = feedUrl;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new RssFeedViewModel(mRepository, mFeedUrl);
    }
}
