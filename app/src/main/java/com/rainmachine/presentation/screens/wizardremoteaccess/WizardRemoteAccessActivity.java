package com.rainmachine.presentation.screens.wizardremoteaccess;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.ExtraConstants;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WizardRemoteAccessActivity extends SprinklerActivity {

    @Inject
    WizardRemoteAccessPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean isInProgress;

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, WizardRemoteAccessActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(ExtraConstants.IS_WIZARD, true);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_wizard_remote_access);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(getString(R.string.all_set_remote_access));

        isInProgress = false;
        if (savedInstanceState != null) {
            isInProgress = savedInstanceState.getBoolean("isInProgress");
        }
    }

    public Object getModule() {
        return new WizardRemoteAccessModule(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isInProgress", isInProgress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.skip, menu);
        MenuItem skipItem = menu.findItem(R.id.menu_skip);
        skipItem.setVisible(!isInProgress);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_skip) {
            presenter.onClickSkip();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
