package com.rainmachine.presentation.screens.stats.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.Utils;
import com.rainmachine.R;
import com.rainmachine.presentation.screens.stats.dashboard.charts.CustomChart;
import com.rainmachine.presentation.util.ViewUtils;
import com.rainmachine.presentation.widgets.VitalsChart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Tremend Software on 3/9/2015.
 */
@SuppressLint("ViewConstructor")
public class DashboardView extends LinearLayout implements View.OnTouchListener {
    public static final String WEEK = "week";
    public static final String MONTH = "month";
    public static final String YEAR = "year";

    private final static int MAX_CLICK_DISTANCE = 5;
    private final static int MAX_CLICK_DURATION = 150;
    public final static int CHART_DEFAULT_HEIGHT_DP = 150;

    @BindView(R.id.container)
    LinearLayout container;
    @BindView(R.id.chart_periods)
    RadioGroup groupChartPeriods;
    @BindView(R.id.week_button)
    RadioButton btnWeek;
    @BindView(R.id.month_button)
    RadioButton btnMonth;
    @BindView(R.id.year_button)
    RadioButton btnYear;
    @BindView(R.id.charts_scroll)
    CustomScrollView scrollView;

    private List<CustomChart> chartsList = new ArrayList<>();

    private VitalsChart vitalsChart;
    private HashMap<String, Float> vitalsPercentages;
    private HashMap<String, String> vitalsTitles;

    private Matrix mMatrix = new Matrix();
    private Matrix mSavedMatrix = new Matrix();

    private String viewType = WEEK;

    private long onChartClickTimeElapsed = 0;

    private float mTouchStartPointX;
    private float mTouchStartPointY;

    private boolean canClickOnChart = false;

    private OnViewTypeChangedListener listener;

    public DashboardView(Context context, final OnViewTypeChangedListener listener) {
        super(context);
        this.listener = listener;

        inflate(context, R.layout.view_dashboard, this);
        ButterKnife.bind(this);

        groupChartPeriods.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.week_button:
                    viewType = DashboardView.WEEK;
                    break;
                case R.id.month_button:
                    viewType = DashboardView.MONTH;
                    break;
                case R.id.year_button:
                    viewType = DashboardView.YEAR;
                    break;
            }

            DashboardView.this.listener.onViewTypeChanged(viewType);

            refreshCharts();
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (chartsList.contains(v)) {
            final CustomChart chart = (CustomChart) v;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    canClickOnChart = true;
                    onChartClickTimeElapsed = System.currentTimeMillis();
                    mTouchStartPointX = event.getX();
                    mTouchStartPointY = event.getY();

                    if (!viewType.equals(YEAR)) {
                        mMatrix = chart.getTransformer().getTouchMatrix();
                        mSavedMatrix.set(mMatrix);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (distance(mTouchStartPointX, mTouchStartPointY, event.getX(), event.getY()
                    ) > MAX_CLICK_DISTANCE) {
                        canClickOnChart = false;
                    }

                    if (!viewType.equals(YEAR)) {
                        if (Math.abs(event.getY() - mTouchStartPointY) <= Math.abs(event.getX() -
                                mTouchStartPointX)) {
                            float deltaX = event.getX() - mTouchStartPointX;

                            scrollView.setEnableScrolling(false);
                            mMatrix.set(mSavedMatrix);

                            mMatrix.postTranslate(deltaX, 0);

                            for (Chart chart1 : chartsList) {
                                // check if not hidden
                                if (chart1.getHeight() > 0) {
                                    chart1.getTransformer().refresh(mMatrix, chart1);
                                }
                            }
                        } else {
                            scrollView.setEnableScrolling(true);
                            return false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (canClickOnChart && chart.getChartClickListener() != null && System
                            .currentTimeMillis() - onChartClickTimeElapsed <= MAX_CLICK_DURATION) {
                        chart.getChartClickListener().onChartClick(viewType);
                    }

                    if (!viewType.equals(YEAR)) {
                        Entry lastEntry = chart.getEntryByTouchPoint(chart.getCenter().x, chart
                                .getCenter().y);
                        for (CustomChart chart1 : chartsList) {
                            chart1.setLastEntry(lastEntry);
                        }
                        scrollView.setEnableScrolling(true);
                    }
                    break;
            }
        }
        return true;
    }

    public void refreshCharts() {
        for (CustomChart chart : chartsList) {
            chart.setData(viewType);
        }
        refreshVitalsChart(viewType);
    }

    private void refreshVitalsChart(String viewType) {
        if (vitalsChart != null) {
            Float percentage = vitalsPercentages.get(viewType);
            if (percentage != null) {
                vitalsChart.setPercentage(percentage);
            }
            String title = vitalsTitles.get(viewType);
            if (title != null) {
                vitalsChart.setTitle(title);
            }
        }
    }

    public void updateChartsViewType(String viewType) {
        this.viewType = viewType;
        if (DashboardView.WEEK.equals(viewType)) {
            btnWeek.setChecked(true);
        } else if (DashboardView.MONTH.equals(viewType)) {
            btnMonth.setChecked(true);
        } else if (DashboardView.YEAR.equals(viewType)) {
            btnYear.setChecked(true);
        }
    }

    public Chart addChart(CustomChart chart, String title, String unit, int height) {
        chart.setOnTouchListener(this);
        chartsList.add(chart);
        container.addView(new ChartWrapperView(getContext(), chart, title, unit, height));
        return chart;
    }

    public Chart addChart(CustomChart chart, String title, String unit) {
        int height = (int) ViewUtils.dpToPixels(CHART_DEFAULT_HEIGHT_DP, getContext());
        return addChart(chart, title, unit, height);
    }

    public void addVitalsChart(VitalsChart vitalsChart) {
        this.vitalsChart = vitalsChart;
        container.addView(new ChartWrapperView(getContext(), vitalsChart, (int) ViewUtils
                .dpToPixels(CHART_DEFAULT_HEIGHT_DP, getContext())));
    }

    public void setVitalsPercentage(String viewType, float percentage, String title) {
        if (vitalsPercentages == null) {
            vitalsPercentages = new HashMap<>(3);
        }
        if (vitalsTitles == null) {
            vitalsTitles = new HashMap<>(3);
        }
        vitalsPercentages.put(viewType, percentage);
        vitalsTitles.put(viewType, title);
    }

    public void showChart(int position) {
        View chartWrapperView = container.getChildAt(position);
        if (chartWrapperView != null) {
            chartWrapperView.setVisibility(View.VISIBLE);
        }
    }

    public void hideChart(int position) {
        View chartWrapperView = container.getChildAt(position);
        if (chartWrapperView != null) {
            chartWrapperView.setVisibility(View.GONE);
        }
    }

    public void setBackgroundForVisible(int position) {
        ChartWrapperView chartWrapperView = (ChartWrapperView) container.getChildAt(position);
        if (chartWrapperView != null) {
            chartWrapperView.setBackgroundForVisible();
        }
    }

    public void setBackgroundForHidden(int position) {
        ChartWrapperView chartWrapperView = (ChartWrapperView) container.getChildAt(position);
        if (chartWrapperView != null) {
            chartWrapperView.setBackgroundForHidden();
        }
    }

    public void addChartWrapper(ChartWrapperView chartWrapperView) {
        container.addView(chartWrapperView);
    }

    public void removeAllCharts() {
        container.removeAllViews();
        chartsList.clear();
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);

        return Utils.convertPixelsToDp(distanceInPx);
    }

    public interface OnViewTypeChangedListener {
        void onViewTypeChanged(String viewType);
    }
}
