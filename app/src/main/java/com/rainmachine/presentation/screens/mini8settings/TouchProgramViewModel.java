package com.rainmachine.presentation.screens.mini8settings;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
class TouchProgramViewModel {

    static final TouchProgramViewModel NOT_SET = new TouchProgramViewModel("Not set");

    public long id;
    public String name;

    private TouchProgramViewModel(String name) {
        this.name = name;
    }

    @ParcelConstructor
    TouchProgramViewModel(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
