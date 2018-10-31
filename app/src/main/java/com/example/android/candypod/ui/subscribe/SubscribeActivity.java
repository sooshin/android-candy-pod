package com.example.android.candypod.ui.subscribe;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.android.candypod.R;
import com.example.android.candypod.model.LookupResponse;
import com.example.android.candypod.model.LookupResult;
import com.example.android.candypod.model.rss.Category;
import com.example.android.candypod.model.rss.Channel;
import com.example.android.candypod.model.rss.Enclosure;
import com.example.android.candypod.model.rss.Item;
import com.example.android.candypod.model.rss.RssFeed;
import com.example.android.candypod.utilities.ITunesSearchApi;
import com.example.android.candypod.utilities.InjectorUtils;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import timber.log.Timber;

import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_ID;
import static com.example.android.candypod.utilities.Constants.I_TUNES_BASE_URL;
import static com.example.android.candypod.utilities.Constants.I_TUNES_LOOKUP;

public class SubscribeActivity extends AppCompatActivity {

    /** The podcast ID */
    private String mResultId;

    /** ViewModel for SubscribeActivity */
    private SubscribeViewModel mSubscribeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        // Get the podcast ID
        mResultId = getResultId();

       // Get the ViewModel from the factory
        setupViewModel();
        // Observe changes in the LookupResponse
        observeLookupResponse();
    }

    /**
     * Returns the podcast ID which is used to create a lookup request to search for content.
     */
    private String getResultId() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_RESULT_ID)) {
            mResultId = intent.getStringExtra(EXTRA_RESULT_ID);
        }
        return mResultId;
    }

    /**
     * Get the ViewModel from the factory.
     */
    private void setupViewModel() {
        SubscribeViewModelFactory factory = InjectorUtils.provideSubscribeViewModelFactory(
                SubscribeActivity.this, I_TUNES_LOOKUP, mResultId);
        mSubscribeViewModel = ViewModelProviders.of(this, factory).get(SubscribeViewModel.class);
    }

    /**
     * Every time the LookupResponse data is updated, the onChanged callback will be invoked and
     * update the UI.
     */
    private void observeLookupResponse() {
        mSubscribeViewModel.getLookupResponse().observe(this, new Observer<LookupResponse>() {
            @Override
            public void onChanged(@Nullable LookupResponse lookupResponse) {
                if (lookupResponse != null) {
                    List<LookupResult> lookupResults = lookupResponse.getLookupResults();
                    String feedUrl = lookupResults.get(0).getFeedUrl();
                    Timber.e("feedUrl: " + feedUrl);

                    parseXml(feedUrl);
                }
            }
        });
    }

    private void parseXml(String feedUrl) {
        Retrofit sRetrofit = new Retrofit.Builder()
                // Set the API base URL
                .baseUrl(I_TUNES_BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.createNonStrict(new Persister(new AnnotationStrategy())))
                .build();

        ITunesSearchApi iTunesSearchApi = sRetrofit.create(ITunesSearchApi.class);
        Call<RssFeed> call = iTunesSearchApi.getRssFeed(feedUrl);
        call.enqueue(new Callback<RssFeed>() {
            @Override
            public void onResponse(Call<RssFeed> call, Response<RssFeed> response) {
                RssFeed rssFeed = response.body();
                if (rssFeed != null) {
                    Channel channel = rssFeed.getChannel();
                    String title = channel.getTitle();
                    Timber.e("title: " + title);
                    String description = channel.getDescription();
                    Timber.e("description: " + description);
                    String author = channel.getITunesAuthor();
                    Timber.e("author: " + author);
                    String language = channel.getLanguage();
                    Timber.e("language: " + language);
                    List<Category> categories = channel.getCategories();
                    String categoryText = categories.get(0).getText();
                    Timber.e("categoryText: " + categoryText);
                    List<Item> itemList = channel.getItemList();
                    Enclosure enclosure = itemList.get(0).getEnclosure();
                    String type = enclosure.getType();
                    Timber.e("type: " + type);
                    String enclosureUrl = enclosure.getUrl();
                    Timber.e("enclosure: " + enclosureUrl);
                }
            }

            @Override
            public void onFailure(Call<RssFeed> call, Throwable t) {
                Timber.e("Failed:" + t.getMessage());
            }
        });
    }
}
