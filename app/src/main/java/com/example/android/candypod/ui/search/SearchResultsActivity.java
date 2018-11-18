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

package com.example.android.candypod.ui.search;

import android.app.SearchManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.android.candypod.R;
import com.example.android.candypod.databinding.ActivitySearchResultsBinding;

/**
 * Reference: @see "https://developer.android.com/training/search/setup#create-sa"
 */
public class SearchResultsActivity extends AppCompatActivity {

    /** This field is used for data binding */
    private ActivitySearchResultsBinding mSearchBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_search_results);

        handleIntent(getIntent());
    }

    /**
     * Since this searchable activity launches in single top mode (android:launchMode="singleTop"),
     * also handle the ACTION_SEARCH intent in the onNewIntent() method.
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //
            Toast.makeText(this, "query: " + query, Toast.LENGTH_SHORT).show();
        }
    }
}
