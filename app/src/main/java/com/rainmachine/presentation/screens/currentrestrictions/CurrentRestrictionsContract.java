package com.rainmachine.presentation.screens.currentrestrictions;

interface CurrentRestrictionsContract {

    interface View {

        void showProgress();

        void showError();

        void updateContent(CurrentRestrictionsViewModel viewModel);

        void showContent();

        void setup();
    }

    interface Container {

        void closeScreen();

        void goToRestrictionsScreen();

        void goToHoursScreen();

        void goToRainSensorScreen();

        void goToSnoozeScreen();
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View> {

        void onClickRetry();

        void onClickSnooze();

        void onClickRainSensor();

        void onClickFreezeProtect();

        void onClickMonth();

        void onClickDay();

        void onClickHour();

        void start();
    }
}
