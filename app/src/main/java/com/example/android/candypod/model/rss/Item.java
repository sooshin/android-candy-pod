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

package com.example.android.candypod.model.rss;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "item", strict = false)
public class Item implements Parcelable {

    @Path("title")
    @Text(required = false)
    private String mTitle;

    @Path("description")
    @Text(required = false)
    private String mDescription;

    @Element(name = "summary", required = false)
    private String mITunesSummary;

    @Element(name = "pubDate", required = false)
    private String mPubDate;

    @Element(name = "duration", required = false)
    private String mITunesDuration;

    @Element(name = "enclosure", required = false)
    private Enclosure mEnclosure;

    @Element(name = "image", required = false)
    private ItemImage mItemImage;

    public Item() {
    }

    public Item(String title, String description, String iTunesSummary, String pubDate,
                String duration, Enclosure enclosure, ItemImage itemImage) {
        mTitle = title;
        mDescription = description;
        mITunesSummary = iTunesSummary;
        mPubDate = pubDate;
        mITunesDuration = duration;
        mEnclosure = enclosure;
        mItemImage = itemImage;
    }

    protected Item(Parcel in) {
        mTitle = in.readString();
        mDescription = in.readString();
        mITunesSummary = in.readString();
        mPubDate = in.readString();
        mITunesDuration = in.readString();
        mEnclosure = (Enclosure) in.readValue(Enclosure.class.getClassLoader());
        mItemImage = (ItemImage) in.readValue(ItemImage.class.getClassLoader());
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

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

    public String getITunesSummary() {
        return mITunesSummary;
    }

    public void setITunesSummary(String iTunesSummary) {
        mITunesSummary = iTunesSummary;
    }

    public String getPubDate() {
        return mPubDate;
    }

    public void setPubDate(String pubDate) {
        mPubDate = pubDate;
    }

    public String getITunesDuration() {
        return mITunesDuration;
    }

    public void setITunesDuration(String iTunesDuration) {
        mITunesDuration = iTunesDuration;
    }

    public Enclosure getEnclosure() {
        return mEnclosure;
    }

    public void setEnclosure(Enclosure enclosure) {
        mEnclosure = enclosure;
    }

    public ItemImage getItemImage() {
        return mItemImage;
    }

    public void setItemImage(ItemImage itemImage) {
        mItemImage = itemImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeString(mITunesSummary);
        dest.writeString(mPubDate);
        dest.writeString(mITunesDuration);
        dest.writeValue(mEnclosure);
        dest.writeValue(mItemImage);
    }
}
