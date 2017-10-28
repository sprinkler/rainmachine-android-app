package com.rainmachine.presentation.screens.zonedetails;

import com.rainmachine.presentation.util.BasePresenter;

import io.reactivex.disposables.CompositeDisposable;

class ZoneDetailsWeatherPresenter extends BasePresenter<ZoneDetailsWeatherView> {

    private ZoneDetailsViewModel viewModel;
    private CompositeDisposable disposables;

    ZoneDetailsWeatherPresenter() {
        disposables = new CompositeDisposable();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    public void onToggleHistoricalData(boolean checked) {
        viewModel.zoneProperties.historicalAverage = checked;
        if (checked) {
            view.toggleAdjust(false);
        } else {
            checkToggleAdjust();
        }
    }

    public void onToggleWeatherData(boolean checked) {
        viewModel.zoneProperties.forecastData = checked;
        if (checked) {
            view.toggleAdjust(false);
        } else {
            checkToggleAdjust();
        }
    }

    public void onToggleAdjust(boolean checked) {
        if (checked) {
            viewModel.zoneProperties.historicalAverage = false;
            view.toggleHistoricalData(false);
            viewModel.zoneProperties.forecastData = false;
            view.toggleWeatherData(false);
        } else {
            // Leave the fields as they are
        }
    }

    public void updateViewModel(ZoneDetailsViewModel viewModel) {
        this.viewModel = viewModel;
        view.updateContent(viewModel);
    }

    private void checkToggleAdjust() {
        if (!viewModel.zoneProperties.historicalAverage && !viewModel.zoneProperties.forecastData) {
            view.toggleAdjust(true);
        }
    }
}
