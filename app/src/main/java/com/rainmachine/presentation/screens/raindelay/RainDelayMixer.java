package com.rainmachine.presentation.screens.raindelay;

import android.content.Context;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletionSingle;
import com.rainmachine.presentation.util.Toasts;

import io.reactivex.Observable;
import io.reactivex.Single;

class RainDelayMixer {

    private Context context;
    private Features features;
    private SprinklerRepositoryImpl sprinklerRepository;
    private GetRestrictionsLive getRestrictionsLive;

    RainDelayMixer(Context context, Features features, SprinklerRepositoryImpl
            sprinklerRepository, GetRestrictionsLive getRestrictionsLive) {
        this.context = context;
        this.features = features;
        this.sprinklerRepository = sprinklerRepository;
        this.getRestrictionsLive = getRestrictionsLive;
    }

    Observable<RainDelayViewModel> refresh() {
        Single<Long> stream;
        if (features.useNewApi()) {
            stream = sprinklerRepository.rainDelay();
        } else {
            stream = sprinklerRepository.rainDelay3();
        }
        return stream
                .map(counterRemaining -> {
                    RainDelayViewModel viewModel = new RainDelayViewModel();
                    viewModel.counterRemaining = counterRemaining;
                    viewModel.showSnoozePhrasing = features.showSnoozePhrasing();
                    viewModel.showGranularContent = features.hasRainDelayMicroFunctionality();
                    return viewModel;
                })
                .toObservable();
    }

    Observable<Irrelevant> saveRainDelay(final int rainDelayDurationDays) {
        final boolean doSnooze = rainDelayDurationDays != 0;
        Single<Irrelevant> stream;
        if (features.useNewApi()) {
            stream = sprinklerRepository.saveRainDelay(rainDelayDurationDays);
        } else {
            stream = sprinklerRepository.saveRainDelay3(rainDelayDurationDays);
        }
        return stream
                .doOnSuccess(irrelevant -> getRestrictionsLive.forceRefresh())
                .doOnSuccess(irrelevant -> {
                    final String snoozeMessage = features.showSnoozePhrasing() ? context
                            .getString(R.string.rain_delay_success_snooze) : context
                            .getString(R.string
                                    .rain_delay_success_set);
                    final String successMsg = doSnooze ? snoozeMessage : context.getString(R
                            .string.rain_delay_success_resume);
                    Toasts.show(successMsg);
                })
                .compose(RunToCompletionSingle.instance())
                .toObservable();
    }

    Observable<Irrelevant> saveSnooze(int numSeconds) {
        return sprinklerRepository
                .saveRainDelayRestriction(numSeconds)
                .doOnSuccess(irrelevant -> getRestrictionsLive.forceRefresh())
                .doOnSuccess(irrelevant -> Toasts.show(R.string.rain_delay_success_snooze))
                .compose(RunToCompletionSingle.instance())
                .toObservable();
    }
}
