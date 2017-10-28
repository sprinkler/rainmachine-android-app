package com.rainmachine.presentation.screens.nearbystations;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NearbyStationsView extends ViewFlipper implements CompoundButton
        .OnCheckedChangeListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    NearbyStationsPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.location)
    TextView tvLocation;
    @BindView(R.id.toggle_personal_weather_stations)
    SwitchCompat togglePersonal;

    private NearbyStationAdapter adapter;

    public NearbyStationsView(Context context, AttributeSet attrs) {
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.toggle_personal_weather_stations) {
            presenter.onCheckedChangedPersonalWeatherStations(isChecked);
        }
    }

    public void updateContent(NearbyStationsViewModel viewModel, boolean showPersonal) {
        int initialCapacity = viewModel.parser.wUndergroundParams
                .airportStations.size() + (showPersonal ? viewModel.parser.wUndergroundParams
                .nearbyStations.size() : 0);
        List<Parser.WeatherStation> list = new ArrayList<>(initialCapacity);
        list.addAll(viewModel.parser.wUndergroundParams.airportStations);
        if (showPersonal) {
            list.addAll(viewModel.parser.wUndergroundParams.nearbyStations);
        }
        adapter.setItems(list);
        tvLocation.setText(viewModel.currentLocationAddress);
        togglePersonal.setOnCheckedChangeListener(null);
        togglePersonal.setChecked(showPersonal);
        togglePersonal.setOnCheckedChangeListener(this);
        if (!Strings.isBlank(viewModel.parser.wUndergroundParams.customStationName)) {
            int position = 0;
            for (Parser.WeatherStation weatherStation : list) {
                if (weatherStation.name.equals(viewModel.parser.wUndergroundParams
                        .customStationName)) {
                    adapter.setItemChecked(position);
                    break;
                }
                position++;
            }
        }
    }

    public void setup() {
        adapter = new NearbyStationAdapter(getContext(), new ArrayList<>(), presenter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), null));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                .VERTICAL, false));
        recyclerView.setAdapter(adapter);
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
