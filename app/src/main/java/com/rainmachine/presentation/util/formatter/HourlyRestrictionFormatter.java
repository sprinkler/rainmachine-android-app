package com.rainmachine.presentation.util.formatter;


import com.rainmachine.domain.model.HourlyRestriction;

import org.joda.time.LocalTime;

public class HourlyRestrictionFormatter {

    public String interval(HourlyRestriction restriction, boolean use24HourFormat) {
        LocalTime ltStart = restriction.fromLocalTime();
        LocalTime ltEnd = restriction.toLocalTime();
        return CalendarFormatter.hourMinColon(ltStart, use24HourFormat) + " - " +
                CalendarFormatter.hourMinColon(ltEnd, use24HourFormat);
    }
}
