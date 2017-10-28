package com.rainmachine.presentation.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rainmachine.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VitalsChart extends LinearLayout implements View.OnClickListener {

    @BindView(R.id.description)
    TextView tvDescription;
    @BindView(R.id.last_weather_update)
    TextView tvLastWeatherUpdate;
    @BindView(R.id.view_percentage)
    CirclePercentageView percentageView;
    @BindView(R.id.title)
    TextView tvTitle;

    private OnClickWeatherUpdateListener listener;

    public VitalsChart(Context context) {
        super(context);
        init(context);
    }

    public VitalsChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        inflate(context, R.layout.include_vitals_chart, this);
        ButterKnife.bind(this);
        setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onClickWeatherUpdate();
        }
    }

    public void setTitle(@Nullable String title) {
        tvTitle.setText(title);
    }

    public void setPercentage(float percentage) {
        percentageView.setPercentage(percentage);
    }

    public void setDescription(@Nullable String address) {
        tvDescription.setText(address);
    }

    public void setLastWeatherUpdate(@Nullable String lastWeatherUpdate) {
        tvLastWeatherUpdate.setText(lastWeatherUpdate);
    }

    public void setOnClickListener(@Nullable OnClickWeatherUpdateListener listener) {
        this.listener = listener;
    }

    public interface OnClickWeatherUpdateListener {
        void onClickWeatherUpdate();
    }
}
