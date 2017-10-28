package com.rainmachine.presentation.screens.weathersettings;

import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.screens.weathersources.WeatherSource;

interface WeatherSettingsContract {

    interface View {

        void showProgress();

        void showError();

        void updateContent(WeatherSettingsViewModel viewModel);

        void showContent();
    }

    interface Container {

        void hideDefaultsMenuItem();

        void showDefaultsMenuItem();

        void goToWeatherSourcesScreen();

        void goToWeatherSensitivityScreen();

        void goToWeatherSourceDetailsScreen(long parserId);

        void showDefaultsDialog();
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            ActionMessageDialogFragment.Callback {

        void onClickWeatherServices();

        void onClickWeatherSensitivity();

        void onClickRetry();

        void onClickWeatherSource(WeatherSource weatherSource);

        void start();

        void onClickDefaults();
    }
}
