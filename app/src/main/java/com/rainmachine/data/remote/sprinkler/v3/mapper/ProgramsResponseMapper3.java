package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.response.ProgramResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.ProgramsResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.WateringTimesResponse3;
import com.rainmachine.data.remote.util.RemoteUtils;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramFrequency;
import com.rainmachine.domain.model.ProgramStartTime;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.domain.util.Strings;

import org.javatuples.Pair;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;

public class ProgramsResponseMapper3 implements Function<ProgramsResponse3, Pair<List<Program>,
        Boolean>> {

    private static final String FORMAT_INBOUND_DATE_TIME_24 = "yyyy/M/d H:m";

    private static volatile ProgramsResponseMapper3 instance;

    public static ProgramsResponseMapper3 instance() {
        if (instance == null) {
            instance = new ProgramsResponseMapper3();
        }
        return instance;
    }

    @Override
    public Pair<List<Program>, Boolean> apply(@NonNull ProgramsResponse3 programs3)
            throws Exception {
        boolean use24HourFormat = false;
        for (Iterator<ProgramResponse3> it = programs3.programs.iterator(); it.hasNext(); ) {
            ProgramResponse3 program = it.next();
            // There is a dummy entry which is always present
            if (program.id == -1) {
                use24HourFormat = program.timeFormat == 0;
                it.remove();
                break;
            }
        }

        List<Program> programs = new ArrayList<>(programs3.programs.size());
        for (ProgramResponse3 programResponse3 : programs3.programs) {
            Program program = new Program();
            program.id = programResponse3.id;
            program.name = programResponse3.name;
            program.delaySeconds = programResponse3.delay * DateTimeConstants.SECONDS_PER_MINUTE;
            program.isDelayEnabled = RemoteUtils.toBoolean(programResponse3.delayOn);
            program.isCycleSoakEnabled = RemoteUtils.toBoolean(programResponse3.csOn);
            program.numCycles = programResponse3.cycles;
            program.soakSeconds = programResponse3.soak * DateTimeConstants.SECONDS_PER_MINUTE;
            program.wateringState = "running".equals(programResponse3.state) ? Program
                    .WateringState.RUNNING : Program.WateringState.IDLE;
            if (programResponse3.isDaily()) {
                program.frequency = new ProgramFrequency(ProgramFrequency.Type.DAILY);
            } else if (programResponse3.isOddDays()) {
                program.frequency = new ProgramFrequency(ProgramFrequency.Type.ODD_DAYS);
            } else if (programResponse3.isEvenDays()) {
                program.frequency = new ProgramFrequency(ProgramFrequency.Type.EVEN_DAYS);
            } else if (programResponse3.isEveryNDays()) {
                program.frequency = new ProgramFrequency(ProgramFrequency.Type
                        .EVERY_N_DAYS, programResponse3.frequencyNumDays());
            } else if (programResponse3.isWeekDays()) {
                program.frequency = new ProgramFrequency(ProgramFrequency.Type.WEEK_DAYS,
                        convertWeekDays(programResponse3.weekdays));
            }

            program.startTime = new ProgramStartTime(getValidDateTime(programResponse3
                    .startTime, (int) programResponse3.timeFormat));
            program.enabled = RemoteUtils.toBoolean(programResponse3.active);
            program.wateringTimes = new ArrayList<>();
            // The returned zones already exclude any master valve zone
            for (WateringTimesResponse3 wtr3 : programResponse3.wateringTimes) {
                ProgramWateringTimes lwt = new ProgramWateringTimes();
                lwt.id = wtr3.id;
                lwt.name = wtr3.name;
                lwt.duration = wtr3.minutes * DateTimeConstants.SECONDS_PER_MINUTE;
                lwt.active = wtr3.minutes > 0;
                program.wateringTimes.add(lwt);
            }
            program.ignoreWeatherData = RemoteUtils.toBoolean
                    (programResponse3.ignoreWeatherData);
            programs.add(program);
        }
        return Pair.with(programs, use24HourFormat);
    }

    private boolean[] convertWeekDays(String weekDays) {
        boolean[] items = new boolean[DateTimeConstants.DAYS_PER_WEEK];
        if (!Strings.isBlank(weekDays)) {
            String[] days = weekDays.split(",");
            if (days.length == DateTimeConstants.DAYS_PER_WEEK) {
                for (int i = 0; i < days.length; i++) {
                    try {
                        int val = Integer.parseInt(days[i]);
                        items[i] = val != 0;
                    } catch (NumberFormatException nfe) {
                        Timber.w(nfe, nfe.getMessage());
                    }
                }
            }
        }
        return items;
    }

    private LocalDateTime getValidDateTime(String startTime, int timeFormat) {
        boolean use24HourFormat = (timeFormat == 0);
        return getValidDateTime(startTime, use24HourFormat);
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
