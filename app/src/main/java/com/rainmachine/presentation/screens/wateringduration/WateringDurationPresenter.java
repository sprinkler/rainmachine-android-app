package com.rainmachine.presentation.screens.wateringduration;


import com.jakewharton.rxrelay2.PublishRelay;
import com.rainmachine.domain.usecases.wateringduration.GetWateringDurationForZones;
import com.rainmachine.domain.usecases.wateringduration.SaveWateringDuration;
import com.rainmachine.domain.util.SchedulerProvider;
import com.rainmachine.presentation.screens.wateringduration.event.ClickZoneEvent;
import com.rainmachine.presentation.screens.wateringduration.event.ClickZoneResult;
import com.rainmachine.presentation.screens.wateringduration.event.RefreshEvent;
import com.rainmachine.presentation.screens.wateringduration.event.RefreshResult;
import com.rainmachine.presentation.screens.wateringduration.event.SaveWateringDurationEvent;
import com.rainmachine.presentation.screens.wateringduration.event.SaveWateringDurationResult;
import com.rainmachine.presentation.screens.wateringduration.event.WateringDurationResult;
import com.rainmachine.presentation.screens.wateringduration.event.WateringDurationViewEvent;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class WateringDurationPresenter extends BasePresenter<WateringDurationContract.View>
        implements WateringDurationContract.Presenter {

    private final CompositeDisposable disposables;
    private final PublishRelay<WateringDurationViewEvent> flowRelay;
    private final GetWateringDurationForZones getWateringDurationForZones;
    private final SaveWateringDuration saveWateringDuration;
    private final SchedulerProvider schedulerProvider;

    WateringDurationPresenter(GetWateringDurationForZones getWateringDurationForZones,
                              SaveWateringDuration saveWateringDuration,
                              SchedulerProvider schedulerProvider) {
        this.getWateringDurationForZones = getWateringDurationForZones;
        this.saveWateringDuration = saveWateringDuration;
        this.schedulerProvider = schedulerProvider;
        disposables = new CompositeDisposable();
        flowRelay = PublishRelay.create();
    }

    @Override
    public void init() {
        WateringDurationViewModel initialViewModel = WateringDurationViewModel.justInitialize();
        setupFlow(initialViewModel);

        flowRelay.accept(new RefreshEvent());
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onClickZone(ZoneViewModel zone) {
        flowRelay.accept(new ClickZoneEvent(zone));
    }

    @Override
    public void onClickSaveWateringDuration(ZoneViewModel zone) {
        flowRelay.accept(new SaveWateringDurationEvent(zone));
    }

    @Override
    public void onClickRetry() {
        flowRelay.accept(new RefreshEvent());
    }

    private void setupFlow(WateringDurationViewModel initialViewModel) {
        disposables.add(flowRelay
                .compose(new EventsMerger(getWateringDurationForZones, saveWateringDuration))
                .compose(new StateReducer(initialViewModel))
                .doOnError(GenericErrorDealer.INSTANCE)
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new FlowSubscriber()));
    }

    private static class EventsMerger implements ObservableTransformer<WateringDurationViewEvent,
            WateringDurationResult> {

        private final GetWateringDurationForZones getWateringDurationForZones;
        private final SaveWateringDuration saveWateringDuration;

        EventsMerger(GetWateringDurationForZones getWateringDurationForZones,
                     SaveWateringDuration saveWateringDuration) {
            this.getWateringDurationForZones = getWateringDurationForZones;
            this.saveWateringDuration = saveWateringDuration;
        }

        @Override
        public ObservableSource<WateringDurationResult> apply
                (Observable<WateringDurationViewEvent> upstream) {
            return upstream.publish(shared -> Observable.merge(
                    shared.ofType(RefreshEvent.class)
                            .flatMap(ignored -> getWateringDurationForZones.execute(new
                                    GetWateringDurationForZones.RequestModel())
                                    .map(responseModel -> new RefreshResult(responseModel))),
                    shared.ofType(ClickZoneEvent.class)
                            .flatMap(event
                                    -> Observable.just(new ClickZoneResult(event.zone))),
                    shared.ofType(SaveWateringDurationEvent.class)
                            .flatMap(event -> saveWateringDuration.execute(
                                    new SaveWateringDuration.RequestModel(event.zone.id, event
                                            .zone.durationSeconds))
                                    .map(responseModel
                                            -> new SaveWateringDurationResult(event.zone,
                                            responseModel)))));
        }
    }

    private static class StateReducer implements ObservableTransformer<WateringDurationResult,
            WateringDurationViewModel> {

        private WateringDurationViewModel initialViewModel;

        StateReducer(WateringDurationViewModel initialViewModel) {
            this.initialViewModel = initialViewModel;
        }

        @Override
        public ObservableSource<WateringDurationViewModel> apply
                (Observable<WateringDurationResult> upstream) {
            return upstream.scan(initialViewModel, (viewModel, result) -> {
                if (result instanceof RefreshResult) {
                    return reduce((RefreshResult) result);
                } else if (result instanceof ClickZoneResult) {
                    return reduce(viewModel, (ClickZoneResult) result);
                } else if (result instanceof SaveWateringDurationResult) {
                    return reduce(viewModel, (SaveWateringDurationResult) result);
                }
                throw new IllegalArgumentException(
                        "No view model representation for this kind of result");
            });
        }


        private WateringDurationViewModel reduce(RefreshResult result) {
            GetWateringDurationForZones.ResponseModel responseModel = result.responseModel;
            if (responseModel.inFlight) {
                return WateringDurationViewModel.progress();
            } else if (responseModel.isError) {
                return WateringDurationViewModel.error();
            } else {
                List<SectionViewModel> sections = new ArrayList<>();
                List<ZoneViewModel> enabledZones = new ArrayList<>();
                List<ZoneViewModel> disabledZones = new ArrayList<>();
                for (GetWateringDurationForZones.ZoneDto zoneDto : responseModel
                        .zones) {
                    ZoneViewModel zone = map(zoneDto);
                    if (zoneDto.isEnabled) {
                        enabledZones.add(zone);
                    } else {
                        disabledZones.add(zone);
                    }
                }
                if (enabledZones.size() > 0) {
                    SectionViewModel sectionEnabled = new SectionViewModel(SectionViewModel
                            .Type.ACTIVE, enabledZones);
                    sections.add(sectionEnabled);
                }
                if (disabledZones.size() > 0) {
                    SectionViewModel sectionDisabled = new SectionViewModel(SectionViewModel
                            .Type.INACTIVE, disabledZones);
                    sections.add(sectionDisabled);
                }
                return WateringDurationViewModel.content(sections);
            }
        }

        private ZoneViewModel map(GetWateringDurationForZones.ZoneDto zoneDto) {
            return new ZoneViewModel(zoneDto.id, zoneDto.name, zoneDto.durationSeconds);
        }

        private WateringDurationViewModel reduce(WateringDurationViewModel viewModel,
                                                 ClickZoneResult result) {
            viewModel.showZoneDialog = true;
            viewModel.zoneForDialog = result.zone;
            viewModel.isContent = false;
            return viewModel;
        }

        private WateringDurationViewModel reduce(WateringDurationViewModel viewModel,
                                                 SaveWateringDurationResult result) {
            SaveWateringDuration.ResponseModel responseModel = result.responseModel;
            if (responseModel.inFlight) {
                viewModel.isProgress = true;
                viewModel.isContent = false;
                viewModel.isError = false;
                return viewModel;
            } else if (responseModel.isError) {
                viewModel.isError = true;
                viewModel.isProgress = false;
                viewModel.isContent = false;
                return viewModel;
            } else {
                viewModel.isContent = true;
                ZoneViewModel zone = result.zone;
                for (SectionViewModel sections : viewModel.sections) {
                    for (ZoneViewModel zone1 : sections.zones) {
                        if (zone1.id == zone.id) {
                            zone1.durationSeconds = zone.durationSeconds;
                            break;
                        }
                    }
                }
                viewModel.isProgress = false;
                viewModel.isError = false;
                return viewModel;
            }
        }
    }

    private final class FlowSubscriber extends DisposableObserver<WateringDurationViewModel> {

        @Override
        public void onNext(WateringDurationViewModel viewModel) {
            view.render(viewModel);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.render(WateringDurationViewModel.error());
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
