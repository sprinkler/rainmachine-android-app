package com.rainmachine.presentation.screens.statsdetails;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.screens.stats.StatsDayViewModel;
import com.rainmachine.presentation.util.Truss;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;
import com.rainmachine.presentation.util.formatter.DecimalFormatter;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class StatsDayDataAdapter extends GenericListAdapter<StatsDayViewModel> {

    private static final String PATTERN_DATE = "MMM dd";
    private LocalDate today = new LocalDate();
    private int chart;
    private boolean isUnitsMetric;
    private int programId;
    private final DecimalFormatter decimalFormatter;

    StatsDayDataAdapter(Context context, List<StatsDayViewModel> items, int chart, boolean
            isUnitsMetric, int programId, DecimalFormatter decimalFormatter) {
        super(context, items);
        this.chart = chart;
        this.isUnitsMetric = isUnitsMetric;
        this.programId = programId;
        this.decimalFormatter = decimalFormatter;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View convertView = this.inflater.inflate(R.layout.item_stats_details, container, false);
        ViewHolder holder = new ViewHolder(convertView);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        StatsDayViewModel dayData = getItem(position);
        boolean isToday = today.equals(dayData.date);
        if (isToday) {
            holder.date.setText(R.string.all_today);
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.text_watering));
            holder.date.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        } else {
            holder.date.setText(dayData.date.toString(PATTERN_DATE, Locale.ENGLISH));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackground(null);
            } else {
                //noinspection deprecation
                view.setBackgroundDrawable(null);
            }
            holder.date.setTextColor(ContextCompat.getColor(getContext(), R.color
                    .selector_color_text_primary));
        }
        CharSequence sValue = null;
        if (chart == StatsDetailsExtra.CHART_WEATHER) {
            String sRainAmount = decimalFormatter.lengthUnitsDecimals(dayData.rainAmount,
                    isUnitsMetric) + " " + (isUnitsMetric ? getContext().getString(R.string
                    .all_mm) : getContext().getString(R.string.all_inch));
            CharSequence formattedRainAmount = new Truss()
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(),
                            isToday ? android.R.color.white : R.color.text_temps_high)))
                    .append(sRainAmount)
                    .popSpan()
                    .build();
            sValue = TextUtils.concat(getTemperatureText(dayData, isToday), "    ",
                    formattedRainAmount);
        } else if (chart == StatsDetailsExtra.CHART_WATER_NEED) {
            sValue = (int) (100 * dayData.dailyWaterNeed) + "%";
            holder.value.setTextColor(ContextCompat.getColor(getContext(), isToday ? android.R
                    .color.white : R.color.text_primary));
        } else if (chart == StatsDetailsExtra.CHART_RAIN_AMOUNT) {
            sValue = decimalFormatter.lengthUnitsDecimals(dayData.rainAmount,
                    isUnitsMetric) + " " + (isUnitsMetric ? getContext().getString(R.string
                    .all_mm) : getContext().getString(R.string.all_inch));
            holder.value.setTextColor(ContextCompat.getColor(getContext(), isToday ? android.R
                    .color.white : R.color.text_primary));
        } else if (chart == StatsDetailsExtra.CHART_PROGRAM) {
            sValue = (int) (dayData.programDailyWaterNeed.get(programId) * 100) + "%";
            holder.value.setTextColor(ContextCompat.getColor(getContext(), isToday ? android.R
                    .color.white : R.color.text_primary));
        } else if (chart == StatsDetailsExtra.CHART_TEMPERATURE) {
            sValue = getTemperatureText(dayData, isToday);
        }
        holder.value.setText(sValue);
    }

    private CharSequence getTemperatureText(StatsDayViewModel item, boolean isToday) {
        final String na = "N/A";
        if (item.maxTemperature == Integer.MIN_VALUE && item.minTemperature == Integer.MIN_VALUE) {
            Truss truss = new Truss()
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(),
                            isToday ? android.R.color.white : R.color.text_temps_high)))
                    .append(na)
                    .popSpan();
            return truss.build();
        } else {
            Truss truss = new Truss()
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(),
                            isToday ? android.R.color.white : R.color.text_temps_high)))
                    .append(item.maxTemperature != Integer.MIN_VALUE ? "" + item.maxTemperature :
                            na)
                    .append(res.getString(R.string.stats_circle_temperature))
                    .popSpan()
                    .append("  ")
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(),
                            isToday ? android.R.color.white : R.color.text_temps_low)))
                    .append(item.minTemperature != Integer.MIN_VALUE ? "" + item.minTemperature :
                            na)
                    .append(res.getString(R.string.stats_circle_temperature))
                    .popSpan();
            return truss.build();
        }
    }

    static class ViewHolder {
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.value)
        TextView value;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
