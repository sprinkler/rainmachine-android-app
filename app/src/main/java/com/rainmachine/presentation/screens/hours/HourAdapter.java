package com.rainmachine.presentation.screens.hours;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.HourlyRestrictionFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HourAdapter extends GenericListAdapter<HourlyRestriction> {

    private CalendarFormatter calendarFormatter;
    private HourlyRestrictionFormatter hourlyRestrictionFormatter;
    private boolean use24HourFormat;

    public HourAdapter(Context context, List<HourlyRestriction> items, CalendarFormatter
            calendarFormatter, HourlyRestrictionFormatter hourlyRestrictionFormatter) {
        super(context, items);
        this.calendarFormatter = calendarFormatter;
        this.hourlyRestrictionFormatter = hourlyRestrictionFormatter;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View convertView = this.inflater.inflate(R.layout.item_hour, container, false);
        ViewHolder holder = new ViewHolder(convertView);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        HourlyRestriction hourlyRestriction = getItem(position);
        holder.frequency.setText(hourlyRestriction.isDaily() ? res.getString(R.string
                .all_every_day_restriction) : calendarFormatter.weekDays(hourlyRestriction
                .weekDays));
        holder.interval.setText(hourlyRestrictionFormatter.interval(hourlyRestriction,
                use24HourFormat));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).uid;
    }

    public void setUse24HourFormat(boolean use24HourFormat) {
        this.use24HourFormat = use24HourFormat;
    }

    static class ViewHolder {
        @BindView(R.id.frequency)
        TextView frequency;
        @BindView(R.id.interval)
        TextView interval;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
