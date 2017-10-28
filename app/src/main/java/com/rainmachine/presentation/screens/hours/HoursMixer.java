package com.rainmachine.presentation.screens.hours;

import android.content.Context;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.presentation.util.Toasts;

import java.util.List;

import io.reactivex.Observable;

class HoursMixer {

    private Context context;
    private SprinklerRepositoryImpl sprinklerRepository;
    private GetRestrictionsLive getRestrictionsLive;

    HoursMixer(Context context, SprinklerRepositoryImpl sprinklerRepository,
               GetRestrictionsLive getRestrictionsLive) {
        this.context = context;
        this.sprinklerRepository = sprinklerRepository;
        this.getRestrictionsLive = getRestrictionsLive;
    }

    Observable<HoursViewModel> refresh() {
        return Observable.combineLatest(
                sprinklerRepository.hourlyRestrictions().toObservable(),
                sprinklerRepository.devicePreferences().toObservable(),
                (hourlyRestrictions, devicePreferences) -> {
                    HoursViewModel viewModel = new HoursViewModel();
                    viewModel.hourlyRestrictions = hourlyRestrictions;
                    viewModel.use24HourFormat = devicePreferences.use24HourFormat;
                    return viewModel;
                });
    }

    Observable<HoursViewModel> deleteHourlyRestrictions(final List<Long> restrictionIds) {
        return Observable
                .fromIterable(restrictionIds)
                .flatMapCompletable(restrictionId ->
                        sprinklerRepository.deleteHourlyRestriction(restrictionId))
                .doOnComplete(() -> {
                    int noDeleted = restrictionIds.size();
                    String msg = context.getResources().getQuantityString(R.plurals
                            .hours_restrictions_deleted, noDeleted, noDeleted);
                    Toasts.show(msg);
                    getRestrictionsLive.forceRefresh();
                })
                .andThen(refresh())
                .compose(RunToCompletion.instance());
    }
}
