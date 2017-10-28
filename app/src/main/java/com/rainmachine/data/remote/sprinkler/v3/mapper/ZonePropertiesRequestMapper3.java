package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.request.ZonePropertiesRequest3;
import com.rainmachine.data.remote.util.RemoteUtils;
import com.rainmachine.domain.model.ZoneProperties;

import org.joda.time.DateTimeConstants;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class ZonePropertiesRequestMapper3 implements Function<ZoneProperties,
        ZonePropertiesRequest3> {

    private static volatile ZonePropertiesRequestMapper3 instance;

    private ZonePropertiesRequestMapper3() {
    }

    public static ZonePropertiesRequestMapper3 instance() {
        if (instance == null) {
            instance = new ZonePropertiesRequestMapper3();
        }
        return instance;
    }

    @Override
    public ZonePropertiesRequest3 apply(@NonNull ZoneProperties zoneProps) throws Exception {
        ZonePropertiesRequest3 request = new ZonePropertiesRequest3();
        request.id = zoneProps.id;
        request.masterValve = RemoteUtils.toInt(zoneProps.masterValve);
        request.before = zoneProps.beforeInSeconds / DateTimeConstants.SECONDS_PER_MINUTE;
        request.after = zoneProps.afterInSeconds / DateTimeConstants.SECONDS_PER_MINUTE;
        request.name = zoneProps.name;
        request.active = RemoteUtils.toInt(zoneProps.enabled);
        request.vegetation = "" + vegetationType(zoneProps.vegetationType);
        request.forecastData = RemoteUtils.toInt(zoneProps.forecastData);
        request.historicalAverage = RemoteUtils.toInt(zoneProps.historicalAverage);
        return request;
    }

    private int vegetationType(ZoneProperties.VegetationType vegetationType) {
        switch (vegetationType) {
            case LAWN:
                return 2;
            case FRUIT_TREES:
                return 3;
            case FLOWERS:
                return 4;
            case VEGETABLES:
                return 5;
            case CITRUS:
                return 6;
            case TREES_BUSHES:
                return 7;
            default:
                return 0;
        }
    }
}