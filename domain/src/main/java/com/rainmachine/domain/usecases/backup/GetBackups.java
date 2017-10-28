package com.rainmachine.domain.usecases.backup;

import com.rainmachine.domain.boundary.data.BackupRepository;
import com.rainmachine.domain.model.DeviceBackup;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.Observable;

public class GetBackups extends ObservableUseCase<GetBackups.RequestModel, GetBackups
        .ResponseModel> {

    private BackupRepository backupRepository;

    public GetBackups(BackupRepository backupRepository) {
        this.backupRepository = backupRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        return backupRepository
                .getAllBackups()
                .map(backupDevices -> new ResponseModel(backupDevices))
                .toObservable();
    }

    public static class RequestModel {
    }

    public static class ResponseModel {
        public List<DeviceBackup> deviceBackups;

        public ResponseModel(List<DeviceBackup> deviceBackups) {
            this.deviceBackups = deviceBackups;
        }
    }
}
