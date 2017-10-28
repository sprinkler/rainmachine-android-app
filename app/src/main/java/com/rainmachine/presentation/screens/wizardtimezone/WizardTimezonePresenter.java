package com.rainmachine.presentation.screens.wizardtimezone;

import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.dialogs.DatePickerDialogFragment;
import com.rainmachine.presentation.dialogs.TimePickerDialogFragment;
import com.rainmachine.presentation.screens.main.MainActivity;
import com.rainmachine.presentation.screens.wizardremoteaccess.WizardRemoteAccessActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.ExtraConstants;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class WizardTimezonePresenter extends BasePresenter<WizardTimezoneView> implements
        DatePickerDialogFragment.Callback, TimePickerDialogFragment.Callback,
        TimezoneDialogFragment.Callback {

    private WizardTimezoneActivity activity;
    private Device device;
    private WizardTimezoneMixer mixer;

    private final CompositeDisposable disposables;

    private boolean isWizard;

    WizardTimezonePresenter(WizardTimezoneActivity activity, Device device, WizardTimezoneMixer
            mixer) {
        this.activity = activity;
        this.device = device;
        this.mixer = mixer;
        isWizard = activity.getIntent().getBooleanExtra(ExtraConstants.IS_WIZARD, false);
        disposables = new CompositeDisposable();
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
        LocalDate date = new LocalDate(year, month + 1, day);
        view.updateContentDate(date);
    }

    @Override
    public void onDialogDatePickerCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogTimePickerPositiveClick(int dialogId, int hourOfDay, int minute) {
        LocalTime time = new LocalTime(hourOfDay, minute);
        view.updateContentTime(time);
    }

    @Override
    public void onDialogTimePickerCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onTimezoneSelected(String timezoneId) {
        view.updateContentTimezone(timezoneId);
    }

    public void onClickDate(LocalDate date) {
        DialogFragment dialog = DatePickerDialogFragment.newInstance(0, activity.getString(R
                .string.all_ok), date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
        activity.showDialogSafely(dialog);
    }

    public void onClickTime(LocalTime time) {
        DialogFragment dialog = TimePickerDialogFragment.newInstance(0, activity.getString(R
                .string.all_ok), time.getHourOfDay(), time.getMinuteOfHour(), DateFormat
                .is24HourFormat(activity));
        activity.showDialogSafely(dialog);
    }

    public void onClickTimezone() {
        DialogFragment dialog = TimezoneDialogFragment.newInstance();
        activity.showDialogSafely(dialog);
    }

    public void onClickSave(LocalDate date, LocalTime time, String timezone) {
        view.showProgress();
        disposables.add(mixer
                .saveTimeDateTimezone(date, time, timezone)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    public void onClickRetry() {
        refresh();
    }

    private void refresh() {
        view.showProgress();
        disposables.add(mixer
                .refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<WizardTimezoneViewModel> {

        @Override
        public void onNext(WizardTimezoneViewModel viewModel) {
            view.updateContent(viewModel);
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

    private final class SaveSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            Toasts.show(R.string.wizard_timezone_success_set_time_date);
            if (isWizard) {
                if (device.isAp()) {
                    activity.startActivity(MainActivity.getStartIntent(activity, true));
                } else {
                    activity.startActivity(WizardRemoteAccessActivity.getStartIntent(activity));
                }
            }
            activity.finish();
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
}
