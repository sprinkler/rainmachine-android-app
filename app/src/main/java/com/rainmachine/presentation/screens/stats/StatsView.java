package com.rainmachine.presentation.screens.stats;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.DashboardGraphs;
import com.rainmachine.domain.model.DayStats;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.stats.dashboard.ChartWrapperView;
import com.rainmachine.presentation.screens.stats.dashboard.DashboardView;
import com.rainmachine.presentation.screens.stats.dashboard.charts.CustomBarChart;
import com.rainmachine.presentation.screens.stats.dashboard.charts.CustomLineChart;
import com.rainmachine.presentation.screens.stats.dashboard.charts.CustomWeatherChart;
import com.rainmachine.presentation.screens.stats.dashboard.charts.utils.OnChartClickListener;
import com.rainmachine.presentation.screens.statsdetails.StatsDetailsExtra;
import com.rainmachine.presentation.util.ViewUtils;
import com.rainmachine.presentation.util.formatter.DecimalFormatter;
import com.rainmachine.presentation.widgets.VitalsChart;

import org.joda.time.LocalDate;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class StatsView extends ViewFlipper implements StatsContract.View, DashboardView
        .OnViewTypeChangedListener, VitalsChart.OnClickWeatherUpdateListener {

    private static final int FLIPPER_CONTENT_LIST = 0;
    private static final int FLIPPER_CONTENT_GRAPHS = 1;
    private static final int FLIPPER_PROGRESS = 2;
    private static final int FLIPPER_ERROR = 3;

    @Inject
    StatsContract.Presenter presenter;
    @Inject
    DecimalFormatter decimalFormatter;

    @BindView(R.id.tv_update)
    TextView tvLastUpdate;
    @BindView(R.id.view_last_update)
    ViewGroup viewLastUpdate;
    @BindView(android.R.id.list)
    ListView list;
    @BindView(R.id.dashboard_parent)
    ViewGroup dashboardParent;
    @BindView(R.id.progress_text)
    TextView tvProgressText;

    private DashboardView dashboardView;
    private StatsAdapter adapter;
    private String latestChartViewType;

    public StatsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        if (!isInEditMode()) {
            presenter.attachView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            presenter.init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            presenter.destroy();
        }
    }

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.onClickRetry();
    }

    @Override
    public void onViewTypeChanged(String viewType) {
        presenter.onChangeViewType(viewType);
    }

    @Override
    public void onClickWeatherUpdate() {
        presenter.onClickWeatherUpdate();
    }

    @Override
    public void updateViews(StatsViewModel viewModel, boolean showGraphs) {
        if (showGraphs) {
            dashboardView.removeAllCharts();
            if (viewModel.showVitals) {
                updateVitalsChart(viewModel, dashboardView);
            }
            updateWeatherChart(viewModel, dashboardView);
            updateTemperatureChart(viewModel, dashboardView);
            updateRainAmountChart(viewModel, dashboardView);
            if (viewModel.showDailyWaterNeedChart) {
                updateDailyWaterNeedChart(viewModel, dashboardView);
            }
            updateProgramCharts(viewModel, dashboardView);

            dashboardView.refreshCharts();

            if (!Strings.isBlank(latestChartViewType)) {
                Timber.d("switch chart view to %s", latestChartViewType);
                dashboardView.updateChartsViewType(latestChartViewType);
                latestChartViewType = null;
            }
        } else {
            adapter.setItems(viewModel.stats);
            // The last entry contains the last update date
            if (viewModel.stats.size() == 5) {
                DayStats dayResponse = viewModel.stats.get(4);
                if (!Strings.isBlank(dayResponse.lastUpdate)) {
                    tvLastUpdate.setText(getContext().getString(R.string.stats_last_update,
                            dayResponse.lastUpdate));
                } else {
                    tvLastUpdate.setText(R.string.stats_not_connected);
                }
                viewLastUpdate.setVisibility(View.VISIBLE);
            } else {
                viewLastUpdate.setVisibility(View.GONE);
            }
        }
    }

    private void updateVitalsChart(StatsViewModel statsViewModel, DashboardView dashboardView) {
        VitalsChart vitalsChart = new VitalsChart(getContext());
        vitalsChart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams
                .MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        vitalsChart.setDescription(statsViewModel.nextWateringCycle);
        vitalsChart.setLastWeatherUpdate(statsViewModel.lastWeatherUpdate);
        vitalsChart.setOnClickListener(this);
        dashboardView.addVitalsChart(vitalsChart);
        if (statsViewModel.yearCategory != null) {
            String sWaterAmount = getContext().getString(statsViewModel.isUnitsMetric ?
                            R.string.stats_water_saved_amount_metric :
                            R.string.stats_water_saved_amount_us,
                    statsViewModel.yearCategory.waterSavedAmount);
            dashboardView.setVitalsPercentage(DashboardView.YEAR, statsViewModel.yearCategory
                    .waterSavedPercentage, getContext().getString(R.string
                    .stats_water_saved_365_days, sWaterAmount));
        }
        if (statsViewModel.monthCategory != null) {
            String sWaterAmount = getContext().getString(statsViewModel.isUnitsMetric ?
                            R.string.stats_water_saved_amount_metric :
                            R.string.stats_water_saved_amount_us,
                    statsViewModel.monthCategory.waterSavedAmount);
            dashboardView.setVitalsPercentage(DashboardView.MONTH, statsViewModel.monthCategory
                    .waterSavedPercentage, getContext().getString(R.string
                    .stats_water_saved_30_days, sWaterAmount));
        }
        if (statsViewModel.weekCategory != null) {
            String sWaterAmount = getContext().getString(statsViewModel.isUnitsMetric ?
                            R.string.stats_water_saved_amount_metric :
                            R.string.stats_water_saved_amount_us,
                    statsViewModel.weekCategory.waterSavedAmount);
            dashboardView.setVitalsPercentage(DashboardView.WEEK, statsViewModel.weekCategory
                    .waterSavedPercentage, getContext().getString(R.string
                    .stats_water_saved_7_days, sWaterAmount));
        }
    }

    private void updateWeatherChart(StatsViewModel statsViewModel, DashboardView dashboardView) {
        CustomWeatherChart chart = new CustomWeatherChart(getContext());
        int height = (int) ViewUtils.dpToPixels(150, getContext());
        dashboardView.addChart(chart, getContext().getString(R.string.all_weather), " ", height);
        String sTemperatureUnit = statsViewModel.isUnitsMetric ? getContext().getString(R.string
                .all_temperature_unit_celsius) : getContext().getString(R.string
                .all_temperature_unit_fahrenheit);
        String sRainUnit = statsViewModel.isUnitsMetric ? getContext().getString(R.string.all_mm) :
                getContext().getString(R.string.all_inch);

        if (statsViewModel.yearCategory != null) {
            fillWeatherData(chart, DashboardView.YEAR, statsViewModel.yearCategory);
        }
        if (statsViewModel.monthCategory != null) {
            fillWeatherData(chart, DashboardView.MONTH, statsViewModel.monthCategory);
        }

        if (statsViewModel.weekCategory != null) {
            fillWeatherData(chart, DashboardView.WEEK, statsViewModel.weekCategory);

            LocalDate date;
            StatsDayViewModel dayData;
            ArrayList<String> temperatureMaxTextValuesWeek = new ArrayList<>();
            ArrayList<String> temperatureMinTextValuesWeek = new ArrayList<>();
            ArrayList<Boolean> temperatureMinFreezeProtect = new ArrayList<>();
            ArrayList<String> rainTextValuesWeek = new ArrayList<>();
            ArrayList<Integer> iconValuesWeek = new ArrayList<>();
            StatsDataCategory category = statsViewModel.weekCategory;
            String sTemp;
            for (int i = 0; i < category.numDays; i++) {
                date = category.startDate.plusDays(i);
                dayData = category.days.get(date);

                String sTemperature = dayData.maxTemperature != Integer.MIN_VALUE ? "" + dayData
                        .maxTemperature : "-";
                temperatureMaxTextValuesWeek.add(sTemperature);

                sTemperature = dayData.minTemperature != Integer.MIN_VALUE ? "" + dayData
                        .minTemperature : "-";
                temperatureMinTextValuesWeek.add(sTemperature);
                temperatureMinFreezeProtect.add(dayData.lessOrEqualToFreezeProtect);

                // Show empty space if there was no rain
                if (dayData.rainAmount > 0) {
                    sTemp = decimalFormatter.lengthUnitsDecimals(dayData.rainAmount,
                            statsViewModel.isUnitsMetric);
                } else {
                    sTemp = "";
                }
                rainTextValuesWeek.add(sTemp);
                iconValuesWeek.add(dayData.iconId);
            }
            chart.setTemperatureMaxTextValues(temperatureMaxTextValuesWeek, sTemperatureUnit);
            chart.setTemperatureMinTextValues(temperatureMinTextValuesWeek, sTemperatureUnit,
                    temperatureMinFreezeProtect);
            chart.setRainTextValues(rainTextValuesWeek, sRainUnit);
            chart.setIcons(iconValuesWeek);
        }

        chart.setOnChartClickListener(viewType -> presenter.onClickWeatherData(viewType));
    }

    private void fillWeatherData(CustomWeatherChart chart, String chartViewType, StatsDataCategory
            category) {
        LocalDate date;
        StatsDayViewModel dayData;
        ArrayList<Float> yValues = new ArrayList<>();
        ArrayList<LocalDate> xValues = new ArrayList<>();
        for (int i = 0; i < category.numDays; i++) {
            date = category.startDate.plusDays(i);
            dayData = category.days.get(date);
            yValues.add(0.0f); // dumb unused data
            xValues.add(dayData.date);
        }
        chart.setChartData(chartViewType, xValues, yValues);
    }

    private void updateTemperatureChart(StatsViewModel statsViewModel, DashboardView
            dashboardView) {
        CustomLineChart chart = new CustomLineChart(getContext());
        dashboardView.addChart(chart, getContext().getString(R.string.all_temperature_max_min),
                statsViewModel
                        .isUnitsMetric ? getContext()
                        .getString(R.string.all_temperature_unit_celsius) : getContext()
                        .getString(R.string
                                .all_temperature_unit_fahrenheit));

        if (statsViewModel.yearCategory != null) {
            fillTemperatureData(chart, DashboardView.YEAR, statsViewModel.yearCategory);
        }
        if (statsViewModel.monthCategory != null) {
            fillTemperatureData(chart, DashboardView.MONTH, statsViewModel.monthCategory);
        }
        if (statsViewModel.weekCategory != null) {
            fillTemperatureData(chart, DashboardView.WEEK, statsViewModel.weekCategory);
        }

        chart.setOnChartClickListener(viewType -> presenter.onClickTemperatureData(viewType));
    }

    private void fillTemperatureData(CustomLineChart chart, String chartViewType, StatsDataCategory
            category) {
        LocalDate date;
        StatsDayViewModel dayData;
        ArrayList<Float> yMaxValues = new ArrayList<>();
        ArrayList<Float> yMinValues = new ArrayList<>();
        ArrayList<LocalDate> xValues = new ArrayList<>();
        for (int i = 0; i < category.numDays; i++) {
            date = category.startDate.plusDays(i);
            dayData = category.days.get(date);
            float maxTemperature = (dayData != null && dayData.maxTemperature != Integer
                    .MIN_VALUE) ? dayData.maxTemperature : Integer.MIN_VALUE;
            yMaxValues.add(maxTemperature);
            float minTemperature = (dayData != null && dayData.minTemperature != Integer
                    .MIN_VALUE) ? dayData.minTemperature : Integer.MIN_VALUE;
            yMinValues.add(minTemperature);
            xValues.add(date);
        }
        chart.setChartData(chartViewType, xValues, yMaxValues, yMinValues, category.scaleViewModel
                .minTemperature, category.scaleViewModel.maxTemperature);
    }

    private void updateRainAmountChart(StatsViewModel statsViewModel, DashboardView dashboardView) {
        CustomBarChart barChart = new CustomBarChart(getContext());
        String sUnit = statsViewModel.isUnitsMetric ? getContext().getString(R.string.all_mm) :
                getContext().getString(R.string.all_inch);
        dashboardView.addChart(barChart, getContext().getString(R.string.all_rain_amount), sUnit);

        if (statsViewModel.yearCategory != null) {
            fillRainAmountData(barChart, DashboardView.YEAR, statsViewModel.yearCategory);
        }
        if (statsViewModel.monthCategory != null) {
            fillRainAmountData(barChart, DashboardView.MONTH, statsViewModel.monthCategory);
        }
        if (statsViewModel.weekCategory != null) {
            fillRainAmountData(barChart, DashboardView.WEEK, statsViewModel.weekCategory);
        }

        barChart.setOnChartClickListener(viewType -> presenter.onClickRainAmountData(viewType));
    }

    private void fillRainAmountData(CustomBarChart chart, String chartViewType, StatsDataCategory
            category) {
        LocalDate date;
        StatsDayViewModel dayData;
        ArrayList<Float> yValues = new ArrayList<>();
        ArrayList<LocalDate> xValues = new ArrayList<>();
        for (int i = 0; i < category.numDays; i++) {
            date = category.startDate.plusDays(i);
            dayData = category.days.get(date);
            yValues.add(dayData.rainAmount);
            xValues.add(dayData.date);
        }
        chart.setChartData(chartViewType, xValues, yValues, 0, category.scaleViewModel
                .maxRainAmount);
    }

    private void updateDailyWaterNeedChart(StatsViewModel statsViewModel, DashboardView
            dashboardView) {
        CustomBarChart barChart = new CustomBarChart(getContext());
        dashboardView.addChart(barChart, getContext().getString(R.string.all_daily_water_need),
                "%");
        String sUnit = statsViewModel.isUnitsMetric ? getContext().getString(R.string
                .all_temperature_unit_celsius) : getContext().getString(R.string
                .all_temperature_unit_fahrenheit);

        if (statsViewModel.yearCategory != null) {
            fillWaterNeedData(barChart, DashboardView.YEAR, statsViewModel.yearCategory);
        }
        if (statsViewModel.monthCategory != null) {
            fillWaterNeedData(barChart, DashboardView.MONTH, statsViewModel.monthCategory);
        }

        if (statsViewModel.weekCategory != null) {
            fillWaterNeedData(barChart, DashboardView.WEEK, statsViewModel.weekCategory);

            LocalDate date;
            StatsDayViewModel dayData;
            ArrayList<String> textValuesWeek = new ArrayList<>();
            ArrayList<Integer> iconValuesWeek = new ArrayList<>();
            StatsDataCategory category = statsViewModel.weekCategory;
            for (int i = 0; i < category.numDays; i++) {
                date = category.startDate.plusDays(i);
                dayData = category.days.get(date);
                String sTemperature = dayData.maxTemperature != Integer.MIN_VALUE ? "" + dayData
                        .maxTemperature : "-";
                textValuesWeek.add(sTemperature);
                iconValuesWeek.add(dayData.iconId);
            }
            barChart.setTextLabels(textValuesWeek, sUnit, true);
            barChart.setIcons(iconValuesWeek, true);
        }

        barChart.setOnChartClickListener(new OnChartClickListener() {
            @Override
            public void onChartClick(String viewType) {
                presenter.onClickWaterNeedData(viewType);
            }
        });
    }

    private void fillWaterNeedData(CustomBarChart chart, String chartViewType, StatsDataCategory
            category) {
        LocalDate date;
        StatsDayViewModel dayData;
        ArrayList<Float> yValues = new ArrayList<>();
        ArrayList<LocalDate> xValues = new ArrayList<>();
        for (int i = 0; i < category.numDays; i++) {
            date = category.startDate.plusDays(i);
            dayData = category.days.get(date);
            yValues.add(dayData.dailyWaterNeed * 100);
            xValues.add(dayData.date);
        }
        chart.setChartData(chartViewType, xValues, yValues, 0, category.scaleViewModel
                .maxDailyWaterNeedPercent);
    }

    private void updateProgramCharts(StatsViewModel statsViewModel, DashboardView dashboardView) {
        for (final Program program : statsViewModel.programs) {
            if (statsViewModel.simulationExpiredProgramIds.contains(program.id)) {
                ChartWrapperView chartWrapperView = new ChartWrapperView(getContext(),
                        DashboardView.CHART_DEFAULT_HEIGHT_DP);
                View view = LayoutInflater.from(getContext()).inflate(R.layout
                        .include_progress_chart, chartWrapperView, true);
                TextView title = ButterKnife.findById(view, R.id.title);
                title.setText(program.name);
                dashboardView.addChartWrapper(chartWrapperView);
            } else {
                CustomBarChart chart = new CustomBarChart(getContext());
                dashboardView.addChart(chart, program.name, "%");

                if (statsViewModel.yearCategory != null) {
                    fillProgramData(chart, DashboardView.YEAR, statsViewModel.yearCategory, (int)
                            program.id);
                }
                if (statsViewModel.monthCategory != null) {
                    fillProgramData(chart, DashboardView.MONTH, statsViewModel.monthCategory, (int)
                            program.id);
                }
                if (statsViewModel.weekCategory != null) {
                    fillProgramData(chart, DashboardView.WEEK, statsViewModel.weekCategory, (int)
                            program.id);
                }

                chart.setZoom();

                chart.setOnChartClickListener(viewType -> presenter.onClickProgramData(viewType,
                        program));
            }
        }
    }

    private void fillProgramData(CustomBarChart chart, String chartViewType, StatsDataCategory
            category, int programId) {
        LocalDate date;
        StatsDayViewModel dayData;
        ArrayList<Float> yValues = new ArrayList<>();
        ArrayList<LocalDate> xValues = new ArrayList<>();
        for (int i = 0; i < category.numDays; i++) {
            date = category.startDate.plusDays(i);
            dayData = category.days.get(date);
            float waterNeed = 0.0f;
            // For some unknown reason, this value can be null
            if (dayData.programDailyWaterNeed.get(programId) != null) {
                waterNeed = dayData.programDailyWaterNeed.get(programId);
            }
            yValues.add(waterNeed * 100);
            xValues.add(dayData.date);
        }
        int maxPercent = 100;
        // For some unknown reason, this value can be null
        if (category.scaleViewModel.maxPercentPrograms.get(programId) != null) {
            maxPercent = category.scaleViewModel.maxPercentPrograms.get(programId);
        }
        chart.setChartData(chartViewType, xValues, yValues, 0, maxPercent);
    }

    private int getChartPosition(StatsViewModel statsViewModel, DashboardGraphs.DashboardGraph
            graph) {
        if (graph.graphType == DashboardGraphs.GraphType.WEATHER) {
            return statsViewModel.showVitals ? 1 : 0;
        } else if (graph.graphType == DashboardGraphs.GraphType.TEMPERATURE) {
            return statsViewModel.showVitals ? 2 : 1;
        } else if (graph.graphType == DashboardGraphs.GraphType.RAIN_AMOUNT) {
            return statsViewModel.showVitals ? 3 : 2;
        }
        if (graph.graphType == DashboardGraphs.GraphType.DAILY_WATER_NEED) {
            return statsViewModel.showVitals ? 4 : 3;
        } else if (graph.graphType == DashboardGraphs.GraphType.PROGRAM) {
            int position = -1;
            for (int i = 0; i < statsViewModel.programs.size(); i++) {
                Program program = statsViewModel.programs.get(i);
                if (graph.programId == program.id) {
                    position = i;
                    break;
                }
            }
            if (position == -1) {
                return -1;
            }
            return statsViewModel.showVitals ? position + 4 + (statsViewModel
                    .showDailyWaterNeedChart ? 1 : 0) : position + 3 + (statsViewModel
                    .showDailyWaterNeedChart ? 1 : 0);
        }
        return -1;
    }

    @Override
    public void showHideCharts(StatsViewModel viewModel, String viewType) {
        for (DashboardGraphs.DashboardGraph graph : viewModel.dashboardGraphs.graphs) {
            int position = getChartPosition(viewModel, graph);
            if (graph.graphType == DashboardGraphs.GraphType.WEATHER) {
                toggleWeatherChart(position, graph, viewType);
            } else {
                toggleChart(position, graph);
            }
        }
        dashboardView.refreshCharts();
    }

    @Override
    public void showAllCharts(StatsViewModel viewModel, String viewType) {
        for (DashboardGraphs.DashboardGraph graph : viewModel.dashboardGraphs.graphs) {
            int position = getChartPosition(viewModel, graph);
            if (graph.graphType == DashboardGraphs.GraphType.WEATHER) {
                if (viewType.equals(DashboardView.WEEK)) {
                    dashboardView.showChart(position);
                    if (graph.isEnabled) {
                        dashboardView.setBackgroundForVisible(position);
                    } else {
                        dashboardView.setBackgroundForHidden(position);
                    }
                } else {
                    dashboardView.hideChart(position);
                }
            } else if (graph.graphType == DashboardGraphs.GraphType.TEMPERATURE) {
                dashboardView.showChart(position);
                if (graph.isEnabled) {
                    dashboardView.setBackgroundForVisible(position);
                } else {
                    dashboardView.setBackgroundForHidden(position);
                }
            } else if (graph.graphType == DashboardGraphs.GraphType.RAIN_AMOUNT) {
                dashboardView.showChart(position);
                if (graph.isEnabled) {
                    dashboardView.setBackgroundForVisible(position);
                } else {
                    dashboardView.setBackgroundForHidden(position);
                }
            } else {
                dashboardView.showChart(position);
                if (graph.isEnabled) {
                    dashboardView.setBackgroundForVisible(position);
                } else {
                    dashboardView.setBackgroundForHidden(position);
                }
            }
        }
        dashboardView.refreshCharts();
    }

    /* Show only in Week tab and only when enabled */
    private void toggleWeatherChart(int position, DashboardGraphs.DashboardGraph graph, String
            viewType) {
        if (viewType.equals(DashboardView.WEEK) && graph.isEnabled) {
            dashboardView.showChart(position);
        } else {
            dashboardView.hideChart(position);
        }
    }

    private void toggleChart(int position, DashboardGraphs.DashboardGraph graph) {
        if (graph.isEnabled) {
            dashboardView.showChart(position);
        } else {
            dashboardView.hideChart(position);
        }
    }

    @Override
    public void updateChartViewType(int viewType) {
        String sChartViewType;
        if (viewType == StatsDetailsExtra.TYPE_WEEK) {
            sChartViewType = DashboardView.WEEK;
        } else if (viewType == StatsDetailsExtra.TYPE_MONTH) {
            sChartViewType = DashboardView.MONTH;
        } else {
            sChartViewType = DashboardView.YEAR;
        }
        latestChartViewType = sChartViewType;
        // We update the charts only after they are created in onResume()
    }

    @Override
    public void showContent(boolean showGraphs) {
        if (showGraphs) {
            setDisplayedChild(FLIPPER_CONTENT_GRAPHS);
        } else {
            setDisplayedChild(FLIPPER_CONTENT_LIST);
        }
    }

    @Override
    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    @Override
    public void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }

    @Override
    public void setup(boolean showGraphs) {
        if (showGraphs) {
            dashboardParent.removeAllViews();
            dashboardView = new DashboardView(getContext(), this);
            dashboardParent.addView(dashboardView);
            tvProgressText.setText(R.string.stats_downloading_data);
        } else {
            adapter = new StatsAdapter(getContext(), new ArrayList<>());
            list.setAdapter(adapter);
        }
    }
}
