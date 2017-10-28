package com.rainmachine.presentation.util.formatter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.rainmachine.R;
import com.rainmachine.domain.util.Preconditions;
import com.rainmachine.infrastructure.util.BaseApplication;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Locale;

public class CalendarFormatter {

    private Context ctx;
    private PeriodFormatter hourMinSecLabelFormatter;
    private PeriodFormatter hourMinSecColonFormatter;

    public CalendarFormatter() {
        ctx = BaseApplication.getContext();
        hourMinSecLabelFormatter = new PeriodFormatterBuilder()
                .appendHours().appendSuffix(" hour ")
                .appendMinutes().appendSuffix(" min ")
                .appendSeconds().appendSuffix(" sec")
                .toFormatter();
        hourMinSecColonFormatter = new PeriodFormatterBuilder()
                .minimumPrintedDigits(2)
                .appendHours().appendSeparator(":")
                .printZeroAlways()
                .appendMinutes().appendSeparator(":")
                .appendSeconds()
                .toFormatter();
    }

    public String timeFormatHourMin(boolean use24HourFormat) {
        return use24HourFormat ? "HH:mm" : "hh:mm a";
    }

    public String timeFormatWithSeconds(boolean use24HourFormat) {
        return use24HourFormat ? "HH:mm:ss" : "hh:mm:ss a";
    }

    public static String hourMinColon(LocalTime time, boolean use24HourFormat) {
        String format = use24HourFormat ? "HH:mm" : "hh:mm a";
        return time.toString(format);
    }

    public String hourMinSecColon(long seconds) {
        Period period = Duration.standardSeconds(seconds).toPeriod();
        return hourMinSecColonFormatter.print(period);
    }

    public String hourMinSecColonFull(long seconds) {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .minimumPrintedDigits(2)
                .printZeroAlways()
                .appendHours().appendSeparator(":")
                .appendMinutes().appendSeparator(":")
                .appendSeconds()
                .toFormatter();

        DateTime now = DateTime.now();
        DateTime then = now.plusSeconds((int) seconds);
        Period period = new Period(now, then, PeriodType.yearMonthDayTime().withMillisRemoved());
        return formatter.print(period);
    }

    public String hourMinSecLabel(long seconds) {
        Period period = Duration.standardSeconds(seconds).toPeriod();
        return hourMinSecLabelFormatter.print(period);
    }

    public String yearMonthDay(@NonNull LocalDateTime dateTime) {
        Preconditions.checkNotNull(dateTime, "dateTime == null");
        return dateTime.toString("yyyy/MM/dd", Locale.ENGLISH);
    }

    public String monthDayYear(@NonNull LocalDate dateTime) {
        return dateTime.toString("MMM dd, yyyy", Locale.ENGLISH);
    }

    public String dayOfWeekmonthDayYear(@NonNull LocalDate dateTime) {
        return dateTime.toString("EEE MMM dd, yyyy", Locale.ENGLISH);
    }

    public String monthDay(@NonNull LocalDate dateTime) {
        return dateTime.toString("MMM dd", Locale.ENGLISH);
    }

    public String dayOfWeekMonthDay(@NonNull DateTime dateTime) {
        return dateTime.toString("EEEEE, MMM d", Locale.ENGLISH);
    }

    public String daysHoursMinutes(long counter, boolean newLine) {
        String sep = newLine ? "\n" : " ";
        PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
                .appendDays().appendSuffix(" " + ctx.getString(R.string.all_day) + sep,
                        " " + ctx.getString(R.string.all_days) + sep)
                .appendHours().appendSuffix(" " + ctx.getString(R.string.all_hour) + sep,
                        " " + ctx.getString(R.string.all_hours) + sep)
                .appendMinutes().appendSuffix(" " + ctx.getString(R.string
                        .all_minute), " " + ctx.getString(R.string.all_minutes))
                .printZeroAlways()
                .toFormatter();
        DateTime now = DateTime.now();
        DateTime then = now.plusSeconds((int) counter);
        Period period = new Period(now, then, PeriodType.yearMonthDayTime().withMillisRemoved());
        return periodFormatter.print(period);
    }

    public String ago(@NonNull LocalDateTime startDateTime, @NonNull LocalDateTime endDateTime) {
        Preconditions.checkNotNull(startDateTime, "startDateTime == null");
        Preconditions.checkNotNull(endDateTime, "endDateTime == null");
        if (startDateTime.isAfter(endDateTime)) {
            return "";
        }
        String ago = " ago";
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        Period period = new Period(startDateTime, endDateTime, PeriodType.standard()
                .withMillisRemoved());
        if (period.getYears() > 0) {
            builder = new PeriodFormatterBuilder()
                    .appendYears()
                    .appendSuffix(" " + ctx.getString(R.string.all_year_lc) + ago,
                            " " + ctx.getString(R.string.all_years_lc) + ago);
        } else if (period.getMonths() > 0) {
            builder = new PeriodFormatterBuilder()
                    .appendMonths()
                    .appendSuffix(" " + ctx.getString(R.string.all_month_lc) + ago,
                            " " + ctx.getString(R.string.all_months_lc) + ago);
        } else if (period.getWeeks() > 0) {
            builder = new PeriodFormatterBuilder()
                    .appendWeeks()
                    .appendSuffix(" " + ctx.getString(R.string.all_week_lc) + ago,
                            " " + ctx.getString(R.string.all_weeks_lc) + ago);
        } else if (period.getDays() > 0) {
            builder = new PeriodFormatterBuilder()
                    .appendDays()
                    .appendSuffix(" " + ctx.getString(R.string.all_day) + ago,
                            " " + ctx.getString(R.string.all_days) + ago);
        } else if (period.getHours() > 0) {
            builder = new PeriodFormatterBuilder()
                    .appendHours()
                    .appendSuffix(" " + ctx.getString(R.string.all_hour) + ago,
                            " " + ctx.getString(R.string.all_hours) + ago);
        } else if (period.getMinutes() > 0) {
            builder = new PeriodFormatterBuilder()
                    .appendMinutes()
                    .appendSuffix(" " + ctx.getString(R.string.all_minute) + ago,
                            " " + ctx.getString(R.string.all_minutes) + ago);
        } else if (period.getSeconds() > 0) {
            builder = new PeriodFormatterBuilder()
                    .appendSeconds()
                    .appendSuffix(" " + ctx.getString(R.string.all_second) + ago,
                            " " + ctx.getString(R.string.all_seconds) + ago);
        }
        return builder.toFormatter().print(period);
    }

    public String uptime(long seconds, @NonNull DateTime endDateTime) {
        Preconditions.checkNotNull(endDateTime, "endDateTime == null");
        if (seconds <= 0) {
            return "";
        }
        String sep = ", ";
        PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
                .appendYears()
                .appendSuffix(" " + ctx.getString(R.string.all_year_lc),
                        " " + ctx.getString(R.string.all_years_lc))
                .appendSeparator(sep)
                .appendMonths()
                .appendSuffix(" " + ctx.getString(R.string.all_month_lc),
                        " " + ctx.getString(R.string.all_months_lc))
                .appendSeparator(sep)
                .appendWeeks()
                .appendSuffix(" " + ctx.getString(R.string.all_week_lc),
                        " " + ctx.getString(R.string.all_weeks_lc))
                .appendSeparator(sep)
                .appendDays()
                .appendSuffix(" " + ctx.getString(R.string.all_day),
                        " " + ctx.getString(R.string.all_days))
                .appendSeparator(sep)
                .appendHours()
                .appendSuffix(" " + ctx.getString(R.string.all_hour),
                        " " + ctx.getString(R.string.all_hours))
                .appendSeparator(sep)
                .appendMinutes()
                .appendSuffix(" " + ctx.getString(R.string.all_minute),
                        " " + ctx.getString(R.string.all_minutes))
                .appendSeparator(sep)
                .appendSeconds()
                .appendSuffix(" " + ctx.getString(R.string.all_second),
                        " " + ctx.getString(R.string.all_seconds))
                .printZeroNever()
                .toFormatter();
        Period period = new Period(endDateTime.minusSeconds((int) seconds), endDateTime,
                PeriodType.standard().withMillisRemoved());
        return periodFormatter.print(period);
    }

    public String weekDays(boolean[] weekdays) {
        StringBuilder sb = new StringBuilder();
        LocalDate localDate = new LocalDate();
        for (int i = 0; i < weekdays.length; i++) {
            if (weekdays[i]) {
                localDate = localDate.withDayOfWeek(i + 1);
                sb.append(localDate.dayOfWeek().getAsShortText(Locale.ENGLISH)).append(", ");
            }
        }
        // Delete last comma
        int len = sb.length();
        if (len >= 2) {
            sb.deleteCharAt(len - 1);
            sb.deleteCharAt(len - 2);
        }
        return sb.toString();
    }

    public String months(boolean[] months) {
        StringBuilder sb = new StringBuilder();
        LocalDate localDate = new LocalDate();
        for (int i = 0; i < months.length; i++) {
            if (months[i]) {
                localDate = localDate.withMonthOfYear(i + 1);
                sb.append(localDate.monthOfYear().getAsShortText(Locale.ENGLISH)).append(", ");
            }
        }
        // Delete last comma
        int len = sb.length();
        if (len >= 2) {
            sb.deleteCharAt(len - 1);
            sb.deleteCharAt(len - 2);
        }
        return sb.toString();
    }
}
