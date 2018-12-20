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

package com.soojeongshin.candypod.ui.search;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.soojeongshin.candypod.data.CandyPodRepository;

public class SearchViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final CandyPodRepository mRepository;
    private final String mSearchUrl;
    private final String mCountry;
    private final String mMedia;
    private final String mTerm;

    public SearchViewModelFactory(CandyPodRepository repository, String searchUrl,
                                  String country, String media, String term) {
        mRepository = repository;
        mSearchUrl = searchUrl;
        mCountry = country;
        mMedia = media;
        mTerm = term;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // noinspection unchecked
        return (T) new SearchViewModel(mRepository, mSearchUrl, mCountry, mMedia, mTerm);
    }
}
