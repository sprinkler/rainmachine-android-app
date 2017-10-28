package com.rainmachine.presentation.screens.weathersourcedetails;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.rainmachine.R;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.Truss;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class WUndergroundParamsView extends LinearLayout {

    private static final String LEARN_MORE_LINK = "http://wikipage.rainmachine.com/index" +
            ".php?title=Wunderground-weather-data-source-how-to";

    @Inject
    WeatherSourceDetailsPresenter presenter;

    @BindView(R.id.learn_more)
    TextView tvLearnMore;
    @BindView(R.id.test_developer_key)
    TextView tvTestDeveloperKey;
    @BindView(R.id.input_developer_api_key)
    EditText inputDeveloperKey;
    @BindView(R.id.view_weather_station)
    ViewGroup viewWeatherStation;
    @BindView(R.id.weather_station)
    TextView tvWeatherStation;

    private CompositeDisposable disposables;

    public WUndergroundParamsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
        disposables = new CompositeDisposable();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        if (!isInEditMode()) {
            presenter.attachWUndergroundView(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disposables.clear();
    }

    @OnClick(R.id.card_weather_station)
    void onClickWeatherStation() {
        presenter.onClickWeatherStation();
    }

    public void updateContent(Parser parser) {
        Truss truss = new Truss();
        truss.pushSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(LEARN_MORE_LINK));
                getContext().startActivity(intent);
            }
        });
        truss.pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.main)));
        truss.append(getContext().getString(R.string.weather_source_details_learn_more));
        truss.popSpan();
        truss.popSpan();
        tvLearnMore.setText(truss.build());
        tvLearnMore.setMovementMethod(LinkMovementMethod.getInstance());

        disposables.add(RxTextView
                .textChanges(inputDeveloperKey)
                .debounce(700, TimeUnit.MILLISECONDS)
                .filter(s -> s.length() >= 10)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(charSequence -> showDeveloperKeyCheckInProgress())
                .switchMap(s -> presenter.wUndergroundDeveloperKeyChanges(s))
                .doOnError(GenericErrorDealer.INSTANCE)
                .subscribeWith(new DeveloperKeySubscriber()));

        inputDeveloperKey.setText(parser.wUndergroundParams.apiKey);
        inputDeveloperKey.setSelection(inputDeveloperKey.length());
        updateWeatherStation(parser.wUndergroundParams.customStationName);
    }

    public void updateWeatherStation(String stationName) {
        if (!Strings.isBlank(stationName)) {
            tvWeatherStation.setText(stationName);
        } else {
            tvWeatherStation.setText(R.string.all);
        }
    }

    private void showDeveloperKeyCheckInProgress() {
        Truss truss = new Truss();
        truss.append(getResources().getString(R.string
                .weather_source_details_test_wunderground_developer_key));
        truss.append(getResources().getString(R.string.weather_source_details_please_wait));
        tvTestDeveloperKey.setText(truss.build());
    }

    private final class DeveloperKeySubscriber extends
            DisposableObserver<WUndergroundDeveloperApiKeyCheck> {

        @Override
        public void onNext(WUndergroundDeveloperApiKeyCheck WUndergroundDeveloperApiKeyCheck) {
            Truss truss = new Truss();
            truss.append(getResources().getString(R.string
                    .weather_source_details_test_wunderground_developer_key));
            if (WUndergroundDeveloperApiKeyCheck.isValid) {
                truss.pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R
                        .color.text_green)));
                truss.append(getResources().getString(R.string.weather_source_details_passed));
                truss.popSpan();
            } else {
                truss.pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R
                        .color.text_red)));
                truss.append(getResources().getString(R.string
                        .weather_source_details_failed_try_again));
                truss.popSpan();
            }
            tvTestDeveloperKey.setText(truss.build());

            if (WUndergroundDeveloperApiKeyCheck.isValid) {
                viewWeatherStation.setVisibility(View.VISIBLE);
            } else {
                viewWeatherStation.setVisibility(View.GONE);
            }
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
