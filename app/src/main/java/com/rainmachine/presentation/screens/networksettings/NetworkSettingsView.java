package com.rainmachine.presentation.screens.networksettings;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.ScrollView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.NonSprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NetworkSettingsView extends ScrollView implements CompoundButton
        .OnCheckedChangeListener {

    @Inject
    protected NetworkSettingsPresenter presenter;

    @BindView(R.id.toggle_local_discovery)
    SwitchCompat toggleLocalDiscovery;

    public NetworkSettingsView(Context context, AttributeSet attrs) {
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.toggle_local_discovery) {
            presenter.onToggleLocalDiscovery(isChecked);
        }
    }

    @OnClick(R.id.view_port_forward_settings)
    public void onClickPortForwardSettings() {
        presenter.onClickDirectAccess();
    }

    @OnClick(R.id.view_local_discovery)
    public void onClickLocalDiscovery() {
        presenter.onClickLocalDiscovery();
    }

    public void updateLocalDiscovery(boolean localDiscoveryEnabled) {
        toggleLocalDiscovery.setOnCheckedChangeListener(null);
        toggleLocalDiscovery.setChecked(localDiscoveryEnabled);
        toggleLocalDiscovery.setOnCheckedChangeListener(this);
    }

    public void render(boolean localDiscoveryEnabled) {
        updateLocalDiscovery(localDiscoveryEnabled);
    }
}
