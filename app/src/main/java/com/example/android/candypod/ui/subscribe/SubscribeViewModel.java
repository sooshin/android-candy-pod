package com.example.android.candypod.ui.subscribe;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.candypod.data.CandyPodRepository;
import com.example.android.candypod.model.LookupResponse;

/**
 * {@link ViewModel} for SubscribeActivity
 */
public class SubscribeViewModel extends ViewModel {

    private final CandyPodRepository mRepository;
    private LiveData<LookupResponse> mLookupResponse;

    public SubscribeViewModel(CandyPodRepository repository, String lookupUrl, String id) {
        mRepository = repository;
        mLookupResponse = mRepository.getLookupResponse(lookupUrl, id);
    }

    public LiveData<LookupResponse> getLookupResponse() {
        return mLookupResponse;
    }
}
