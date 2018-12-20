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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "image", strict = false)
public class ArtworkImage {

    @Attribute(name = "href", required = false)
    private String mHref;

    @Element(name = "url", required = false)
    private String mUrl;

    public ArtworkImage() {
    }

    public String getImageHref() {
        return mHref;
    }

    public void setImageHref(String href) {
        mHref = href;
    }

    public String getImageUrl() {
        return mUrl;
    }

    public void setImageUrl(String url) {
        mUrl = url;
    }
}
