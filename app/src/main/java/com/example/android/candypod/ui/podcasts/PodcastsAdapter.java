package com.example.android.candypod.ui.podcasts;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.android.candypod.R;
import com.example.android.candypod.data.PodcastEntry;
import com.example.android.candypod.databinding.PodcastsListItemBinding;

import java.util.List;

public class PodcastsAdapter extends RecyclerView.Adapter<PodcastsAdapter.PodcastsViewHolder> {

    private List<PodcastEntry> mPodcastEntries;

    private Context mContext;

    private final PodcastsAdapterOnClickHandler mOnClickHandler;

    public interface PodcastsAdapterOnClickHandler {
        void onPodcastClick(PodcastEntry podcastEntry);
    }

    public PodcastsAdapter(Context context, PodcastsAdapterOnClickHandler onClickHandler) {
        mContext = context;
        mOnClickHandler = onClickHandler;
    }

    @NonNull
    @Override
    public PodcastsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        PodcastsListItemBinding podcastsListItemBinding = DataBindingUtil.inflate(
                layoutInflater, R.layout.podcasts_list_item, viewGroup, false);
        return new PodcastsViewHolder(podcastsListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastsViewHolder podcastsViewHolder, int position) {
        PodcastEntry podcastEntry = mPodcastEntries.get(position);
        podcastsViewHolder.bind(podcastEntry);
    }

    @Override
    public int getItemCount() {
        if (null == mPodcastEntries) return 0;
        return mPodcastEntries.size();
    }

    public void setPodcastEntries(List<PodcastEntry> podcastEntries) {
        mPodcastEntries = podcastEntries;
        notifyDataSetChanged();
    }

    public class PodcastsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private PodcastsListItemBinding mPodcastsListItemBinding;

        public PodcastsViewHolder(PodcastsListItemBinding podcastsListItemBinding) {
            super(podcastsListItemBinding.getRoot());
            mPodcastsListItemBinding = podcastsListItemBinding;

            itemView.setOnClickListener(this);
        }

        void bind(PodcastEntry podcastEntry) {
            String artworkImageUrl = podcastEntry.getArtworkImageUrl();
            Glide.with(mContext)
                    .load(artworkImageUrl)
                    .into(mPodcastsListItemBinding.ivArtwork);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            PodcastEntry podcastEntry = mPodcastEntries.get(adapterPosition);
            mOnClickHandler.onPodcastClick(podcastEntry);
        }
    }
}
