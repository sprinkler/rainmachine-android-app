package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.response.DayResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.WeatherDataResponse3;
import com.rainmachine.domain.model.DayStats;
import com.rainmachine.domain.util.Strings;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;


public class WeatherDataResponseMapper3 implements Function<WeatherDataResponse3,
        List<DayStats>> {

    private static volatile WeatherDataResponseMapper3 instance;

    public static WeatherDataResponseMapper3 instance() {
        if (instance == null) {
            instance = new WeatherDataResponseMapper3();
        }
        return instance;
    }

    @Override
    public List<DayStats> apply(@NonNull WeatherDataResponse3 weatherDataResponse3) throws
            Exception {
        List<DayStats> stats = new ArrayList<>();
        if (weatherDataResponse3.homeScreen != null) {
            for (DayResponse3 day3 : weatherDataResponse3.homeScreen) {
                DayStats day = new DayStats();
                day.id = day3.id;
                day.date = null; // only available in v4
                day.maxTemp = day3.maxt;
                day.minTemp = day3.mint;
                day.percentage = day3.percentage;
                day.lastUpdate = day3.lastUpdate;
                day.wateringFlag = day3.waterFlag;
                day.iconName = Strings.valueOrDefault(day3.icon, "na");
                stats.add(day);
            }
        }
        return stats;
    }
}
