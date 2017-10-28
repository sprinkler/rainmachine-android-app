package com.rainmachine.presentation.screens.stats.dashboard;

import org.joda.time.LocalDate;

/**
 * Created by Tremend Software on 4/2/2015.
 */
public class ChartUtils {

    public static boolean isCurrentDate(LocalDate date) {
        LocalDate currDate = new LocalDate();

        return date.getYear() == currDate.getYear() && date.getMonthOfYear() == currDate
                .getMonthOfYear() && date.getDayOfMonth() == currDate.getDayOfMonth();
    }
}