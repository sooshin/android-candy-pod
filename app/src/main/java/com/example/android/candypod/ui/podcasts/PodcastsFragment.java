package com.example.android.candypod.ui.podcasts;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.candypod.R;
import com.example.android.candypod.data.PodcastEntry;
import com.example.android.candypod.databinding.FragmentPodcastsBinding;
import com.example.android.candypod.ui.GridAutofitLayoutManager;
import com.example.android.candypod.ui.add.AddPodcastActivity;
import com.example.android.candypod.utilities.InjectorUtils;

import java.util.List;

import static com.example.android.candypod.utilities.Constants.GRID_AUTO_FIT_COLUMN_WIDTH;

/**
 * A simple {@link Fragment} subclass.
 */
public class PodcastsFragment extends Fragment
        implements PodcastsAdapter.PodcastsAdapterOnClickHandler {

    /** This field is used for data binding */
    private FragmentPodcastsBinding mPodcastsBinding;

    /** Member variable for PodcastsAdapter */
    private PodcastsAdapter mPodcastsAdapter;

    /** PodcastsViewModel which stores and manages LiveData the list of PodcastEntries */
    private PodcastsViewModel mPodcastsViewModel;

    public PodcastsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mPodcastsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_podcasts,
                container, false);
        View rootView = mPodcastsBinding.getRoot();

        // When a FAB is clicked, start the AddPodcastActivity
        startAddPodcastActivity();

        // Create a GridAutofitLayoutManager and PodcastsAdapter, and set them to the RecyclerView
        initAdapter();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup PodcatsViewModel
        setupViewModel(this.getActivity());
    }

    /**
     * When the user clicks a FAB, start the AddPodcastActivity.
     */
    private void startAddPodcastActivity() {
        mPodcastsBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the Intent that will start the AddPodcastActivity
                Intent intent = new Intent(getContext(), AddPodcastActivity.class);
                // Once the Intent has been created, start the AddPodcastActivity
                startActivity(intent);
            }
        });
    }

    /**
     * Create a GridAutofitLayoutManager and PodcastsAdapter, and set them to the RecyclerView.
     */
    private void initAdapter() {
        // A GridAutofitLayoutManager is responsible for calculating the amount of GridView columns
        // based on screen size and positioning item views within a RecyclerView into a grid layout.
        // Reference: @see "https://codentrick.com/part-4-android-recyclerview-grid/"
        GridAutofitLayoutManager layoutManager = new GridAutofitLayoutManager(
                getContext(), GRID_AUTO_FIT_COLUMN_WIDTH);
        // Set the layout for the RecyclerView to be a grid layout
        mPodcastsBinding.rvPodcasts.setLayoutManager(layoutManager);
        // Use this setting to improve performance if you know that changes in content do not
        // change the child layout size in the RecyclerView
        mPodcastsBinding.rvPodcasts.setHasFixedSize(true);

        // Initialize the adapter and attach it to the RecyclerView
        mPodcastsAdapter = new PodcastsAdapter(getContext(), this);
        mPodcastsBinding.rvPodcasts.setAdapter(mPodcastsAdapter);
    }

    /**
     * Every time the podcast data is updated, update the UI.
     */
    private void setupViewModel(Context context) {
        // Get the ViewModel from the factory
        PodcastsViewModelFactory podcastsFactory = InjectorUtils.providePodcastsViewModelFactory(context);
        mPodcastsViewModel = ViewModelProviders.of(this, podcastsFactory)
                .get(PodcastsViewModel.class);

        // Observe the list of all {@link PodcastEntry}
        mPodcastsViewModel.getPodcasts().observe(this, new Observer<List<PodcastEntry>>() {
            @Override
            public void onChanged(@Nullable List<PodcastEntry> podcastEntries) {
                if (podcastEntries != null) {
                    // Update the list of PodcastEntries and notify the adapter of any changes
                    mPodcastsAdapter.setPodcastEntries(podcastEntries);
                }
            }
        });
    }

    @Override
    public void onPodcastClick(PodcastEntry podcastEntry) {

    }
}
