package com.rainmachine.presentation.screens.programdetailsadvanced;

import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.dialogs.ClickableRadioOptionsDialogFragment;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;
import com.rainmachine.presentation.screens.programdetailsold.CycleSoakDialogFragment;
import com.rainmachine.presentation.screens.programdetailsold.StationDelayDialogFragment;

interface ProgramDetailsAdvancedContract {

    interface View {

        void setup(Program program, boolean isUnitsMetric);

        void updateDelayZones(Program program);

        void updateCycleSoak(Program program);

        void updateMaxAmountNotRun(Program program, boolean isUnitsMetric);
    }

    interface Container {

        void closeScreen(Program program);

        ProgramDetailsAdvancedExtra getExtra();

        void showDelayZonesDialog(int dialogId, Program program);

        void showCycleSoakDialog(int dialogId, Program program);

        void showCustomDelayZonesDialog(Program program);

        void showCustomCycleSoakDialog(Program program);

        void showDoNotRunDialog(int dialogId, Program program, boolean isUnitsMetric);

        int getMaxRainAmount(int checkedItemPosition);
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            RadioOptionsDialogFragment.Callback,
            ClickableRadioOptionsDialogFragment.Callback, StationDelayDialogFragment.Callback,
            CycleSoakDialogFragment.Callback {

        void onChangeAdjustWeatherTimes(boolean isChecked);

        void onClickCycleSoak();

        void onClickDelayZones();

        void onClickDoNotRun();

        void onClickBack();
    }
}
