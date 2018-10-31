package com.example.android.candypod.ui.add;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.widget.Toast;

import com.example.android.candypod.R;
import com.example.android.candypod.databinding.ActivityAddPodcastBinding;
import com.example.android.candypod.model.Feed;
import com.example.android.candypod.model.ITunesResponse;
import com.example.android.candypod.model.Result;
import com.example.android.candypod.ui.subscribe.SubscribeActivity;
import com.example.android.candypod.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_ID;

public class AddPodcastActivity extends AppCompatActivity
        implements AddPodcastAdapter.AddPodcastAdapterOnClickHandler {

    /** This field is used for data binding **/
    private ActivityAddPodcastBinding mAddPodBinding;

    /** Member variable for the list of results which includes the information of Podcasts */
    private List<Result> mResults;

    /** Member variable for AddPodcastAdapter */
    private AddPodcastAdapter mAddPodAdapter;

    /** ViewModel for AddPodcastActivity */
    private AddPodViewModel mAddPodViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAddPodBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_podcast);

        // Create a GridLayoutManager and AddPodcastAdapter, and set them to the RecyclerView
        initAdapter();

        String country = "us";
        // Get the ViewModel from the factory
        setupViewModel(country);
        // Observe changes in the ITunesResponse
        observeITunesResponse();
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

    /**
     * Get the ViewModel from the factory.
     */
    private void setupViewModel(String country) {
        AddPodViewModelFactory factory = InjectorUtils.provideAddPodViewModelFactory(
                AddPodcastActivity.this, country);
        mAddPodViewModel = ViewModelProviders.of(this, factory).get(AddPodViewModel.class);
    }

    /**
     * Every time the ITunesResponse data is updated, the onChanged callback will be invoked and
     * update the UI.
     */
    private void observeITunesResponse() {
        mAddPodViewModel.getITunesResponse().observe(this, new Observer<ITunesResponse>() {
            @Override
            public void onChanged(@Nullable ITunesResponse iTunesResponse) {
                if (iTunesResponse != null) {
                    Feed feed = iTunesResponse.getFeed();
                    List<Result> results = feed.getResults();
                    mAddPodAdapter.addAll(results);
                }
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
        // Pass the podcast ID
        intent.putExtra(EXTRA_RESULT_ID, result.getId());
        // Once the Intent has been created, start the SubscribeActivity
        startActivity(intent);
        Toast.makeText(this, result.getId(), Toast.LENGTH_SHORT).show();
    }
}
