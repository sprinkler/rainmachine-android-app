package com.rainmachine.presentation.screens.programdetails;

import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;

import org.joda.time.LocalDateTime;

interface ProgramDetailsContract {

    interface View {

        void setup(Program program, boolean isUnitsMetric, boolean use24HourFormat);

        void showContent();

        void showProgress();

        void showErrorSaveMessage();

        void showErrorNoDaySelectedMessage();

        void showErrorAtLeastOneWateringTime();

        void updateNextRun(Program program, boolean use24HourFormat);

        void updateStartTime(Program program, boolean use24HourFormat);

        void updateFrequency(Program program);

        void updateTotalWatering(Program program);

        void updateWateringTimes(Program program);
    }

    interface Container {

        ProgramDetailsExtra getExtra();

        void toggleCustomActionBar(boolean makeVisible);

        void goToProgramZone(Program program, int positionInList, LocalDateTime
                sprinklerLocalDateTime);

        void goToStartTimeScreen(Program program, LocalDateTime sprinklerLocalDateTime, boolean
                use24HourFormat);

        void goToFrequencyScreen(Program program, LocalDateTime sprinklerLocalDateTime);

        void goToAdvancedScreen(Program program, boolean isUnitsMetric);

        void showDiscardDialog();

        void closeScreen();
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            ActionMessageDialogFragment.Callback {

        void onClickSave();

        void onClickProgramZone(int adapterPosition);

        void onClickPlus();

        void onClickMinus();

        void onClickStartTime();

        void onClickFrequency();

        void onClickAdvancedSettings();

        void onClickDiscardOrBack();

        void onChangeProgramName(String s);

        void onComingBackFromZones(Program program);

        void onComingBackFromFrequency(Program program);

        void onComingBackFromStartTime(Program program);

        void onComingBackFromAdvanced(Program program);
    }
}
