package com.example.android.candypod.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * {@link Dao} which provides an api for all data operations with the {@link CandyPodDatabase}
 */
@Dao
public interface PodcastDao {

    @Query("SELECT * FROM podcast")
    LiveData<List<PodcastEntry>> loadPodcasts();
}
