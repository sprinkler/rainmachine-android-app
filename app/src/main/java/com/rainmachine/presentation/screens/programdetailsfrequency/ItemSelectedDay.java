package com.rainmachine.presentation.screens.programdetailsfrequency;

import org.parceler.Parcel;

@Parcel
public class ItemSelectedDay {
    public int index;
    public String name;
    public boolean isChecked;

    public ItemSelectedDay() {
    }

    public ItemSelectedDay(int index, String name, boolean isChecked) {
        this.index = index;
        this.name = name;
        this.isChecked = isChecked;
    }
}
