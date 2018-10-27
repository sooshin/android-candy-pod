package com.example.android.candypod.ui.podcasts;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.candypod.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PodcastsFragment extends Fragment {


    public PodcastsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_podcasts, container, false);
    }

}
