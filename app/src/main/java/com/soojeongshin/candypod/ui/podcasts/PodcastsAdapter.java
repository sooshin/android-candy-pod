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

package com.soojeongshin.candypod.ui.podcasts;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.soojeongshin.candypod.AppExecutors;
import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.data.CandyPodDatabase;
import com.soojeongshin.candypod.data.PodcastEntry;
import com.soojeongshin.candypod.databinding.PodcastsListItemBinding;

import java.util.List;

import static com.soojeongshin.candypod.utilities.Constants.DELETE;
import static com.soojeongshin.candypod.utilities.Constants.GROUP_ID_DELETE;
import static com.soojeongshin.candypod.utilities.Constants.ORDER_DELETE;

/**
 * Exposes a list of subscribed podcasts from a list of {@link PodcastEntry} to a {@link RecyclerView}.
 */
public class PodcastsAdapter extends RecyclerView.Adapter<PodcastsAdapter.PodcastsViewHolder> {

    /** Member variable for the list of PodcastEntries that holds subscribed podcast data */
    private List<PodcastEntry> mPodcastEntries;

    /** Context we use to utility methods, app resources and layout inflaters */
    private Context mContext;

    /**
     * An on-click handler that we've defined to make it easy for a Fragment to interface with
     * our RecyclerView
     */
    private final PodcastsAdapterOnClickHandler mOnClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface PodcastsAdapterOnClickHandler {
        void onPodcastClick(PodcastEntry podcastEntry, ImageView imageView);
    }

    /**
     * Creates a PodcastsAdapter.
     */
    public PodcastsAdapter(Context context, PodcastsAdapterOnClickHandler onClickHandler) {
        mContext = context;
        mOnClickHandler = onClickHandler;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new PodcastsViewHolder that holds the PodcastsListItemBinding
     */
    @NonNull
    @Override
    public PodcastsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        PodcastsListItemBinding podcastsListItemBinding = DataBindingUtil.inflate(
                layoutInflater, R.layout.podcasts_list_item, viewGroup, false);
        return new PodcastsViewHolder(podcastsListItemBinding);
    }

    /**
     * Called by the RecyclerView to display the data at the specified position.
     */
    @Override
    public void onBindViewHolder(@NonNull PodcastsViewHolder podcastsViewHolder, int position) {
        PodcastEntry podcastEntry = mPodcastEntries.get(position);
        podcastsViewHolder.bind(podcastEntry);
    }

    /**
     * Returns the number of items available in the podcasts.
     */
    @Override
    public int getItemCount() {
        if (null == mPodcastEntries) return 0;
        return mPodcastEntries.size();
    }

    /**
     * When data changes, updates the list of podcastEntries and notifies the adapter to use
     * the new values on it.
     */
    public void setPodcastEntries(List<PodcastEntry> podcastEntries) {
        mPodcastEntries = podcastEntries;
        notifyDataSetChanged();
    }

    /**
     * Cache of the children views for the podcast list item.
     */
    public class PodcastsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        /** This field is used for data binding */
        private PodcastsListItemBinding mPodcastsListItemBinding;

        /**
         * Constructor for the PodcastsViewHolder.
         *
         * @param podcastsListItemBinding Used to access the layout's variables and views
         */
        public PodcastsViewHolder(PodcastsListItemBinding podcastsListItemBinding) {
            super(podcastsListItemBinding.getRoot());
            mPodcastsListItemBinding = podcastsListItemBinding;

            // Set OnClickListener on the view
            itemView.setOnClickListener(this);
            // Set OnCreateContextMenuListener on the view
            itemView.setOnCreateContextMenuListener(this);
        }

        /**
         * This method will take a PodcastEntry object as input and use it to display
         * the appropriate image within a list item.
         */
        void bind(PodcastEntry podcastEntry) {
            String artworkImageUrl = podcastEntry.getArtworkImageUrl();
            Glide.with(mContext)
                    .load(artworkImageUrl)
                    .into(mPodcastsListItemBinding.ivArtwork);
        }

        /**
         * Called whenever a user clicks on a podcast in the list.
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            PodcastEntry podcastEntry = mPodcastEntries.get(adapterPosition);
            mOnClickHandler.onPodcastClick(podcastEntry, mPodcastsListItemBinding.ivArtwork);
        }

        /**
         * When the user performs a long-click on a podcast, a floating menu appears.
         *
         *  Reference @see "https://stackoverflow.com/questions/36958800/recyclerview-getmenuinfo-always-null"
         *  "https://stackoverflow.com/questions/37601346/create-options-menu-for-recyclerview-item"
         */
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int adapterPosition = getAdapterPosition();
            // Set the itemId to adapterPosition to retrieve podcastEntry later
            MenuItem item = menu.add(GROUP_ID_DELETE, adapterPosition, ORDER_DELETE,
                    v.getContext().getString(R.string.action_delete));
            // Set OnMenuItemClickListener on the menu item
            item.setOnMenuItemClickListener(this);
        }

        /**
         * This gets called when a menu item is clicked.
         */
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getTitle().toString()) {
                case DELETE:
                    int adapterPosition = item.getItemId();
                    PodcastEntry podcastEntry = mPodcastEntries.get(adapterPosition);
                    // Delete the podcast from the database
                    delete(podcastEntry);
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Deletes the podcast when the user clicks "Delete" menu option.
         * @param podcastEntry The PodcastEntry the user want to delete
         */
        private void delete(final PodcastEntry podcastEntry) {
            // Get the database instance
            final CandyPodDatabase db = CandyPodDatabase.getInstance(mContext);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    // Delete the podcast from the database by using the podcastDao
                    db.podcastDao().deletePodcast(podcastEntry);
                }
            });
        }
    }
}
