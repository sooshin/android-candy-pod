package com.example.android.candypod.model.rss;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "item", strict = false)
public class Item {

//    @Element(name = "title", required = false)
//    private String mTitle;

    @Element(name = "description", required = false)
    private String mDescription;

    @Element(name = "summary", required = false)
    private String mITunesSummary;

    @Element(name = "pubDate", required = false)
    private String mPubDate;

    @Element(name = "duration", required = false)
    private String mITunesDuration;

    @Element(name = "enclosure", required = false)
    private Enclosure mEnclosure;

    public Item() {
    }

//    public String getTitle() {
//        return mTitle;
//    }
//
//    public void setTitle(String title) {
//        mTitle = title;
//    }

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
}
