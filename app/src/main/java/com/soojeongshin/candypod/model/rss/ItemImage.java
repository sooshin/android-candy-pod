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

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "image", strict = false)
public class ItemImage implements Parcelable {

    @Attribute(name = "href", required = false)
    private String mHref;

    public ItemImage() {
    }

    public ItemImage(String href) {
        mHref = href;
    }

    protected ItemImage(Parcel in) {
        mHref = in.readString();
    }

    public static final Creator<ItemImage> CREATOR = new Creator<ItemImage>() {
        @Override
        public ItemImage createFromParcel(Parcel in) {
            return new ItemImage(in);
        }

        @Override
        public ItemImage[] newArray(int size) {
            return new ItemImage[size];
        }
    };

    public String getItemImageHref() {
        return mHref;
    }

    public void setItemImageHref(String href) {
        mHref = href;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mHref);
    }
}
