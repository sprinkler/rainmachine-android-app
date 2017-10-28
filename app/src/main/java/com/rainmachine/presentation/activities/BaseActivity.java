package com.rainmachine.presentation.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.parceler.Parcels;

import java.util.UUID;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseActivity extends AppCompatActivity {

    private boolean wasCalledSaveInstanceState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("CREATE SCREEN [" + getClass().getSimpleName() + "]"
                + (savedInstanceState != null ? " -> re-initialized" : ""));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.i("RESUME SCREEN [" + getClass().getSimpleName() + "]");
        wasCalledSaveInstanceState = false;
    }

    @Override
    protected void onPause() {
        Timber.i("PAUSE SCREEN [" + getClass().getSimpleName() + "]");
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        wasCalledSaveInstanceState = true;
    }

    @Override
    protected void onDestroy() {
        Timber.i("DESTROY SCREEN [" + getClass().getSimpleName() + "]");
        super.onDestroy();
    }

    @NonNull
    @Override
    public ActionBar getSupportActionBar() {
        // Small hack here so that Lint does not warn me in every single activity about null
        // action bar
        return super.getSupportActionBar();
    }

    public boolean canShowDialogs() {
        return !wasCalledSaveInstanceState;
    }

    protected void addFragment(int resId, Fragment fragment) {
        fragment.setArguments(intentToFragmentArguments(getIntent()));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(resId, fragment);
        ft.commit();
    }

    private Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable("_uri", data);
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    public <T> T getParcelable(String extra) {
        return getParcelable(getIntent(), extra);
    }

    public static <T> T getParcelable(Intent intent, String extra) {
        return Parcels.unwrap(intent.getParcelableExtra(extra));
    }

    public void setParcelable(String extra, Object obj) {
        getIntent().putExtra(extra, Parcels.wrap(obj));
    }

    protected void linkToolbar(Toolbar toolbar) {
        try {
            setSupportActionBar(toolbar);
        } catch (Throwable throwable) {
            // Samsung 4.2.2 bug
            Timber.e(throwable, "Problem with toolbar");
        }
    }

    public void showDialogSafely(DialogFragment dialogFragment) {
        if (canShowDialogs()) {
            dialogFragment.show(getSupportFragmentManager(), UUID.randomUUID().toString());
        }
    }
}
