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

package com.soojeongshin.candypod.ui.search;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.databinding.SearchListItemBinding;
import com.soojeongshin.candypod.model.SearchResult;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<SearchResult> mSearchResults;

    private final SearchAdapterOnClickHandler mOnClickHandler;

    public interface SearchAdapterOnClickHandler {
        void onItemClick(SearchResult searchResult, ImageView imageView);
    }

    public SearchAdapter(List<SearchResult> searchResults, SearchAdapterOnClickHandler onClickHandler) {
        mSearchResults = searchResults;
        mOnClickHandler = onClickHandler;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        SearchListItemBinding searchListItemBinding = DataBindingUtil
                .inflate(layoutInflater, R.layout.search_list_item, viewGroup, false);
        return new SearchViewHolder(searchListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder searchViewHolder, int position) {
        SearchResult searchResult = mSearchResults.get(position);
        searchViewHolder.bind(searchResult);
    }

    @Override
    public int getItemCount() {
        if (mSearchResults == null) return 0;
        return mSearchResults.size();
    }

    /**
     * This method is to update a list of {@link SearchResult}s and notify the adapter of any changes.
     * @param searchResults The list of searchResults
     */
    public void addAll(List<SearchResult> searchResults) {
        mSearchResults.clear();
        mSearchResults.addAll(searchResults);
        notifyDataSetChanged();
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private SearchListItemBinding mSearchListItemBinding;

        public SearchViewHolder(@NonNull SearchListItemBinding searchListItemBinding) {
            super(searchListItemBinding.getRoot());
            mSearchListItemBinding = searchListItemBinding;

            itemView.setOnClickListener(this);
        }

        void bind(SearchResult searchResult) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.candy_error);

            Glide.with(itemView.getContext())
                    .load(searchResult.getArtworkUrl600())
                    .apply(options)
                    .into(mSearchListItemBinding.ivArtwork);

            mSearchListItemBinding.tvName.setText(searchResult.getCollectionName());
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            SearchResult searchResult = mSearchResults.get(adapterPosition);
            mOnClickHandler.onItemClick(searchResult, mSearchListItemBinding.ivArtwork);
        }
    }
}
