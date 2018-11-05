package com.example.android.candypod.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * {@link Dao} which provides an api for all data operations with the {@link CandyPodDatabase}
 */
@Dao
public interface PodcastDao {

    @Query("SELECT * FROM podcast")
    LiveData<List<PodcastEntry>> loadPodcasts();

    @Query("SELECT * FROM podcast WHERE podcast_id = :podcastId")
    LiveData<PodcastEntry> loadPodcastByPodcastId(String podcastId);

    @Insert
    void insertPodcast(PodcastEntry podcastEntry);

    @Delete
    void deletePodcast(PodcastEntry podcastEntry);

}
