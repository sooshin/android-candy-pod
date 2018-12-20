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

package com.soojeongshin.candypod.ui.detail;

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
import com.soojeongshin.candypod.databinding.DetailListItemBinding;
import com.soojeongshin.candypod.model.rss.Item;
import com.soojeongshin.candypod.utilities.CandyPodUtils;

import java.util.List;

import static com.soojeongshin.candypod.utilities.Constants.IMG_HTML_TAG;
import static com.soojeongshin.candypod.utilities.Constants.REPLACEMENT_EMPTY;

/**
 * Exposes a list of episodes from a list of {@link Item} to a {@link RecyclerView}.
 */
public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.DetailViewHolder> {

    /** Member variable for the list of {@link Item}s which is the episodes in the podcast */
    private List<Item> mItems;

    /** The podcast image URL used when there is no episode image */
    private String mPodcastImage;

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final DetailAdapterOnClickHandler mOnClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface DetailAdapterOnClickHandler {
        void onItemClick(Item item, ImageView imageView);
    }

    /**
     * Creates a DetailAdapter.
     *
     * @param items The list of items to display
     * @param onClickHandler The on-click handler for this adapter. This single handler is called
     *                      when an item is clicked.
     * @param podcastImage The podcast image URL used when there is no episode image.
     */
    public DetailAdapter(List<Item> items, DetailAdapterOnClickHandler onClickHandler, String podcastImage) {
        mItems = items;
        mOnClickHandler = onClickHandler;
        mPodcastImage = podcastImage;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     */
    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        DetailListItemBinding detailListItemBinding = DataBindingUtil
                .inflate(layoutInflater, R.layout.detail_list_item, viewGroup, false);
        return new DetailViewHolder(detailListItemBinding);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified position.
     */
    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        Item item = mItems.get(position);
        holder.bind(item);
    }

    /**
     * This method simply return the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in the podcast
     */
    @Override
    public int getItemCount() {
        if (null == mItems) return 0;
        return mItems.size();
    }

    /**
     * This method is to update a list of {@Link Item}s and notify the adapter of any changes.
     *
     * @param items The list of items to display
     */
    public void addAll(List<Item> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    /**
     * Caches of the children views for a list item.
     */
    public class DetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        /** This field is used for data binding */
        private DetailListItemBinding mDetailListItemBinding;

        /**
         * Constructor for the DetailViewHolder.
         * @param detailListItemBinding Used to access the layout's variables and views
         */
        public DetailViewHolder(@NonNull DetailListItemBinding detailListItemBinding) {
            super(detailListItemBinding.getRoot());
            mDetailListItemBinding = detailListItemBinding;

            // Set an onClickListener on the itemView to listen for clicks
            itemView.setOnClickListener(this);
        }

        /**
         * This method will take an Item object as input and use that item to display the appropriate
         * text within a list item.
         *
         * @param item An episode of the podcast
         */
        void bind(Item item) {
            // If an episode image exists, use it. Otherwise, use the podcast image.
            String itemImageUrl = CandyPodUtils.getItemImageUrl(item, mPodcastImage);
            // Use Glide library to upload the episode image
            Glide.with(itemView.getContext())
                    .load(itemImageUrl)
                    .into(mDetailListItemBinding.ivDetailArtwork);

            // Get the title of an episode and set text
            String title = item.getTitle();
            mDetailListItemBinding.tvDetailTitle.setText(title);

            // Get the description of an episode
            String description = item.getDescription();
            // If the description contains the img tag, remove it, then convert HTML to plain text.
            // Reference: @see "https://stackoverflow.com/questions/11178533/how-to-skip-image-tag-in-html-data-in-android"
            // @see "https://stackoverflow.com/questions/22573319/how-to-convert-html-text-to-plain-text-in-android"
            if (description != null) {
                String descriptionWithoutImageTag = description.replaceAll(IMG_HTML_TAG, REPLACEMENT_EMPTY);
                mDetailListItemBinding.tvDetailDescription.setText(
                        Html.fromHtml(Html.fromHtml(descriptionWithoutImageTag).toString()));
            }

            // Get the pub date of an episode and set text
            String pubDate = item.getPubDate();
            // Convert the pub date into something to display to users
            String formattedPubDate = CandyPodUtils.getFormattedDateString(pubDate);
            mDetailListItemBinding.tvDetailPubDate.setText(formattedPubDate);

            // Get the duration of an episode and set text
            String duration = item.getITunesDuration();
            if (TextUtils.isEmpty(duration)) {
                // Hide the duration TextView when the duration is empty
                mDetailListItemBinding.tvDetailDuration.setVisibility(View.GONE);
            } else {
                mDetailListItemBinding.tvDetailDuration.setText(duration);
            }
        }

        /**
         * Called by th child views during a click.
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Item item = mItems.get(adapterPosition);
            // Trigger the callback onItemClick
            mOnClickHandler.onItemClick(item, mDetailListItemBinding.ivDetailArtwork);
        }
    }
}
