package com.rainmachine.presentation.screens.systemsettings;

import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.notifiers.DeviceNameStore;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.infrastructure.InfrastructureUtils;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.DatePickerDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;
import com.rainmachine.presentation.dialogs.TimePickerDialogFragment;
import com.rainmachine.presentation.screens.advancedsettings.AdvancedSettingsActivity;
import com.rainmachine.presentation.screens.locationsettings.LocationSettingsActivity;
import com.rainmachine.presentation.screens.mini8settings.Mini8SettingsActivity;
import com.rainmachine.presentation.screens.remoteaccess.RemoteAccessActivity;
import com.rainmachine.presentation.screens.restorebackup.RestoreBackupActivity;
import com.rainmachine.presentation.screens.softwareupdate.SoftwareUpdateActivity;
import com.rainmachine.presentation.screens.wifi.WifiActivity;
import com.rainmachine.presentation.screens.wizardtimezone.TimezoneDialogFragment;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

class SystemSettingsPresenter extends BasePresenter<SystemSettingsView> implements
        DatePickerDialogFragment.Callback, TimePickerDialogFragment.Callback,
        RadioOptionsDialogFragment.Callback, ActionMessageDialogFragment.Callback,
        TimezoneDialogFragment.Callback, InfoMessageDialogFragment.Callback {

    private static final int DIALOG_ID_RESET_DEFAULTS = 1;
    private static final int DIALOG_ID_REBOOT = 2;

    private SystemSettingsActivity activity;
    private Features features;
    private SystemSettingsMixer mixer;
    private Device device;
    private DeviceNameStore deviceNameStore;

    private final CompositeDisposable disposables;

    private SystemSettingsViewModel viewModel;

    SystemSettingsPresenter(SystemSettingsActivity activity, Features features,
                            SystemSettingsMixer mixer, Device device, DeviceNameStore
                                    deviceNameStore) {
        this.activity = activity;
        this.features = features;
        this.mixer = mixer;
        this.device = device;
        this.deviceNameStore = deviceNameStore;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(SystemSettingsView view) {
        super.attachView(view);

        view.setup(features);
        view.showProgress();
    }

    @Override
    public void init() {
        disposables.add(deviceNameStore
                .observe()
                .doOnError(GenericErrorDealer.INSTANCE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DeviceNameChangedSubscriber()));
    }

    public void start() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogDatePickerPositiveClick(int dialogId, int year, int month, int day) {
        if (viewModel == null) {
            return;
        }
        viewModel.sprinklerLocalDateTime = viewModel.sprinklerLocalDateTime.withYear(year)
                .withMonthOfYear
                        (month + 1).withDayOfMonth(day);
        view.updateDate(viewModel);
        onSaveDateTime(viewModel);
    }

    @Override
    public void onDialogDatePickerCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogTimePickerPositiveClick(int dialogId, int hourOfDay, int minute) {
        if (viewModel == null) {
            return;
        }
        viewModel.sprinklerLocalDateTime = viewModel.sprinklerLocalDateTime.withHourOfDay(hourOfDay)
                .withMinuteOfHour(minute);
        view.updateTime(viewModel);
        onSaveDateTime(viewModel);
    }

    @Override
    public void onDialogTimePickerCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogRadioOptionsPositiveClick(int dialogId, String[] items, int
            checkedItemPosition) {
        if (viewModel == null) {
            return;
        }
        boolean isUnitsMetric = (checkedItemPosition == 0);
        if (isUnitsMetric != viewModel.isUnitsMetric) {
            view.showProgress();
            disposables.add(mixer
                    .saveUnits(isUnitsMetric)
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new RefreshSubscriber()));
        }
    }

    @Override
    public void onDialogRadioOptionsCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogActionMessagePositiveClick(int dialogId) {
        if (dialogId == DIALOG_ID_RESET_DEFAULTS) {
            view.showProgress();
            disposables.add(mixer
                    .resetToDefault()
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new ResetToDefaultSubscriber()));
        } else if (dialogId == DIALOG_ID_REBOOT) {
            view.showProgress();
            disposables.add(mixer
                    .reboot()
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new RefreshSubscriber()));
        }
    }

    @Override
    public void onDialogActionMessageNegativeClick(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogActionMessageCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogInfoMessageClick(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogInfoMessageCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onTimezoneSelected(String timezoneId) {
        onSaveTimezone(timezoneId);
    }

    public void onClickNetworkSettings() {
        activity.startActivity(WifiActivity.getStartIntent(activity, false, false, false));
    }

    public void onClickResetDefaults() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance(DIALOG_ID_RESET_DEFAULTS,
                activity.getString
                        (R.string.all_are_you_sure), activity.getString(R.string
                        .system_settings_reset_default_description), activity.getString(R.string
                        .all_yes), activity
                        .getString(R.string.all_no));
        activity.showDialogSafely(dialog);
    }

    public void onClickRestoreBackup() {
        activity.startActivity(RestoreBackupActivity.getStartIntent(activity, false));
    }

    public void onClickLocationSettings() {
        if (viewModel == null) {
            return;
        }
        activity.startActivity(LocationSettingsActivity.getStartIntent(activity, viewModel
                .address));
    }

    public void onClickDeviceName() {
        if (viewModel == null) {
            return;
        }
        DialogFragment dialog = DeviceNameDialogFragment.newInstance(viewModel.deviceName);
        activity.showDialogSafely(dialog);
    }

    public void onClickTimezone() {
        if (viewModel == null) {
            return;
        }
        DialogFragment dialog = TimezoneDialogFragment.newInstance();
        activity.showDialogSafely(dialog);
    }

    public void onClickRemoteAccess() {
        if (viewModel == null) {
            return;
        }
        activity.startActivity(RemoteAccessActivity.getStartIntent(activity, false));
    }

    public void onClickUnits() {
        if (viewModel == null) {
            return;
        }
        String[] items = new String[]{activity.getString(R.string.system_settings_metric),
                activity.getString(R.string.system_settings_us)};
        int checkedItemPosition = viewModel.isUnitsMetric ? 0 : 1;
        DialogFragment dialog = RadioOptionsDialogFragment.newInstance(0,
                activity.getString(R.string.system_settings_set_units),
                activity.getString(R.string.all_save), items, checkedItemPosition);
        activity.showDialogSafely(dialog);
    }

    public void onClickDate() {
        if (viewModel == null) {
            return;
        }
        DialogFragment dialog = DatePickerDialogFragment.newInstance(0,
                activity.getString(R.string.all_save),
                viewModel.sprinklerLocalDateTime.getYear(),
                viewModel.sprinklerLocalDateTime.getMonthOfYear() - 1,
                viewModel.sprinklerLocalDateTime.getDayOfMonth());
        activity.showDialogSafely(dialog);
    }

    public void onClickTime() {
        if (viewModel == null) {
            return;
        }
        DialogFragment dialog = TimePickerDialogFragment.newInstance(0,
                activity.getString(R.string.all_save),
                viewModel.sprinklerLocalDateTime.getHourOfDay(),
                viewModel.sprinklerLocalDateTime.getMinuteOfHour(),
                viewModel.use24HourFormat);
        activity.showDialogSafely(dialog);
    }

    public void onCheckedChanged24HourFormat(boolean isChecked) {
        if (viewModel == null) {
            return;
        }
        viewModel.use24HourFormat = isChecked;
        view.updateTime(viewModel);
        mixer.saveUse24HourFormat(isChecked);
    }

    public void onSaveTimezone(String timezone) {
        view.showProgress();
        disposables.add(mixer
                .saveTimezone(timezone)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    public void onSaveDateTime(SystemSettingsViewModel deviceSettings) {
        view.showProgress();
        disposables.add(mixer
                .saveTimeDate(deviceSettings)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    public void onSaveDeviceName(String deviceName) {
        view.showProgress();
        disposables.add(mixer
                .saveDeviceName(deviceName)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    public void onRefresh() {
        refresh();
    }

    public void onClickGeneratePin() {
        view.showProgress();
        disposables.add(mixer
                .generateSupportPin()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new GenerateSupportPinSubscriber()));
    }

    public void onClickAdvancedSettings() {
        activity.startActivity(AdvancedSettingsActivity.getStartIntent(activity));
    }

    public void onClickMini8Settings() {
        activity.startActivity(Mini8SettingsActivity.getStartIntent(activity));
    }

    public void onClickSoftwareUpdate() {
        activity.startActivity(SoftwareUpdateActivity.getStartIntent(activity));
    }

    public void onClickReboot() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance(DIALOG_ID_REBOOT,
                activity.getString
                        (R.string.all_are_you_sure), activity.getString(R.string
                        .system_settings_reboot_description), activity.getString(R.string
                        .all_yes), activity.getString(R.string.all_no));
        activity.showDialogSafely(dialog);
    }

    private void refresh() {
        view.showProgress();
        disposables.add(mixer
                .refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<SystemSettingsViewModel> {

        @Override
        public void onNext(SystemSettingsViewModel viewModel) {
            SystemSettingsPresenter.this.viewModel = viewModel;
            view.render(viewModel, features);
            view.showContent();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class ResetToDefaultSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            InfrastructureUtils.finishAllSprinklerActivities();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class GenerateSupportPinSubscriber extends DisposableObserver<String> {

        @Override
        public void onNext(String totp) {
            DialogFragment dialog = InfoMessageDialogFragment.newInstance(0,
                    activity.getString(R.string.system_settings_support_pin, totp),
                    activity.getString(R.string.system_settings_support_pin_details),
                    activity
                            .getString(R.string.all_ok));
            activity.showDialogSafely(dialog);
            view.showContent();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class DeviceNameChangedSubscriber extends DisposableObserver<Object> {

        @Override
        public void onNext(Object object) {
            Timber.d("changed device name");
            activity.getSupportActionBar().setTitle(device.name);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            // Do nothing
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
