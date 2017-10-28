package com.rainmachine.presentation.screens.hours;

import android.support.v7.view.ActionMode;

import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.presentation.screens.savehourlyrestriction.SaveHourlyRestrictionActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class HoursPresenter extends BasePresenter<HoursView> {

    private HoursActivity activity;
    private HoursMixer mixer;

    private final CompositeDisposable disposables;

    private HoursViewModel viewModel;

    HoursPresenter(HoursActivity activity, HoursMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(HoursView view) {
        super.attachView(view);

        view.setup();
    }

    @Override
    public void init() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    public void onDeleteHourlyRestrictions(List<Long> uids) {
        view.showProgress();
        disposables.add(mixer
                .deleteHourlyRestrictions(uids)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    public void onClickAddRestriction() {
        HourlyRestriction defaultRestriction = HourlyRestriction.newDefaultInstance();
        activity.startActivityForResult(SaveHourlyRestrictionActivity.getStartIntent(activity,
                defaultRestriction, viewModel.use24HourFormat),
                HoursActivity.REQ_CODE_HOURS_DETAILS);
    }

    public void onClickRetry() {
        refresh();
    }

    public void onCreateActionMode(ActionMode mode) {
        activity.updateCabTitle(mode, view.getCheckedItems());
    }

    public void onDestroyActionMode() {
        view.uncheckAllItems();
    }

    public void onActionItemClicked() {
        view.onActionModeClicked();
    }

    public void onItemClick(int position) {
        if (activity.getActionMode() == null) {
            HourlyRestriction hourlyRestriction = view.getItem(position);
            activity.startActivityForResult(SaveHourlyRestrictionActivity.getStartIntent
                    (activity, hourlyRestriction, viewModel.use24HourFormat), HoursActivity
                    .REQ_CODE_HOURS_DETAILS);
            view.checkItem(position, false);
        } else {
            activity.updateCabTitle(activity.getActionMode(), view.getCheckedItems());
        }
    }

    public boolean onItemLongClick(int position) {
        if (activity.getActionMode() == null) {
            view.checkItem(position, true);
            activity.showActionMode();
            return true;
        }
        return false;
    }

    public void onComingBackFromAddEditHour() {
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

    private final class RefreshSubscriber extends DisposableObserver<HoursViewModel> {

        @Override
        public void onNext(HoursViewModel viewModel) {
            HoursPresenter.this.viewModel = viewModel;
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
}
