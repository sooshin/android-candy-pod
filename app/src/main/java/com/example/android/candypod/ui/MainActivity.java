package com.example.android.candypod.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.candypod.R;
import com.example.android.candypod.databinding.ActivityMainBinding;
import com.example.android.candypod.ui.downloads.DownloadsFragment;
import com.example.android.candypod.ui.favorites.FavoritesFragment;
import com.example.android.candypod.ui.playlists.PlaylistsFragment;
import com.example.android.candypod.ui.podcasts.PodcastsFragment;

import timber.log.Timber;

import static com.example.android.candypod.utilities.Constants.INDEX_ZERO;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /** This field is used for data binding **/
    private ActivityMainBinding mMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        } else if (id == R.id.nav_playlists) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new PlaylistsFragment())
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
}
