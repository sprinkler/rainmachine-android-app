package com.rainmachine.presentation.screens.restorebackup;

import android.os.Parcelable;
import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.domain.usecases.backup.RestoreBackup;
import com.rainmachine.presentation.dialogs.ActionMessageParcelableDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.rainmachine.presentation.screens.wizardtimezone.WizardTimezoneActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.ExtraConstants;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.parceler.Parcels;

import java.util.Locale;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class RestoreBackupPresenter extends BasePresenter<RestoreBackupView> implements
        RestoreBackupDialogFragment.Callback, ActionMessageParcelableDialogFragment.Callback,
        InfoMessageDialogFragment.Callback {

    private RestoreBackupActivity activity;
    private RestoreBackupMixer mixer;

    private boolean isWizard;
    private CompositeDisposable disposables;

    RestoreBackupPresenter(RestoreBackupActivity activity, RestoreBackupMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(RestoreBackupView view) {
        super.attachView(view);

        isWizard = activity.getIntent().getBooleanExtra(ExtraConstants.IS_WIZARD, false);
        view.setup();
    }

    @Override
    public void init() {
    }

    public void start() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onClickRestoreBackup(RestoreBackupViewModel.BackupDeviceData backupDeviceData,
                                     RestoreBackupViewModel.Backup backup) {
        String date = backup.localDateTime.toString("MMM dd, yyyy", Locale.ENGLISH);
        String time = CalendarFormatter.hourMinColon(backup.localDateTime.toLocalTime(),
                backupDeviceData.use24HourFormat);
        String fullDate = activity.getString(R.string.restore_backup_description_date, date,
                time);
        String message = activity.getString(R.string.restore_backup_are_you_sure_message,
                backupDeviceData.name, fullDate);
        RestoreBackupDialogParcel parcel = new RestoreBackupDialogParcel();
        parcel.backup = backup;
        parcel._backupDeviceDatabaseId = backupDeviceData._databaseId;
        parcel.deviceId = backupDeviceData.deviceId;
        DialogFragment dialog = ActionMessageParcelableDialogFragment
                .newInstance(0, activity.getString(R.string.all_are_you_sure), message, activity
                        .getString(R.string.all_yes), activity.getString(R
                        .string.all_no), Parcels.wrap(parcel));
        activity.showDialogSafely(dialog);
    }

    @Override
    public void onClickSkipRestoreBackup() {
        // Do nothing
    }

    @Override
    public void onDialogActionMessageParcelablePositiveClick(int dialogId, Parcelable parcelable) {
        RestoreBackupDialogParcel parcel = Parcels.unwrap(parcelable);
        view.showProgress();
        disposables.add(mixer.restoreBackup(parcel.deviceId, parcel._backupDeviceDatabaseId,
                parcel.backup)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RestoreBackupSubscriber()));
    }

    @Override
    public void onDialogActionMessageParcelableNegativeClick(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogActionMessageParcelableCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogInfoMessageClick(int dialogId) {
        moveOn();
    }

    @Override
    public void onDialogInfoMessageCancel(int dialogId) {
        moveOn();
    }

    public void onRetry() {
        refresh();
    }

    public void onClickBackupDevice(RestoreBackupViewModel.BackupDeviceData backupDeviceData) {
        DialogFragment dialog = RestoreBackupDialogFragment.newInstance(backupDeviceData,
                activity.getString(R.string.all_cancel));
        activity.showDialogSafely(dialog);
    }

    private void moveOn() {
        if (isWizard) {
            activity.startActivity(WizardTimezoneActivity.getStartIntent(activity));
            activity.finish();
        } else {
            view.showContent();
        }
    }

    private void refresh() {
        view.showProgress();
        disposables.add(mixer.refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    public void onClickSkip() {
        moveOn();
    }

    private final class RefreshSubscriber extends DisposableObserver<RestoreBackupViewModel> {

        @Override
        public void onNext(RestoreBackupViewModel viewModel) {
            view.render(viewModel);
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

    private final class RestoreBackupSubscriber extends
            DisposableObserver<RestoreBackupOutcomeViewModel> {

        @Override
        public void onNext(RestoreBackupOutcomeViewModel outcomeData) {
            if (outcomeData.backupOutcome.isSuccess) {
                String message = activity.getString(R.string.restore_backup_success);
                if (outcomeData.backupOutcome.zoneNumberStatus == RestoreBackup.ResponseModel
                        .ZoneNumberStatus.ZONES_8_VS_12) {
                    message = activity.getString(R.string.restore_backup_success_zones_8_vs_12);
                } else if (outcomeData.backupOutcome.zoneNumberStatus == RestoreBackup.ResponseModel
                        .ZoneNumberStatus.ZONES_8_VS_16) {
                    message = activity.getString(R.string.restore_backup_success_zones_8_vs_16);
                } else if (outcomeData.backupOutcome.zoneNumberStatus == RestoreBackup.ResponseModel
                        .ZoneNumberStatus.ZONES_12_VS_16) {
                    message = activity.getString(R.string
                            .restore_backup_success_zones_12_vs_16);
                }
                DialogFragment dialog = InfoMessageDialogFragment.newInstance(0, activity
                        .getString(R.string.restore_backup_title), message, activity.getString(R
                        .string.all_ok));
                activity.showDialogSafely(dialog);
            } else {
                DialogFragment dialog = InfoMessageDialogFragment.newInstance(0, activity
                        .getString(R.string.restore_backup_title), activity.getString(R.string
                        .restore_backup_failure), activity.getString(R.string.all_ok));
                activity.showDialogSafely(dialog);
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showContent();
            Toasts.show(R.string.system_settings_error_restore_backup);
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
