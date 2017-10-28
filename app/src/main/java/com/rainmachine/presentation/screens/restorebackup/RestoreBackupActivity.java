package com.rainmachine.presentation.screens.restorebackup;

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

public class RestoreBackupActivity extends SprinklerActivity {

    @Inject
    RestoreBackupPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean isWizard;

    public static Intent getStartIntent(Context context, boolean isWizard) {
        Intent intent = new Intent(context, RestoreBackupActivity.class);
        intent.putExtra(ExtraConstants.IS_WIZARD, isWizard);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_restore_backup);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.all_restore_backup);

        isWizard = getIntent().getBooleanExtra(ExtraConstants.IS_WIZARD, false);
    }

    public Object getModule() {
        return new RestoreBackupModule(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isWizard) {
            getMenuInflater().inflate(R.menu.skip, menu);
        }
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
}
