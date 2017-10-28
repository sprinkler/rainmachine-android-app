package com.rainmachine.presentation.screens.stats;

import android.content.Intent;
import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.DashboardGraphs;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.notifiers.StatsNeedRefreshNotifier;
import com.rainmachine.domain.util.Features;
import com.rainmachine.presentation.screens.main.MainActivity;
import com.rainmachine.presentation.screens.programdetails.ProgramDetailsActivity;
import com.rainmachine.presentation.screens.programdetailsold.ProgramDetailsOldActivity;
import com.rainmachine.presentation.screens.stats.dashboard.DashboardView;
import com.rainmachine.presentation.screens.statsdetails.StatsDetailsActivity;
import com.rainmachine.presentation.screens.statsdetails.StatsDetailsExtra;
import com.rainmachine.presentation.screens.weathersettings.WeatherSettingsActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import org.joda.time.LocalDate;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

public class StatsPresenter extends BasePresenter<StatsContract.View> implements StatsContract
        .Presenter {

    private MainActivity activity;
    private StatsMixer mixer;
    private StatsNeedRefreshNotifier statsNeedRefreshNotifier;
    private boolean showGraphs;
    private Features features;

    private StatsViewModel viewModel;
    private LocalDate today = new LocalDate();
    private boolean isEditMode;
    private String chartViewType = DashboardView.WEEK;
    private CompositeDisposable disposables;

    public StatsPresenter(MainActivity activity, StatsMixer mixer, StatsNeedRefreshNotifier
            statsNeedRefreshNotifier, Features features) {
        this.activity = activity;
        this.mixer = mixer;
        this.statsNeedRefreshNotifier = statsNeedRefreshNotifier;
        showGraphs = features.showGraphs();
        this.features = features;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(StatsContract.View view) {
        super.attachView(view);

        view.setup(showGraphs);
    }

    @Override
    public void init() {
        refresh(StatsMixer.DATA_TYPE_WEEK, true);

        if (showGraphs) {
            disposables.add(mixer.dashboardGraphsChanges()
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new DashboardGraphsSubscriber()));
            disposables.add(statsNeedRefreshNotifier.observe()
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new ForceRefreshSubscriber()));
        }
    }

    @Override
    public void start() {
        /* We might come back to this screen in a different day and we need to make sure to
        refresh*/
        if (!today.equals(new LocalDate())) {
            Timber.i("Force refresh because another day");
            refreshConditionally();
        }
    }

    @Override
    public void stop() {
        // Do nothing
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogStatsGraphPositiveClick() {
        if (viewModel != null) {
            mixer.saveToDatabase(viewModel.dashboardGraphs);
        }
    }

    @Override
    public void onDialogStatsGraphShowAllData(DashboardGraphs.DashboardGraph graph, String
            viewType) {
        if (graph.graphType == DashboardGraphs.GraphType.WEATHER) {
            showWeatherData();
        } else if (graph.graphType == DashboardGraphs.GraphType.TEMPERATURE) {
            showTemperatureData(viewType);
        } else if (graph.graphType == DashboardGraphs.GraphType.RAIN_AMOUNT) {
            showRainAmountData(viewType);
        } else if (graph.graphType == DashboardGraphs.GraphType.DAILY_WATER_NEED) {
            showWaterNeedData(viewType);
        } else if (graph.graphType == DashboardGraphs.GraphType.PROGRAM) {
            if (viewModel != null) {
                for (Program program : viewModel.programs) {
                    if (graph.programId == program.id) {
                        showProgramData(viewType, program);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onDialogStatsGraphEditProgram(Program program) {
        if (viewModel != null) {
            if (features.showNewProgramDetailsScreen()) {
                Intent intent = ProgramDetailsActivity.getStartIntent(activity, program, viewModel
                        .sprinklerLocalDateTime, viewModel.isUnitsMetric, viewModel
                        .use24HourFormat);
                activity.startActivity(intent);
            } else {
                Intent intent = ProgramDetailsOldActivity.getStartIntent(activity, program,
                        viewModel.sprinklerLocalDateTime, viewModel.use24HourFormat);
                activity.startActivity(intent);
            }
        }
    }

    @Override
    public void onClickRetry() {
        view.updateChartViewType(StatsDetailsExtra.TYPE_WEEK);
        refresh(StatsMixer.DATA_TYPE_WEEK, true);
    }

    @Override
    public void onClickWeatherUpdate() {
        activity.startActivity(WeatherSettingsActivity.getStartIntent(activity));
    }

    @Override
    public void onClickWeatherData(String viewType) {
        if (viewModel != null) {
            DashboardGraphs.DashboardGraph rightGraph = null;
            for (DashboardGraphs.DashboardGraph graph : viewModel.dashboardGraphs.graphs) {
                if (graph.graphType == DashboardGraphs.GraphType.WEATHER) {
                    rightGraph = graph;
                    break;
                }
            }
            if (rightGraph == null) {
                return;
            }
            DialogFragment dialog = StatsGraphDialogFragment.newInstance(activity.getString(R
                    .string.all_weather), activity
                    .getString(R.string.all_ok), rightGraph, viewType, null);
            activity.showDialogSafely(dialog);
        }
    }

    @Override
    public void onClickTemperatureData(String viewType) {
        if (viewModel != null) {
            DashboardGraphs.DashboardGraph rightGraph = null;
            for (DashboardGraphs.DashboardGraph graph : viewModel.dashboardGraphs.graphs) {
                if (graph.graphType == DashboardGraphs.GraphType.TEMPERATURE) {
                    rightGraph = graph;
                    break;
                }
            }
            if (rightGraph == null) {
                return;
            }
            DialogFragment dialog = StatsGraphDialogFragment
                    .newInstance(activity.getString(R.string.all_temperature_max_min), activity
                            .getString(R.string.all_ok), rightGraph, viewType, null);
            activity.showDialogSafely(dialog);
        }
    }

    @Override
    public void onClickRainAmountData(String viewType) {
        if (viewModel != null) {
            DashboardGraphs.DashboardGraph rightGraph = null;
            for (DashboardGraphs.DashboardGraph graph : viewModel.dashboardGraphs.graphs) {
                if (graph.graphType == DashboardGraphs.GraphType.RAIN_AMOUNT) {
                    rightGraph = graph;
                    break;
                }
            }
            if (rightGraph == null) {
                return;
            }
            DialogFragment dialog = StatsGraphDialogFragment
                    .newInstance(activity.getString(R.string.all_rain_amount), activity.getString
                            (R.string.all_ok), rightGraph, viewType, null);
            activity.showDialogSafely(dialog);
        }
    }

    @Override
    public void onClickWaterNeedData(String viewType) {
        if (viewModel != null) {
            DashboardGraphs.DashboardGraph rightGraph = null;
            for (DashboardGraphs.DashboardGraph graph : viewModel.dashboardGraphs.graphs) {
                if (graph.graphType == DashboardGraphs.GraphType.DAILY_WATER_NEED) {
                    rightGraph = graph;
                    break;
                }
            }
            if (rightGraph == null) {
                return;
            }
            DialogFragment dialog = StatsGraphDialogFragment
                    .newInstance(activity.getString(R.string.all_daily_water_need), activity
                            .getString(R.string.all_ok), rightGraph, viewType, null);
            activity.showDialogSafely(dialog);
        }
    }

    @Override
    public void onClickProgramData(String viewType, Program program) {
        if (viewModel != null) {
            DashboardGraphs.DashboardGraph rightGraph = null;
            for (DashboardGraphs.DashboardGraph graph : viewModel.dashboardGraphs.graphs) {
                if (graph.graphType == DashboardGraphs.GraphType.PROGRAM && graph.programId ==
                        program.id) {
                    rightGraph = graph;
                    break;
                }
            }
            if (rightGraph == null) {
                return;
            }
            DialogFragment dialog = StatsGraphDialogFragment.newInstance(rightGraph.programName,
                    activity.getString(R.string.all_ok), rightGraph, viewType, program);
            activity.showDialogSafely(dialog);
        }
    }

    @Override
    public void onChangeViewType(String viewType) {
        chartViewType = viewType;
        if (viewType.equals(DashboardView.WEEK)) {
            if (viewModel == null || viewModel.weekCategory == null) {
                refresh(StatsMixer.DATA_TYPE_WEEK, false);
            }
        } else {
            if (viewModel == null || viewModel.monthCategory == null || viewModel.yearCategory ==
                    null) {
                refresh(StatsMixer.DATA_TYPE_MONTH_YEAR, false);
            }
        }
        if (viewModel != null) {
            updateVisibilityForCharts();
        }
    }

    @Override
    public void onChangeEditMode(boolean isEditDashboardMode) {
        this.isEditMode = isEditDashboardMode;
        updateVisibilityForCharts();
    }

    private void showWeatherData() {
        // Always week view
        int chartType = StatsDetailsExtra.TYPE_WEEK;
        activity.startActivityForResult(StatsDetailsActivity.getStartIntent(activity,
                chartType, StatsDetailsExtra.CHART_WEATHER, 0, null), MainActivity
                .REQ_CODE_STATS_DETAILS);
        MainActivity.latestChartViewType = chartType;
    }

    private void showWaterNeedData(String viewType) {
        int chartType;
        if (DashboardView.WEEK.equals(viewType)) {
            chartType = StatsDetailsExtra.TYPE_WEEK;
        } else if (DashboardView.MONTH.equals(viewType)) {
            chartType = StatsDetailsExtra.TYPE_MONTH;
        } else {
            chartType = StatsDetailsExtra.TYPE_YEAR;
        }
        activity.startActivityForResult(StatsDetailsActivity.getStartIntent(activity,
                chartType, StatsDetailsExtra.CHART_WATER_NEED, 0, null), MainActivity
                .REQ_CODE_STATS_DETAILS);
        MainActivity.latestChartViewType = chartType;
    }

    private void showTemperatureData(String viewType) {
        int chartType;
        if (DashboardView.WEEK.equals(viewType)) {
            chartType = StatsDetailsExtra.TYPE_WEEK;
        } else if (DashboardView.MONTH.equals(viewType)) {
            chartType = StatsDetailsExtra.TYPE_MONTH;
        } else {
            chartType = StatsDetailsExtra.TYPE_YEAR;
        }
        activity.startActivityForResult(StatsDetailsActivity.getStartIntent(activity,
                chartType, StatsDetailsExtra.CHART_TEMPERATURE, 0, null), MainActivity
                .REQ_CODE_STATS_DETAILS);
        MainActivity.latestChartViewType = chartType;
    }

    private void showRainAmountData(String viewType) {
        int chartType;
        if (DashboardView.WEEK.equals(viewType)) {
            chartType = StatsDetailsExtra.TYPE_WEEK;
        } else if (DashboardView.MONTH.equals(viewType)) {
            chartType = StatsDetailsExtra.TYPE_MONTH;
        } else {
            chartType = StatsDetailsExtra.TYPE_YEAR;
        }
        activity.startActivityForResult(StatsDetailsActivity.getStartIntent(activity,
                chartType, StatsDetailsExtra.CHART_RAIN_AMOUNT, 0, null), MainActivity
                .REQ_CODE_STATS_DETAILS);
        MainActivity.latestChartViewType = chartType;
    }

    private void showProgramData(String viewType, Program program) {
        int chartType;
        if (DashboardView.WEEK.equals(viewType)) {
            chartType = StatsDetailsExtra.TYPE_WEEK;
        } else if (DashboardView.MONTH.equals(viewType)) {
            chartType = StatsDetailsExtra.TYPE_MONTH;
        } else {
            chartType = StatsDetailsExtra.TYPE_YEAR;
        }
        activity.startActivityForResult(StatsDetailsActivity.getStartIntent
                (activity, chartType, StatsDetailsExtra.CHART_PROGRAM, (int) program
                        .id, program.name), MainActivity.REQ_CODE_STATS_DETAILS);
        MainActivity.latestChartViewType = chartType;
    }

    private void updateVisibilityForCharts() {
        if (viewModel != null && showGraphs) {
            if (isEditMode) {
                view.showAllCharts(viewModel, chartViewType);
            } else {
                view.showHideCharts(viewModel, chartViewType);
            }
        }
    }

    private void refreshConditionally() {
        view.updateChartViewType(StatsDetailsExtra.TYPE_WEEK);
        refresh(StatsMixer.DATA_TYPE_WEEK, true);
    }

    private void refresh(int dataType, boolean force) {
        view.showProgress();
        today = new LocalDate();
        disposables.add(mixer.refresh(dataType, force)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<StatsViewModel> {

        @Override
        public void onNext(StatsViewModel viewModel) {
            StatsPresenter.this.viewModel = viewModel;
            view.updateViews(viewModel, showGraphs);
            if (showGraphs) {
                updateVisibilityForCharts();
            }
            view.showContent(showGraphs);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class DashboardGraphsSubscriber extends DisposableObserver<DashboardGraphs> {

        @Override
        public void onNext(DashboardGraphs dashboardGraphs) {
            if (viewModel != null) {
                viewModel.dashboardGraphs = dashboardGraphs;
                updateVisibilityForCharts();
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class ForceRefreshSubscriber extends DisposableObserver<Object> {

        @Override
        public void onNext(Object object) {
            Timber.i("Force refresh because some data changed somewhere else");
            refreshConditionally();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            // Do nothing
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
