package com.rainmachine.presentation.screens.hours;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HoursActivity extends SprinklerActivity {

    static final int REQ_CODE_HOURS_DETAILS = 0;

    @Inject
    HoursPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ActionMode mode;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, HoursActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_hours);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.all_hours_title);

        boolean startActionMode = false;
        if (savedInstanceState != null) {
            startActionMode = savedInstanceState.getBoolean("isActionMode");
        }
        if (startActionMode) {
            showActionMode();
        }
    }

    public Object getModule() {
        return new HoursModule(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hours, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_bulk_edit) {
            showActionMode();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isActionMode", mode != null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_HOURS_DETAILS && resultCode == Activity.RESULT_OK) {
            presenter.onComingBackFromAddEditHour();
        }
    }

    public void showActionMode() {
        if (mode == null) {
            mode = startSupportActionMode(new ModeCallback());
        }
    }

    public void updateCabTitle(ActionMode mode, SparseBooleanArray items) {
        int count = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.valueAt(i)) {
                count++;
            }
        }
        if (count > 0) {
            mode.setTitle(getResources().getQuantityString(R.plurals.hours_restrictions_selected,
                    count, count));
        } else {
            mode.setTitle(R.string.hours_select_restrictions);
        }
    }

    public ActionMode getActionMode() {
        return mode;
    }

    private final class ModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.hours_context, menu);
            presenter.onCreateActionMode(mode);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            presenter.onDestroyActionMode();
            HoursActivity.this.mode = null;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_delete) {
                presenter.onActionItemClicked();
                HoursActivity.this.mode.finish();
                return true;
            }
            return false;
        }
    }
}