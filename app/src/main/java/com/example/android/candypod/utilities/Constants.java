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

package com.example.android.candypod.utilities;

/**
 * Constants class is to define constants used in the app.
 */
public class Constants {

    private Constants() {
        // Restrict instantiation
    }

    /** Base URL for iTunes Search API */
    public static final String I_TUNES_BASE_URL = "https://rss.itunes.apple.com/api/v1/";

    /** URL for a lookup request */
    public static final String I_TUNES_LOOKUP = "https://itunes.apple.com/lookup";

    /** Index for representing a default fragment */
    public static final int INDEX_ZERO = 0;

    /** A key for the Extra to pass data via Intent */
    public static final String EXTRA_RESULT_ID = "extra_result_id";
    public static final String EXTRA_RESULT_NAME = "extra_result_name";

    /** Database name */
    public static final String DATABASE_NAME = "podcast";

    /** Used to replace the HTML img tag with empty string */
    public static final String IMG_HTML_TAG = "<img.+?>";
    public static final String REPLACEMENT_EMPTY = "";

    /** The value of column width in the GridAutofitLayoutManager */
    public static final int GRID_AUTO_FIT_COLUMN_WIDTH = 380;
    /** Default value for column width in the GridAutofitLayoutManager */
    public static final int GRID_COLUMN_WIDTH_DEFAULT = 48;
    /** Initial span count for the GridAutofitLayoutManager */
    public static final int GRID_SPAN_COUNT = 1;

    /** Context menu option in the PodcastsAdapter */
    public static final String DELETE = "Delete";
    public static final int GROUP_ID_DELETE = 0;
    public static final int ORDER_DELETE = 0;

}
