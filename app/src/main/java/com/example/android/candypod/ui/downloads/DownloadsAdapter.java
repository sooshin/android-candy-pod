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

package com.example.android.candypod.ui.downloads;

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
import com.example.android.candypod.data.DownloadEntry;
import com.example.android.candypod.databinding.DownloadsListItemBinding;

import java.util.List;

/**
 * Exposes a list of downloaded episodes from a list of {@link DownloadEntry} to a {@link RecyclerView}
 */
public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.DownloadViewHolder> {

    private List<DownloadEntry> mDownloadEntries;

    private Context mContext;

    private final DownloadsAdapterOnClickHandler mOnClickHandler;

    public interface DownloadsAdapterOnClickHandler {
        void onItemClick(DownloadEntry downloadEntry);
    }

    public DownloadsAdapter(Context context, DownloadsAdapterOnClickHandler onClickHandler) {
        mContext = context;
        mOnClickHandler = onClickHandler;
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        DownloadsListItemBinding downloadsListItemBinding = DataBindingUtil.inflate(
                layoutInflater, R.layout.downloads_list_item, viewGroup,false);
        return new DownloadViewHolder(downloadsListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder downloadViewHolder, int position) {
        DownloadEntry downloadEntry = mDownloadEntries.get(position);
        downloadViewHolder.bind(downloadEntry);
    }

    @Override
    public int getItemCount() {
        if (mDownloadEntries == null) return 0;
        return mDownloadEntries.size();
    }

    public void setDownloadEntries(List<DownloadEntry> downloadEntries) {
        mDownloadEntries = downloadEntries;
        notifyDataSetChanged();
    }

    public class DownloadViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private DownloadsListItemBinding mDownloadsListItemBinding;

        public DownloadViewHolder(@NonNull DownloadsListItemBinding downloadsListItemBinding) {
            super(downloadsListItemBinding.getRoot());
            mDownloadsListItemBinding = downloadsListItemBinding;

            itemView.setOnClickListener(this);
        }

        void bind(DownloadEntry downloadEntry) {
            String podcastTitle = downloadEntry.getTitle();
            String podcastImage = downloadEntry.getArtworkImageUrl();
            String itemTitle = downloadEntry.getItemTitle();

            String itemImageUrl = downloadEntry.getItemImageUrl();
            if (TextUtils.isEmpty(itemImageUrl)) {
                itemImageUrl = podcastImage;
            }
            Glide.with(mContext)
                    .load(itemImageUrl)
                    .into(mDownloadsListItemBinding.ivEpisode);

            mDownloadsListItemBinding.tvEpisodeTitle.setText(itemTitle);
            mDownloadsListItemBinding.tvPodcastTitle.setText(podcastTitle);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            DownloadEntry downloadEntry = mDownloadEntries.get(adapterPosition);
            mOnClickHandler.onItemClick(downloadEntry);
        }
    }
}
