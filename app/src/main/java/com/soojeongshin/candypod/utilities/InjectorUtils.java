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

package com.soojeongshin.candypod.utilities;

import android.content.Context;

import com.soojeongshin.candypod.AppExecutors;
import com.soojeongshin.candypod.data.CandyPodDatabase;
import com.soojeongshin.candypod.data.CandyPodRepository;
import com.soojeongshin.candypod.ui.add.AddPodViewModelFactory;
import com.soojeongshin.candypod.ui.nowplaying.DownloadEntryViewModelFactory;
import com.soojeongshin.candypod.ui.detail.PodcastEntryViewModelFactory;
import com.soojeongshin.candypod.ui.downloads.DownloadsViewModelFactory;
import com.soojeongshin.candypod.ui.favorites.FavViewModelFactory;
import com.soojeongshin.candypod.ui.nowplaying.FavoriteEntryViewModelFactory;
import com.soojeongshin.candypod.ui.podcasts.PodcastsViewModelFactory;
import com.soojeongshin.candypod.ui.search.SearchViewModelFactory;
import com.soojeongshin.candypod.ui.subscribe.RssFeedViewModelFactory;
import com.soojeongshin.candypod.ui.subscribe.SubscribeViewModelFactory;

/**
 * Provides static methods to inject the various classes needed for CandyPod.
 */
public class InjectorUtils {

    public static CandyPodRepository provideRepository(Context context) {
        CandyPodDatabase database = CandyPodDatabase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        ITunesSearchApi iTunesSearchApi = RetrofitClient.getClient().create(ITunesSearchApi.class);
        return CandyPodRepository.getInstance(database.podcastDao(), iTunesSearchApi, executors);
    }

    public static AddPodViewModelFactory provideAddPodViewModelFactory(Context context, String country) {
        CandyPodRepository repository = provideRepository(context.getApplicationContext());
        return new AddPodViewModelFactory(repository, country);
    }

    public static SubscribeViewModelFactory provideSubscribeViewModelFactory(
            Context context, String lookupUrl, String id) {
        CandyPodRepository repository = provideRepository(context.getApplicationContext());
        return new SubscribeViewModelFactory(repository, lookupUrl, id);
    }

    public static RssFeedViewModelFactory provideRssViewModelFactory(Context context, String feedUrl) {
        CandyPodRepository repository = provideRepository(context.getApplicationContext());
        return new RssFeedViewModelFactory(repository, feedUrl);
    }

    public static PodcastEntryViewModelFactory providePodcastEntryViewModelFactory(
            Context context, String podcastId) {
        CandyPodRepository repository = provideRepository(context.getApplicationContext());
        return new PodcastEntryViewModelFactory(repository, podcastId);
    }

    public static PodcastsViewModelFactory providePodcastsViewModelFactory(Context context) {
        CandyPodRepository repository = provideRepository(context.getApplicationContext());
        return new PodcastsViewModelFactory(repository);
    }

    public static FavoriteEntryViewModelFactory provideFavoriteEntryViewModelFactory(Context context, String url) {
        CandyPodRepository repository = provideRepository(context.getApplicationContext());
        return new FavoriteEntryViewModelFactory(repository, url);
    }

    public static FavViewModelFactory provideFavViewModelFactory(Context context) {
        CandyPodRepository repository = provideRepository(context.getApplicationContext());
        return new FavViewModelFactory(repository);
    }

    public static DownloadsViewModelFactory provideDownloadsViewModelFactory(Context context) {
        CandyPodRepository repository = provideRepository(context.getApplicationContext());
        return new DownloadsViewModelFactory(repository);
    }

    public static DownloadEntryViewModelFactory provideDownloadEntryViewModelFactory(
            Context context, String enclosureUrl) {
        CandyPodRepository repository = provideRepository(context.getApplicationContext());
        return new DownloadEntryViewModelFactory(repository, enclosureUrl);
    }

    public static SearchViewModelFactory provideSearchViewModelFactory(
            Context context, String searchUrl, String country, String media, String term) {
        CandyPodRepository repository = provideRepository(context.getApplicationContext());
        return new SearchViewModelFactory(repository, searchUrl, country, media, term);
    }
}
