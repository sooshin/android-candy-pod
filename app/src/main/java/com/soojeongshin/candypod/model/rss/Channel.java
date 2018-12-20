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

package com.soojeongshin.candypod.model.rss;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import java.util.List;

@Root(name = "channel", strict = false)
public class Channel {

    @Element(name = "title", required = false)
    private String mTitle;

    @Path("description")
    @Text(required = false)
    private String mDescription;

    @Path("author")
    @Text(required = false)
    private String mITunesAuthor;

    @Element(name = "language", required = false)
    private String mLanguage;

    @ElementList(inline = true, name = "category", required = false)
    private List<Category> mCategories;

    @ElementList(inline = true, name = "image", required = false)
    private List<ArtworkImage> mArtworkImages;

    @ElementList(inline = true, name = "item", required = false)
    private List<Item> mItemList;

    public Channel() {
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getITunesAuthor() {
        return mITunesAuthor;
    }

    public void setITunesAuthor(String iTunesAuthor) {
        mITunesAuthor = iTunesAuthor;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        mLanguage = language;
    }

    public List<Category> getCategories() {
        return mCategories;
    }

    public void setCategory(List<Category> categories) {
        mCategories = categories;
    }

    public List<ArtworkImage> getImages() {
        return mArtworkImages;
    }

    public void setImages(List<ArtworkImage> artworkImages) {
        mArtworkImages = artworkImages;
    }

    public List<Item> getItemList() {
        return mItemList;
    }

    public void setItemList(List<Item> itemList) {
        mItemList = itemList;
    }
}
