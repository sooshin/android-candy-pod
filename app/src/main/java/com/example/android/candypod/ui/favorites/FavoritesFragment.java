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

package com.example.android.candypod.ui.favorites;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.candypod.R;
import com.example.android.candypod.data.FavoriteEntry;
import com.example.android.candypod.databinding.FragmentFavoritesBinding;
import com.example.android.candypod.utilities.InjectorUtils;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesFragment extends Fragment implements FavoritesAdapter.FavoritesAdapterOnClickHandler {

    /** This field is used for data binding */
    private FragmentFavoritesBinding mFavoritesBinding;

    /** Member variable for FavoritesAdapter */
    private FavoritesAdapter mFavoritesAdapter;

    /***The ViewModel for FavoritesFragment */
    private FavViewModel mFavViewModel;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the data binding layout for this fragment
        mFavoritesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorites,
                container, false);
        View rootView = mFavoritesBinding.getRoot();

        // Create and set the adapter to the RecyclerView
        initAdapter();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup ViewModel
        setupViewModel(this.getActivity());
    }

    /**
     * Create a LinearLayoutManager and FavoritesAdapter, and set them to the RecyclerView.
     */
    private void initAdapter() {
        // A LinearLayoutManager is responsible for measuring and positioning item views within a
        // RecyclerView into a linear list.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        // Set the layout for the RecyclerView to be a linear layout
        mFavoritesBinding.rvFavorites.setLayoutManager(layoutManager);
        // Use this setting to improve performance if you know that changes in content do not
        // change the child layout size in the RecyclerView
        mFavoritesBinding.rvFavorites.setHasFixedSize(true);

        // Initialize the adapter and attach it to the RecyclerView
        mFavoritesAdapter = new FavoritesAdapter(getContext(), this);
        mFavoritesBinding.rvFavorites.setAdapter(mFavoritesAdapter);
    }

    /**
     * Every time the favorite episode data is updated, update the UI.
     * @param context
     */
    private void setupViewModel(Context context) {
        // Get the ViewModel from the factory
        FavViewModelFactory favFactory = InjectorUtils.provideFavViewModelFactory(context);
        mFavViewModel = ViewModelProviders.of(this, favFactory).get(FavViewModel.class);

        // Observe FavoriteEntry data
        mFavViewModel.getFavorites().observe(this, new Observer<List<FavoriteEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavoriteEntry> favoriteEntries) {
                if (favoriteEntries != null && favoriteEntries.size() != 0) {
                    showFavoritesView();
                    mFavoritesAdapter.setFavoriteEntries(favoriteEntries);
                } else {
                    showEmptyView();
                }
            }
        });
    }

    /**
     *
     * @param favoriteEntry
     */
    @Override
    public void onFavoriteClick(FavoriteEntry favoriteEntry) {

    }

    /**
     * This method will make the view for favorites visible.
     */
    private void showFavoritesView() {
        // First, hide an empty view
        mFavoritesBinding.tvEmptyFavorites.setVisibility(View.GONE);
        // Then, make sure the favorites list data is visible
        mFavoritesBinding.rvFavorites.setVisibility(View.VISIBLE);
    }

    /**
     * When the favorite list is empty, show an empty view.
     */
    private void showEmptyView() {
        // First, hide the view for the favorites
        mFavoritesBinding.rvFavorites.setVisibility(View.GONE);
        // Then, show an empty view
        mFavoritesBinding.tvEmptyFavorites.setVisibility(View.VISIBLE);
    }
}
