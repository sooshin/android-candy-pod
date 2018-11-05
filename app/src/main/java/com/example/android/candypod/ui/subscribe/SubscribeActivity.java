package com.example.android.candypod.ui.subscribe;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.candypod.AppExecutors;
import com.example.android.candypod.R;
import com.example.android.candypod.data.CandyPodDatabase;
import com.example.android.candypod.data.PodcastEntry;
import com.example.android.candypod.databinding.ActivitySubscribeBinding;
import com.example.android.candypod.model.LookupResponse;
import com.example.android.candypod.model.LookupResult;
import com.example.android.candypod.model.rss.ArtworkImage;
import com.example.android.candypod.model.rss.Category;
import com.example.android.candypod.model.rss.Channel;
import com.example.android.candypod.model.rss.Item;
import com.example.android.candypod.model.rss.RssFeed;
import com.example.android.candypod.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.example.android.candypod.utilities.Constants.EXTRA_RESULT_ID;
import static com.example.android.candypod.utilities.Constants.I_TUNES_LOOKUP;

public class SubscribeActivity extends AppCompatActivity {

    /** The podcast ID */
    private String mResultId;

    /** ViewModel for SubscribeActivity */
    private SubscribeViewModel mSubscribeViewModel;
    /** ViewModel which stores and manages LiveData RssFeed */
    private RssFeedViewModel mRssFeedViewModel;
    /** Member variable for the PodcastEntryViewModel to store and manage LiveData PodcastEntry */
    private PodcastEntryViewModel mPodcastEntryViewModel;

    /** This field is used for data binding **/
    private ActivitySubscribeBinding mSubscribeBinding;

    /** Member variable for SubscribeAdapter */
    private SubscribeAdapter mSubscribeAdapter;

    /** Member variable for the list of {@link Item}s which is the episodes in the podcast */
    private List<Item> mItemList;

    /** Member variable for the Database */
    private CandyPodDatabase mDb;
    /** Member variable for the PodcastEntry */
    private PodcastEntry mPodcastEntry;
    /** True when the user subscribed the podcast, otherwise false */
    private boolean mIsSubscribed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSubscribeBinding = DataBindingUtil.setContentView(this, R.layout.activity_subscribe);

        // Get the podcast ID
        mResultId = getResultId();

       // Get the ViewModel from the factory
        setupViewModel();
        // Observe changes in the LookupResponse
        observeLookupResponse();

        // Create a LinearLayoutManager and SubscribeAdapter, and set them to the RecyclerView
        initAdapter();

        // Get the Database instance
        mDb = CandyPodDatabase.getInstance(getApplicationContext());
        // Check if the podcast is subscribed or not
        mIsSubscribed = isSubscribed();
    }

    /**
     * Create a LinearLayoutManager and SubscribeAdapter, and set them to the RecyclerView.
     */
    private void initAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Set the layout manager to the RecyclerView
        mSubscribeBinding.rvItem.setLayoutManager(layoutManager);
        // Use this setting to improve performance if you know that changes in content do not
        // change the child layout size in the RecyclerView
        mSubscribeBinding.rvItem.setHasFixedSize(true);

        // Create an empty ArrayList
        mItemList = new ArrayList<>();
        // SubscribeAdapter is responsible for displaying each item in the list.
        mSubscribeAdapter = new SubscribeAdapter(mItemList);
        // Set adapter to the RecyclerView
        mSubscribeBinding.rvItem.setAdapter(mSubscribeAdapter);
    }

    /**
     * Returns the podcast ID which is used to create a lookup request to search for content.
     */
    private String getResultId() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_RESULT_ID)) {
            mResultId = intent.getStringExtra(EXTRA_RESULT_ID);
        }
        return mResultId;
    }

    /**
     * Get the ViewModel from the factory.
     */
    private void setupViewModel() {
        SubscribeViewModelFactory factory = InjectorUtils.provideSubscribeViewModelFactory(
                SubscribeActivity.this, I_TUNES_LOOKUP, mResultId);
        mSubscribeViewModel = ViewModelProviders.of(this, factory).get(SubscribeViewModel.class);
    }

    /**
     * Every time the LookupResponse data is updated, the onChanged callback will be invoked and
     * update the UI.
     */
    private void observeLookupResponse() {
        mSubscribeViewModel.getLookupResponse().observe(this, new Observer<LookupResponse>() {
            @Override
            public void onChanged(@Nullable LookupResponse lookupResponse) {
                if (lookupResponse != null) {
                    List<LookupResult> lookupResults = lookupResponse.getLookupResults();
                    String feedUrl = lookupResults.get(0).getFeedUrl();
                    Timber.e("feedUrl: " + feedUrl);

                    // Get the RssFeedViewModel from the factory
                    setupRssFeedViewModel(feedUrl);
                    // Observe changes in the RssFeed
                    observeRssFeed();
                }
            }
        });
    }

    /**
     * Get the RssFeedViewModel from the factory.
     * @param feedUrl The feed URL extracted from the list of LookupResults has the episode
     *                  metadata and stream URLs for the audio file.
     */
    private void setupRssFeedViewModel(String feedUrl) {
        RssFeedViewModelFactory rssFactory = InjectorUtils.provideRssViewModelFactory(
                SubscribeActivity.this, feedUrl);
        mRssFeedViewModel = ViewModelProviders.of(this, rssFactory).get(RssFeedViewModel.class);
    }

    /**
     * Observe changes in the RssFeed
     */
    private void observeRssFeed() {
        mRssFeedViewModel.getRssFeed().observe(this, new Observer<RssFeed>() {
            @Override
            public void onChanged(@Nullable RssFeed rssFeed) {
                if (rssFeed != null) {
                    Channel channel = rssFeed.getChannel();

                    // Show the details of the podcast
                    showDetails(channel);
                    // Show the episodes of the podcast
                    showItems(channel);
                }
            }
        });
    }

    /**
     * Show the details of the podcast and create the PodcastEntry based on the data.
     * @param channel Channel object that contains data, such as title, description, author,
     *                language, categories, image, items.
     */
    private void showDetails(Channel channel) {
        // Get the image URL which consists of two types. One has a href attribute, the other has
        // an url element. If the url element is null, use the href attribute.
        List<ArtworkImage> artworkImage = channel.getImages();
        ArtworkImage image = artworkImage.get(0);
        String artworkImageUrl = image.getImageUrl();
        if (artworkImageUrl == null) {
            artworkImageUrl = image.getImageHref();
        }
        // Use Glide library to upload the artwork
        Glide.with(this)
                .load(artworkImageUrl)
                .into(mSubscribeBinding.ivArtwork);

        // Get the title and set the text
        String title = channel.getTitle();
        mSubscribeBinding.tvTitle.setText(title);

        // Get the author and set the text
        String author = channel.getITunesAuthor();
        mSubscribeBinding.tvAuthor.setText(author);

        // Get the categories and set the categories
        List<Category> categories = channel.getCategories();
        for (Category category:categories) {
            String categoryText = category.getText();
            if (categoryText != null) {
                mSubscribeBinding.tvCategory.append(categoryText + "  ");
            }
        }

        // Get the language and set the text
        String language = channel.getLanguage();
        mSubscribeBinding.tvLanguage.setText(language);

        // Get the description
        String description = channel.getDescription();
        // Convert HTML to plain text and set the text
        // Reference: @see "https://stackoverflow.com/questions/22573319/how-to-convert-html-text-to-plain-text-in-android"
        mSubscribeBinding.tvDescription.setText(Html.fromHtml(Html.fromHtml(description).toString()));

        // Create the PodcastEntry based on the data
        mPodcastEntry = new PodcastEntry(mResultId, title, description, author, artworkImageUrl);
    }

    /**
     * Show the episodes of the podcast.
     * @param channel Channel object that includes the items data which is the podcast episodes.
     */
    private void showItems(Channel channel) {
        // Get the list of items
        mItemList = channel.getItemList();
        // Update the data source and notify the adapter of any changes.
        mSubscribeAdapter.addAll(mItemList);
    }

    /**
     * Return true when the user subscribed the podcast otherwise, return false.
     */
    private boolean isSubscribed() {
        // Get the PodcastEntryViewModel from the factory
        PodcastEntryViewModelFactory podcastEntryFactory = InjectorUtils.providePodcastEntryViewModelFactory(
                this, mResultId);
        mPodcastEntryViewModel = ViewModelProviders.of(this, podcastEntryFactory)
                .get(PodcastEntryViewModel.class);

        // Observe the PodcastEntry and changes the button text based on whether or not the podcast
        // exists
        mPodcastEntryViewModel.getPodcastEntry().observe(this, new Observer<PodcastEntry>() {
            @Override
            public void onChanged(@Nullable PodcastEntry podcastEntry) {
                if (mPodcastEntryViewModel.getPodcastEntry().getValue() == null) {
                    mSubscribeBinding.btSubscribe.setText("subscribe");
                    mIsSubscribed = false;
                } else {
                    mSubscribeBinding.btSubscribe.setText("unsubscribe");
                    mIsSubscribed = true;
                }
            }
        });
        return mIsSubscribed;
    }

    /**
     * Called when the subscribe button is clicked. If the podcast is not in the podcast table,
     * insert the data into the underlying database. Otherwise, delete the podcast data from the
     * database.
     */
    public void onSubscribeClick(View view) {
        // Check if the PodcastEntry is not null
        if (mPodcastEntry != null) {
            if (!mIsSubscribed) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        // Insert the podcast data into the database by using the podcastDao
                        mDb.podcastDao().insertPodcast(mPodcastEntry);
                    }
                });
                Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();
            } else {
                mPodcastEntry = mPodcastEntryViewModel.getPodcastEntry().getValue();
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        // Delete the podcast from the database by using the podcastDao
                        mDb.podcastDao().deletePodcast(mPodcastEntry);
                    }
                });
                Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
