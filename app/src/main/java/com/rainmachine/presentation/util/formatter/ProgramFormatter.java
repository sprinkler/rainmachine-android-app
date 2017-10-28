package com.rainmachine.presentation.util.formatter;

import android.content.Context;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramWateringTimes;

import java.util.List;
import java.util.Locale;

public class ProgramFormatter {

    private final Context context;
    private final CalendarFormatter calendarFormatter;

    public ProgramFormatter(Context context, CalendarFormatter calendarFormatter) {
        this.context = context;
        this.calendarFormatter = calendarFormatter;
    }

    public String wateringTimesDuration(Program program, List<ProgramWateringTimes
            .SelectedDayDuration> values) {
        if (!program.isWeekDays()) {
            return calendarFormatter.hourMinSecLabel(values.get(0).seconds);
        } else {
            StringBuilder sb = new StringBuilder();
            for (ProgramWateringTimes.SelectedDayDuration value : values) {
                sb.append(value.dayOfWeek.getAsShortText(Locale.ENGLISH));
                sb.append(": ");
                sb.append(calendarFormatter.hourMinSecLabel(value.seconds));
                sb.append("\n");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }
    }

    public String startTime(Program program, boolean use24HourFormat) {
        if (program.startTime.isTimeOfDay()) {
            return CalendarFormatter.hourMinColon(program.startTime.localDateTime.toLocalTime(),
                    use24HourFormat);
        } else {
            StringBuilder sb = new StringBuilder(15);
            // As requested, we show x minutes for the first time
            if (program.startTime.isTimeOfDay() && program.isNew()) {
                sb.append("x");
            } else {
                sb.append(program.startTime.offsetMinutes);
            }
            sb.append(" ")
                    .append(context.getString(R.string.all_minutes))
                    .append(" ");
            if (program.startTime.isBefore()) {
                sb.append(context.getString(R.string.all_before));
            } else {
                sb.append(context.getString(R.string.all_after));
            }
            sb.append(" ");
            if (program.startTime.isSunrise()) {
                sb.append(context.getString(R.string.all_sunrise));
            } else {
                sb.append(context.getString(R.string.all_sunset));
            }
            return sb.toString();
        }
    }
}
