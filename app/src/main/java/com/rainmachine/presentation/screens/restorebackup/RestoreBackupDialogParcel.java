package com.rainmachine.presentation.screens.restorebackup;

import org.parceler.Parcel;

@Parcel
class RestoreBackupDialogParcel {
    RestoreBackupViewModel.Backup backup;
    Long _backupDeviceDatabaseId;
    String deviceId;
}
