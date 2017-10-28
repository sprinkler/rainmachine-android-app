package com.rainmachine.presentation.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.infrastructure.InfrastructureUtils;
import com.rainmachine.injection.Injector;

import javax.inject.Inject;

import dagger.ObjectGraph;
import timber.log.Timber;

public abstract class SprinklerActivity extends BaseActivity {

    @Inject
    protected Device device;
    @Inject
    protected SprinklerState sprinklerState;

    private ObjectGraph graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(InfrastructureUtils.ACTION_FINISH_ACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(finishReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(finishReceiver);
        // Eagerly clear the reference to the activity graph to allow it to be garbage collected as
        // soon as possible.
        graph = null;
        super.onDestroy();
    }

    protected boolean buildGraphAndInject() {
        ObjectGraph sprinklerGraph = Injector.getSprinklerGraph();
        // The graph might be null in some edge cases
        if (sprinklerGraph != null) {
            graph = sprinklerGraph.plus(getModule());
            graph.inject(this);
            return true;
        } else {
            Timber.w("The sprinkler graph is null for some unknown reason");
            finish();
            return false;
        }
    }

    public void inject(Object object) {
        if (graph != null) {
            graph.inject(object);
        }
    }

    public abstract Object getModule();

    private BroadcastReceiver finishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Received finish activity broadcast");
            finish();
        }
    };
}
