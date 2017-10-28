package com.rainmachine.presentation.screens.programdetailsfrequency;

import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.dialogs.DatePickerDialogFragment;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;

import org.joda.time.LocalDate;

public interface ProgramDetailsFrequencyContract {

    interface View {

        void setup(Program program);

        void updateWeekDays(Program program);

        void hideWeekDays();

        void updateEveryNDays(Program program);

        void updateNextRun(Program program);
    }

    interface Container {
        ProgramDetailsFrequencyExtra getExtra();

        void closeScreen(Program program);

        void showWeekDaysDialog(Program program);

        void showEveryNDaysDialog(int dialogId, Program program);

        void showNextRunDialog(LocalDate nextRunSprinklerLocalDate);
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            RadioOptionsDialogFragment.Callback, DatePickerDialogFragment.Callback,
            SelectedDaysDialogFragment.Callback {

        void onClickBack();

        void onCheckFrequencyDaily();

        void onCheckFrequencyWeekdays();

        void onCheckFrequencyOddDays();

        void onCheckFrequencyEvenDays();

        void onCheckFrequencyEveryNDays();

        void onClickNextRun();
    }
}
