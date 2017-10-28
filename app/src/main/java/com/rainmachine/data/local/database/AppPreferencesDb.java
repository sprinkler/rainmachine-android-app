package com.rainmachine.data.local.database;

import com.rainmachine.domain.model.HandPreference;

public class AppPreferencesDb {
    public Long _id;
    public HandPreference handPreference;

    public AppPreferencesDb() {
    }

    public AppPreferencesDb(HandPreference handPreference) {
        this.handPreference = handPreference;
    }
}
