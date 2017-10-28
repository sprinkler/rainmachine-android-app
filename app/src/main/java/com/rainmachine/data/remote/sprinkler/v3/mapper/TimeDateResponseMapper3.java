package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.response.TimeDateResponse3;
import com.rainmachine.domain.model.TimeDate3;
import com.rainmachine.domain.util.Strings;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;

public class TimeDateResponseMapper3 implements Function<TimeDateResponse3, TimeDate3> {

    private static final String FORMAT_INBOUND_DATE_TIME_24 = "yyyy/M/d H:m";

    private static volatile TimeDateResponseMapper3 instance;

    public static TimeDateResponseMapper3 instance() {
        if (instance == null) {
            instance = new TimeDateResponseMapper3();
        }
        return instance;
    }

    @Override
    public TimeDate3 apply(@NonNull TimeDateResponse3 timeDateResponse3) throws Exception {
        TimeDate3 timeDate3 = new TimeDate3();
        timeDate3.sprinklerLocalDateTime = convertAppDate(timeDateResponse3.settings.appDate,
                timeDateResponse3.settings.timeFormat);
        timeDate3.use24HourFormat = timeDateResponse3.settings.timeFormat == 24;
        return timeDate3;
    }

    private LocalDateTime convertAppDate(String appDate, int timeFormat) {
        boolean use24HourFormat = (timeFormat == 24);
        if (!Strings.isBlank(appDate) && (appDate.endsWith(" am") || appDate.endsWith(" pm") ||
                appDate.endsWith(" AM") || appDate.endsWith(" PM"))) {
            use24HourFormat = false;
            Timber.d("We set it as am/pm in order to avoid crash");
        }
        return getValidDateTime(appDate, use24HourFormat);
    }

    // date&time {"appDate":"2014/1/5 6:6","am_pm":0,"time_format":24}
    // date&time {"appDate":"2014/1/5 6:6 am","am_pm":1,"time_format":12}
    // programs { "time_format":1,"startTime":"2014/1/14 0:15 pm" ...}
    // programs {"time_format":0,"startTime":"2014/1/14 12:15" ...}
    private LocalDateTime getValidDateTime(String time, boolean use24HourFormat) {
        /*
        Because the returned string uses 0 instead of 12 for AM or PM, we have to manually parse the
        string
         */
        if (!Strings.isBlank(time)) {
            if (use24HourFormat) {
                DateTimeFormatter formatterDate = DateTimeFormat.forPattern
                        (FORMAT_INBOUND_DATE_TIME_24);
                return formatterDate.parseLocalDateTime(time);
            } else {
                return parseAmPmTime(time);
            }
        }
        return new LocalDateTime();
    }

    /*
  A lof of hacking to make it work most of the time. Crazy sprinkler API!
   */
    private LocalDateTime parseAmPmTime(String time) {
        String goodTime;
        String[] split = time.split(" "); // yyyy/M/d h:m a
        if (split.length == 3) {
            String[] splitTime = split[1].split(":");
            if (splitTime.length == 2) {
                if ("am".equalsIgnoreCase(split[2])) {
                    goodTime = split[0] + " " + split[1];
                    DateTimeFormatter formatterDate = DateTimeFormat.forPattern
                            (FORMAT_INBOUND_DATE_TIME_24);
                    return formatterDate.parseLocalDateTime(goodTime);
                } else if ("pm".equalsIgnoreCase(split[2])) {
                    try {
                        if (splitTime[0].equals("12")) {
                            splitTime[0] = "0";
                        }
                        int hour = 12 + Integer.parseInt(splitTime[0]);
                        goodTime = split[0] + " " + hour + ":" + splitTime[1];
                        DateTimeFormatter formatterDate = DateTimeFormat.forPattern
                                (FORMAT_INBOUND_DATE_TIME_24);
                        return formatterDate.parseLocalDateTime(goodTime);
                    } catch (NumberFormatException nfe) {
                        Timber.w(nfe, nfe.getMessage());
                    }
                }
            }
        }
        return new LocalDateTime();
    }
}
