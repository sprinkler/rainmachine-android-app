package com.rainmachine.presentation.screens.statsdetails;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.stats.StatsDayViewModel;
import com.rainmachine.presentation.screens.stats.StatsViewModel;
import com.rainmachine.presentation.util.formatter.DecimalFormatter;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatsDetailsView extends LinearLayout {

    @Inject
    StatsDetailsPresenter presenter;
    @Inject
    DecimalFormatter decimalFormatter;

    @BindView(android.R.id.list)
    ListView list;
    @BindView(R.id.title)
    TextView tvTitle;

    public StatsDetailsView(Context context, AttributeSet attrs) {
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

    public void render(StatsViewModel viewModel, StatsDetailsExtra extra) {
        String sTitle = null;
        if (extra.chart == StatsDetailsExtra.CHART_WEATHER) {
            sTitle = getContext().getString(R.string.stats_details_weather_title);
        } else if (extra.chart == StatsDetailsExtra.CHART_WATER_NEED) {
            sTitle = getContext().getString(R.string.all_daily_water_need);
        } else if (extra.chart == StatsDetailsExtra.CHART_RAIN_AMOUNT) {
            sTitle = getContext().getString(R.string.all_rain_amount);
        } else if (extra.chart == StatsDetailsExtra.CHART_PROGRAM) {
            sTitle = extra.programName;
        } else if (extra.chart == StatsDetailsExtra.CHART_TEMPERATURE) {
            sTitle = getContext().getString(R.string.all_temperature_max_min);
        }
        tvTitle.setText(sTitle);

        LocalDate startDate = new LocalDate();
        int numDays = 0;
        LocalDate date;
        StatsDayViewModel dayData;
        int todayListPosition = 0;
        if (extra.type == StatsDetailsExtra.TYPE_WEEK) {
            numDays = viewModel.weekCategory.numDays;
            startDate = viewModel.weekCategory.startDate;
            int position = 0;
            for (int i = numDays - 1; i >= 0; i--) {
                date = startDate.plusDays(i);
                if (date.equals(new LocalDate())) {
                    todayListPosition = position;
                    break;
                }
                position++;
            }
        } else if (extra.type == StatsDetailsExtra.TYPE_MONTH) {
            numDays = 30;
            startDate = viewModel.monthCategory.startDate.plusDays(viewModel.monthCategory
                    .numDays - numDays);
        } else if (extra.type == StatsDetailsExtra.TYPE_YEAR) {
            numDays = viewModel.yearCategory.numDays;
            startDate = viewModel.yearCategory.startDate;
        }
        List<StatsDayViewModel> items = new ArrayList<>();
        for (int i = numDays - 1; i >= 0; i--) {
            date = startDate.plusDays(i);
            if (extra.type == StatsDetailsExtra.TYPE_WEEK) {
                dayData = viewModel.weekCategory.days.get(date);
            } else if (extra.type == StatsDetailsExtra.TYPE_MONTH) {
                dayData = viewModel.monthCategory.days.get(date);
            } else {
                dayData = viewModel.yearCategory.days.get(date);
            }
            items.add(dayData);
        }
        final StatsDayDataAdapter adapter = new StatsDayDataAdapter(getContext(), items, extra
                .chart, viewModel.isUnitsMetric, extra.programId, decimalFormatter);
        list.setAdapter(adapter);

        if (extra.type == StatsDetailsExtra.TYPE_WEEK) {
            final int finalTodayListPosition = todayListPosition;
            list.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                list.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                //noinspection deprecation
                                list.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                            int listHeight = list.getHeight();
                            View childView = adapter.getView(finalTodayListPosition, null, list);
                            childView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec
                                            .UNSPECIFIED),
                                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                            int itemHeight = childView.getMeasuredHeight();
                            list.setSelectionFromTop(finalTodayListPosition, listHeight / 2 -
                                    itemHeight / 2);
                        }
                    });
        }
    }
}
