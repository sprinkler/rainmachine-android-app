package com.rainmachine.presentation.screens.hiddendrawer;

import android.support.v4.widget.DrawerLayout;

import com.rainmachine.infrastructure.util.RainApplication;
import com.rainmachine.presentation.activities.BaseActivity;

import io.palaima.debugdrawer.DebugDrawer;
import io.palaima.debugdrawer.commons.BuildModule;
import io.palaima.debugdrawer.commons.DeviceModule;

public class HiddenDrawer {

    private DebugDrawer debugDrawer;

    public HiddenDrawer(final BaseActivity activity) {
        debugDrawer = new DebugDrawer.Builder(activity)
                .modules(
                        new TimberDrawerModule(activity),
                        new CloudDrawerModule(activity),
                        new ScanDrawerModule(activity),
                        new SprinklerBehaviorDrawerModule(activity),
                        new PushDrawerModule(activity),
                        new DeviceModule(activity),
                        new BuildModule(activity)
                )
                .build();
        debugDrawer.setDrawerLockMode(RainApplication.isDebug() ? DrawerLayout.LOCK_MODE_UNLOCKED
                : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void openDrawer() {
        debugDrawer.openDrawer();
    }

    public void onStart() {
        debugDrawer.onStart();
    }

    public void onResume() {
        debugDrawer.onResume();
    }

    public void onPause() {
        debugDrawer.onPause();
    }

    public void onStop() {
        debugDrawer.onStop();
    }
}
