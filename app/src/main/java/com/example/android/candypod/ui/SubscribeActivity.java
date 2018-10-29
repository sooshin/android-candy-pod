package com.example.android.candypod.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.android.candypod.R;
import com.example.android.candypod.model.LookupResponse;
import com.example.android.candypod.model.LookupResult;
import com.example.android.candypod.utilities.ITunesSearchApi;
import com.example.android.candypod.utilities.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_ID;
import static com.example.android.candypod.utilities.Constants.I_TUNES_LOOKUP;

public class SubscribeActivity extends AppCompatActivity {

    /** The podcast ID */
    private String mResultId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        // Get the podcast ID
        mResultId = getResultId();

        call();
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

    private void call() {
        Retrofit retrofit = RetrofitClient.getClient();
        ITunesSearchApi iTunesSearchApi = retrofit.create(ITunesSearchApi.class);

        Call<LookupResponse> call = iTunesSearchApi
                .getLookupResponse(I_TUNES_LOOKUP, mResultId);
        call.enqueue(new Callback<LookupResponse>() {
            @Override
            public void onResponse(Call<LookupResponse> call, Response<LookupResponse> response) {
                LookupResponse lookupResponse = response.body();
                List<LookupResult> lookupResults = lookupResponse.getLookupResults();
                LookupResult lookupResult = lookupResults.get(0);
                String feeUrl = lookupResult.getFeedUrl();
                Toast.makeText(SubscribeActivity.this, feeUrl, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<LookupResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
