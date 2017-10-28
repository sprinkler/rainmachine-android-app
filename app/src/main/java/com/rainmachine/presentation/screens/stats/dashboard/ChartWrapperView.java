package com.rainmachine.presentation.screens.stats.dashboard;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.rainmachine.R;
import com.rainmachine.presentation.screens.stats.dashboard.charts.CustomChart;
import com.rainmachine.presentation.util.ViewUtils;

/**
 * Created by Tremend Software on 2/17/2015.
 */
public class ChartWrapperView extends RelativeLayout {

    public ChartWrapperView(Context context) {
        super(context);
    }

    public ChartWrapperView(Context context, int height) {
        super(context);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout
                .LayoutParams.MATCH_PARENT, height);
        layoutParams.setMargins(0, 0, 0, (int) ViewUtils.dpToPixels(8, context));
        setLayoutParams(layoutParams);
        setBackgroundResource(R.drawable.chart_shape);
    }

    public ChartWrapperView(Context context, CustomChart chart, String chartTitle, String
            chartUnit, int height) {
        this(context, chart, height);
        chart.setHeader(chartTitle, chartUnit);
    }

    public ChartWrapperView(Context context, View chart, int height) {
        this(context, height);
        addView(chart);
    }

    public void setBackgroundForVisible() {
        setBackgroundResource(R.drawable.chart_shape);
    }

    public void setBackgroundForHidden() {
        setBackgroundResource(R.drawable.chart_shape_hidden);
    }
}
