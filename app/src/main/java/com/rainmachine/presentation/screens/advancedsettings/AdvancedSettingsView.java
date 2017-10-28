package com.rainmachine.presentation.screens.advancedsettings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.HandPreference;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AdvancedSettingsView extends ViewFlipper implements CompoundButton
        .OnCheckedChangeListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    AdvancedSettingsPresenter presenter;

    @BindView(R.id.check_amazon_alexa)
    CheckBox checkAmazonAlexa;
    @BindView(R.id.check_beta_updates)
    CheckBox checkBetaUpdates;
    @BindView(R.id.check_ssh_access)
    CheckBox checkSshAccess;
    @BindView(R.id.tv_log_level)
    TextView tvLogLevel;
    @BindView(R.id.view_bonjour)
    View viewBonjour;
    @BindView(R.id.check_bonjour)
    CheckBox checkBonjour;
    @BindView(R.id.tv_interface_option)
    TextView tvInterfaceOption;

    public AdvancedSettingsView(Context context, AttributeSet attrs) {
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.check_amazon_alexa) {
            presenter.onCheckedChangedAmazonAlexa(isChecked);
        } else if (id == R.id.check_beta_updates) {
            presenter.onCheckedChangedBetaUpdates(isChecked);
        } else if (id == R.id.check_ssh_access) {
            presenter.onCheckedChangedSshAccess(isChecked);
        } else if (id == R.id.check_bonjour) {
            presenter.onCheckedChangedBonjour(isChecked);
        }
    }

    @OnClick({R.id.view_amazon_alexa, R.id.view_beta_updates, R.id.view_ssh_access,
            R.id.view_log_level, R.id.btn_retry, R.id.view_bonjour, R.id.view_interface_options})
    void onClick(View view) {
        int id = view.getId();
        if (id == R.id.view_amazon_alexa) {
            checkAmazonAlexa.toggle();
        } else if (id == R.id.view_beta_updates) {
            checkBetaUpdates.toggle();
        } else if (id == R.id.view_ssh_access) {
            checkSshAccess.toggle();
        } else if (id == R.id.view_log_level) {
            presenter.onClickLogLevel();
        } else if (id == R.id.btn_retry) {
            presenter.onClickRetry();
        } else if (id == R.id.view_bonjour) {
            checkBonjour.toggle();
        } else if (id == R.id.view_interface_options) {
            presenter.onClickInterfaceOptions();
        }
    }

    void setup(boolean showBonjourService) {
        viewBonjour.setVisibility(showBonjourService ? View.VISIBLE : View.GONE);
    }

    void render(AdvancedSettingsViewModel viewModel) {
        checkAmazonAlexa.setOnCheckedChangeListener(null);
        checkAmazonAlexa.setChecked(viewModel.amazonAlexa);
        checkAmazonAlexa.setOnCheckedChangeListener(this);

        checkBetaUpdates.setOnCheckedChangeListener(null);
        checkBetaUpdates.setChecked(viewModel.betaUpdates);
        checkBetaUpdates.setOnCheckedChangeListener(this);

        checkSshAccess.setOnCheckedChangeListener(null);
        checkSshAccess.setChecked(viewModel.sshAccess);
        checkSshAccess.setOnCheckedChangeListener(this);

        checkBonjour.setOnCheckedChangeListener(null);
        checkBonjour.setChecked(viewModel.bonjourService);
        checkBonjour.setOnCheckedChangeListener(this);

        tvLogLevel.setText(viewModel.logLevel == LogLevel.DEBUG ? getContext().getString(R.string
                .advanced_settings_log_debug) : (viewModel.logLevel == LogLevel.NORMAL ?
                getContext()
                        .getString(R.string.advanced_settings_log_normal) : getContext().getString(R
                .string.advanced_settings_log_warnings)));

        int handPreference = viewModel.handPreference == HandPreference.RIGHT_HAND
                ? R.string.advanced_settings_right_hand : R.string.advanced_settings_left_hand;
        tvInterfaceOption.setText(getContext().getString(handPreference));
    }

    void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }
}
