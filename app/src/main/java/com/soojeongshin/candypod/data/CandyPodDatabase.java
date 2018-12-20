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

package com.soojeongshin.candypod.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import timber.log.Timber;

import static com.soojeongshin.candypod.utilities.Constants.DATABASE_NAME;

@Database(entities = {PodcastEntry.class, FavoriteEntry.class, DownloadEntry.class}, version = 1, exportSchema = false)
@TypeConverters({ItemsConverter.class, DateConverter.class})
public abstract class CandyPodDatabase extends RoomDatabase {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static CandyPodDatabase sInstance;

    public static CandyPodDatabase getInstance(Context context) {
        Timber.d("Getting the database");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        CandyPodDatabase.class, DATABASE_NAME).build();
                Timber.d("Made new database");
            }
        }
        return sInstance;
    }

    // The associated DAOs for the database
    public abstract PodcastDao podcastDao();
}
