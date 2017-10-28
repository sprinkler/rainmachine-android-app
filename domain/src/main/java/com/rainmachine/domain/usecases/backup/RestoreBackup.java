package com.rainmachine.domain.usecases.backup;

import com.rainmachine.domain.boundary.data.BackupRepository;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.model.BackupInfo;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.domain.notifiers.StatsNeedRefreshNotifier;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class RestoreBackup extends ObservableUseCase<RestoreBackup.RequestModel, RestoreBackup
        .ResponseModel> {

    private final SprinklerRepository sprinklerRepository;
    private final BackupRepository backupRepository;
    private final Features features;
    private final SprinklerState sprinklerState;
    private final StatsNeedRefreshNotifier statsNeedRefreshNotifier;

    public RestoreBackup(SprinklerRepository sprinklerRepository,
                         BackupRepository backupRepository,
                         Features features, SprinklerState sprinklerState,
                         StatsNeedRefreshNotifier statsNeedRefreshNotifier) {
        this.sprinklerRepository = sprinklerRepository;
        this.backupRepository = backupRepository;
        this.features = features;
        this.sprinklerState = sprinklerState;
        this.statsNeedRefreshNotifier = statsNeedRefreshNotifier;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        return Observable.combineLatest(
                backupRepository
                        .getBackupForPrograms(requestModel._backupDeviceDatabaseId, requestModel
                                .position).toObservable()
                        .flatMap(backupInfo -> restoreProgramsAndRefresh(requestModel
                                .currentDeviceId, requestModel.deviceId, backupInfo)),
                backupRepository
                        .getBackupForZonesProperties(requestModel._backupDeviceDatabaseId,
                                requestModel.position).toObservable()
                        .flatMap(backupInfo -> {
                            if (backupInfo != BackupInfo.NOT_FOUND) {
                                return restoreZonesProperties(backupInfo);
                            } else {
                                return Observable.just(new ZonesPropertiesOutcome(true));
                            }
                        }),
                backupRepository
                        .getBackupForGlobalRestrictions(requestModel._backupDeviceDatabaseId,
                                requestModel.position).toObservable()
                        .flatMap(backupInfo -> {
                            // for SPK1 we do not have global restrictions
                            if (backupInfo != BackupInfo.NOT_FOUND && !features.isSpk1()) {
                                return restoreGlobalRestrictions(backupInfo);
                            } else {
                                return Observable.just(new GlobalRestrictionsOutcome(true));
                            }
                        }),
                backupRepository
                        .getBackupForHourlyRestrictions(requestModel._backupDeviceDatabaseId,
                                requestModel.position).toObservable()
                        .flatMap(backupInfo -> {
                            // for SPK1 we do not have hourly restrictions
                            if (backupInfo != BackupInfo.NOT_FOUND && !features.isSpk1()) {
                                return restoreHourlyRestrictions(backupInfo.body);
                            } else {
                                return Observable.just(new HourlyRestrictionsOutcome(true));
                            }
                        }),
                backupRepository
                        .getBackupForZonesProperties(requestModel._backupDeviceDatabaseId,
                                requestModel.position).toObservable()
                        .flatMap(backupInfo -> {
                            if (backupInfo != BackupInfo.NOT_FOUND) {
                                return zonePropertiesStatus(backupInfo);
                            } else {
                                return Observable.just(new NumberOfZonesStatus());
                            }
                        }),
                (outcome1, outcome2, outcome3, outcome4, numberOfZonesStatus) -> {
                    ResponseModel responseModel = new ResponseModel();
                    responseModel.isSuccess = outcome1.isSuccess && outcome2
                            .isSuccess && outcome3.isSuccess && outcome4.isSuccess;
                    if (numberOfZonesStatus.numZones == numberOfZonesStatus
                            .zoneProperties.size()) {
                        responseModel.zoneNumberStatus = ResponseModel.ZoneNumberStatus
                                .EQUAL_NUM_ZONES;
                    } else if (numberOfZonesStatus.numZones == 8 &&
                            numberOfZonesStatus.zoneProperties.size() == 12) {
                        responseModel.zoneNumberStatus = ResponseModel.ZoneNumberStatus
                                .ZONES_8_VS_12;
                    } else if (numberOfZonesStatus.numZones == 8 &&
                            numberOfZonesStatus.zoneProperties.size() == 16) {
                        responseModel.zoneNumberStatus = ResponseModel.ZoneNumberStatus
                                .ZONES_8_VS_16;
                    } else if (numberOfZonesStatus.numZones == 12 &&
                            numberOfZonesStatus.zoneProperties.size() == 16) {
                        responseModel.zoneNumberStatus = ResponseModel.ZoneNumberStatus
                                .ZONES_12_VS_16;
                    }
                    return responseModel;
                })
                .onErrorReturn(throwable -> {
                    ResponseModel responseModel = new ResponseModel();
                    responseModel.isSuccess = false;
                    return responseModel;
                })
                .doOnSubscribe(disposable -> sprinklerState.setIsBackupInProgress(true))
                .doAfterTerminate(() -> sprinklerState.setIsBackupInProgress(false));
    }

    private Observable<ProgramOutcome> restoreProgramsAndRefresh(String currentDeviceId,
                                                                 String deviceId,
                                                                 BackupInfo backupInfo) {
        if (backupInfo != BackupInfo.NOT_FOUND) {
            return restorePrograms(currentDeviceId, deviceId, backupInfo)
                    .andThen(sprinklerRepository.runAllParsers()
                            .andThen(waitForParsersToFinishRunning()))
                    .doOnComplete(() -> statsNeedRefreshNotifier.publish(new Object()))
                    .andThen(Observable.just(new ProgramOutcome(true)))
                    .onErrorReturn(throwable -> new ProgramOutcome(false));
        } else {
            return Observable.just(new ProgramOutcome(true));
        }
    }

    private Observable<ZonesPropertiesOutcome> restoreZonesProperties(BackupInfo backupInfo) {
        return zonePropertiesStatus(backupInfo)
                .flatMap(numberOfZonesStatus -> Observable
                        .fromIterable(numberOfZonesStatus.zoneProperties)
                        .take(numberOfZonesStatus.numZones))
                .flatMapCompletable(zoneProperties -> {
                    if (features.useNewApi()) {
                        return sprinklerRepository.saveZoneProperties(zoneProperties);
                    } else {
                        return sprinklerRepository.saveZonesProperties3(zoneProperties);
                    }
                })
                .andThen(Observable.just(new ZonesPropertiesOutcome(true)))
                .onErrorReturn(throwable -> new ZonesPropertiesOutcome(false));
    }

    private Observable<NumberOfZonesStatus> zonePropertiesStatus(BackupInfo backupInfo) {
        Observable<List<ZoneProperties>> stream;
        if (!backupInfo.isOldApiFormat) {
            stream = backupRepository.zonesProperties(backupInfo.body).toObservable();
        } else {
            stream = backupRepository.zonesProperties3(backupInfo.body).toObservable();
        }
        return Observable.combineLatest(
                stream,
                sprinklerRepository.numberOfZones().toObservable(),
                (zoneProperties, numZones) -> {
                    NumberOfZonesStatus numberOfZonesStatus = new NumberOfZonesStatus();
                    numberOfZonesStatus.zoneProperties = zoneProperties;
                    numberOfZonesStatus.numZones = numZones;
                    return numberOfZonesStatus;
                });
    }

    private Completable restorePrograms(String currentDeviceId, String deviceId,
                                        BackupInfo backupInfo) {
        Observable<List<Program>> observableProgramsDevice;
        if (features.useNewApi()) {
            observableProgramsDevice = sprinklerRepository.programs().toObservable();
        } else {
            observableProgramsDevice = sprinklerRepository
                    .programs3().toObservable()
                    .map(listBooleanPair -> listBooleanPair.getValue0());
        }
        Observable<List<Program>> observableProgramsBackup;
        if (!backupInfo.isOldApiFormat) {
            observableProgramsBackup = backupRepository.programs(backupInfo.body).toObservable();
        } else {
            observableProgramsBackup = backupRepository.programs3(backupInfo.body).toObservable();
        }

        return Observable
                .combineLatest(observableProgramsDevice,
                        observableProgramsBackup,
                        (programsDevice, programsBackup) -> {
                            ProgramsStatus status = new ProgramsStatus();
                            if (currentDeviceId.equalsIgnoreCase(deviceId)) {
                                status.programsDeviceToDelete = new ArrayList<>();
                                status.programsBackupToCreate = new ArrayList<>();
                                status.programsBackupToUpdate = new ArrayList<>();
                                for (Program programDevice : programsDevice) {
                                    boolean foundMatch = false;
                                    for (Program programBackup : programsBackup) {
                                        if (programDevice.id == programBackup.id) {
                                            foundMatch = true;
                                            status.programsBackupToUpdate.add(programBackup);
                                            break;
                                        }
                                    }
                                    if (!foundMatch) {
                                        status.programsDeviceToDelete.add(programDevice);
                                    }
                                }

                                for (Program program : programsBackup) {
                                    boolean foundMatch = false;
                                    for (Program programUpdate : status
                                            .programsBackupToUpdate) {
                                        if (program.id == programUpdate.id) {
                                            foundMatch = true;
                                            break;
                                        }
                                    }
                                    if (!foundMatch) {
                                        status.programsBackupToCreate.add(program);
                                    }
                                }
                            } else {
                                status.programsDeviceToDelete = programsDevice;
                                status.programsBackupToCreate = programsBackup;
                                status.programsBackupToUpdate = new ArrayList<>();
                            }
                            return status;
                        })
                .flatMapCompletable(programsStatus -> sprinklerRepository.devicePreferences()
                        .toObservable()
                        .flatMapCompletable(devicePreferences -> {
                            boolean use24HourFormat = devicePreferences.use24HourFormat;
                            return Completable
                                    .mergeArray(deletePrograms(programsStatus
                                                    .programsDeviceToDelete),
                                            createPrograms(programsStatus.programsBackupToCreate,
                                                    use24HourFormat),
                                            updatePrograms(programsStatus.programsBackupToUpdate,
                                                    use24HourFormat));
                        }));
    }

    private Completable deletePrograms(List<Program> programs) {
        return Observable
                .just(programs)
                .flatMap(programs1 -> Observable.fromIterable(programs1))
                .flatMapCompletable(program -> {
                    if (features.useNewApi()) {
                        return sprinklerRepository.deleteProgram(program.id);
                    } else {
                        return sprinklerRepository.deleteProgram3(program.id);
                    }
                });
    }

    private Completable createPrograms(List<Program> programs, final boolean use24HourFormat) {
        return Observable.combineLatest(
                Observable.just(programs),
                sprinklerRepository.numberOfZones().toObservable(),
                (programs1, numZones) -> {
                    if (programs1.size() > 0) {
                        int numBackupZones = programs1.get(0).wateringTimes.size();
                        if (numBackupZones > numZones) {
                            // Remove the extra backup zones in order for the API to work
                            for (Program program : programs1) {
                                Iterator<ProgramWateringTimes> it = program.wateringTimes
                                        .iterator();
                                int count = 0;
                                while (it.hasNext()) {
                                    it.next();
                                    count++;
                                    if (count > numZones) {
                                        it.remove();
                                    }
                                }
                            }
                        }
                        for (Program program : programs1) {
                            // Remove the zones that have ids larger than the largest zone
                            // number possible for this device
                            Iterator<ProgramWateringTimes> it = program.wateringTimes
                                    .iterator();
                            while (it.hasNext()) {
                                ProgramWateringTimes zone = it.next();
                                if (zone.id > numZones) {
                                    it.remove();
                                }
                            }
                        }
                    }
                    return programs1;
                })
                .flatMap(programs1 -> Observable.fromIterable(programs1))
                .flatMapCompletable(program -> {
                    if (features.useNewApi()) {
                        // Need to clear the next run field in order for the API to work
                        program.nextRunSprinklerLocalDate = null;
                        return sprinklerRepository.createProgram(program);
                    } else {
                        return sprinklerRepository.createUpdateProgram3(program, use24HourFormat);
                    }
                });
    }

    private Completable updatePrograms(List<Program> programs, final boolean use24HourFormat) {
        return Observable
                .fromIterable(programs)
                .flatMapCompletable(program -> {
                    if (features.useNewApi()) {
                        return sprinklerRepository.updateProgram(program);
                    } else {
                        return sprinklerRepository.createUpdateProgram3(program, use24HourFormat);
                    }
                });
    }

    private Observable<GlobalRestrictionsOutcome> restoreGlobalRestrictions(BackupInfo backupInfo) {
        return sprinklerRepository
                .saveGlobalRestrictionsRaw(backupInfo.body)
                .andThen(Observable.just(new GlobalRestrictionsOutcome(true)))
                .onErrorReturn(throwable -> new GlobalRestrictionsOutcome(false));
    }

    private Observable<HourlyRestrictionsOutcome> restoreHourlyRestrictions(final String body) {
        return sprinklerRepository
                .hourlyRestrictions().toObservable()
                .flatMap(hourlyRestrictions -> Observable.fromIterable(hourlyRestrictions))
                .flatMapCompletable(hourlyRestriction ->
                        sprinklerRepository.deleteHourlyRestriction(hourlyRestriction.uid))
                .andThen(backupRepository.hourlyRestrictions(body))
                .flatMapObservable(hourlyRestrictions -> Observable.fromIterable
                        (hourlyRestrictions))
                .flatMapCompletable(hourlyRestriction ->
                        sprinklerRepository.saveHourlyRestriction(hourlyRestriction))
                .andThen(Observable.just(new HourlyRestrictionsOutcome(true)))
                .onErrorReturn(throwable -> new HourlyRestrictionsOutcome(false));
    }

    private Completable waitForParsersToFinishRunning() {
        return Observable
                .interval(5, 5, TimeUnit.SECONDS)
                .take(4)
                .switchMapSingle(aLong -> sprinklerRepository.parsers())
                .filter(parsers -> {
                    for (Parser parser : parsers) {
                        if (parser.isRunning) {
                            return false;
                        }
                    }
                    return true;
                })
                .firstElement()
                .ignoreElement();
    }

    public static class RequestModel {
        String currentDeviceId;
        String deviceId;
        Long _backupDeviceDatabaseId;
        int position;

        public RequestModel(String currentDeviceId, String deviceId, Long
                _backupDeviceDatabaseId, int position) {
            this.currentDeviceId = currentDeviceId;
            this.deviceId = deviceId;
            this._backupDeviceDatabaseId = _backupDeviceDatabaseId;
            this.position = position;
        }
    }

    public static class ResponseModel {
        public boolean isSuccess;
        public ZoneNumberStatus zoneNumberStatus;

        public enum ZoneNumberStatus {EQUAL_NUM_ZONES, ZONES_8_VS_12, ZONES_8_VS_16, ZONES_12_VS_16}
    }

    private static class ProgramsStatus {
        List<Program> programsDeviceToDelete;
        List<Program> programsBackupToUpdate;
        List<Program> programsBackupToCreate;
    }

    private static class NumberOfZonesStatus {
        List<ZoneProperties> zoneProperties; // from backup
        int numZones; // from current device
    }

    private static class ProgramOutcome {
        public boolean isSuccess;

        ProgramOutcome(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }
    }

    private static class ZonesPropertiesOutcome {
        public boolean isSuccess;

        ZonesPropertiesOutcome(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }
    }

    private static class GlobalRestrictionsOutcome {
        public boolean isSuccess;

        GlobalRestrictionsOutcome(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }
    }

    private static class HourlyRestrictionsOutcome {
        public boolean isSuccess;

        HourlyRestrictionsOutcome(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }
    }
}
