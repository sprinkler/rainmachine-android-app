package com.rainmachine.presentation.screens.restorebackup;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.model.DeviceBackup;
import com.rainmachine.domain.usecases.backup.GetBackups;
import com.rainmachine.domain.usecases.backup.RestoreBackup;
import com.rainmachine.domain.util.RunToCompletion;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.reactivex.Observable;

class RestoreBackupMixer {

    private SprinklerRepositoryImpl sprinklerRepository;
    private RestoreBackup restoreBackup;
    private GetBackups getBackups;
    private Device device;

    RestoreBackupMixer(SprinklerRepositoryImpl sprinklerRepository, RestoreBackup restoreBackup,
                       GetBackups getBackups, Device device) {
        this.sprinklerRepository = sprinklerRepository;
        this.restoreBackup = restoreBackup;
        this.getBackups = getBackups;
        this.device = device;
    }

    Observable<RestoreBackupViewModel> refresh() {
        return Observable.combineLatest(
                getBackups
                        .execute(new GetBackups.RequestModel())
                        .map(responseModel -> responseModel.deviceBackups),
                sprinklerRepository.devicePreferences().toObservable(),
                (backupDevices, devicePreferences) -> {
                    RestoreBackupViewModel viewModel = new RestoreBackupViewModel();
                    viewModel.backupDevices = new ArrayList<>(backupDevices.size());
                    for (DeviceBackup deviceBackup : backupDevices) {
                        if (deviceBackup.entries.size() == 0) {
                            continue;
                        }
                        RestoreBackupViewModel.BackupDeviceData item = new
                                RestoreBackupViewModel.BackupDeviceData();
                        item._databaseId = deviceBackup._databaseId;
                        item.deviceId = deviceBackup.deviceId;
                        item.name = deviceBackup.name;
                        item.deviceType = deviceBackup.deviceType;
                        item.use24HourFormat = devicePreferences.use24HourFormat;
                        item.backups = new ArrayList<>();
                        viewModel.backupDevices.add(item);
                        for (int i = 0; i < deviceBackup.entries.size(); i++) {
                            DeviceBackup.BackupEntry entry = deviceBackup.entries.get(i);
                            RestoreBackupViewModel.Backup backup = new RestoreBackupViewModel
                                    .Backup();
                            backup.position = i;
                            backup.localDateTime = entry.localDateTime;
                            item.backups.add(backup);
                        }
                    }
                    // Find the date of the latest backup
                    for (RestoreBackupViewModel.BackupDeviceData device : viewModel.backupDevices) {
                        LocalDateTime lastDateTime = null;
                        for (RestoreBackupViewModel.Backup backup : device.backups) {
                            if (lastDateTime == null || lastDateTime.isBefore(backup
                                    .localDateTime)) {
                                lastDateTime = backup.localDateTime;
                            }
                        }
                        device.lastBackupLocalDateTime = lastDateTime;
                        Collections.sort(device.backups, backupComparator);
                    }
                    return viewModel;
                });
    }

    private Comparator<RestoreBackupViewModel.Backup> backupComparator = (lhs, rhs) -> {
        if (lhs.localDateTime.isBefore(rhs.localDateTime)) {
            return 1;
        } else {
            return -1;
        }
    };

    Observable<RestoreBackupOutcomeViewModel> restoreBackup(String deviceId,
                                                            Long _backupDeviceDatabaseId,
                                                            RestoreBackupViewModel.Backup backup) {
        return restoreBackup
                .execute(new RestoreBackup.RequestModel(device.deviceId, deviceId,
                        _backupDeviceDatabaseId, backup.position))
                .map(backupOutcome -> {
                    RestoreBackupOutcomeViewModel viewModel = new RestoreBackupOutcomeViewModel();
                    viewModel.backupOutcome = backupOutcome;
                    return viewModel;
                })
                .compose(RunToCompletion.instance());
    }
}