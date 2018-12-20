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

package com.soojeongshin.candypod.ui.subscribe;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.soojeongshin.candypod.data.CandyPodRepository;
import com.soojeongshin.candypod.model.rss.RssFeed;

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
