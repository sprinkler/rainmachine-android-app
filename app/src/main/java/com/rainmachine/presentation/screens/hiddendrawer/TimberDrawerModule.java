package com.rainmachine.presentation.screens.hiddendrawer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.rainmachine.R;
import com.rainmachine.infrastructure.util.RainApplication;
import com.rainmachine.infrastructure.util.log.LogDialog;
import com.rainmachine.injection.Injector;
import com.rainmachine.presentation.screens.devices.DevicesActivity;
import com.rainmachine.presentation.widgets.SpinnerUserSelection;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.palaima.debugdrawer.base.DebugModule;

class TimberDrawerModule implements DebugModule {

    @BindView(R.id.spinner)
    Spinner spinner;

    private Activity activity;

    TimberDrawerModule(Activity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull final ViewGroup parent) {
        View view = inflater.inflate(R.layout.hidden_drawer_module_timber, parent, false);
        ButterKnife.bind(this, view);
        setup();
        return view;
    }

    private void setup() {
        List<LoggingSettingItem> list = new ArrayList<>();
        list.add(new LoggingSettingItem(false));
        list.add(new LoggingSettingItem(true));

        final GenericSpinnerAdapter<LoggingSettingItem> adapter = new GenericSpinnerAdapter<>
                (activity, list);
        spinner.setAdapter(adapter);

        SpinnerUserSelection userSelection = new SpinnerUserSelection(position -> {
            spinner.setTag(position);
            LoggingSettingItem selected = adapter.getItem(position);
            RainApplication.setDebugLogging(selected.enabled);
            Injector.createGraphAndInjectApp();

            // Restart app in order to do proper dependency injection
            activity.startActivity(DevicesActivity.getStartIntent(activity, true));
        });
        spinner.setOnTouchListener(userSelection);
        spinner.setOnItemSelectedListener(userSelection);
        setSelection();
    }

    private void setSelection() {
        int checkedItemPosition = RainApplication.isDebugLogging() ? 1 : 0;
        spinner.setSelection(checkedItemPosition);
    }

    @OnClick(R.id.btn_show_logs)
    void onClickShowLogs() {
        new LogDialog(activity).show();
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
