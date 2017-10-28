package com.rainmachine.presentation.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.rainmachine.R;

public abstract class DrawerActivity extends NonSprinklerActivity {

    protected DrawerHelper drawerHelper = new DrawerHelper(this);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handledDrawer = drawerHelper.onOptionsItemSelected(item);
        return handledDrawer || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerHelper.onPostCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerHelper.onConfigurationChanged(newConfig);
    }

    public void onNewActivity() {
        drawerHelper.closeDrawer(GravityCompat.START);
    }

    public void onCloseDrawer() {
        drawerHelper.closeDrawer(GravityCompat.START);
    }

    public static class DrawerHelper {

        private BaseActivity activity;
        private DrawerLayout drawerLayout;
        private ActionBarDrawerToggle drawerToggle;

        DrawerHelper(BaseActivity activity) {
            this.activity = activity;
        }

        public void setupDrawer(Toolbar toolbar) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setHomeButtonEnabled(true);

            drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);

            // ActionBarDrawerToggle ties together the the proper interactions
            // between the sliding drawer and the action bar app icon
            drawerToggle = new ActionBarDrawerToggle(
                    activity,                  /* host Activity */
                    drawerLayout,         /* DrawerLayout object */
                    toolbar,
                    R.string.drawer_open,  /* "open drawer" description for accessibility */
                    R.string.drawer_close  /* "close drawer" description for accessibility */
            ) {
                @Override
                public void onDrawerClosed(View view) {
                    activity.supportInvalidateOptionsMenu(); // creates call to
                    // onPrepareOptionsMenu()
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    activity.supportInvalidateOptionsMenu(); // creates call to
                    // onPrepareOptionsMenu()
                }
            };
            drawerLayout.addDrawerListener(drawerToggle);
        }

        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                if (isDrawerOpen(GravityCompat.START)) {
                    closeDrawer(GravityCompat.START);
                } else {
                    openDrawer(GravityCompat.START);
                }
                return true;
            }
            return false;
        }

        void closeDrawer(int gravity) {
            drawerLayout.closeDrawer(gravity);
        }

        void onPostCreate() {
            drawerToggle.syncState();
        }

        void onConfigurationChanged(Configuration newConfig) {
            // Pass any configuration change to the drawer toggle
            drawerToggle.onConfigurationChanged(newConfig);
        }

        private boolean isDrawerOpen(int gravity) {
            return drawerLayout.isDrawerOpen(gravity);
        }

        private void openDrawer(int gravity) {
            drawerLayout.openDrawer(gravity);
        }
    }
}
