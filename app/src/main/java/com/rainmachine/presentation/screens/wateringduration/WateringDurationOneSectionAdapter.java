package com.rainmachine.presentation.screens.wateringduration;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.util.adapter.GenericRecyclerAdapter;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class WateringDurationOneSectionAdapter extends
        GenericRecyclerAdapter<ZoneViewModel,
                WateringDurationOneSectionAdapter.ViewHolder> {

    private final CalendarFormatter calendarFormatter;
    private final int textColor;
    private final WateringDurationContract.Presenter presenter;

    WateringDurationOneSectionAdapter(Context context, CalendarFormatter calendarFormatter,
                                      List<ZoneViewModel> items,
                                      int textColor, WateringDurationContract.Presenter presenter) {
        super(context, items);
        this.calendarFormatter = calendarFormatter;
        this.textColor = textColor;
        this.presenter = presenter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View convertView = inflater.inflate(R.layout.item_watering_duration, viewGroup, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ZoneViewModel item = getItem(position);
        holder.name.setText(item.id + ". " + item.name);
        holder.name.setTextColor(textColor);
        holder.duration.setText(calendarFormatter.hourMinSecColon(item.durationSeconds));
        holder.view.setOnClickListener(holder);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.tv_duration)
        TextView duration;

        View view;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                ZoneViewModel zoneModel = getItem(adapterPosition);
                presenter.onClickZone(zoneModel);
            }
        }
    }
}
