package com.rainmachine.presentation.screens.savehourlyrestriction;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;

import io.reactivex.Completable;
import io.reactivex.Observable;

class SaveHourlyRestrictionMixer {

    private SprinklerRepositoryImpl sprinklerRepository;
    private GetRestrictionsLive getRestrictionsLive;
    private Features features;

    SaveHourlyRestrictionMixer(SprinklerRepositoryImpl sprinklerRepository,
                               GetRestrictionsLive getRestrictionsLive, Features features) {
        this.sprinklerRepository = sprinklerRepository;
        this.getRestrictionsLive = getRestrictionsLive;
        this.features = features;
    }

    Observable<Irrelevant> saveHourlyRestriction(final HourlyRestriction restriction) {
        Completable stream;
        if (!restriction.isNewRestriction()) {
            if (features.canUpdateHourlyRestriction()) {
                stream = sprinklerRepository.updateHourlyRestriction(restriction);
            } else {
                // Small hack to modify an existing restriction: delete and create a new one
                stream = sprinklerRepository
                        .deleteHourlyRestriction(restriction.uid)
                        .andThen(sprinklerRepository.saveHourlyRestriction(restriction));
            }
        } else {
            stream = sprinklerRepository.saveHourlyRestriction(restriction);
        }
        return stream
                .doOnComplete(() -> getRestrictionsLive.forceRefresh())
                .andThen(Observable.just(Irrelevant.INSTANCE))
                .compose(RunToCompletion.instance());
    }
}