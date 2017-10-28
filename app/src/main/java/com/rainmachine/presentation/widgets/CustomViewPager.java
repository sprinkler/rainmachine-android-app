package com.rainmachine.presentation.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import timber.log.Timber;

public class CustomViewPager extends ViewPager {

    private boolean enabled;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return this.enabled && super.onTouchEvent(event);
        } catch (IllegalArgumentException iae) {
            Timber.w(iae, iae.getMessage());
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            return this.enabled && super.onInterceptTouchEvent(event);
        } catch (IllegalArgumentException iae) {
            Timber.w(iae, iae.getMessage());
        }
        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
