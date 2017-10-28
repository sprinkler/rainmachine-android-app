package com.rainmachine.domain.model;

public class BackupInfo {

    public static final BackupInfo NOT_FOUND = new BackupInfo();

    public String body;
    public boolean isOldApiFormat; // the body is a json in the old API format

    private BackupInfo() {
    }

    public BackupInfo(String body, boolean isOldApiFormat) {
        this.body = body;
        this.isOldApiFormat = isOldApiFormat;
    }
}
