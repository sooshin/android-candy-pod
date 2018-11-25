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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

import static com.example.android.candypod.utilities.Constants.FORMATTED_PATTERN;
import static com.example.android.candypod.utilities.Constants.PUB_DATE_PATTERN;

public class CandyPodUtils {

    /**
     * Converts the publication date into something to display to users.
     * @param pubDate The publication date (i.e. Tue, 25 Nov 2018 05:00:00 -0000)
     * @return The formatted date (i.e. Nov 25, 2018)
     */
    public static String getFormattedDateString(String pubDate) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(PUB_DATE_PATTERN, Locale.US);
        Date currentTime = null;
        try {
            currentTime = simpleDateFormat.parse(pubDate);
        } catch (ParseException e) {
            Timber.e("Error formatting date: " + e.getMessage());
        }

        SimpleDateFormat formatter = new SimpleDateFormat(FORMATTED_PATTERN, Locale.US);
        return formatter.format(currentTime);
    }
}
