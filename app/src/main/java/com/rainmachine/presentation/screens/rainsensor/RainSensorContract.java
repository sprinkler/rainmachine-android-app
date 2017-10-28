package com.rainmachine.presentation.screens.rainsensor;

import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;

interface RainSensorContract {

    interface View {
        void setup(boolean showExtraFields);

        void render(RainSensorViewModel viewModel);

        void render(RainDetectedViewModel viewModel);

        void showContent();

        void showProgress();

        void showError();
    }

    interface Container {

        void showRainDetectedOptions(String[] items, int checkedItemPosition);
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            RadioOptionsDialogFragment.Callback {

        void onCheckedChangedRainSensorHardware(boolean isChecked);

        void onCheckedChangedRainSensorClosed(boolean isChecked);

        void onClickRainSensor();

        void onRetry();

        void start();

        void onClickRainDetectedOption();
    }
}
