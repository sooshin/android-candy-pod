package com.example.android.candypod.model.rss;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "enclosure", strict = false)
public class Enclosure {

    @Attribute(name = "url", required = false)
    private String mUrl;

    @Attribute(name = "type", required = false)
    private String mType;

    @Attribute(name = "length", required = false)
    private String mLength;

    public Enclosure() {
    }

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
}
