package com.rainmachine.domain.usecases.watering;

import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.WateringQueueItem;
import com.rainmachine.domain.model.Zone;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class GetWateringLive extends ObservableUseCase<GetWateringLive.RequestModel, GetWateringLive
        .ResponseModel> {

    private SprinklerRepository sprinklerRepository;
    private SprinklerState sprinklerState;
    private Features features;

    private Observable<ResponseModel> stream;
    private Relay<Long> forceRefresh;
    private ResponseModel latest;
    private boolean fakeRefresherActive;

    public GetWateringLive(SprinklerRepository sprinklerRepository, SprinklerState sprinklerState,
                           Features features) {
        this.sprinklerRepository = sprinklerRepository;
        this.sprinklerState = sprinklerState;
        this.features = features;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        if (stream == null) {
            forceRefresh = PublishRelay.<Long>create().toSerialized();
            Observable<Long> interval = Observable.interval(0, 5, TimeUnit.SECONDS);

            Observable<ResponseModel> network = Observable.merge(forceRefresh, interval)
                    .filter(step -> !sprinklerState.isRefreshersBlocked())
                    .switchMap(step -> {
                        if (features.isAtLeastSpk2()) {
                            return api();
                        } else {
                            return apiSPK1();
                        }
                    })
                    .doOnNext(responseModel -> latest = responseModel)
                    .doOnNext(responseModel -> fakeRefresherActive = responseModel.runningCounter
                            >= 0);

            stream = Observable
                    .merge(network, fake())
                    .replay(1)
                    .refCount();
        } else {
            if (requestModel.forceRefresh) {
                forceRefresh.accept(-1L);
            }
        }
        return stream;
    }

    private Observable<ResponseModel> api() {
        return sprinklerRepository
                .wateringQueue().toObservable()
                .concatMap(queue -> {
                    final TransitionData transitionData = new TransitionData();
                    transitionData.queue = queue;
                    Observable<TransitionData> observable = Observable.just(transitionData);
                    if (queue.size() > 0) {
                        final WateringQueueItem item = queue.get(0);
                        if (item.wateringState == WateringQueueItem.WateringState
                                .MASTER_VALVE_START || item.wateringState ==
                                WateringQueueItem.WateringState.MASTER_VALVE_STOP || item
                                .wateringState == WateringQueueItem.WateringState.ZONE_RUNNING) {
                            observable = observable
                                    .concatMap(transitionData1 -> zones(transitionData1));
                        }

                        if (item.programId > 0) {
                            observable = observable
                                    .concatMap(transitionData1 -> programs(transitionData1));
                        }
                    }
                    return observable;
                })
                .map(transitionData -> buildResponse(transitionData.queue, transitionData.zones,
                        transitionData.programs))
                .onErrorResumeNext(Observable.empty());
    }

    private static class TransitionData {
        List<WateringQueueItem> queue;
        List<Zone> zones;
        List<Program> programs;
    }

    private Observable<TransitionData> programs(final TransitionData transitionData) {
        return sprinklerRepository
                .programs().toObservable()
                .map(programs -> {
                    transitionData.programs = programs;
                    return transitionData;
                });
    }

    private Observable<TransitionData> zones(final TransitionData transitionData) {
        return sprinklerRepository
                .zones().toObservable()
                .map(zones -> {
                    transitionData.zones = zones;
                    return transitionData;
                });
    }

    private Observable<ResponseModel> fake() {
        return Observable
                .interval(0, 1, TimeUnit.SECONDS)
                .filter(aLong -> fakeRefresherActive)
                .concatMap(aLong -> {
                    latest.runningCounter--;
                    return Observable.just(latest);
                })
                .filter(responseModel -> responseModel.runningCounter >= 0)
                .doOnNext(responseModel -> {
                    if (responseModel.runningCounter == 0) {
                        fakeRefresherActive = false;
                        forceRefresh.accept(-1L);
                    }
                });
    }

    /*
    The zones and programs params may be null but they are not being used at all when they are null
     */
    private ResponseModel buildResponse(List<WateringQueueItem> queue, List<Zone> zones,
                                        List<Program> programs) {
        ResponseModel responseModel = new ResponseModel();
        if (queue.size() > 0) {
            WateringQueueItem item = queue.get(0);
            switch (item.wateringState) {
                case MASTER_VALVE_START:
                    responseModel.wateringState = ResponseModel.WateringState.MASTER_VALVE_START;
                    for (Zone zone : zones) {
                        if (zone.isMaster) {
                            setRunningCounter(responseModel, zone.counter);
                            break;
                        }
                    }
                    break;
                case MASTER_VALVE_STOP:
                    responseModel.wateringState = ResponseModel.WateringState.MASTER_VALVE_STOP;
                    for (Zone zone : zones) {
                        if (zone.isMaster) {
                            setRunningCounter(responseModel, zone.counter);
                            break;
                        }
                    }
                    break;
                case DELAY_BETWEEN_ZONES:
                    responseModel.wateringState = ResponseModel.WateringState.DELAY_BETWEEN_ZONES;
                    break;
                case SOAKING:
                    responseModel.wateringState = ResponseModel.WateringState.SOAKING;
                    break;
                case ZONE_RUNNING:
                    responseModel.wateringState = ResponseModel.WateringState.ZONE_RUNNING;
                    responseModel.zoneId = item.zid;
                    for (Zone zone : zones) {
                        if (item.zid == zone.id) {
                            setRunningCounter(responseModel, zone.counter);
                            responseModel.zoneName = zone.name;
                            break;
                        }
                    }
                    break;
            }

            if (item.programId > 0) {
                responseModel.isProgramRunning = true;
                responseModel.numTotalCycles = item.cycles;
                responseModel.currentCycle = item.cycle;
                responseModel.showCycle = responseModel.numTotalCycles > 1
                        && responseModel.wateringState != ResponseModel.WateringState.SOAKING;
                // For soaking we need the cycle before it so we need to subtract 1 from what the
                // API returns
                if (responseModel.wateringState == ResponseModel.WateringState.SOAKING) {
                    responseModel.currentCycle = responseModel.currentCycle - 1;
                }
                for (Program program : programs) {
                    if (program.id == item.programId) {
                        responseModel.programName = program.name;
                        break;
                    }
                }
            }
            responseModel.isManual = item.manual;
            fakeRefresherActive = responseModel.runningCounter > 0;
        } else {
            responseModel.wateringState = ResponseModel.WateringState.IDLE;
            fakeRefresherActive = false;
        }
        return responseModel;
    }

    private void setRunningCounter(ResponseModel data, int counter) {
        ResponseModel latestResponse = latest;
        if (latestResponse == null || Math.abs(latestResponse.runningCounter - counter) >= 2) {
            data.runningCounter = counter;
        } else {
            data.runningCounter = latestResponse.runningCounter;
        }
    }

    private Observable<ResponseModel> apiSPK1() {
        Observable<List<Zone>> observable;
        if (features.useNewApi()) {
            observable = sprinklerRepository.zones().toObservable();
        } else {
            observable = sprinklerRepository
                    .zones3().toObservable()
                    .flatMap(zones -> {
                        // We do not get the counter for the zones so we need another API call
                        for (Zone zone : zones) {
                            if (zone.isWatering()) {
                                return sprinklerRepository
                                        .zone3(zone.id).toObservable()
                                        .map(wateringZone -> {
                                            for (Zone zone1 : zones) {
                                                if (zone1.id == wateringZone.id) {
                                                    zone1.counter = wateringZone.counter;
                                                }
                                            }
                                            return zones;
                                        });
                            }
                        }
                        return Observable.just(zones);
                    });
        }
        return observable.map(zones -> buildResponseSPK1(zones));
    }

    private ResponseModel buildResponseSPK1(List<Zone> zones) {
        ResponseModel data = new ResponseModel();
        data.wateringState = ResponseModel.WateringState.IDLE;
        for (Zone zone : zones) {
            if (zone.isWatering()) {
                data.wateringState = ResponseModel.WateringState.ZONE_RUNNING;
                data.zoneName = zone.name;
                data.zoneId = (int) zone.id;
                setRunningCounter(data, zone.counter);
                break;
            }
        }
        data.isManual = true; // We say it is manual although we cannot know
        fakeRefresherActive = data.runningCounter > 0;
        return data;
    }

    public static class RequestModel {
        boolean forceRefresh;

        public RequestModel(boolean forceRefresh) {
            this.forceRefresh = forceRefresh;
        }
    }

    public static class ResponseModel {
        public WateringState wateringState;
        public int runningCounter;
        public String zoneName; // if a zone is currently running
        public String programName; // if a program is currently running
        public int zoneId; // if a zone is currently running
        public boolean isProgramRunning;
        public int currentCycle;
        public int numTotalCycles;
        public boolean showCycle;
        public boolean isManual;

        public enum WateringState {
            IDLE, ZONE_RUNNING, MASTER_VALVE_START, MASTER_VALVE_STOP, DELAY_BETWEEN_ZONES, SOAKING
        }

        public boolean isWateringIdle() {
            return wateringState == WateringState.IDLE;
        }
    }
}
