package com.rainmachine.presentation.screens.settings;

import com.rainmachine.domain.util.Features;
import com.rainmachine.presentation.screens.about.AboutActivity;
import com.rainmachine.presentation.screens.dashboardgraphs.DashboardGraphsActivity;
import com.rainmachine.presentation.screens.help.HelpActivity;
import com.rainmachine.presentation.screens.main.MainActivity;
import com.rainmachine.presentation.screens.pushnotifications.PushNotificationsActivity;
import com.rainmachine.presentation.screens.raindelay.RainDelayActivity;
import com.rainmachine.presentation.screens.rainsensor.RainSensorActivity;
import com.rainmachine.presentation.screens.restrictions.RestrictionsActivity;
import com.rainmachine.presentation.screens.systemsettings.SystemSettingsActivity;
import com.rainmachine.presentation.screens.wateringhistory.WateringHistoryActivity;
import com.rainmachine.presentation.screens.weathersettings.WeatherSettingsActivity;
import com.rainmachine.presentation.util.BasePresenter;

public class SettingsPresenter extends BasePresenter<SettingsView> {

    private MainActivity activity;
    private Features features;

    public SettingsPresenter(MainActivity activity, Features features) {
        this.activity = activity;
        this.features = features;
    }

    @Override
    public void attachView(SettingsView view) {
        super.attachView(view);

        view.setupViews(features.hasRestrictionsFunctionality(), features.showWateringHistory(),
                features.showSnoozePhrasing(), features.showWeather(), features
                        .showFullSettingsSubtitle(), features.showGraphs(), features
                        .showNotifications(), features.showRainSensor());
    }

    public void onClick(AdapterItemSetting item) {
        switch (item.id) {
            case SettingsView.ITEM_ID_RAIN_DELAY: {
                RainDelayActivity.start(activity);
                break;
            }
            case SettingsView.ITEM_ID_RESTRICTIONS: {
                activity.startActivity(RestrictionsActivity.getStartIntent(activity));
                break;
            }
            case SettingsView.ITEM_ID_RAIN_SENSOR: {
                RainSensorActivity.start(activity);
                break;
            }
            case SettingsView.ITEM_ID_WEATHER: {
                activity.startActivity(WeatherSettingsActivity.getStartIntent(activity));
                break;
            }
            case SettingsView.ITEM_ID_DEVICE_SETTINGS: {
                activity.startActivity(SystemSettingsActivity.getStartIntent(activity));
                break;
            }
            case SettingsView.ITEM_ID_ABOUT: {
                activity.startActivity(AboutActivity.getStartIntent(activity));
                break;
            }
            case SettingsView.ITEM_ID_WATERING_HISTORY: {
                activity.startActivity(WateringHistoryActivity.getStartIntent(activity));
                break;
            }
            case SettingsView.ITEM_ID_DASHBOARD_GRAPHS: {
                activity.startActivity(DashboardGraphsActivity.getStartIntent(activity));
                break;
            }
            case SettingsView.ITEM_ID_HELP: {
                activity.startActivity(HelpActivity.getStartIntent(activity));
                break;
            }
            case SettingsView.ITEM_ID_NOTIFICATIONS: {
                activity.startActivity(PushNotificationsActivity.getStartIntent(activity));
                break;
            }
        }
    }
}
