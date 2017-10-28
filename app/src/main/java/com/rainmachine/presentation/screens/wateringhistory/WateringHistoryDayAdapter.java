package com.rainmachine.presentation.screens.wateringhistory;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.util.adapter.GenericRecyclerAdapter;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.DecimalFormatter;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.rainmachine.R.dimen.activity_horizontal_padding;

class WateringHistoryDayAdapter extends GenericRecyclerAdapter<WateringHistoryViewModel.Day,
        WateringHistoryDayAdapter.ViewHolder> {

    private static final String PATTERN_DATE = "EEEE, MMMM dd";

    private Context context;
    private boolean use24HourFormat;
    private boolean isUnitsMetric;
    private CalendarFormatter calendarFormatter;
    private final DecimalFormatter decimalFormatter;
    private LocalDate today;

    WateringHistoryDayAdapter(Context context, List<WateringHistoryViewModel.Day> items,
                              CalendarFormatter calendarFormatter, DecimalFormatter
                                      decimalFormatter) {
        super(context, items);
        this.context = context;
        this.calendarFormatter = calendarFormatter;
        this.decimalFormatter = decimalFormatter;
        today = new LocalDate();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View convertView = inflater.inflate(R.layout.item_watering_history, viewGroup, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WateringHistoryViewModel.Day dayData = getItem(position);
        holder.gridLayout.removeAllViews();

        TextView textView = new TextView(context);
        textView.setTextAppearance(context, R.style.SectionHeader);
        if (today.equals(dayData.date)) {
            textView.setText(R.string.all_today);
        } else {
            textView.setText(dayData.date.toString(PATTERN_DATE, Locale.ENGLISH));
        }
        applyLeftPadding(textView);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 3, 1f);
        textView.setLayoutParams(params);
        holder.gridLayout.addView(textView);

        if (dayData.programs.size() > 0) {
            // add empty view for first column
            textView = new TextView(context);
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setGravity(Gravity.FILL_VERTICAL);
            textView.setLayoutParams(params);
            holder.gridLayout.addView(textView);

            textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.text_large));
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            textView.setText(String.format(" %s ", context.getString(R.string
                    .watering_history_scheduled)));
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1);
            params.setGravity(Gravity.FILL_HORIZONTAL);
            textView.setLayoutParams(params);
            holder.gridLayout.addView(textView);

            textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen
                    .text_large));
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            textView.setText(R.string.watering_history_watered);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            applyRightPadding(textView);
            params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1);
            params.setGravity(Gravity.FILL_HORIZONTAL);
            textView.setLayoutParams(params);
            holder.gridLayout.addView(textView);

            boolean isFirstProgram = true;
            for (WateringHistoryViewModel.Program programData : dayData.programs) {
                if (!isFirstProgram) {
                    addWhiteVerticalSpace(holder.gridLayout);
                }
                textView = new TextView(context);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R
                        .dimen.text_large));
                textView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                textView.setText(programData.name);
                textView.setTypeface(null, Typeface.BOLD);
                applyLeftPadding(textView);
                params = new GridLayout.LayoutParams();
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 3, 1f);
                textView.setLayoutParams(params);
                holder.gridLayout.addView(textView);

                for (WateringHistoryViewModel.Zone zoneData : programData.zones) {
                    textView = new TextView(context);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            res.getDimension(R.dimen.text_large));
                    textView.setEllipsize(TextUtils.TruncateAt.END);
                    textView.setSingleLine(true);
                    textView.setText(zoneData.name);
                    textView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    textView.setWidth(0);
                    applyLeftPadding(textView);
                    params = new GridLayout.LayoutParams();
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1);
                    params.setGravity(Gravity.FILL_HORIZONTAL);
                    textView.setLayoutParams(params);
                    holder.gridLayout.addView(textView);

                    textView = new TextView(context);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            res.getDimension(R.dimen.text_large));
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    textView.setText(calendarFormatter.hourMinSecColon(zoneData.totalScheduled));
                    textView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    params = new GridLayout.LayoutParams();
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1);
                    params.setGravity(Gravity.FILL_HORIZONTAL);
                    textView.setLayoutParams(params);
                    holder.gridLayout.addView(textView);

                    textView = new TextView(context);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            res.getDimension(R.dimen.text_large));
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    textView.setText(calendarFormatter.hourMinSecColon(zoneData.totalWatered));
                    textView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    applyRightPadding(textView);
                    params = new GridLayout.LayoutParams();
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1);
                    params.setGravity(Gravity.FILL_HORIZONTAL);
                    textView.setLayoutParams(params);
                    holder.gridLayout.addView(textView);

                    if (zoneData.cycles.size() > 0) {
                        textView = new TextView(context);
                        textView.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                res.getDimension(R.dimen.text_large));
                        textView.setGravity(Gravity.START);
                        String startTime = zoneData.cycles.get(0).startTime.toString
                                (calendarFormatter.timeFormatWithSeconds(use24HourFormat));
                        textView.setText(res.getString(R.string
                                .watering_history_started_at, startTime));
                        textView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        applyLeftPadding(textView);
                        params = new GridLayout.LayoutParams();
                        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 3, 1f);
                        textView.setLayoutParams(params);
                        holder.gridLayout.addView(textView);

                        textView = new TextView(context);
                        textView.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                res.getDimension(R.dimen.text_large));
                        textView.setGravity(Gravity.START);
                        String units = res.getString(isUnitsMetric ? R.string.all_m3 : R.string
                                .all_gal);
                        textView.setText(res.getString(R.string
                                .watering_history_water_saved, decimalFormatter.limitedDecimals
                                (zoneData.waterSavedAmount, 2), units));
                        textView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        applyLeftPadding(textView);
                        params = new GridLayout.LayoutParams();
                        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 3, 1f);
                        textView.setLayoutParams(params);
                        holder.gridLayout.addView(textView);
                    }
                    addWhiteVerticalSpace(holder.gridLayout);
                }
                isFirstProgram = false;
            }
        } else {
            textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen
                    .text_large));
            textView.setText(context.getString(R.string.watering_history_no_watering_data));
            textView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            textView.setTypeface(null, Typeface.ITALIC);
            applyLeftRightPadding(textView);
            params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 3, 1f);
            textView.setLayoutParams(params);
            holder.gridLayout.addView(textView);

            addVerticalSpace(holder.gridLayout);
        }

        addVerticalSpace(holder.gridLayout);
    }

    private void addVerticalSpace(GridLayout gridLayout) {
        Space space = new Space(context);
        space.setMinimumHeight(res.getDimensionPixelSize(R.dimen.spacing_medium));
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 3, 1f);
        space.setLayoutParams(params);
        gridLayout.addView(space);
    }

    private void addWhiteVerticalSpace(GridLayout gridLayout) {
        TextView space = new TextView(context);
        space.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        space.setMinimumHeight(res.getDimensionPixelSize(R.dimen.spacing_small));
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 3, 1f);
        space.setLayoutParams(params);
        gridLayout.addView(space);
    }

    private void applyLeftPadding(View view) {
        view.setPadding(res.getDimensionPixelSize(activity_horizontal_padding), 0, 0, 0);
    }

    private void applyLeftRightPadding(View view) {
        view.setPadding(res.getDimensionPixelSize(activity_horizontal_padding), 0, res
                .getDimensionPixelSize(activity_horizontal_padding), 0);
    }

    private void applyRightPadding(View view) {
        view.setPadding(0, 0, res.getDimensionPixelSize(activity_horizontal_padding), 0);
    }

    public void setItems(List<WateringHistoryViewModel.Day> days, boolean use24HourFormat,
                         boolean isUnitsMetric) {
        this.use24HourFormat = use24HourFormat;
        this.isUnitsMetric = isUnitsMetric;
        setItems(days);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.grid)
        GridLayout gridLayout;

        View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            ButterKnife.bind(this, view);
        }
    }
}
