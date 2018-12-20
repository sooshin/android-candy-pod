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

package com.soojeongshin.candypod.ui.downloads;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.data.DownloadEntry;
import com.soojeongshin.candypod.databinding.DownloadsListItemBinding;
import com.soojeongshin.candypod.utilities.CandyPodUtils;

import java.util.List;

import static com.soojeongshin.candypod.utilities.Constants.IMG_HTML_TAG;
import static com.soojeongshin.candypod.utilities.Constants.REPLACEMENT_EMPTY;

/**
 * Exposes a list of downloaded episodes from a list of {@link DownloadEntry} to a {@link RecyclerView}
 */
public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.DownloadViewHolder> {

    private List<DownloadEntry> mDownloadEntries;

    private Context mContext;

    private final DownloadsAdapterOnClickHandler mOnClickHandler;

    public interface DownloadsAdapterOnClickHandler {
        void onItemClick(DownloadEntry downloadEntry, ImageView imageView);
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
            // Get the podcast title and set the text
            String podcastTitle = downloadEntry.getTitle();
            mDownloadsListItemBinding.tvPodcastTitle.setText(podcastTitle);

            // Get image URL, and load the image using Glide
            String podcastImage = downloadEntry.getArtworkImageUrl();
            String itemImageUrl = downloadEntry.getItemImageUrl();
            if (TextUtils.isEmpty(itemImageUrl)) {
                itemImageUrl = podcastImage;
            }
            Glide.with(mContext)
                    .load(itemImageUrl)
                    .into(mDownloadsListItemBinding.ivEpisode);

            // Get an episode title and set the text
            String itemTitle = downloadEntry.getItemTitle();
            mDownloadsListItemBinding.tvEpisodeTitle.setText(itemTitle);

            // Get the description of an episode and set the text
            String description = downloadEntry.getItemDescription();
            // If the description contains the img tag, remove it, then convert HTML to plain text.
            // Reference: @see "https://stackoverflow.com/questions/11178533/how-to-skip-image-tag-in-html-data-in-android"
            // @see "https://stackoverflow.com/questions/22573319/how-to-convert-html-text-to-plain-text-in-android"
            if (description != null) {
                String descriptionWithoutImageTag = description.replaceAll(IMG_HTML_TAG, REPLACEMENT_EMPTY);
                mDownloadsListItemBinding.tvDescription.setText(
                        Html.fromHtml(Html.fromHtml(descriptionWithoutImageTag).toString()));
            }

            // Get the pub date of an episode and set the text
            String pubDate = downloadEntry.getItemPubDate();
            // Convert the pub date into something to display to users
            String formattedPubDate = CandyPodUtils.getFormattedDateString(pubDate);
            mDownloadsListItemBinding.tvPubDate.setText(formattedPubDate);

            // Get the duration of an episode and set the text
            String duration = downloadEntry.getItemDuration();
            if (TextUtils.isEmpty(duration)) {
                // Hide the duration TextView when the duration is empty
                mDownloadsListItemBinding.tvDuration.setVisibility(View.GONE);
            } else {
                mDownloadsListItemBinding.tvDuration.setText(duration);
            }
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            DownloadEntry downloadEntry = mDownloadEntries.get(adapterPosition);
            // Trigger the callback onItemClick
            mOnClickHandler.onItemClick(downloadEntry, mDownloadsListItemBinding.ivEpisode);
        }
    }
}
