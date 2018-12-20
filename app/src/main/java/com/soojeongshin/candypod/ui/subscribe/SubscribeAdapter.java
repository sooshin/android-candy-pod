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

package com.soojeongshin.candypod.ui.subscribe;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.databinding.SubscribeListItemBinding;
import com.soojeongshin.candypod.model.rss.Item;
import com.soojeongshin.candypod.utilities.CandyPodUtils;

import java.util.List;

import static com.soojeongshin.candypod.utilities.Constants.IMG_HTML_TAG;
import static com.soojeongshin.candypod.utilities.Constants.REPLACEMENT_EMPTY;

/**
 * Exposes a list of episodes from a list of {@link Item} to a {@link RecyclerView}.
 */
public class SubscribeAdapter extends RecyclerView.Adapter<SubscribeAdapter.SubscribeViewHolder> {

    /** Member variable for the list of {@link Item}s which is the episodes in the podcast */
    private List<Item> mItems;

    /** The podcast image URL used when there is no episode image */
    private String mPodcastImage;

    /**
     * Constructor for SubscribeAdapter that accepts the list of items to display.
     * @param items The list of items to display
     * @param podcastImage The podcast image
     */
    public SubscribeAdapter(List<Item> items, String podcastImage) {
        mItems = items;
        mPodcastImage = podcastImage;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType If your RecyclerView has more than one type of item (which ours doesn't) you
     *                   can use this viewType integer to provide a different layout.
     * @return A new SubscribeViewHolder that holds SubscribeListItemBinding
     */
    @NonNull
    @Override
    public SubscribeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        SubscribeListItemBinding subscribeListItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.subscribe_list_item, viewGroup, false);
        return new SubscribeViewHolder(subscribeListItemBinding);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull SubscribeViewHolder holder, int position) {
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
     * Sets the podcast image.
     * @param podcastImage The podcast image
     */
    public void setPodcastImage(String podcastImage) {
        mPodcastImage = podcastImage;
    }

    /**
     * Cache of the children views for a list item.
     */
    public class SubscribeViewHolder extends RecyclerView.ViewHolder {

        /** This field is used for data binding */
        private SubscribeListItemBinding mSubscribeListItemBinding;

        /**
         * Constructor for SubscribeViewHolder
         *
         * @param subscribeListItemBinding Used to access the layout's variables and views
         */
        public SubscribeViewHolder(SubscribeListItemBinding subscribeListItemBinding) {
            super(subscribeListItemBinding.getRoot());
            mSubscribeListItemBinding = subscribeListItemBinding;
        }

        /**
         * This method will take an Item object as input and use that item to display the appropriate
         * text within a list item.
         *
         * @param item The item object
         */
        void bind(Item item) {
            // Get the title of an episode
            String title = item.getTitle();
            mSubscribeListItemBinding.tvItemTitle.setText(title);

            // Get the description of an episode
            String description = item.getDescription();
            // If the description contains the img tag, remove it, then convert HTML to plain text.
            // Reference: @see "https://stackoverflow.com/questions/11178533/how-to-skip-image-tag-in-html-data-in-android"
            // @see "https://stackoverflow.com/questions/22573319/how-to-convert-html-text-to-plain-text-in-android"
            if (description != null) {
                String descriptionWithoutImageTag = description.replaceAll(IMG_HTML_TAG, REPLACEMENT_EMPTY);
                mSubscribeListItemBinding.tvItemDescription.setText(
                        Html.fromHtml(Html.fromHtml(descriptionWithoutImageTag).toString()));
            }

            // If an episode image exists, use it. Otherwise, use the podcast image.
            String imageUrl = CandyPodUtils.getItemImageUrl(item, mPodcastImage);
            // Use Glide library to load the episode image
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .into(mSubscribeListItemBinding.ivItemArtwork);

            // Get the pub date of an episode
            String pubDate = item.getPubDate();
            if (pubDate != null) {
                // Convert the pub date into something to display to users
                String formattedPubDate = CandyPodUtils.getFormattedDateString(pubDate);
                mSubscribeListItemBinding.tvItemPubDate.setText(formattedPubDate);
            }

            // Get the duration of an episode
            String iTunesDuration = item.getITunesDuration();
            if (TextUtils.isEmpty(iTunesDuration)) {
                // Hide the duration TextView when the duration is empty
                mSubscribeListItemBinding.tvItemDuration.setVisibility(View.GONE);
            } else {
                mSubscribeListItemBinding.tvItemDuration.setText(iTunesDuration);
            }
        }
    }
}
