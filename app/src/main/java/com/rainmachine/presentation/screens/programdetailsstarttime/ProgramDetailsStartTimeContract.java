package com.rainmachine.presentation.screens.programdetailsstarttime;

import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramStartTime;
import com.rainmachine.presentation.dialogs.TimePickerDialogFragment;
import com.rainmachine.presentation.screens.programdetailsold.SunriseSunsetDialogFragment;

public interface ProgramDetailsStartTimeContract {

    interface View {

        void setup(Program program, boolean use24HourFormat);

        void updateStartTimeOfDay(Program program, boolean use24HourFormat);

        void updateSunriseSunset(Program program);
    }

    interface Container {
        ProgramDetailsStartTimeExtra getExtra();

        void closeScreen(Program program);

        void showSunriseSunsetDialog(ProgramStartTime programStartTime);

        void showTimeOfDayDialog(Program program, boolean use24HourFormat);
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            SunriseSunsetDialogFragment.Callback, TimePickerDialogFragment.Callback {

        void onClickBack();

        void onClickStartTime();

        void onClickSunriseSunset();
    }
}
