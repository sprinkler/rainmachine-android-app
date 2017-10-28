package com.rainmachine.presentation.screens.remoteaccess;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.ExtraConstants;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RemoteAccessActivity extends SprinklerActivity {

    @Inject
    RemoteAccessPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean isInProgress;

    public static Intent getStartIntent(Context context, boolean isWizard) {
        Intent intent = new Intent(context, RemoteAccessActivity.class);
        if (isWizard) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }
        intent.putExtra(ExtraConstants.IS_WIZARD, isWizard);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_remote_access);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(getString(R.string.all_set_remote_access));

        isInProgress = false;
        if (savedInstanceState != null) {
            isInProgress = savedInstanceState.getBoolean("isInProgress");
        }
    }

    @Override
    public Object getModule() {
        return new RemoteAccessModule(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isInProgress", isInProgress);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    public void showProgress() {
        isInProgress = true;
    }

    public void showContent() {
        isInProgress = false;
    }

    public void showError() {
        isInProgress = false;
    }
}
