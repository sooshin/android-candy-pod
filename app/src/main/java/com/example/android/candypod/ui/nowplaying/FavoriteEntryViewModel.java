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

package com.example.android.candypod.ui.nowplaying;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.candypod.data.CandyPodRepository;
import com.example.android.candypod.data.FavoriteEntry;

/**
 * FavoriteEntryViewModel class is designed to store and manage {@link LiveData} FavoriteEntry.
 */
public class FavoriteEntryViewModel extends ViewModel {

    private final CandyPodRepository mRepository;
    private LiveData<FavoriteEntry> mFavoriteEntry;

    public FavoriteEntryViewModel(CandyPodRepository repository, String itemTitle) {
        mRepository = repository;
        mFavoriteEntry = mRepository.getFavoriteEpisodeByItemTitle(itemTitle);
    }

    public LiveData<FavoriteEntry> getFavoriteEntry() {
        return mFavoriteEntry;
    }
}
