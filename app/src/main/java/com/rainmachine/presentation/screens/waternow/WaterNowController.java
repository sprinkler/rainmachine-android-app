package com.rainmachine.presentation.screens.waternow;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.airbnb.epoxy.OnModelClickListener;
import com.airbnb.epoxy.TypedEpoxyController;
import com.rainmachine.R;
import com.rainmachine.domain.model.HandPreference;
import com.rainmachine.presentation.util.adapter.SpaceItemModel_;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

class WaterNowController extends TypedEpoxyController<WaterNowState> {

    private Context context;
    private WaterNowContract.Presenter presenter;
    private CalendarFormatter formatter;
    private LinearLayoutManager layoutManager;
    private final int scrollOffset;
    private boolean isFirstTimeRender;

    WaterNowController(Context context, WaterNowContract.Presenter presenter, CalendarFormatter
            formatter, LinearLayoutManager layoutManager, int scrollOffset) {
        this.context = context;
        this.presenter = presenter;
        this.formatter = formatter;
        this.layoutManager = layoutManager;
        this.scrollOffset = scrollOffset;
        isFirstTimeRender = true;
    }

    @Override
    protected void buildModels(WaterNowState state) {
        for (ZoneViewModel zone : state.enabledZones) {
            new WaterNowItemModel_(context, state.showMinusPlus, formatter)
                    .id(zone.id)
                    .item(zone)
                    .handPreference(state.handPreference)
                    .clickListener(clickListener)
                    .addTo(this);
        }

        new SpaceItemModel_()
                .id("spacing")
                .spacing(context.getResources().getDimensionPixelSize(R.dimen.spacing_medium))
                .addTo(this);

        for (ZoneViewModel zone : state.disabledZones) {
            new WaterNowItemModel_(context, state.showMinusPlus, formatter)
                    .id(zone.id)
                    .item(zone)
                    .handPreference(state.handPreference)
                    .clickListener(clickListener)
                    .addTo(this);
        }

        scrollToRunningIfApplicable(state);
    }

    private void scrollToRunningIfApplicable(WaterNowState state) {
        if (!isFirstTimeRender || !state.hasCompleteData) {
            return;
        }
        boolean shouldScroll = false;
        int zonePosition = 0;
        for (ZoneViewModel zoneViewModel : state.enabledZones) {
            if (zoneViewModel.state == ZoneViewModel.State.RUNNING) {
                shouldScroll = true;
                break;
            }
            zonePosition++;
        }
        if (!shouldScroll) {
            for (ZoneViewModel zoneViewModel : state.disabledZones) {
                if (zoneViewModel.state == ZoneViewModel.State.RUNNING) {
                    shouldScroll = true;
                    break;
                }
                zonePosition++;
            }
        }
        if (shouldScroll) {
            layoutManager.scrollToPositionWithOffset(zonePosition, scrollOffset);
        }
        isFirstTimeRender = false;
    }

    private OnModelClickListener<WaterNowItemModel_, WaterNowItemModel.ViewHolder> clickListener
            = new OnModelClickListener<WaterNowItemModel_, WaterNowItemModel.ViewHolder>() {

        @Override
        public void onClick(WaterNowItemModel_ model, WaterNowItemModel.ViewHolder
                parentView, View clickedView, int position) {
            int id = clickedView.getId();
            ZoneViewModel item = model.item();
            if (id == R.id.view_right) {
                if (model.handPreference() == HandPreference.RIGHT_HAND) {
                    if (item.state == ZoneViewModel.State.IDLE) {
                        presenter.onClickStart(item);
                    } else {
                        presenter.onClickStop(item);
                    }
                } else {
                    presenter.onClickEdit(item);
                }
            } else if (id == R.id.view_left) {
                if (model.handPreference() == HandPreference.RIGHT_HAND) {
                    presenter.onClickEdit(item);
                } else {
                    if (item.state == ZoneViewModel.State.IDLE) {
                        presenter.onClickStart(item);
                    } else {
                        presenter.onClickStop(item);
                    }
                }
            } else if (id == R.id.view_minus) {
                presenter.onClickMinus(item);
            } else if (id == R.id.view_plus) {
                presenter.onClickPlus(item);
            } else if (id == R.id.view_item) {
                presenter.onClickEdit(item);
            }
        }
    };
}
