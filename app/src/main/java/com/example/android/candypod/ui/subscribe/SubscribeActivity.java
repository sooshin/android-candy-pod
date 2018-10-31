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
import com.example.android.candypod.utilities.InjectorUtils;

import java.util.List;

import timber.log.Timber;

import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_ID;
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
                }
            }
        });
    }
}
