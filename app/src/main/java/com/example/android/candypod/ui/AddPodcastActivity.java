package com.example.android.candypod.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.widget.Toast;

import com.example.android.candypod.R;
import com.example.android.candypod.databinding.ActivityAddPodcastBinding;
import com.example.android.candypod.model.Feed;
import com.example.android.candypod.model.ITunesResponse;
import com.example.android.candypod.model.Result;
import com.example.android.candypod.utilities.ITunesSearchApi;
import com.example.android.candypod.utilities.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddPodcastActivity extends AppCompatActivity
        implements AddPodcastAdapter.AddPodcastAdapterOnClickHandler {

    /** This field is used for data binding **/
    private ActivityAddPodcastBinding mAddPodBinding;

    /** Member variable for the list of results which includes the information of Podcasts */
    private List<Result> mResults;

    /** Member variable for AddPodcastAdapter */
    private AddPodcastAdapter mAddPodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAddPodBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_podcast);

        // Create a GridLayoutManager and AddPodcastAdapter, and set them to the RecyclerView
        initAdapter();

        call();
    }

    /**
     * Create a GridLayoutManager and AddPodcastAdapter, and set them to the RecyclerView.
     */
    private void initAdapter() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        // Set the layout manager to the RecyclerView
        mAddPodBinding.rvAddPod.setLayoutManager(layoutManager);
        // Use this setting to improve performance if you know that changes in content do not
        // change the child layout size in the RecyclerView
        mAddPodBinding.rvAddPod.setHasFixedSize(true);

        // Create an empty ArrayList
        mResults = new ArrayList<>();
        // AddPodcastAdapter is responsible for displaying each result in the list.
        mAddPodAdapter = new AddPodcastAdapter(mResults, this);
        // Set adapter to the RecyclerView
        mAddPodBinding.rvAddPod.setAdapter(mAddPodAdapter);
    }

    private void call() {
        RetrofitClient retrofitClient = new RetrofitClient();
        Retrofit retrofit = retrofitClient.getClient();
        ITunesSearchApi iTunesSearchApi = retrofit.create(ITunesSearchApi.class);

        Call<ITunesResponse> call = iTunesSearchApi.getTopPodcasts("us");
        call.enqueue(new Callback<ITunesResponse>() {
            @Override
            public void onResponse(Call<ITunesResponse> call, Response<ITunesResponse> response) {
                ITunesResponse iTunesResponse = response.body();
                Feed feed = iTunesResponse.getFeed();
                List<Result> results = feed.getResults();
                mAddPodAdapter.addAll(results);
            }

            @Override
            public void onFailure(Call<ITunesResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * This is where we receive our callback from
     * {@link AddPodcastAdapter.AddPodcastAdapterOnClickHandler}
     *
     * This callback is invoked when you click on an item in the list.
     *
     * @param result Result object
     */
    @Override
    public void onItemClick(Result result) {
        // Create the Intent that will start the SubscribeActivity
        Intent intent = new Intent(this, SubscribeActivity.class);
        // Once the Intent has been created, start the SubscribeActivity
        startActivity(intent);
        Toast.makeText(this, result.getName(), Toast.LENGTH_SHORT).show();
    }
}
