package com.example.android.candypod.ui.add;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.candypod.data.CandyPodRepository;
import com.example.android.candypod.model.ITunesResponse;

/**
 * {@link ViewModel} for AddPodcastActivity
 */
public class AddPodViewModel extends ViewModel {

    private final CandyPodRepository mRepository;
    private LiveData<ITunesResponse> mITunesResponse;

    public AddPodViewModel(CandyPodRepository repository, String country) {
        mRepository = repository;
        mITunesResponse = mRepository.getITunesResponse(country);
    }

    public LiveData<ITunesResponse> getITunesResponse() {
        return mITunesResponse;
    }

}
