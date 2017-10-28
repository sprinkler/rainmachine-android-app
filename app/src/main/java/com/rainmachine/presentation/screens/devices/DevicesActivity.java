package com.rainmachine.presentation.screens.devices;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.DrawerActivity;
import com.rainmachine.presentation.screens.hiddendrawer.HiddenDrawer;
import com.rainmachine.presentation.screens.offline.OfflineActivity;
import com.rainmachine.presentation.screens.sprinklerdelegate.SprinklerDelegateActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DevicesActivity extends DrawerActivity implements DevicesContract.Container {

    @Inject
    DevicesContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private HiddenDrawer hiddenDrawer;

    public static Intent getStartIntent(Context context, boolean restart) {
        Intent intent = new Intent(context, DevicesActivity.class);
        if (restart) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGraphAndInject();
        setContentView(R.layout.activity_devices);
        ButterKnife.bind(this);
        linkToolbar(toolbar);

        drawerHelper.setupDrawer(toolbar);
        setupHiddenDrawer();
    }

    public Object getModule() {
        return new DevicesModule(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.devices, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            presenter.onClickRefreshManual();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        hiddenDrawer.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
        hiddenDrawer.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stop();
        hiddenDrawer.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        hiddenDrawer.onStop();
    }

    @Override
    public void goToDeviceScreen() {
        startActivity(SprinklerDelegateActivity.getStartIntent(this));
    }

    @Override
    public void goToOfflineScreen(long _deviceDatabaseId, String deviceName) {
        startActivity(OfflineActivity.getStartIntent(this, _deviceDatabaseId, deviceName));
    }

    private void setupHiddenDrawer() {
        hiddenDrawer = new HiddenDrawer(this);
        toolbar.setOnClickListener(toolbarClickListener);
    }

    private View.OnClickListener toolbarClickListener = new View.OnClickListener() {

        private static final int NUM_SUCCESSIVE_CLICKS = 11;

        private int numClicks;
        private long lastClickTime;

        @Override
        public void onClick(View v) {
            if (lastClickTime >= System.currentTimeMillis() - 900) {
                numClicks++;
            } else {
                numClicks = 1;
            }
            lastClickTime = System.currentTimeMillis();
            if (numClicks >= NUM_SUCCESSIVE_CLICKS) {
                hiddenDrawer.openDrawer();
                numClicks = 0;
                lastClickTime = 0;
            }
        }
    };
}
