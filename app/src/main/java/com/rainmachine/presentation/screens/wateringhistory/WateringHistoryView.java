package com.rainmachine.presentation.screens.wateringhistory;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.DecimalFormatter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WateringHistoryView extends ViewFlipper {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    WateringHistoryPresenter presenter;
    @Inject
    CalendarFormatter calendarFormatter;
    @Inject
    DecimalFormatter decimalFormatter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private WateringHistoryDayAdapter adapter;

    public WateringHistoryView(Context context, AttributeSet attrs) {
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

    public void updateContent(WateringHistoryViewModel viewModel, WateringHistoryInterval
            wateringHistoryInterval) {
        List<WateringHistoryViewModel.Day> items;
        if (wateringHistoryInterval == WateringHistoryInterval.WEEK) {
            items = viewModel.days.subList(0, 7);
        } else if (wateringHistoryInterval == WateringHistoryInterval.MONTH) {
            items = viewModel.days.subList(0, 31);
        } else {
            items = viewModel.days.subList(0, WateringHistoryConstants.NUM_PAST_DAYS);
        }
        adapter.setItems(items, viewModel.use24HourFormat, viewModel.isUnitsMetric);
    }

    public void setup() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                .VERTICAL, false));
        adapter = new WateringHistoryDayAdapter(getContext(), new ArrayList<>(),
                calendarFormatter, decimalFormatter);
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