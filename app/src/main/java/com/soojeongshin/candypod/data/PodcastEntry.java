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

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.soojeongshin.candypod.model.rss.Item;

import java.util.Date;
import java.util.List;

@Entity(tableName = "podcast")
public class PodcastEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "podcast_id")
    private String podcastId;

    private String title;

    private String description;

    private String author;

    @ColumnInfo(name = "artwork_image_url")
    private String artworkImageUrl;

    @TypeConverters(ItemsConverter.class)
    private List<Item> items;

    private Date date;

    /**
     * Constructor
     *
     * @param podcastId
     * @param title
     * @param description
     * @param author
     * @param artworkImageUrl
     * @param items
     * @param date
     */
    @Ignore
    public PodcastEntry(String podcastId, String title, String description, String author,
                        String artworkImageUrl, List<Item> items, Date date) {
        this.podcastId = podcastId;
        this.title = title;
        this.description = description;
        this.author = author;
        this.artworkImageUrl = artworkImageUrl;
        this.items = items;
        this.date = date;
    }

    public PodcastEntry(int id, String podcastId, String title, String description, String author,
                        String artworkImageUrl, List<Item> items, Date date) {
        this.id = id;
        this.podcastId = podcastId;
        this.title = title;
        this.description = description;
        this.author = author;
        this.artworkImageUrl = artworkImageUrl;
        this.items = items;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getPodcastId() {
        return podcastId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getArtworkImageUrl() {
        return artworkImageUrl;
    }

    @TypeConverters(ItemsConverter.class)
    public List<Item> getItems() {
        return items;
    }

    public Date getDate() {
        return date;
    }
}
