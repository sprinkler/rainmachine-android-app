package com.rainmachine.presentation.screens.wateringduration;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.rainmachine.infrastructure.util.BaseApplication.getContext;

public class WateringDurationActivity extends SprinklerActivity implements
        WateringDurationContract.View {

    private static final int FLIPPER_CHILD_CONTENT = 0;
    private static final int FLIPPER_CHILD_PROGRESS = 1;
    private static final int FLIPPER_CHILD_ERROR = 2;

    @Inject
    WateringDurationContract.Presenter presenter;
    @Inject
    CalendarFormatter calendarFormatter;

    @BindView(R.id.flipper)
    ViewFlipper flipper;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    private WateringDurationAdapter adapter;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, WateringDurationActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_watering_duration);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        presenter.attachView(this);
        presenter.init();
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.destroy();
        }
        super.onDestroy();
    }

    @Override
    public Object getModule() {
        return new WateringDurationModule(this);
    }

    @Override
    public void render(WateringDurationViewModel viewModel) {
        if (viewModel.initialize) {
            recyclerView.addItemDecoration(new DividerItemDecoration(this, null, true));
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                    .VERTICAL, false));
            adapter = new WateringDurationAdapter(this, calendarFormatter, new ArrayList<>(),
                    presenter);
            recyclerView.setAdapter(adapter);
        } else if (viewModel.isProgress) {
            showProgress();
        } else if (viewModel.isError) {
            showError();
        } else if (viewModel.isContent) {
            adapter.setItems(viewModel.sections);
            showContent();
        } else if (viewModel.showZoneDialog) {
            DialogFragment dialog = WateringDurationDialogFragment
                    .newInstance(viewModel.zoneForDialog);
            showDialogSafely(dialog);
        }
    }

    @Override
    public void onDialogWateringDurationPositiveClick(ZoneViewModel zone) {
        presenter.onClickSaveWateringDuration(zone);
    }

    private void showContent() {
        flipper.setDisplayedChild(FLIPPER_CHILD_CONTENT);
    }

    private void showProgress() {
        flipper.setDisplayedChild(FLIPPER_CHILD_PROGRESS);
    }

    private void showError() {
        flipper.setDisplayedChild(FLIPPER_CHILD_ERROR);
    }

    @OnClick(R.id.btn_retry)
    void onClickRetry() {
        presenter.onClickRetry();
    }
}
