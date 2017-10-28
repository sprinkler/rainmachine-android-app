package com.rainmachine.presentation.activities;

import com.rainmachine.injection.Injector;

import dagger.ObjectGraph;

public abstract class NonSprinklerActivity extends BaseActivity {

    private ObjectGraph graph;

    @Override
    protected void onDestroy() {
        // Eagerly clear the reference to the activity graph to allow it to be garbage collected as
        // soon as possible.
        graph = null;
        super.onDestroy();
    }

    protected void buildGraphAndInject() {
        graph = Injector.graph.plus(getModule());
        graph.inject(this);
    }

    public void inject(Object object) {
        graph.inject(object);
    }

    public abstract Object getModule();
}
