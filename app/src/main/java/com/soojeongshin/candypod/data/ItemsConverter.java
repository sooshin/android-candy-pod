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

package com.soojeongshin.candypod.data;

import android.arch.persistence.room.TypeConverter;

import com.soojeongshin.candypod.model.rss.Item;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * {@link TypeConverter} for string to the list of {@link Item}s
 * <p>
 * This stores the list of items as a string in the database, but returns it as a list of {@link Item}s
 *
 * References: @see "https://stackoverflow.com/questions/44580702/android-room-persistent-library
 * -how-to-insert-class-that-has-a-list-object-fie"
 * "https://medium.com/@toddcookevt/android-room-storing-lists-of-objects-766cca57e3f9"
 * "https://google.github.io/gson/apidocs/com/google/gson/Gson.html"
 */
public class ItemsConverter {

    @TypeConverter
    public static List<Item> toItemList(String itemString) {
        if (itemString == null) {
            return Collections.emptyList();
        }
        // Create a Gson instance
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Item>>() {}.getType();
        // Deserializes the specified Json into the list of items
        return gson.fromJson(itemString, listType);
    }

    @TypeConverter
    public static String toItemString(List<Item> itemList) {
        if (itemList == null) {
            return null;
        }
        // Create a Gson instance
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Item>>() {}.getType();
        // Serializes the list of items into its equivalent Json representation
        return gson.toJson(itemList, listType);
    }
}
