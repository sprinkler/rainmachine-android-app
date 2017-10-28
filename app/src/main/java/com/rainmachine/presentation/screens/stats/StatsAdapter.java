package com.rainmachine.presentation.screens.stats;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.DayStats;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.util.Truss;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;
import com.rainmachine.presentation.widgets.WaterPercentageView;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

class StatsAdapter extends GenericListAdapter<DayStats> {

    private static final String FORMAT_DAY = "MMM d";

    private final Context context;
    private DateTime now;
    private int maxTextWidth = 0;

    StatsAdapter(Context context, List<DayStats> items) {
        super(context, items);
        this.context = context;
        now = new DateTime();
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View convertView = this.inflater.inflate(R.layout.item_day, container, false);
        ViewHolder holder = new ViewHolder(convertView);
        holder.day.setWidth(maxTextWidth);
        holder.temps.setWidth(maxTextWidth);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        DayStats day = getItem(position);
        if (day.wateringFlag >= 1 && day.wateringFlag <= 4) {
            String[] flags = res.getStringArray(R.array.stats_watering_flag);
            holder.waterFlag.setText(flags[day.wateringFlag]);
            holder.flipperWater.setDisplayedChild(1);
        } else {
            holder.waterView.setPercentage(day.percentage);
            holder.flipperWater.setDisplayedChild(0);
        }

        holder.ivWeather.setImageResource(day.weatherImageId);

        holder.day.setText(getDayText(day));
        holder.temps.setText(getTemperatureText(day));
    }

    @Override
    public void setItems(List<DayStats> items) {
        this.items = items;
        computeMaxTextWidth();
        notifyDataSetChanged();
    }

    private String getDayText(DayStats item) {
        if (item.id == 0) {
            return res.getString(R.string.all_today);
        } else {
            int numDays = (int) item.id;
            return now.plusDays(numDays).toString(FORMAT_DAY, Locale.ENGLISH);
        }
    }

    private CharSequence getTemperatureText(DayStats item) {
        if (!isValidTemperature(item.maxTemp) || !isValidTemperature(item.minTemp)) {
            return null;
        } else {
            Truss truss = new Truss()
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color
                            .text_temps_high)))
                    .append(item.maxTemp)
                    .append(context.getString(R.string.stats_circle_temperature))
                    .popSpan()
                    .append("  ")
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color
                            .text_temps_low)))
                    .append(item.minTemp)
                    .append(context.getString(R.string.stats_circle_temperature))
                    .popSpan();
            return truss.build();
        }
    }

    private void computeMaxTextWidth() {
        View view = View.inflate(getContext(), R.layout.item_day, null);
        TextView tvDay = ButterKnife.findById(view, R.id.day);
        TextView tvTemps = ButterKnife.findById(view, R.id.temps);
        int width;
        for (DayStats item : items) {
            width = (int) tvDay.getPaint().measureText(getDayText(item));
            if (width > maxTextWidth) {
                maxTextWidth = width;
            }

            CharSequence cs = getTemperatureText(item);
            if (!Strings.isBlank(cs)) {
                width = (int) tvTemps.getPaint().measureText(cs.toString());
                if (width > maxTextWidth) {
                    maxTextWidth = width;
                }
            }
        }
        Timber.d("Max text width %d", maxTextWidth);
    }

    private static boolean isValidTemperature(int temp) {
        return (temp > -200 && temp < 200);
    }

    static class ViewHolder {
        @BindView(R.id.iv_weather)
        ImageView ivWeather;
        @BindView(R.id.day)
        TextView day;
        @BindView(R.id.temps)
        TextView temps;
        @BindView(R.id.water)
        WaterPercentageView waterView;
        @BindView(R.id.water_flag)
        TextView waterFlag;
        @BindView(R.id.flipper_water)
        ViewFlipper flipperWater;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
