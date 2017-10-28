package com.rainmachine.presentation.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class ForegroundDetector implements Application.ActivityLifecycleCallbacks {

    private int refs;
    private boolean isChangingOrientation;
    private PublishSubject<ForegroundState> subject = PublishSubject.create();

    public ForegroundDetector(Application app) {
        app.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Timber.d("activity started");
        if (++refs == 1 && !isChangingOrientation) {
            subject.onNext(ForegroundState.APP_BECAME_FOREGROUND);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Timber.d("activity resumed");
        subject.onNext(ForegroundState.ACTIVITY_RESUMED);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Timber.d("activity paused");
        subject.onNext(ForegroundState.ACTIVITY_PAUSED);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Timber.d("activity stopped");
        isChangingOrientation = activity.isChangingConfigurations();
        if (--refs == 0 && !isChangingOrientation) {
            subject.onNext(ForegroundState.APP_BECAME_BACKGROUND);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    public boolean isForeground() {
        return refs > 0;
    }

    public boolean isBackground() {
        return refs == 0 && !isChangingOrientation;
    }

    public Observable<ForegroundState> refresher() {
        return subject;
    }

    public enum ForegroundState {
        APP_BECAME_FOREGROUND, APP_BECAME_BACKGROUND, ACTIVITY_RESUMED,
        ACTIVITY_PAUSED
    }
}
