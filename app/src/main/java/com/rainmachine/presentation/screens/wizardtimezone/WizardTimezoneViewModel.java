package com.rainmachine.presentation.screens.wizardtimezone;

import org.joda.time.LocalDateTime;
import org.parceler.Parcel;

@Parcel
class WizardTimezoneViewModel {
    public LocalDateTime localDateTime;
    public String timezone;

    public WizardTimezoneViewModel() {
    }
}
