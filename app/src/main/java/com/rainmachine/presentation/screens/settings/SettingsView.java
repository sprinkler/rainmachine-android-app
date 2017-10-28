package com.rainmachine.presentation.screens.settings;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class SettingsView extends RecyclerView {

    public static final int ITEM_ID_RAIN_DELAY = 1;
    public static final int ITEM_ID_RESTRICTIONS = 2;
    public static final int ITEM_ID_WEATHER = 3;
    public static final int ITEM_ID_DEVICE_SETTINGS = 4;
    public static final int ITEM_ID_ABOUT = 5;
    public static final int ITEM_ID_WATERING_HISTORY = 6;
    public static final int ITEM_ID_DASHBOARD_GRAPHS = 7;
    public static final int ITEM_ID_HELP = 8;
    public static final int ITEM_ID_NOTIFICATIONS = 9;
    public static final int ITEM_ID_RAIN_SENSOR = 10;

    @Inject
    SettingsPresenter presenter;

    public SettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        if (!isInEditMode()) {
            presenter.attachView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            presenter.init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            presenter.destroy();
        }
    }

    public void setupViews(boolean showRestrictions, boolean showWateringHistory,
                           boolean showSnoozePhrasing, boolean showWeather, boolean
                                   showNewSubtitle, boolean showDashboardGraphs, boolean
                                   showNotifications, boolean showRainSensor) {
        addItemDecoration(new DividerItemDecoration(getContext(), null, true));
        setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,
                false));
        SettingAdapter adapter = new SettingAdapter(getContext(), presenter, new ArrayList<>());
        setAdapter(adapter);

        List<AdapterItemSetting> items = new ArrayList<>();
        AdapterItemSetting item;
        if (showNotifications) {
            item = new AdapterItemSetting();
            item.id = ITEM_ID_NOTIFICATIONS;
            item.name = getContext().getString(R.string.all_notifications);
            item.useOneLine = true;
            items.add(item);
        }
        if (showWateringHistory) {
            item = new AdapterItemSetting();
            item.id = ITEM_ID_WATERING_HISTORY;
            item.name = getContext().getString(R.string.all_watering_history);
            item.useOneLine = true;
            items.add(item);
        }
        if (showDashboardGraphs) {
            item = new AdapterItemSetting();
            item.id = ITEM_ID_DASHBOARD_GRAPHS;
            item.name = getContext().getString(R.string.all_dashboard_graphs);
            item.description = getContext().getString(R.string.settings_show_graph);
            item.useOneLine = false;
            items.add(item);
        }
        item = new AdapterItemSetting();
        item.id = ITEM_ID_RAIN_DELAY;
        item.name = showSnoozePhrasing ? getContext().getString(R.string.all_snooze) : getContext()
                .getString(R.string.settings_rain_delay);
        item.description = getContext().getString(R.string.settings_help_snooze);
        item.useOneLine = false;
        items.add(item);
        if (showRestrictions) {
            item = new AdapterItemSetting();
            item.id = ITEM_ID_RESTRICTIONS;
            item.name = getContext().getString(R.string.all_restrictions);
            item.description = getContext().getString(R.string.settings_help_restrictions);
            item.useOneLine = false;
            items.add(item);
        }
        if (showRainSensor) {
            item = new AdapterItemSetting();
            item.id = ITEM_ID_RAIN_SENSOR;
            item.name = getContext().getString(R.string.all_rain_sensor);
            item.useOneLine = true;
            items.add(item);
        }
        if (showWeather) {
            item = new AdapterItemSetting();
            item.id = ITEM_ID_WEATHER;
            item.name = getContext().getString(R.string.all_weather);
            item.description = getContext().getString(R.string.settings_help_weather);
            item.useOneLine = false;
            items.add(item);
        }
        item = new AdapterItemSetting();
        item.id = ITEM_ID_DEVICE_SETTINGS;
        item.name = getContext().getString(R.string.all_device_settings);
        item.description = showNewSubtitle ? getContext().getString(R.string
                .settings_device_subtitle) :
                getContext().getString(R.string.settings_device_subtitle3);
        items.add(item);
        item = new AdapterItemSetting();
        item.id = ITEM_ID_ABOUT;
        item.name = getContext().getString(R.string.all_about);
        item.description = showNewSubtitle ? getContext().getString(R.string
                .settings_about_subtitle) :
                getContext().getString(R.string.settings_about_subtitle3);
        items.add(item);
        item = new AdapterItemSetting();
        item.id = ITEM_ID_HELP;
        item.name = getContext().getString(R.string.all_help);
        item.useOneLine = true;
        items.add(item);
        adapter.setItems(items);
    }
}
