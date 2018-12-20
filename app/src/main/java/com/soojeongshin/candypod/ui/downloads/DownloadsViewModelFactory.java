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

package com.soojeongshin.candypod.ui.downloads;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.soojeongshin.candypod.data.CandyPodRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link CandyPodRepository}
 */
public class DownloadsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final CandyPodRepository mRepository;

    public DownloadsViewModelFactory(CandyPodRepository repository) {
        mRepository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // noinspection unchecked
        return (T) new DownloadsViewModel(mRepository);
    }
}
