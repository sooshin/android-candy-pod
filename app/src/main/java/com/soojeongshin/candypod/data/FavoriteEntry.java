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

/**
 * Defines the schema of a table in room for a single favorite episode.
 */
@Entity(tableName = "favorite_episodes")
public class FavoriteEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "podcast_id")
    private String podcastId;

    /** The podcast title */
    private String title;

    @ColumnInfo(name = "artwork_image_url")
    private String artworkImageUrl;

    @ColumnInfo(name = "item_title")
    private String itemTitle;

    @ColumnInfo(name = "item_description")
    private String itemDescription;

    @ColumnInfo(name = "item_pub_date")
    private String itemPubDate;

    @ColumnInfo(name = "item_duration")
    private String itemDuration;

    @ColumnInfo(name = "item_enclosure_url")
    private String itemEnclosureUrl;

    @ColumnInfo(name = "item_enclosure_type")
    private String itemEnclosureType;

    @ColumnInfo(name = "item_enclosure_length")
    private String itemEnclosureLength;

    @ColumnInfo(name = "item_image_url")
    private String itemImageUrl;

    /**
     * Constructor to create FavoriteEntry. Uses ignore annotation to guide Room to use the right
     * constructor.
     * @param podcastId The podcast ID
     * @param title The podcast title
     * @param artworkImageUrl The artwork image URL
     * @param itemTitle The episode title
     * @param itemDescription The episode description
     * @param itemPubDate The pub date of an episode
     * @param itemDuration The duration of an episode
     * @param itemEnclosureUrl The enclosure URL of an episode
     * @param itemEnclosureLength The length of an episode
     * @param itemImageUrl The image URL of an episode
     */
    @Ignore
    public FavoriteEntry(String podcastId, String title,
                         String artworkImageUrl, String itemTitle, String itemDescription,
                         String itemPubDate, String itemDuration, String itemEnclosureUrl,
                         String itemEnclosureType, String itemEnclosureLength, String itemImageUrl) {
        this.podcastId = podcastId;
        this.title = title;
        this.artworkImageUrl = artworkImageUrl;
        this.itemTitle = itemTitle;
        this.itemDescription = itemDescription;
        this.itemPubDate = itemPubDate;
        this.itemDuration = itemDuration;
        this.itemEnclosureUrl = itemEnclosureUrl;
        this.itemEnclosureType = itemEnclosureType;
        this.itemEnclosureLength = itemEnclosureLength;
        this.itemImageUrl = itemImageUrl;
    }

    /**
     * Constructor used by Room to create FavoriteEntries.
     */
    public FavoriteEntry(int id, String podcastId, String title,
                         String artworkImageUrl, String itemTitle, String itemDescription,
                         String itemPubDate, String itemDuration, String itemEnclosureUrl,
                         String itemEnclosureType, String itemEnclosureLength, String itemImageUrl) {
        this.id = id;
        this.podcastId = podcastId;
        this.title = title;
        this.artworkImageUrl = artworkImageUrl;
        this.itemTitle = itemTitle;
        this.itemDescription = itemDescription;
        this.itemPubDate = itemPubDate;
        this.itemDuration = itemDuration;
        this.itemEnclosureUrl = itemEnclosureUrl;
        this.itemEnclosureType = itemEnclosureType;
        this.itemEnclosureLength = itemEnclosureLength;
        this.itemImageUrl = itemImageUrl;
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

    public String getArtworkImageUrl() {
        return artworkImageUrl;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public String getItemPubDate() {
        return itemPubDate;
    }

    public String getItemDuration() {
        return itemDuration;
    }

    public String getItemEnclosureUrl() {
        return itemEnclosureUrl;
    }

    public String getItemEnclosureType() {
        return itemEnclosureType;
    }

    public String getItemEnclosureLength() {
        return itemEnclosureLength;
    }

    public String getItemImageUrl() {
        return itemImageUrl;
    }
}
