package com.rainmachine.presentation.screens.wateringduration;

import org.parceler.Parcel;

@Parcel
public class ZoneViewModel {
    long id;
    String name;
    long durationSeconds;

    public ZoneViewModel() {
    }

    ZoneViewModel(long id, String name, long durationSeconds) {
        this.id = id;
        this.name = name;
        this.durationSeconds = durationSeconds;
    }
}
