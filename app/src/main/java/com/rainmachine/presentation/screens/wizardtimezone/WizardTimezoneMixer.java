package com.rainmachine.presentation.screens.wizardtimezone;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import io.reactivex.Observable;

class WizardTimezoneMixer {

    private SprinklerRepositoryImpl sprinklerRepository;

    WizardTimezoneMixer(SprinklerRepositoryImpl sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    Observable<WizardTimezoneViewModel> refresh() {
        return Observable.combineLatest(
                sprinklerRepository.provision().toObservable(),
                sprinklerRepository.timeDate().toObservable(),
                (provision, sprinklerLocalDateTime) -> buildViewModel(provision,
                        sprinklerLocalDateTime));
    }

    private WizardTimezoneViewModel buildViewModel(Provision provision, LocalDateTime
            sprinklerLocalDateTime) {
        WizardTimezoneViewModel viewModel = new WizardTimezoneViewModel();
        viewModel.timezone = provision.location.timezone;
        viewModel.localDateTime = sprinklerLocalDateTime;
        return viewModel;
    }

    Observable<Irrelevant> saveTimeDateTimezone(final LocalDate date, final
    LocalTime time, final String timezone) {
        return Observable.combineLatest(
                sprinklerRepository.saveTimezone(timezone).toObservable(),
                sprinklerRepository.saveTimeDate(date, time).toObservable(),
                saveProvisionDefaults(),
                (irrelevant, irrelevant1, irrelevant2) -> Irrelevant.INSTANCE)
                .compose(RunToCompletion.instance());
    }

    private Observable<Irrelevant> saveProvisionDefaults() {
        return sprinklerRepository.saveProvisionDefaults(30, 1.5f).toObservable();
    }
}
