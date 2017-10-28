package com.rainmachine.presentation.screens.systemsettings;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Truss;
import com.rainmachine.presentation.util.ViewUtils;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SystemSettingsView extends ViewFlipper implements CompoundButton
        .OnCheckedChangeListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    SystemSettingsPresenter presenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.tv_units)
    TextView tvUnits;
    @BindView(R.id.check_hour_format)
    CheckBox checkHourFormat;
    @BindView(R.id.view_hour_format)
    ViewGroup viewHourFormat;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.view_network_settings)
    ViewGroup viewNetworkSettings;
    @BindView(R.id.view_location_settings)
    ViewGroup viewLocationSettings;
    @BindView(R.id.view_device_name)
    ViewGroup viewDeviceName;
    @BindView(R.id.view_timezone)
    ViewGroup viewTimezone;
    @BindView(R.id.tv_device_name)
    TextView tvDeviceName;
    @BindView(R.id.tv_timezone)
    TextView tvTimezone;
    @BindView(R.id.tv_email)
    TextView tvEmail;
    @BindView(R.id.view_remote_access)
    ViewGroup viewRemoteAccess;
    @BindView(R.id.tv_sprinkler_address)
    TextView tvSprinklerAddress;
    @BindView(R.id.view_reset_defaults)
    ViewGroup viewResetDefaults;
    @BindView(R.id.container_pin)
    ViewGroup viewContainerPin;
    @BindView(R.id.container_restore)
    ViewGroup viewContainerRestore;
    @BindView(R.id.view_advanced_settings)
    ViewGroup viewAdvancedSettings;
    @BindView(R.id.view_mini8_settings)
    ViewGroup viewMini8Settings;
    @BindView(R.id.view_reboot)
    ViewGroup viewReboot;
    @BindView(R.id.tv_restore_subtitle)
    TextView tvRestoreSubtitle;
    @BindView(R.id.container_advanced)
    ViewGroup viewContainerAdvanced;
    @BindView(R.id.container_reboot_reset)
    ViewGroup viewContainerRebootReset;

    public SystemSettingsView(Context context, AttributeSet attrs) {
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

    @OnClick({R.id.view_units, R.id.view_date, R.id.view_time, R.id.view_hour_format,
            R.id.view_network_settings, R.id.view_location_settings,
            R.id.view_device_name, R.id.view_timezone, R.id.view_remote_access, R.id
            .view_restore_backup, R.id.view_reset_defaults, R.id.view_generate_pin, R.id
            .view_advanced_settings, R.id.view_mini8_settings, R.id.view_reboot, R.id
            .view_software_update})
    public void onClickedOption(View view) {
        int id = view.getId();
        if (id == R.id.view_network_settings) {
            presenter.onClickNetworkSettings();
            return;
        }
        if (id == R.id.view_reset_defaults) {
            presenter.onClickResetDefaults();
            return;
        }
        if (id == R.id.view_location_settings) {
            presenter.onClickLocationSettings();
            return;
        }
        if (id == R.id.view_device_name) {
            presenter.onClickDeviceName();
            return;
        }
        if (id == R.id.view_timezone) {
            presenter.onClickTimezone();
            return;
        }
        if (id == R.id.view_remote_access) {
            presenter.onClickRemoteAccess();
            return;
        }
        if (id == R.id.view_restore_backup) {
            presenter.onClickRestoreBackup();
            return;
        }
        if (id == R.id.view_software_update) {
            presenter.onClickSoftwareUpdate();
            return;
        }
        if (id == R.id.view_units) {
            presenter.onClickUnits();
        } else if (id == R.id.view_date) {
            presenter.onClickDate();
        } else if (id == R.id.view_time) {
            presenter.onClickTime();
        } else if (id == R.id.view_hour_format) {
            checkHourFormat.toggle();
        } else if (id == R.id.view_generate_pin) {
            presenter.onClickGeneratePin();
        } else if (id == R.id.view_advanced_settings) {
            presenter.onClickAdvancedSettings();
        } else if (id == R.id.view_mini8_settings) {
            presenter.onClickMini8Settings();
        } else if (id == R.id.view_reboot) {
            presenter.onClickReboot();
        }
    }

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.onRefresh();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        presenter.onCheckedChanged24HourFormat(checked);
    }

    public void updateTime(SystemSettingsViewModel viewModel) {
        tvTime.setText(CalendarFormatter.hourMinColon(viewModel.sprinklerLocalDateTime
                        .toLocalTime(),
                viewModel.use24HourFormat));
    }

    public void updateDate(SystemSettingsViewModel viewModel) {
        tvDate.setText(formatter.yearMonthDay(viewModel.sprinklerLocalDateTime));
    }

    public void render(SystemSettingsViewModel viewModel, Features features) {
        if (viewModel.isUnitsMetric) {
            tvUnits.setText(R.string.system_settings_metric);
        } else {
            tvUnits.setText(R.string.system_settings_us);
        }
        updateDate(viewModel);
        updateTime(viewModel);

        if (features.showHourFormat()) {
            checkHourFormat.setChecked(viewModel.use24HourFormat);
        }
        if (features.showDeviceName()) {
            tvDeviceName.setText(viewModel.deviceName);
        }
        if (features.showTimezone()) {
            tvTimezone.setText(viewModel.timezone);
        }
        if (features.hasRemoteAccess()) {
            if (!viewModel.cloudSettings.enabled) {
                tvEmail.setText(R.string.system_settings_not_activated);
            } else {
                if (!Strings.isBlank(viewModel.cloudSettings.pendingEmail)) {
                    Truss truss = new Truss()
                            .append(viewModel.cloudSettings.pendingEmail)
                            .append(" ")
                            .pushSpan(new StyleSpan(Typeface.ITALIC))
                            .append(getContext().getString(R.string.all_pending))
                            .popSpan();
                    tvEmail.setText(truss.build());
                } else {
                    if (!Strings.isBlank(viewModel.cloudSettings.email)) {
                        tvEmail.setText(viewModel.cloudSettings.email);
                    } else {
                        tvEmail.setText(R.string.all_not_set);
                    }
                }
            }
        }
        if (features.showNetworkSettings()) {
            viewNetworkSettings.setEnabled(viewModel.enabledWifiSettings);
            ViewUtils.updateBackgroundResourceWithRetainedPadding(viewNetworkSettings,
                    viewModel.enabledWifiSettings ? R.drawable.row_background : R.drawable
                            .rain_list_selector_disabled_holo_light);
        }
        tvSprinklerAddress.setText(viewModel.address);
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    public void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }

    public void setup(Features features) {
        viewHourFormat.setVisibility(features.showHourFormat() ? View.VISIBLE : View.GONE);
        checkHourFormat.setOnCheckedChangeListener(features.showHourFormat() ? this : null);
        viewNetworkSettings.setVisibility(features.showNetworkSettings() ? View.VISIBLE : View
                .GONE);
        viewLocationSettings.setVisibility(features.showLocationSettings() ? View.VISIBLE : View
                .GONE);
        viewDeviceName.setVisibility(features.showDeviceName() ? View.VISIBLE : View.GONE);
        viewTimezone.setVisibility(features.showTimezone() ? View.VISIBLE : View.GONE);
        viewRemoteAccess.setVisibility(features.hasRemoteAccess() ? View.VISIBLE : View.GONE);

        viewContainerRebootReset.setVisibility(features.showReboot() || features
                .showResetDefaults() ? View.VISIBLE : View.GONE);
        viewReboot.setVisibility(features.showReboot() ? View.VISIBLE : View.GONE);
        viewResetDefaults.setVisibility(features.showResetDefaults() ? View.VISIBLE : View.GONE);

        viewContainerAdvanced.setVisibility(features.showAdvancedSettings() || features
                .showMini8Settings() ? View.VISIBLE : View.GONE);
        viewAdvancedSettings.setVisibility(features.showAdvancedSettings() ? View.VISIBLE : View
                .GONE);
        viewMini8Settings.setVisibility(features.showMini8Settings() ? View.VISIBLE : View.GONE);

        viewContainerRestore.setVisibility(features.showRestoreBackup() ? View.VISIBLE : View.GONE);
        viewContainerPin.setVisibility(features.hasSupportLogin() ? View.VISIBLE : View.GONE);
        tvRestoreSubtitle.setText(features.isAtLeastSpk2()
                ? R.string.system_settings_restore_backup_info
                : R.string.system_settings_restore_backup_info_spk1);
    }
}
