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
}
