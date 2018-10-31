package com.example.android.candypod.ui.subscribe;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.example.android.candypod.data.CandyPodRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link CandyPodRepository}, lookupUrl, and id.
 */
public class SubscribeViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final CandyPodRepository mRepository;
    private final String mLookupUrl;
    private final String mId;

    public SubscribeViewModelFactory(CandyPodRepository repository, String lookupUrl, String id) {
        mRepository = repository;
        mLookupUrl = lookupUrl;
        mId = id;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new SubscribeViewModel(mRepository, mLookupUrl, mId);
    }
}
