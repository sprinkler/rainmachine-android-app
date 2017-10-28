package com.rainmachine.presentation.screens.physicaltouch;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhysicalTouchView extends LinearLayout {

    @Inject
    PhysicalTouchPresenter presenter;

    @BindView(R.id.iv_device_screenshot)
    ImageView ivDeviceScreenshot;

    public PhysicalTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        if (!isInEditMode()) {
            presenter.attachView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            presenter.init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            presenter.destroy();
        }
    }

    public void updateViews(boolean showScreenshot) {
        ivDeviceScreenshot.setVisibility(showScreenshot ? View.VISIBLE : View.GONE);
    }
}
