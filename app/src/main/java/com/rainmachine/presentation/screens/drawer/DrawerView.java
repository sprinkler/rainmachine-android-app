package com.rainmachine.presentation.screens.drawer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.NonSprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DrawerView extends FrameLayout {

    @Inject
    DrawerPresenter presenter;

    @BindView(R.id.tv_version)
    TextView tvVersion;

    public DrawerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((NonSprinklerActivity) getContext()).inject(this);
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

    @OnClick({R.id.devices, R.id.cloud_accounts, R.id.other_devices})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.devices) {
            presenter.onClickDevices();
        } else if (id == R.id.cloud_accounts) {
            presenter.onClickCloudAccounts();
        } else if (id == R.id.other_devices) {
            presenter.onClickNetworkSettings();
        }
    }

    public void render(String versionName) {
        tvVersion.setText(versionName);
    }
}
