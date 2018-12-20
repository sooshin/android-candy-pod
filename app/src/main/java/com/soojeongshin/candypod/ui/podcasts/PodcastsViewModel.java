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

package com.soojeongshin.candypod.ui.podcasts;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.soojeongshin.candypod.data.CandyPodRepository;
import com.soojeongshin.candypod.data.PodcastEntry;

import java.util.List;

/**
 * PodcastsViewModel class is designed to store and manage {@link LiveData} the list of PodcastEntries.
 */
public class PodcastsViewModel extends ViewModel {

    private final CandyPodRepository mRepository;
    private final LiveData<List<PodcastEntry>> mPodcastEntries;

    public PodcastsViewModel(CandyPodRepository repository) {
        mRepository = repository;
        mPodcastEntries = mRepository.getPodcasts();
    }

    public LiveData<List<PodcastEntry>> getPodcasts() {
        return mPodcastEntries;
    }
}
