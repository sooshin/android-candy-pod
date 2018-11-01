package com.example.android.candypod.ui.subscribe;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.candypod.data.CandyPodRepository;
import com.example.android.candypod.model.rss.RssFeed;

/**
 * RssFeedViewModel class is designed to store and manage {@link LiveData} RssFeed
 */
public class RssFeedViewModel extends ViewModel {

    private final LiveData<RssFeed> mRssFeed;

    private final CandyPodRepository mRepository;

    public RssFeedViewModel(CandyPodRepository repository, String feedUrl) {
        mRepository = repository;
        mRssFeed = mRepository.getRssFeed(feedUrl);
    }

    public LiveData<RssFeed> getRssFeed() {
        return mRssFeed;
    }
}
