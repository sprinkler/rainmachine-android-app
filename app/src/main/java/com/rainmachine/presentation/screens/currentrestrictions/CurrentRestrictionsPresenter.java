package com.rainmachine.presentation.screens.currentrestrictions;

import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsDetails;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class CurrentRestrictionsPresenter extends BasePresenter<CurrentRestrictionsContract.View>
        implements CurrentRestrictionsContract.Presenter {

    private CurrentRestrictionsContract.Container container;
    private GetRestrictionsDetails getRestrictionsDetails;
    private CompositeDisposable disposables;

    CurrentRestrictionsPresenter(CurrentRestrictionsContract.Container container,
                                 GetRestrictionsDetails getRestrictionsDetails) {
        this.container = container;
        this.getRestrictionsDetails = getRestrictionsDetails;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(CurrentRestrictionsContract.View view) {
        super.attachView(view);

        view.setup();
        view.showProgress();
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void start() {
        refresh();
    }

    @Override
    public void onClickRetry() {
        refresh();
    }

    @Override
    public void onClickSnooze() {
        container.goToSnoozeScreen();
    }

    @Override
    public void onClickRainSensor() {
        container.goToRainSensorScreen();
    }

    @Override
    public void onClickFreezeProtect() {
        container.goToRestrictionsScreen();
    }

    @Override
    public void onClickMonth() {
        container.goToRestrictionsScreen();
    }

    @Override
    public void onClickDay() {
        container.goToRestrictionsScreen();
    }

    @Override
    public void onClickHour() {
        container.goToHoursScreen();
    }

    private void refresh() {
        view.showProgress();
        Observable<CurrentRestrictionsViewModel> observable = getRestrictionsDetails
                .execute(new GetRestrictionsDetails.RequestModel(true))
                .map(responseModel -> {
                    CurrentRestrictionsViewModel viewModel = new CurrentRestrictionsViewModel();
                    viewModel.numActiveRestrictions = responseModel.numActiveRestrictions;
                    viewModel.isRainDelay = responseModel.rainDelay;
                    viewModel.rainDelayCounterRemaining = responseModel.rainDelayCounter;
                    viewModel.isRainSensor = responseModel.rainSensor;
                    if (viewModel.isRainSensor) {
                        viewModel.rainSensorSnoozeDuration = responseModel.rainSensorSnoozeDuration;
                    }
                    viewModel.isFreezeProtect = responseModel.freeze;
                    if (viewModel.isFreezeProtect) {
                        viewModel.isUnitsMetric = responseModel.isUnitsMetric;
                        viewModel.freezeProtectTemp = responseModel.globalRestrictions
                                .freezeProtectTemperature(responseModel.isUnitsMetric);
                    }
                    viewModel.isMonth = responseModel.month;
                    if (viewModel.isMonth) {
                        viewModel.monthOfYear = responseModel.sprinklerLocalDateTime.monthOfYear();
                    }
                    viewModel.isDay = responseModel.weekDay;
                    if (viewModel.isDay) {
                        viewModel.dayOfWeek = responseModel.sprinklerLocalDateTime.dayOfWeek();
                    }
                    viewModel.isHour = responseModel.hourly;
                    if (viewModel.isHour) {
                        viewModel.use24HourFormat = responseModel.use24HourFormat;
                        for (HourlyRestriction restriction : responseModel.hourlyRestrictions) {
                            LocalTime startTime = restriction.fromLocalTime();
                            LocalTime endTime = restriction.toLocalTime();
                            LocalTime currentTime = responseModel.sprinklerLocalDateTime
                                    .toLocalTime();
                            if ((restriction.isDaily()
                                    || isRestrictionForDay(restriction, responseModel
                                    .sprinklerLocalDateTime.dayOfWeek()))
                                    && DomainUtils.isBetweenInclusive(startTime, endTime,
                                    currentTime)) {
                                // We only save the first restriction that applies. There
                                // may be more
                                viewModel.hourlyRestriction = restriction;
                                break;
                            }
                        }
                        // In case of a bug and there is no matching hour restriction, we hide
                        // this type of restriction
                        if (viewModel.hourlyRestriction == null) {
                            viewModel.isHour = false;
                            viewModel.numActiveRestrictions--;
                        }
                    }
                    return viewModel;
                });
        disposables.add(observable
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private boolean isRestrictionForDay(HourlyRestriction restriction, LocalDateTime.Property
            dayOfWeek) {
        return restriction.weekDays[dayOfWeek.get() - 1];
    }

    private final class RefreshSubscriber extends DisposableObserver<CurrentRestrictionsViewModel> {

        @Override
        public void onNext(CurrentRestrictionsViewModel viewModel) {
            if (viewModel.numActiveRestrictions > 0) {
                view.updateContent(viewModel);
                view.showContent();
            } else {
                container.closeScreen();
            }
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
