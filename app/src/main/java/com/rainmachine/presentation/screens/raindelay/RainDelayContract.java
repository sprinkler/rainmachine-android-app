package com.rainmachine.presentation.screens.raindelay;

interface RainDelayContract {

    interface View {

        void showProgress();

        void showError();

        void render(RainDelayViewModel viewModel);

        void showContentOld();

        void showContent();

        void showInvalidDurationMessage();
    }

    interface Container {

        void closeScreen();
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View> {

        void onClickDelay(int timerValue);

        void onClickRetry();

        void start();

        void onClickSnooze(int numSeconds);
    }
}
