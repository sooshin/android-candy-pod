package com.example.android.candypod.model.rss;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "rss", strict = false)
public class RssFeed {

    @Element(name = "channel", required = false)
    private Channel mChannel;

    public RssFeed() {
    }

    public Channel getChannel() {
        return mChannel;
    }

    public void setChannel(Channel channel) {
        mChannel = channel;
    }
}
