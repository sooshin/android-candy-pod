package com.example.android.candypod.ui.podcasts;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.candypod.R;
import com.example.android.candypod.databinding.FragmentPodcastsBinding;
import com.example.android.candypod.ui.AddPodcastActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class PodcastsFragment extends Fragment {

    /** This field is used for data binding */
    private FragmentPodcastsBinding mPodcastsBinding;

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

        return rootView;
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
}
