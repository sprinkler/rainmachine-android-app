package com.rainmachine.presentation.screens.weathersettings;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.weathersources.WeatherSource;
import com.rainmachine.presentation.util.formatter.ParserFormatter;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WeatherSettingsView extends ViewFlipper implements WeatherSettingsContract.View {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    WeatherSettingsContract.Presenter presenter;
    @Inject
    ParserFormatter parserFormatter;

    @BindView(R.id.view_data_sources)
    LinearLayout viewDataSources;
    @BindView(R.id.help_noaa)
    TextView tvHelpNOAA;
    @BindView(R.id.tv_num_additional)
    TextView tvNumAdditional;
    @BindView(R.id.tv_subtitle_sensitivity)
    TextView tvSubtitleSensitivity;

    public WeatherSettingsView(Context context, AttributeSet attrs) {
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

    @OnClick(R.id.card_weather_services)
    public void onClickWeatherServices() {
        presenter.onClickWeatherServices();
    }

    @OnClick(R.id.card_weather_sensitivity)
    public void onClickWeatherSensitivity() {
        presenter.onClickWeatherSensitivity();
    }

    @OnClick(R.id.btn_retry)
    public void onClickRetry() {
        presenter.onClickRetry();
    }

    @Override
    public void updateContent(WeatherSettingsViewModel viewModel) {
        viewDataSources.removeAllViews();
        viewDataSources.addView(inflateSourceCard(viewModel.defaultSource, true));
        for (WeatherSource weatherSource : viewModel.enabledSources) {
            viewDataSources.addView(inflateSourceCard(weatherSource, false));
        }
        // Show help only if NOAA is shown on this screen
        tvHelpNOAA.setVisibility(viewModel.defaultSource.parser.isNOAA() && viewModel
                .enabledSources.size()
                == 0 ? View.VISIBLE : View.GONE);

        tvNumAdditional.setText(String.format(Locale.ENGLISH, "%d", viewModel.numDisabledSources));

        if (viewModel.isRainSensitivityChanged || viewModel.isFieldCapacityChanged || viewModel
                .isWindSensitivityChanged) {
            StringBuilder sb = new StringBuilder(30);
            if (viewModel.isRainSensitivityChanged) {
                int rainSensitivity = (int) (viewModel.rainSensitivity * 100);
                sb.append(getContext().getString(R.string.weather_settings_rain_sensitivity_changed,
                        rainSensitivity));
                sb.append("\n");
            }
            if (viewModel.isFieldCapacityChanged) {
                sb.append(getContext().getResources().getQuantityString(R.plurals
                        .weather_settings_field_capacity_changed, viewModel.fieldCapacity, viewModel
                        .fieldCapacity));
                sb.append("\n");
            }
            if (viewModel.isWindSensitivityChanged) {
                int windSensitivity = (int) (viewModel.windSensitivity * 100);
                sb.append(getContext().getString(R.string.weather_settings_wind_sensitivity_changed,
                        windSensitivity));
                sb.append("\n");
            }
            tvSubtitleSensitivity.setText(sb.substring(0, sb.length() - 1));
            tvSubtitleSensitivity.setVisibility(View.VISIBLE);
        } else {
            tvSubtitleSensitivity.setVisibility(View.GONE);
        }
    }

    private View inflateSourceCard(final WeatherSource weatherSource, boolean isDefault) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.include_weather_source,
                viewDataSources, false);
        String name = isDefault ? getContext().getString(R.string
                        .weather_settings_default_weather_source,
                weatherSource.parser.name) : weatherSource.parser.name;
        ((TextView) ButterKnife.findById(view, R.id.name)).setText(name);
        ((SwitchCompat) ButterKnife.findById(view, R.id.toggle_enabled)).setChecked
                (weatherSource.parser.enabled);
        ((TextView) ButterKnife.findById(view, R.id.last_run)).setText(parserFormatter
                .lastRun(getContext(), weatherSource.parser));
        view.setOnClickListener(v -> presenter.onClickWeatherSource(weatherSource));
        return view;
    }

    @Override
    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    @Override
    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    @Override
    public void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }
}
