package com.rainmachine.presentation.screens.weathersources;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.ParserFormatter;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WeatherSourcesView extends ViewFlipper {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    WeatherSourcesPresenter presenter;
    @Inject
    ParserFormatter parserFormatter;

    @BindView(android.R.id.list)
    ListView list;
    @BindView(R.id.btn_add_weather_source)
    Button btnAddWeatherSource;

    private WeatherSourceAdapter adapter;

    public WeatherSourcesView(Context context, AttributeSet attrs) {
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
    public void onClickRetry() {
        presenter.onClickRetry();
    }

    @OnClick(R.id.btn_add_weather_source)
    public void onClickAddWeatherSource() {
        presenter.onClickAddWeatherSource();
    }

    public void updateContent(WeatherSourcesViewModel viewModel) {
        adapter.setItems(viewModel.sources);
    }

    public void setup(boolean hasFullFunctionality) {
        adapter = new WeatherSourceAdapter(getContext(), new ArrayList<>(),
                presenter, parserFormatter);
        list.setAdapter(adapter);
        list.addFooterView(LayoutInflater.from(getContext()).inflate(R.layout
                .item_footer_weather_sources, list, false));
        btnAddWeatherSource.setVisibility(hasFullFunctionality ? ViewFlipper.VISIBLE : GONE);
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    public void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }
}
