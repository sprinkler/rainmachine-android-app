package com.rainmachine.presentation.screens.savehourlyrestriction;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.dialogs.MultiChoiceDialogFragment;
import com.rainmachine.presentation.dialogs.TimePickerDialogFragment;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;

import java.util.Arrays;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

class SaveHourlyRestrictionPresenter extends BasePresenter<SaveHourlyRestrictionView>
        implements TimePickerDialogFragment.Callback, MultiChoiceDialogFragment.Callback {

    private static final int DIALOG_ID_FROM = 1;
    private static final int DIALOG_ID_TO = 2;

    private SaveHourlyRestrictionActivity activity;
    private SaveHourlyRestrictionMixer mixer;

    private final CompositeDisposable disposables;

    private SaveHourlyRestrictionExtra extra;

    SaveHourlyRestrictionPresenter(SaveHourlyRestrictionActivity activity,
                                   SaveHourlyRestrictionMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(SaveHourlyRestrictionView view) {
        super.attachView(view);

        extra = activity.getParcelable(SaveHourlyRestrictionActivity.EXTRA_HOURLY_RESTRICTION);
        view.updateContent(extra);
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogTimePickerPositiveClick(int dialogId, int hourOfDay, int minute) {
        LocalTime lt = new LocalTime(hourOfDay, minute);
        switch (dialogId) {
            case DIALOG_ID_FROM:
                extra.restriction.dayStartMinute = hourOfDay * DateTimeConstants.MINUTES_PER_HOUR +
                        minute;
                extra.restriction.minuteDuration = 0;
                view.updateFrom(extra);
                break;
            case DIALOG_ID_TO:
                LocalTime fromLt = extra.restriction.fromLocalTime();
                if (lt.isBefore(fromLt) || lt.isEqual(fromLt)) {
                    Toasts.show(R.string.save_hourly_restriction_to_before_from);
                } else {
                    extra.restriction.minuteDuration = (lt.getMillisOfDay() - fromLt
                            .getMillisOfDay()) / DateTimeConstants.MILLIS_PER_MINUTE;
                    view.updateTo(extra);
                }
                break;
        }
    }

    @Override
    public void onDialogTimePickerCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogMultiChoicePositiveClick(int dialogId, String[] items, boolean[]
            checkedItemPositions) {
        Timber.d("Got [%s]", Arrays.toString(checkedItemPositions));
        extra.restriction.weekDays = checkedItemPositions;
        if (extra.restriction.isDaily()) {
            view.showWeekDays();
        }
        view.updateWeekDays(extra.restriction);
    }

    @Override
    public void onDialogMultiChoiceCancel(int dialogId) {
        // Do nothing
    }

    public void onClickSave() {
        if (!canSaveHourlyRestriction()) {
            return;
        }
        activity.toggleCustomActionBar(false);
        view.showProgress();
        disposables.add(mixer
                .saveHourlyRestriction(extra.restriction)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    public void onClickDiscard() {
        // "Discard" OR Back
        activity.setResult(Activity.RESULT_CANCELED);
        activity.finish();
    }

    public void onClickFrom() {
        LocalTime lt = extra.restriction.fromLocalTime();
        DialogFragment dialog = TimePickerDialogFragment.newInstance(DIALOG_ID_FROM, activity
                .getString(R.string.all_done), lt.getHourOfDay(), lt.getMinuteOfHour(), extra
                .use24HourFormat);
        activity.showDialogSafely(dialog);
    }

    public void onClickTo() {
        LocalTime lt = extra.restriction.toLocalTime();
        DialogFragment dialog = TimePickerDialogFragment.newInstance(DIALOG_ID_TO, activity
                .getString(R.string.all_done), lt.getHourOfDay(), lt.getMinuteOfHour(), extra
                .use24HourFormat);
        activity.showDialogSafely(dialog);
    }

    public void onClickWeekdays() {
        DialogFragment dialog = MultiChoiceDialogFragment.newInstance(0, activity.getString(R.string
                .save_hourly_restriction_select_restriction_days), activity.getString(R
                .string.all_ok), activity
                .getResources().getStringArray(R.array.all_week_days), extra.restriction.weekDays);
        activity.showDialogSafely(dialog);
    }

    public void onCheckedEveryDay() {
        extra.restriction.makeDaily();
        view.updateWeekDays(extra.restriction);
    }

    private boolean canSaveHourlyRestriction() {
        if (extra.restriction.minuteDuration == 0) {
            Toasts.show(R.string.save_hourly_restriction_to_before_from);
            return false;
        }
        if (!extra.restriction.isDaily() && !DomainUtils.isAtLeastOneWeekDaySelected(extra
                .restriction.weekDays)) {
            Toasts.show(R.string.save_hourly_restriction_no_selected_days);
            return false;
        }
        return true;
    }

    private final class SaveSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            Toasts.show(R.string.save_hourly_restriction_success_save_restriction);
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            activity.toggleCustomActionBar(true);
            view.showContent();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
