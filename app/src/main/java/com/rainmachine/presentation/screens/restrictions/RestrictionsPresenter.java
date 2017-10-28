package com.rainmachine.presentation.screens.restrictions;

import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.dialogs.InputNumberNoteDialogFragment;
import com.rainmachine.presentation.screens.hours.HoursActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class RestrictionsPresenter extends BasePresenter<RestrictionsView> implements
        FreezeProtectDialogFragment.Callback, RestrictedDaysDialogFragment.Callback,
        RestrictedMonthsDialogFragment.Callback, InputNumberNoteDialogFragment.Callback {

    private static final int DIALOG_ID_WEEKDAYS = 0;
    private static final int DIALOG_ID_MONTHS = 1;
    private static final int DIALOG_ID_FREEZE_PROTECT = 2;
    private static final int DIALOG_ID_MIN_DURATION_THRESHOLD = 3;

    private RestrictionsActivity activity;
    private RestrictionsMixer mixer;

    private RestrictionsViewModel viewModel;
    private CompositeDisposable disposables;

    RestrictionsPresenter(RestrictionsActivity activity, RestrictionsMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(RestrictionsView view) {
        super.attachView(view);

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
    public void onDialogFreezeProtectPositiveClick(int dialogId, int value) {
        switch (dialogId) {
            case DIALOG_ID_FREEZE_PROTECT:
                viewModel.globalRestrictions.freezeProtectEnabled = true;
                viewModel.globalRestrictions.setFreezeProtectTemperature(value, viewModel
                        .isUnitsMetric);
                saveWateringRestrictions(viewModel);
                view.render(viewModel);
                break;
        }
    }

    @Override
    public void onDialogFreezeProtectCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogMultiChoicePositiveClick(int dialogId, String[] items, boolean[]
            checkedItemPositions) {
        switch (dialogId) {
            case DIALOG_ID_MONTHS:
                viewModel.globalRestrictions.noWaterInMonths = checkedItemPositions;
                saveWateringRestrictions(viewModel);
                view.render(viewModel);
                break;
            case DIALOG_ID_WEEKDAYS:
                viewModel.globalRestrictions.noWaterInWeekDays = checkedItemPositions;
                saveWateringRestrictions(viewModel);
                view.render(viewModel);
                break;
        }
    }

    @Override
    public void onDialogMultiChoiceCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogInputNumberNotePositiveClick(int dialogId, final int value) {
        if (dialogId == DIALOG_ID_MIN_DURATION_THRESHOLD) {
            viewModel.minWateringDurationThreshold = value;
            view.showProgress();
            disposables.add(mixer.saveMinWateringDurationThreshold(value)
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new SaveSubscriber()));
            view.render(viewModel);
        }
    }

    @Override
    public void onDialogInputNumberNoteCancel(int dialogId) {
        // Do nothing
    }

    public void onClickedRetry() {
        refresh();
    }

    public void onCheckedChangedFreezeProtect(boolean isChecked) {
        viewModel.globalRestrictions.freezeProtectEnabled = isChecked;
        if (isChecked) {
            showFreezeProtectDialog(viewModel);
        } else {
            saveWateringRestrictions(viewModel);
        }
    }

    public void onClickHotDays() {
        showHotDaysDialog(viewModel);
    }

    public void onClickFreezeProtect() {
        showFreezeProtectDialog(viewModel);
    }

    public void onClickMonths() {
        DialogFragment dialog = RestrictedMonthsDialogFragment.newInstance(DIALOG_ID_MONTHS,
                activity.getString(R
                        .string.restrictions_restricted_months), activity.getString(R.string
                        .all_save),
                activity.getResources().getStringArray(R.array.restrictions_months),
                viewModel.globalRestrictions.noWaterInMonths);
        activity.showDialogSafely(dialog);
    }

    public void onClickWeekDays() {
        DialogFragment dialog = RestrictedDaysDialogFragment.newInstance(DIALOG_ID_WEEKDAYS,
                activity.getString(R
                        .string.restrictions_restricted_days), activity.getString(R.string
                        .all_save),
                activity.getResources().getStringArray(R.array.all_week_days),
                viewModel.globalRestrictions.noWaterInWeekDays);
        activity.showDialogSafely(dialog);
    }

    public void onClickHours() {
        activity.startActivity(HoursActivity.getStartIntent(activity));
    }

    public void onCheckedChangedHotDays(boolean isChecked) {
        viewModel.globalRestrictions.hotDaysExtraWatering = isChecked;
        saveWateringRestrictions(viewModel);

        if (isChecked) {
            showHotDaysDialog(viewModel);
        }
    }

    public void onClickDurationThreshold() {
        DialogFragment dialog = InputNumberNoteDialogFragment.newInstance
                (DIALOG_ID_MIN_DURATION_THRESHOLD,
                        activity.getString(R.string.restrictions_duration_threshold),
                        activity.getString(R.string.all_save),
                        viewModel.minWateringDurationThreshold,
                        activity.getString(R.string.all_seconds),
                        0, 999, activity.getString(R.string
                                .restrictions_duration_threshold_subtitle));
        activity.showDialogSafely(dialog);
    }

    public void refresh() {
        view.showProgress();
        disposables.add(mixer.refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    public void saveWateringRestrictions(final RestrictionsViewModel viewModel) {
        view.showProgress();
        disposables.add(mixer.saveGlobalRestrictions(viewModel)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    private void showFreezeProtectDialog(RestrictionsViewModel viewModel) {
        DialogFragment dialog = FreezeProtectDialogFragment.newInstance(DIALOG_ID_FREEZE_PROTECT,
                activity.getString
                        (R.string.restrictions_do_not_water_under_title),
                activity.getString(R.string.all_save),
                viewModel.globalRestrictions.freezeProtectTemperature(viewModel.isUnitsMetric),
                viewModel.isUnitsMetric);
        activity.showDialogSafely(dialog);
    }

    private void showHotDaysDialog(RestrictionsViewModel viewModel) {
        DialogFragment dialog = HotDaysDialogFragment.newInstance(viewModel.maxWateringCoefficient);
        activity.showDialogSafely(dialog);
    }

    public void onSaveHotDaysMaxWatering(final int maxWateringCoefficient) {
        viewModel.maxWateringCoefficient = maxWateringCoefficient;
        viewModel.globalRestrictions.hotDaysExtraWatering = true;

        Observable<Irrelevant> observable = mixer.saveGlobalRestrictions(viewModel)
                .concatMap(irrelevant -> mixer.saveMaxWateringCoefficient
                        (maxWateringCoefficient));

        view.showProgress();
        disposables.add(observable
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<RestrictionsViewModel> {

        @Override
        public void onNext(RestrictionsViewModel viewModel) {
            RestrictionsPresenter.this.viewModel = viewModel;
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

    private final class SaveSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            refresh();
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
