package com.rainmachine.presentation.screens.programdetailszones;

import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.presentation.screens.programdetailsold.ZoneDurationDialogFragment;

public interface ProgramDetailsZonesContract {

    interface View {
        void render(Program program, ProgramWateringTimes programWateringTimes, ZonePosition
                zonePosition);

        void updateCustomWatering(ProgramWateringTimes programWateringTimes);

        void updateDeterminedWatering(Program program, ProgramWateringTimes programWateringTimes);
    }

    interface Container {
        ProgramDetailsZonesExtra getExtra();

        void closeScreen(Program program);

        void updateTitle(ProgramWateringTimes programWateringTimes);

        void showCustomZoneDurationDialog(ProgramWateringTimes programWateringTimes);

        void goToZone(long id);
    }

    interface SuggestedDialog {
        void render(Program program, ProgramWateringTimes programWateringTimes);
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            ZoneDurationDialogFragment.Callback {

        void onClickBack();

        void onSelectedCustom();

        void onSelectedDoNotWater();

        void onSelectedDetermined();

        void onClickMinus();

        void onClickPlus();

        void onClickPreviousZone();

        void onClickNextZone();

        void onClickDefaultZone();

        void onComingBackFromChangingZoneProperties(ZoneProperties zoneProperties);

        void onShownSuggestedDialog(SuggestedDialog suggestedDialog);
    }
}
