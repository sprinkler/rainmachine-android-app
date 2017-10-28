package com.rainmachine.presentation.screens.hiddendrawer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.rainmachine.R;
import com.rainmachine.data.local.pref.util.IntPreference;
import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.injection.Injector;
import com.rainmachine.presentation.screens.devices.DevicesActivity;
import com.rainmachine.presentation.widgets.SpinnerUserSelection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.palaima.debugdrawer.base.DebugModule;
import timber.log.Timber;

public class ScanDrawerModule implements DebugModule {

    @Inject
    DeviceRepository deviceRepository;
    @Inject
    @Named("device_cache_timeout_pref")
    IntPreference deviceCacheTimeout;

    @BindView(R.id.spinner)
    Spinner spinner;

    private Activity activity;
    private List<DeviceCacheTimeoutItem> items;

    public ScanDrawerModule(Activity activity) {
        this.activity = activity;
        Injector.inject(this);

        items = new ArrayList<>();
        items.add(new DeviceCacheTimeoutItem("5 seconds", 5));
        items.add(new DeviceCacheTimeoutItem("10 seconds", 10));
        items.add(new DeviceCacheTimeoutItem("30 seconds", 30));
        items.add(new DeviceCacheTimeoutItem("60 seconds", DomainUtils.DEVICE_CACHE_TIMEOUT));
        items.add(new DeviceCacheTimeoutItem("2 minutes", 2 * 60));
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.hidden_drawer_module_scan, parent, false);
        ButterKnife.bind(this, view);
        setup();
        return view;
    }

    private void setup() {
        final GenericSpinnerAdapter<DeviceCacheTimeoutItem> adapter = new GenericSpinnerAdapter<>
                (activity, items);
        spinner.setAdapter(adapter);

        SpinnerUserSelection userSelection = new SpinnerUserSelection(position -> {
            DeviceCacheTimeoutItem selected = adapter.getItem(position);
            Timber.d("Selected %s %d %d", selected.name, selected.seconds, position);
            deviceCacheTimeout.set(selected.seconds);
            // Remove all automatic devices discovered until now
            deviceRepository.deleteAllLocalDiscoveredDevices();
            Injector.createGraphAndInjectApp();
            // Restart app in order to do proper dependency injection
            activity.startActivity(DevicesActivity.getStartIntent(activity, true));
        });
        spinner.setOnTouchListener(userSelection);
        spinner.setOnItemSelectedListener(userSelection);
        setSelection();
    }

    private void setSelection() {
        int checkedItemPosition = 0;
        for (int i = 0; i < items.size(); i++) {
            DeviceCacheTimeoutItem timeout = items.get(i);
            if (timeout.seconds == deviceCacheTimeout.get()) {
                checkedItemPosition = i;
                break;
            }
        }
        spinner.setSelection(checkedItemPosition);
    }

    @Override
    public void onOpened() {
        setSelection();
    }

    @Override
    public void onClosed() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }
}
