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

@Root(name = "enclosure", strict = false)
public class Enclosure implements Parcelable {

    @Attribute(name = "url", required = false)
    private String mUrl;

    @Attribute(name = "type", required = false)
    private String mType;

    @Attribute(name = "length", required = false)
    private String mLength;

    public Enclosure() {
    }

    public Enclosure(String url, String type, String length) {
        mUrl = url;
        mType = type;
        mLength = length;
    }

    protected Enclosure(Parcel in) {
        mUrl = in.readString();
        mType = in.readString();
        mLength = in.readString();
    }

    public static final Creator<Enclosure> CREATOR = new Creator<Enclosure>() {
        @Override
        public Enclosure createFromParcel(Parcel in) {
            return new Enclosure(in);
        }

        @Override
        public Enclosure[] newArray(int size) {
            return new Enclosure[size];
        }
    };

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getLength() {
        return mLength;
    }

    public void setLength(String length) {
        mLength = length;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUrl);
        dest.writeString(mType);
        dest.writeString(mLength);
    }
}
