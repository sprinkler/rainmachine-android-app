package com.rainmachine.presentation.screens.mini8settings;

import com.rainmachine.presentation.dialogs.InputNumberDialogFragment;

import java.util.List;

interface Mini8SettingsContract {

    int DIALOG_ID_MIN_LED_BRIGHTNESS = 1;
    int DIALOG_ID_MAX_LED_BRIGHTNESS = 2;
    int DIALOG_ID_TOUCH_SLEEP_TIMEOUT = 3;
    int DIALOG_ID_TOUCH_LONG_PRESS_TIMEOUT = 4;


    interface View {

        void showProgress();

        void render(Mini8SettingsViewModel viewModel);

        void showError();

        void showContent();
    }

    interface Container {

        void showMinLedBrightnessDialog(int minLedBrightness);

        void showMaxLedBrightnessDialog(int maxLedBrightness);

        void showTouchSleepTimeoutDialog(int touchSleepTimeout);

        void showTouchLongPressTimeoutDialog(int touchLongPressTimeout);

        void showProgramsDialog(List<TouchProgramViewModel> programs, TouchProgramViewModel
                touchProgramToRun);

        void goToManualWateringDurationScreen();
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            InputNumberDialogFragment.Callback {

        void onCheckedChangedTouchAdvanced(boolean isChecked);

        void onCheckedChangedLedDelay(boolean isChecked);

        void onClickMinLedBrightness();

        void onClickMaxLedBrightness();

        void onClickTouchSleepTimeout();

        void onClickTouchLongPressTimeout();

        void onClickRetry();

        void onClickTouchStartProgram();

        void onClickManualWateringDuration();

        void onSelectedProgram(TouchProgramViewModel selectedItem);
    }
}
