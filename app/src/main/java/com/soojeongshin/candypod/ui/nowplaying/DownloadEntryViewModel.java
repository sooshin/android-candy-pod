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

package com.soojeongshin.candypod.ui.nowplaying;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.soojeongshin.candypod.data.CandyPodRepository;
import com.soojeongshin.candypod.data.DownloadEntry;

/**
 * DownloadEntryViewModel class is designed to store and manage {@link LiveData} DownloadEntry.
 */
public class DownloadEntryViewModel extends ViewModel {

    private final CandyPodRepository mRepository;
    private LiveData<DownloadEntry> mDownloadEntry;

    public DownloadEntryViewModel(CandyPodRepository repository, String enclosureUrl) {
        mRepository = repository;
        mDownloadEntry = mRepository.getDownloadedEpisodeByEnclosureUrl(enclosureUrl);
    }

    public LiveData<DownloadEntry> getDownloadEntry() {
        return mDownloadEntry;
    }
}
