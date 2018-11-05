package com.example.android.candypod.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

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

    /**
     * Constructor
     *
     * @param podcastId
     * @param title
     * @param description
     * @param author
     * @param artworkImageUrl
     */
    @Ignore
    public PodcastEntry(String podcastId, String title, String description, String author, String artworkImageUrl) {
        this.podcastId = podcastId;
        this.title = title;
        this.description = description;
        this.author = author;
        this.artworkImageUrl = artworkImageUrl;
    }

    public PodcastEntry(int id, String podcastId, String title, String description, String author, String artworkImageUrl) {
        this.id = id;
        this.podcastId = podcastId;
        this.title = title;
        this.description = description;
        this.author = author;
        this.artworkImageUrl = artworkImageUrl;
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
}
