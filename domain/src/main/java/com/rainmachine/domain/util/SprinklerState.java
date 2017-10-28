package com.rainmachine.domain.util;

public class SprinklerState {

    private boolean isRefreshersBlocked;
    private boolean isInitialSetup;
    private boolean isBackupInProgress;

    private String lastValidPasswordUsed;

    public void setRefreshersBlocked(boolean isRefreshersBlocked) {
        this.isRefreshersBlocked = isRefreshersBlocked;
    }

    public boolean isRefreshersBlocked() {
        return isRefreshersBlocked;
    }

    public void setInitialSetup(boolean isInitialSetup) {
        this.isInitialSetup = isInitialSetup;
    }

    public boolean isInitialSetup() {
        return isInitialSetup;
    }

    public void setIsBackupInProgress(boolean isBackupInProgress) {
        this.isBackupInProgress = isBackupInProgress;
    }

    public boolean isBackupInProgress() {
        return isBackupInProgress;
    }

    public void keepPasswordForLater(String password) {
        lastValidPasswordUsed = password;
    }

    public String lastValidPasswordUsed() {
        return lastValidPasswordUsed;
    }
}
