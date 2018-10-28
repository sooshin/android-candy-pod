package com.example.android.candypod.ui;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.android.candypod.R;
import com.example.android.candypod.databinding.AddPodListItemBinding;
import com.example.android.candypod.model.Result;

import java.util.List;

public class AddPodcastAdapter extends RecyclerView.Adapter<AddPodcastAdapter.AddPodcastViewHolder> {

    /** Member variable for the list of {@link Result}s */
    private List<Result> mResults;

    /**
     * Constructor for the AddPodcastAdapter that accepts a list of results to display
     *
     * @param results The list of {@link Result}s
     */
    public AddPodcastAdapter(List<Result> results) {
        mResults = results;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent The ViewGroup that these ViewHolders are contained within.
     * @param viewType If your RecyclerView has more than one type of item (which ours doesn't) you
     *                    can use this viewType integer to provide a different layout.
     * @return A new AddPodcastViewHolder that holds the AddPodListItemBinding
     */
    @NonNull
    @Override
    public AddPodcastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        AddPodListItemBinding addPodListItemBinding = DataBindingUtil
                .inflate(layoutInflater, R.layout.add_pod_list_item, parent, false);
        return new AddPodcastViewHolder(addPodListItemBinding);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull AddPodcastViewHolder holder, int position) {
        Result result = mResults.get(position);
        holder.bind(result);
    }

    /**
     * Returns the number of items to display. It is used behind the scenes to help layout our
     * Views and for animations.
     *
     * @return The number of results
     */
    @Override
    public int getItemCount() {
        if (null == mResults) return 0;
        return mResults.size();
    }

    /**
     * This method is to add a list of {@link Result}s
     *
     * @param results The data source of the adapter
     */
    public void addAll(List<Result> results) {
        mResults.clear();
        mResults.addAll(results);
        notifyDataSetChanged();
    }

    /**
     * Caches of the children views for a result list item.
     */
    public class AddPodcastViewHolder extends RecyclerView.ViewHolder {

        /** This field is used for data binding */
        private AddPodListItemBinding mAddPodListItemBinding;

        /**
         * Constructor for AddPodcastViewHolder
         *
         * @param addPodListItemBinding Used to access the layout's variables and views
         */
        public AddPodcastViewHolder(AddPodListItemBinding addPodListItemBinding) {
            super(addPodListItemBinding.getRoot());
            mAddPodListItemBinding = addPodListItemBinding;
        }

        /**
         * This method will take a Result object as input and use that result to display the
         * appropriate text and the image within a list item.
         *
         * @param result The result object
         */
        void bind(Result result) {
            // Get the artwork URL
            String artworkUrl = result.getArtworkUrl();
            // Use Glide library to upload the artwork image
            Glide.with(itemView.getContext())
                    .load(artworkUrl)
                    .into(mAddPodListItemBinding.ivArtwork);

            // Set the name
            mAddPodListItemBinding.tvName.setText(result.getName());
        }
    }
}
