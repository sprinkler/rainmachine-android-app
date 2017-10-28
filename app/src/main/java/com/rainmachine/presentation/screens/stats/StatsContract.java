package com.rainmachine.presentation.screens.stats;

import com.rainmachine.domain.model.Program;

public interface StatsContract {

    interface View {
        void showContent(boolean showGraphs);

        void showProgress();

        void showError();

        void setup(boolean showGraphs);

        void updateViews(StatsViewModel viewModel, boolean showGraphs);

        void updateChartViewType(int viewType);

        void showAllCharts(StatsViewModel viewModel, String chartViewType);

        void showHideCharts(StatsViewModel viewModel, String chartViewType);
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            StatsGraphDialogFragment.Callback {

        void start();

        void stop();

        void onClickRetry();

        void onClickWeatherUpdate();

        void onClickWeatherData(String viewType);

        void onClickTemperatureData(String viewType);

        void onClickRainAmountData(String viewType);

        void onClickWaterNeedData(String viewType);

        void onClickProgramData(String viewType, Program program);

        void onChangeViewType(String viewType);

        void onChangeEditMode(boolean isEditDashboardMode);
    }
}
