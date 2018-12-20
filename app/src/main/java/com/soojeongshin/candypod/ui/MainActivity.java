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

package com.soojeongshin.candypod.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.soojeongshin.candypod.R;
import com.soojeongshin.candypod.ui.podcasts.PodcastsFragment;
import com.soojeongshin.candypod.databinding.ActivityMainBinding;
import com.soojeongshin.candypod.ui.downloads.DownloadsFragment;
import com.soojeongshin.candypod.ui.favorites.FavoritesFragment;
import com.soojeongshin.candypod.ui.settings.SettingsActivity;

import timber.log.Timber;

import static com.soojeongshin.candypod.utilities.Constants.INDEX_ZERO;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    /** This field is used for data binding **/
    private ActivityMainBinding mMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSharedPreferences();

        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mMainBinding.appBarMain.toolbar);

        // Setup Timber
        Timber.plant(new Timber.DebugTree());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mMainBinding.drawerLayout, mMainBinding.appBarMain.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mMainBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mMainBinding.navView.setNavigationItemSelectedListener(this);

        // Register MainActivity as an OnPreferenceChangedListener to receive a callback when a
        // SharedPreference has changed. Please note that we must unregister MainActivity as an
        // OnSharedPreferenceChanged listener in onDestroy to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // Set PodcastsFragment as a default fragment when starting the app
        if (savedInstanceState == null) {
            onNavigationItemSelected(mMainBinding.navView.getMenu().getItem(INDEX_ZERO).setChecked(true));
        }
    }

    @Override
    public void onBackPressed() {
        if (mMainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Replace the fragment using a FragmentManager and Transaction
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_podcasts) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new PodcastsFragment())
                    .commit();
        } else if (id == R.id.nav_favorites) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new FavoritesFragment())
                    .commit();
        } else if (id == R.id.nav_downloads) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new DownloadsFragment())
                    .commit();
        }

        mMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(sharedPreferences);
    }

    private void setTheme(SharedPreferences sharedPreferences) {
        boolean isDarkTheme = sharedPreferences.getBoolean(
                getString(R.string.pref_dark_theme_key),
                getResources().getBoolean(R.bool.pref_dark_theme_default));
        if (isDarkTheme) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_dark_theme_key))) {
            setTheme(sharedPreferences);
            recreate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister MainActivity as an OnPreferenceChangedListener to avoid any memory leaks
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
