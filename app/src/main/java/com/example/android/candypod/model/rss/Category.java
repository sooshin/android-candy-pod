package com.example.android.candypod.model.rss;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "category", strict = false)
public class Category {

    @Attribute(name = "text")
    private String mText;

    public Category() {
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }
}
