package com.rainmachine.presentation.screens.programdetailsold;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramStartTime;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.domain.usecases.program.SaveProgram;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.domain.util.Features;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.DatePickerDialogFragment;
import com.rainmachine.presentation.dialogs.MultiChoiceDialogFragment;
import com.rainmachine.presentation.dialogs.TimePickerDialogFragment;
import com.rainmachine.presentation.screens.programdetails.ProgramDetailsExtra;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

class ProgramDetailsOldPresenter extends BasePresenter<ProgramDetailsOldView> implements
        DatePickerDialogFragment.Callback,
        ZoneDurationDialogFragment.Callback, ActionMessageDialogFragment.Callback,
        CycleSoakDialogFragment.Callback, StationDelayDialogFragment.Callback,
        TimePickerDialogFragment.Callback, MultiChoiceDialogFragment.Callback,
        SunriseSunsetDialogFragment.Callback {

    private static final int DIALOG_ID_ACTION_MESSAGE_DISCARD = 1;

    private static final LocalTime MEDIUM_SUNRISE_TIME = new LocalTime(6, 20);
    private static final LocalTime MEDIUM_SUNSET_TIME = new LocalTime(18, 30);

    private ProgramDetailsOldActivity activity;
    private Features features;
    private SaveProgram saveProgram;

    private final CompositeDisposable disposables;

    private ProgramDetailsExtra extra;

    ProgramDetailsOldPresenter(ProgramDetailsOldActivity activity, Features features,
                               SaveProgram saveProgram) {
        this.activity = activity;
        this.features = features;
        this.saveProgram = saveProgram;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(ProgramDetailsOldView view) {
        super.attachView(view);

        extra = activity.getParcelable(ProgramDetailsOldActivity.EXTRA_PROGRAM_DETAILS);
        view.setupContent(extra.program, features.showNextRun(), features.showMinutesSeconds(),
                features.hasStartTimeParams(), extra.use24HourFormat, features.showWeatherOption());
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogDatePickerPositiveClick(int dialogId, int year, int month, int day) {
        LocalDate nextRunDate = new LocalDate(year, month + 1, day);

        LocalTime startTime = extra.program.startTime.localDateTime.toLocalTime();
        if (nextRunDate.toLocalDateTime(startTime).isBefore(extra.sprinklerLocalDateTime)) {
            Toasts.show(R.string.program_details_past_next_run);
            return;
        }

        extra.program.nextRunSprinklerLocalDate = nextRunDate;
        if (features.showNextRun()) {
            view.updateNextRun(extra.program);
        }
    }

    @Override
    public void onDialogDatePickerCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogZoneDurationPositiveClick(long zoneId, int duration) {
        view.updateZoneDuration(zoneId, duration);
    }

    @Override
    public void onDialogZoneDurationCancel(long zoneId, int duration) {
        view.updateZoneDuration(zoneId, duration);
    }

    @Override
    public void onDialogActionMessagePositiveClick(int dialogId) {
        leaveScreen();
    }

    @Override
    public void onDialogActionMessageNegativeClick(int dialogId) {
        // Do nothing. Stay on this screen
    }

    @Override
    public void onDialogActionMessageCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogCycleSoakPositiveClick(int cycles, int soak) {
        extra.program.isCycleSoakEnabled = true;
        extra.program.numCycles = cycles;
        extra.program.soakSeconds = soak;
        view.updateCycleSoak(extra.program.isCycleSoakEnabled, extra.program.numCycles, extra
                .program.soakSeconds);
    }

    @Override
    public void onDialogCycleSoakCancel() {
        view.updateCycleSoak(extra.program.isCycleSoakEnabled, extra.program.numCycles, extra
                .program.soakSeconds);
    }

    @Override
    public void onDialogStartTimeSunriseSunsetPositiveClick(ProgramStartTime programStartTime) {
        extra.program.startTime.type = programStartTime.type;
        extra.program.startTime.sunPosition = programStartTime.sunPosition;
        extra.program.startTime.beforeAfter = programStartTime.beforeAfter;
        extra.program.startTime.offsetMinutes = programStartTime.offsetMinutes;
        view.updateSunriseSunset(extra.program);
        updateNextRun();
    }

    @Override
    public void onDialogStartTimeSunriseSunsetCancel() {
        // Do nothing
    }

    @Override
    public void onDialogStationDelayPositiveClick(int duration) {
        extra.program.isDelayEnabled = duration > 0;
        extra.program.delaySeconds = duration;
        view.updateStationDelay(extra.program.isDelayEnabled, extra.program.delaySeconds,
                features.showMinutesSeconds());
    }

    @Override
    public void onDialogStationDelayCancel() {
        view.updateStationDelay(extra.program.isDelayEnabled, extra.program.delaySeconds,
                features.showMinutesSeconds());
    }

    @Override
    public void onDialogTimePickerPositiveClick(int dialogId, int hourOfDay, int minute) {
        LocalDateTime dateTime = extra.program.startTime.localDateTime;
        extra.program.startTime.localDateTime = dateTime.withHourOfDay(hourOfDay).withMinuteOfHour
                (minute);
        view.updateStartTimeOfDay(extra.program, features.hasStartTimeParams(), extra
                .use24HourFormat);
        updateNextRun();
    }

    @Override
    public void onDialogTimePickerCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogMultiChoicePositiveClick(int dialogId, String[] items, boolean[]
            checkedItemPositions) {
        extra.program.updateFrequencyWeekDays(checkedItemPositions);
        view.updateWeekdays(extra.program);
        updateNextRun();
    }

    @Override
    public void onDialogMultiChoiceCancel(int dialogId) {
        // Do nothing
    }

    public void onClickDiscardOrBack() {
        if (hasUnsavedChanges()) {
            confirmLeaveScreen();
        } else {
            leaveScreen();
        }
    }

    public void onClickSave() {
        if (!canSaveProgram()) {
            return;
        }
        view.showProgress();
        activity.toggleCustomActionBar(false);
        disposables.add(saveProgram
                .execute(new SaveProgram.RequestModel(extra.program, extra.originalProgram, extra
                        .use24HourFormat))
                .compose(RunOnProperThreads.instance())
                .doOnNext(responseModel -> Toasts.show(R.string
                        .program_details_success_save_program))
                .doOnError(GenericErrorDealer.INSTANCE)
                .subscribeWith(new SaveProgramSubscriber()));
    }

    public void onClickedWateringZone(ProgramWateringTimes activeZone) {
        showZoneDurationDialog(activeZone);
    }

    public void onCheckedWateringZone(ProgramWateringTimes activeZone, boolean isChecked) {
        if (isChecked) {
            showZoneDurationDialog(activeZone);
        } else {
            activeZone.active = false;
            activeZone.duration = 0; // reset duration if it's not active
            view.refreshWateringZones();
        }
    }

    private void showZoneDurationDialog(ProgramWateringTimes activeZone) {
        DialogFragment dialog = ZoneDurationDialogFragment.newInstance(activeZone.id, activeZone
                .name, (int) activeZone.duration, features.showMinutesSeconds());
        activity.showDialogSafely(dialog);
    }

    public void onClickStartTime() {
        extra.program.startTime.type = ProgramStartTime.StartTimeType.TIME_OF_DAY;
        LocalDateTime dateTime = extra.program.startTime.localDateTime;
        DialogFragment dialog = TimePickerDialogFragment.newInstance(0, activity.getString(R
                        .string.all_done),
                dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), extra.use24HourFormat);
        activity.showDialogSafely(dialog);
    }

    public void onClickNextRun() {
        if (extra.program.isEveryNDays() && extra.program.nextRunSprinklerLocalDate != null) {
            LocalDate date = extra.program.nextRunSprinklerLocalDate;
            DialogFragment dialog = DatePickerDialogFragment.newInstance(0, activity.getString(R
                    .string.all_save), date.getYear(), date.getMonthOfYear() - 1, date
                    .getDayOfMonth());
            activity.showDialogSafely(dialog);
        }
    }

    public void onClickCycleSoak() {
        DialogFragment dialog = CycleSoakDialogFragment.newInstance(extra.program.numCycles,
                extra.program.soakSeconds);
        activity.showDialogSafely(dialog);
    }

    public void onToggleCycleSoak(boolean isChecked) {
        if (isChecked) {
            DialogFragment dialog = CycleSoakDialogFragment.newInstance(extra.program.numCycles,
                    extra.program.soakSeconds);
            activity.showDialogSafely(dialog);
        } else {
            extra.program.isCycleSoakEnabled = false;
            extra.program.numCycles = 0;
            extra.program.soakSeconds = 0;
            view.updateCycleSoak(extra.program.isCycleSoakEnabled, extra.program.numCycles,
                    extra.program.soakSeconds);
        }
    }

    public void onClickStationDelay() {
        DialogFragment dialog = StationDelayDialogFragment.newInstance(extra.program
                .delaySeconds, features.showMinutesSeconds());
        activity.showDialogSafely(dialog);
    }

    public void onToggleStationDelay(boolean isChecked) {
        if (isChecked) {
            DialogFragment dialog = StationDelayDialogFragment.newInstance(extra.program
                    .delaySeconds, features.showMinutesSeconds());
            activity.showDialogSafely(dialog);
        } else {
            extra.program.isDelayEnabled = false;
            extra.program.delaySeconds = 0;
            view.updateStationDelay(extra.program.isDelayEnabled, extra.program.delaySeconds,
                    features.showMinutesSeconds());
        }
    }

    public void onToggleActive(boolean isChecked) {
        extra.program.enabled = isChecked;
    }

    public void onToggleWeatherData(boolean isChecked) {
        extra.program.ignoreWeatherData = !isChecked;
    }

    public void onClickWeekdays() {
        view.activateWeekdays();
        extra.program.updateFrequencyWeekDays();
        DialogFragment dialog = MultiChoiceDialogFragment.newInstance(0, activity.getString(R.string
                        .program_details_select_week_days),
                activity.getString(R.string.all_ok), activity.getResources().getStringArray(R
                        .array.all_week_days), extra.program.frequencyWeekDays());
        activity.showDialogSafely(dialog);
    }

    public void onClickedEveryNDays() {
        view.activateEveryNDays();
    }

    public void onClickedEveryNDaysItem(int numDays) {
        extra.program.updateFrequencyEveryNDays(numDays);
        updateNextRun();
    }

    public void onCheckedFrequencyEveryDay() {
        extra.program.updateFrequencyDaily();
        view.hideWeekdays();
        updateNextRun();
    }

    public void onCheckedFrequencyOddDays() {
        extra.program.updateFrequencyOddDays();
        view.hideWeekdays();
        updateNextRun();
    }

    public void onCheckedFrequencyEvenDays() {
        extra.program.updateFrequencyEvenDays();
        view.hideWeekdays();
        updateNextRun();
    }

    public void onCheckedFrequencyEveryNDays(int numDays) {
        extra.program.updateFrequencyEveryNDays(numDays);
        view.hideWeekdays();
        updateNextRun();
    }

    public void onCheckedFrequencyWeekdays() {
        onClickWeekdays();
        updateNextRun();
    }

    public void onChangedProgramName(String name) {
        extra.program.name = name;
    }

    private void confirmLeaveScreen() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance
                (DIALOG_ID_ACTION_MESSAGE_DISCARD,
                        activity.getString(R.string.all_unsaved_changes),
                        activity.getString(R.string.program_details_unsaved_changes_program),
                        activity.getString(R.string.all_yes), activity.getString(R.string.all_no));
        activity.showDialogSafely(dialog);
    }

    public void finishScreen() {
        // Success
        activity.setResult(Activity.RESULT_OK);
        activity.finish();
    }

    public void leaveScreen() {
        // "Discard" OR Back
        activity.setResult(Activity.RESULT_CANCELED);
        activity.finish();
    }

    private boolean hasUnsavedChanges() {
        // If this is a new program, of course it has unsaved changes
        if (extra.program.isNew()) {
            return true;
        }

        if (!extra.originalProgram.name.equals(extra.program.name)) {
            return true;
        }
        return isProgramDifferent(extra.program, extra.originalProgram);
    }

    private boolean canSaveProgram() {
        if (extra.program.isWeekDays()) {
            if (!DomainUtils.isAtLeastOneWeekDaySelected(extra.program.frequencyWeekDays())) {
                Toasts.showLong(R.string.program_details_select_weekday);
                return false;
            }
        }

        boolean hasAtLeastOneWateringTime = false;
        for (ProgramWateringTimes wtr : extra.program.wateringTimes) {
            if (wtr.duration > 0) {
                hasAtLeastOneWateringTime = true;
                break;
            }
        }

        if (!hasAtLeastOneWateringTime) {
            Toasts.showLong(R.string.program_details_at_least_one_watering_time);
            return false;
        }

        return true;
    }

    private void updateNextRun() {
        if (!features.showNextRun()) {
            return;
        }
        computeNextRun();
        view.updateNextRun(extra.program);
    }

    private void computeNextRun() {
        LocalDate nextRunDate = extra.sprinklerLocalDateTime.toLocalDate();
        LocalTime startTime;
        if (extra.program.startTime.isTimeOfDay()) {
            startTime = extra.program.startTime.localDateTime.toLocalTime();
        } else {
            int offsetMinutes = extra.program.startTime.offsetMinutes;
            if (extra.program.startTime.isSunrise()) {
                startTime = MEDIUM_SUNRISE_TIME;
            } else {
                startTime = MEDIUM_SUNSET_TIME;
            }
            if (extra.program.startTime.isBefore()) {
                startTime = startTime.minusMinutes(offsetMinutes);
            } else {
                startTime = startTime.plusMinutes(offsetMinutes);
            }
        }
        if (startTime.isBefore(extra.sprinklerLocalDateTime.toLocalTime())) {
            nextRunDate = nextRunDate.plusDays(1);
        }
        if (extra.program.isOddDays()) {
            // odd
            if (nextRunDate.getDayOfMonth() % 2 == 0) {
                nextRunDate = nextRunDate.plusDays(1);
            }
        } else if (extra.program.isEvenDays()) {
            // even
            if (nextRunDate.getDayOfMonth() % 2 == 1) {
                nextRunDate = nextRunDate.plusDays(1);
            }
        } else if (extra.program.isWeekDays()) {
            // week days
            boolean[] weekdays = extra.program.frequencyWeekDays();
            // repeat at most for 7 consecutive days
            for (int i = 0; i < weekdays.length; i++) {
                if (!weekdays[nextRunDate.getDayOfWeek() - 1]) {
                    nextRunDate = nextRunDate.plusDays(1);
                } else {
                    break;
                }
            }
        }
        Timber.d("Next run date : %s", nextRunDate.toString());
        extra.program.nextRunSprinklerLocalDate = nextRunDate;
    }

    public boolean isProgramDifferent(Program prog1, Program prog2) {
        if (prog1.enabled != prog2.enabled) {
            return true;
        }
        if (prog1.ignoreWeatherData != prog2.ignoreWeatherData) {
            return true;
        }
        if (Program.isFrequencyDifferent(prog1, prog2)) {
            return true;
        }

        if (!prog1.startTime.equals(prog2.startTime)) {
            return true;
        }

        if (prog1.isCycleSoakEnabled != prog2.isCycleSoakEnabled) {
            return true;
        }
        if (prog1.isCycleSoakEnabled && prog1.numCycles != prog2.numCycles) {
            return true;
        }
        if (prog1.isCycleSoakEnabled && prog1.soakSeconds != prog2.soakSeconds) {
            return true;
        }
        if (prog1.isDelayEnabled != prog2.isDelayEnabled) {
            return true;
        }
        if (prog1.isDelayEnabled && prog1.delaySeconds != prog2.delaySeconds) {
            return true;
        }

        for (int i = 0; i < prog1.wateringTimes.size(); i++) {
            if (prog1.wateringTimes.get(i).duration != prog2.wateringTimes.get(i).duration) {
                return true;
            }
        }

        return false;
    }

    public void onClickSunriseSunset() {
        ProgramStartTime currentStartTime = extra.program.startTime;
        ProgramStartTime programStartTime = new ProgramStartTime();
        programStartTime.type = ProgramStartTime.StartTimeType.SUN_POSITION;
        programStartTime.offsetMinutes = currentStartTime.offsetMinutes;
        programStartTime.beforeAfter = currentStartTime.beforeAfter;
        programStartTime.sunPosition = currentStartTime.sunPosition;
        DialogFragment dialog = SunriseSunsetDialogFragment.newInstance(programStartTime);
        activity.showDialogSafely(dialog);
    }

    private final class SaveProgramSubscriber extends DisposableObserver<SaveProgram
            .ResponseModel> {

        @Override
        public void onNext(SaveProgram.ResponseModel responseModel) {
            finishScreen();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Toasts.show(R.string.program_details_error_saving_program);
            view.showContent();
            activity.toggleCustomActionBar(true);
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
