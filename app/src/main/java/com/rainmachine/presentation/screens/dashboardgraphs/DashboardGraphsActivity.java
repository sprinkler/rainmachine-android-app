package com.rainmachine.presentation.screens.dashboardgraphs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DashboardGraphsActivity extends SprinklerActivity {

    @Inject
    DashboardGraphsPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, DashboardGraphsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_dashboard_graphs);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stop();
    }

    public Object getModule() {
        return new DashboardGraphsModule(this);
    }
}
