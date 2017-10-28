package com.rainmachine.presentation.screens.stats.dashboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import timber.log.Timber;

/*
** Workaround for Android bug: IllegalArgumentException: pointerIndex out of range
 */
public class CustomScrollView extends ScrollView {

    private boolean enableScrolling = true;

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return enableScrolling && super.onTouchEvent(ev);
        } catch (IllegalArgumentException iae) {
            Timber.w(iae, iae.getMessage());
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return enableScrolling && super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException iae) {
            Timber.w(iae, iae.getMessage());
        }
        return false;
    }

    public boolean isEnableScrolling() {
        return enableScrolling;
    }

    public void setEnableScrolling(boolean enableScrolling) {
        this.enableScrolling = enableScrolling;
    }
}
