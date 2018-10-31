package com.example.android.candypod.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.example.android.candypod.AppExecutors;
import com.example.android.candypod.model.ITunesResponse;
import com.example.android.candypod.model.LookupResponse;
import com.example.android.candypod.utilities.ITunesSearchApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Handles data operations in CandyPod. Acts as a mediator between {@link ITunesSearchApi} and
 * {@link PodcastDao}.
 */
public class CandyPodRepository {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static CandyPodRepository sInstance;
    private final PodcastDao mPodcastDao;
    private final ITunesSearchApi mITunesSearchApi;
    private final AppExecutors mExecutors;

    private CandyPodRepository(PodcastDao podcastDao,
                               ITunesSearchApi iTunesSearchApi,
                               AppExecutors executors) {
        mPodcastDao = podcastDao;
        mITunesSearchApi = iTunesSearchApi;
        mExecutors = executors;
    }

    public synchronized static CandyPodRepository getInstance(
           PodcastDao podcastDao, ITunesSearchApi iTunesSearchApi, AppExecutors executors) {
        Timber.d("Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                Timber.d("Making new repository");
                sInstance = new CandyPodRepository(podcastDao, iTunesSearchApi, executors);
            }
        }
        return sInstance;
    }

    /**
     * Make a network request by calling enqueue
     * @param country The two-letter country code
     * @return {@link LiveData} ITunesResponse object
     */
    public LiveData<ITunesResponse> getITunesResponse(String country) {
        final MutableLiveData<ITunesResponse> iTunesResponseData = new MutableLiveData<>();

        mITunesSearchApi.getTopPodcasts(country)
                .enqueue(new Callback<ITunesResponse>() {
                    @Override
                    public void onResponse(Call<ITunesResponse> call, Response<ITunesResponse> response) {
                        if (response.isSuccessful()) {
                            ITunesResponse iTunesResponse = response.body();
                            iTunesResponseData.setValue(iTunesResponse);
                        }
                    }

                    @Override
                    public void onFailure(Call<ITunesResponse> call, Throwable t) {
                        iTunesResponseData.setValue(null);
                        Timber.e("Failed getting iTunesResponse data. " + t.getMessage());
                    }
                });
        return iTunesResponseData;
    }

    /**
     * Make a network request by calling enqueue
     * @param lookupUrl URL for a lookup request
     * @param id The podcast ID
     * @return {@link LiveData} LookupResponse object
     */
    public LiveData<LookupResponse> getLookupResponse(String lookupUrl, String id) {
        final MutableLiveData<LookupResponse> lookupResponseData = new MutableLiveData<>();

        mITunesSearchApi.getLookupResponse(lookupUrl, id)
                .enqueue(new Callback<LookupResponse>() {
                    @Override
                    public void onResponse(Call<LookupResponse> call, Response<LookupResponse> response) {
                        if (response.isSuccessful()) {
                            LookupResponse lookupResponse = response.body();
                            lookupResponseData.setValue(lookupResponse);
                        }
                    }

                    @Override
                    public void onFailure(Call<LookupResponse> call, Throwable t) {
                        lookupResponseData.setValue(null);
                        Timber.e("Failed getting LookupResponse data. " + t.getMessage());
                    }
                });
        return lookupResponseData;
    }
}
