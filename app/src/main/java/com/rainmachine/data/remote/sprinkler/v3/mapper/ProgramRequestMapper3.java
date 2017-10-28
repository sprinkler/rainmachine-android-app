package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.request.ProgramRequest3;
import com.rainmachine.data.remote.sprinkler.v3.response.WateringTimesResponse3;
import com.rainmachine.data.remote.util.RemoteUtils;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramWateringTimes;

import org.javatuples.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class ProgramRequestMapper3 implements Function<Pair<Program, Boolean>, ProgramRequest3> {

    private static volatile ProgramRequestMapper3 instance;

    private ProgramRequestMapper3() {
    }

    public static ProgramRequestMapper3 instance() {
        if (instance == null) {
            instance = new ProgramRequestMapper3();
        }
        return instance;
    }

    @Override
    public ProgramRequest3 apply(@NonNull Pair<Program, Boolean> programBooleanPair) throws
            Exception {
        Program program = programBooleanPair.getValue0();
        boolean use24HourFormat = programBooleanPair.getValue1();
        ProgramRequest3 request = new ProgramRequest3();
        request.id = program.id;
        request.timeFormat = use24HourFormat ? 0 : 1;
        request.delay = program.delaySeconds / DateTimeConstants.SECONDS_PER_MINUTE;
        request.delayOn = RemoteUtils.toInt(program.isDelayEnabled);
        request.csOn = RemoteUtils.toInt(program.isCycleSoakEnabled);
        request.cycles = program.numCycles;
        request.soak = program.soakSeconds / DateTimeConstants.SECONDS_PER_MINUTE;
        request.state = program.wateringState == Program.WateringState.IDLE ? "stopped" : "running";
        if (program.isDaily()) {
            request.frequency = 0;
            request.weekdays = "D";
        } else if (program.isOddDays()) {
            request.frequency = 4;
            request.weekdays = "ODD";
        } else if (program.isEvenDays()) {
            request.frequency = 5;
            request.weekdays = "EVD";
        } else if (program.isEveryNDays()) {
            int numDays = program.frequencyNumDays();
            request.frequency = 4 + numDays;
            request.weekdays = "INT " + numDays;
        } else if (program.isWeekDays()) {
            request.frequency = 2;
            request.weekdays = convertWeekDays(program.frequencyWeekDays());
        }
        request.name = program.name;
        request.ignoreWeatherData = RemoteUtils.toInt(program.ignoreWeatherData);

        // We use the timezone the device is currently on but this is wrong if the RainMachine
        // has different timezone
        DateTime date = program.startTime.localDateTime.toDateTime();
        request.startTime = date.getMillis() / DateTimeConstants.MILLIS_PER_SECOND;
        request.programStartTime = date.toString("H:m");

        request.active = RemoteUtils.toInt(program.enabled);
        request.wateringTimes = convertWateringTimes(program.wateringTimes);
        return request;
    }

    public String convertWeekDays(boolean[] weekDays) {
        StringBuilder sb = new StringBuilder();
        for (boolean weekday : weekDays) {
            sb.append(weekday ? 1 : 0).append(",");
        }
        // delete last comma
        int len = sb.length();
        if (len >= 1) {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    }

    private List<WateringTimesResponse3> convertWateringTimes(List<ProgramWateringTimes> lwts) {
        List<WateringTimesResponse3> wtrs = new ArrayList<>();
        for (ProgramWateringTimes lwt : lwts) {
            WateringTimesResponse3 wtr3 = new WateringTimesResponse3();
            wtr3.id = lwt.id;
            wtr3.name = lwt.name;
            wtr3.minutes = lwt.duration / DateTimeConstants.SECONDS_PER_MINUTE;
            wtrs.add(wtr3);
        }
        return wtrs;
    }
}