package com.rainmachine.presentation.screens.weathersettings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.screens.weathersensitivity.WeatherSensitivityActivity;
import com.rainmachine.presentation.screens.weathersourcedetails.WeatherSourceDetailsActivity;
import com.rainmachine.presentation.screens.weathersources.WeatherSourcesActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherSettingsActivity extends SprinklerActivity implements WeatherSettingsContract
        .Container {

    @Inject
    WeatherSettingsContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean showDefaultsMenuItem;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, WeatherSettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.all_weather);
    }

    public Object getModule() {
        return new WeatherSettingsModule(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather, menu);
        MenuItem defaultsMenuItem = menu.findItem(R.id.menu_defaults);
        defaultsMenuItem.setVisible(showDefaultsMenuItem);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_defaults) {
            presenter.onClickDefaults();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void hideDefaultsMenuItem() {
        showDefaultsMenuItem = false;
        supportInvalidateOptionsMenu();
    }

    @Override
    public void showDefaultsMenuItem() {
        showDefaultsMenuItem = true;
        supportInvalidateOptionsMenu();
    }

    @Override
    public void goToWeatherSourcesScreen() {
        startActivity(WeatherSourcesActivity.getStartIntent(this));
    }

    @Override
    public void goToWeatherSensitivityScreen() {
        startActivity(WeatherSensitivityActivity.getStartIntent(this));
    }

    @Override
    public void goToWeatherSourceDetailsScreen(long parserId) {
        Intent intent = WeatherSourceDetailsActivity.getStartIntent(this, parserId);
        startActivity(intent);
    }

    @Override
    public void showDefaultsDialog() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance(0, null,
                getString(R.string.weather_settings_are_you_sure_defaults),
                getString(R.string.all_yes), getString(R.string.all_no));
        showDialogSafely(dialog);
    }
}
