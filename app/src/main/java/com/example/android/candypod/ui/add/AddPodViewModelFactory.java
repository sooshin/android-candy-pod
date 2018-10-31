package com.example.android.candypod.ui.add;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.example.android.candypod.data.CandyPodRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link CandyPodRepository} and country.
 */
public class AddPodViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final CandyPodRepository mRepository;
    private final String mCountry;

    public AddPodViewModelFactory(CandyPodRepository repository, String country) {
        mRepository = repository;
        mCountry = country;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddPodViewModel(mRepository, mCountry);
    }
}
