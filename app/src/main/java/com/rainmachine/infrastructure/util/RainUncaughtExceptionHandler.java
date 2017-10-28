package com.rainmachine.infrastructure.util;

import android.content.Context;

import com.rainmachine.presentation.activities.DelegateActivity;

public class RainUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Context context;

    public RainUncaughtExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        context.startActivity(DelegateActivity.getStartIntent(context));

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
