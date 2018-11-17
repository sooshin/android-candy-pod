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

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.android.candypod.R;
import com.example.android.candypod.data.FavoriteEntry;
import com.example.android.candypod.databinding.FavoritesListItemBinding;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private List<FavoriteEntry> mFavoriteEntries;

    private Context mContext;

    private final FavoritesAdapterOnClickHandler mOnClickHandler;

    public interface FavoritesAdapterOnClickHandler {
        void onFavoriteClick(FavoriteEntry favoriteEntry);
    }

    public FavoritesAdapter(Context context, FavoritesAdapterOnClickHandler onClickHandler) {
        mContext = context;
        mOnClickHandler = onClickHandler;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        FavoritesListItemBinding favoritesListItemBinding = DataBindingUtil.inflate(
                layoutInflater, R.layout.favorites_list_item, viewGroup, false);
        return new FavoritesViewHolder(favoritesListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        FavoriteEntry favoriteEntry = mFavoriteEntries.get(position);
        holder.bind(favoriteEntry);
    }

    @Override
    public int getItemCount() {
        if (mFavoriteEntries == null) return 0;
        return mFavoriteEntries.size();
    }

    /**
     * When data changes, updates the list of favoriteEntries and notifies the adapter to use
     * the new values on it.
     */
    public void setFavoriteEntries(List<FavoriteEntry> favoriteEntries) {
        mFavoriteEntries = favoriteEntries;
        notifyDataSetChanged();
    }

    public class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FavoritesListItemBinding mFavListItemBinding;

        public FavoritesViewHolder(@NonNull FavoritesListItemBinding favoritesListItemBinding) {
            super(favoritesListItemBinding.getRoot());
            mFavListItemBinding = favoritesListItemBinding;

            // Set OnClickListener
            itemView.setOnClickListener(this);
        }

        void bind(FavoriteEntry favoriteEntry) {

            String podcastTitle = favoriteEntry.getTitle();
            String podcastImage = favoriteEntry.getArtworkImageUrl();
            String itemTitle = favoriteEntry.getItemTitle();

            String itemImageUrl = favoriteEntry.getItemImageUrl();

            if (TextUtils.isEmpty(itemImageUrl)) {
                itemImageUrl = podcastImage;
            }
            Glide.with(mContext)
                    .load(itemImageUrl)
                    .into(mFavListItemBinding.ivEpisode);

            mFavListItemBinding.tvEpisodeTitle.setText(itemTitle);
            mFavListItemBinding.tvPodcastTitle.setText(podcastTitle);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            FavoriteEntry favoriteEntry = mFavoriteEntries.get(adapterPosition);
            mOnClickHandler.onFavoriteClick(favoriteEntry);
        }
    }
}
