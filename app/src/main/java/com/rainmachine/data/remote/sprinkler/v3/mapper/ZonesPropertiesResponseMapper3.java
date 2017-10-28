package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.response.ZonePropertiesResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.ZonesPropertiesResponse3;
import com.rainmachine.data.remote.util.RemoteUtils;
import com.rainmachine.domain.model.ZoneProperties;

import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;


public class ZonesPropertiesResponseMapper3 implements Function<ZonesPropertiesResponse3,
        List<ZoneProperties>> {

    private static volatile ZonesPropertiesResponseMapper3 instance;

    public static ZonesPropertiesResponseMapper3 instance() {
        if (instance == null) {
            instance = new ZonesPropertiesResponseMapper3();
        }
        return instance;
    }

    @Override
    public List<ZoneProperties> apply(@NonNull ZonesPropertiesResponse3 zonesPropertiesResponse)
            throws Exception {
        List<ZoneProperties> zonesProperties = new ArrayList<>();
        for (ZonePropertiesResponse3 prop3 : zonesPropertiesResponse.zones) {
            ZoneProperties prop = new ZoneProperties();
            prop.id = prop3.id;
            prop.masterValve = RemoteUtils.toBoolean(prop3.masterValve);
            prop.beforeInSeconds = prop3.before * DateTimeConstants.SECONDS_PER_MINUTE;
            if (prop.beforeInSeconds < 0) {
                prop.beforeInSeconds = 0;
            }
            prop.afterInSeconds = prop3.after * DateTimeConstants.SECONDS_PER_MINUTE;
            if (prop.afterInSeconds < 0) {
                prop.afterInSeconds = 0;
            }
            prop.enabled = RemoteUtils.toBoolean(prop3.active);
            prop.name = prop3.name;
            prop.vegetationType = vegetationType(prop3.vegetation);
            prop.forecastData = RemoteUtils.toBoolean(prop3.forecastData);
            prop.historicalAverage = RemoteUtils.toBoolean(prop3.historicalAverage);
            prop.soilType = ZoneProperties.SoilType.NOT_SET;
            prop.sprinklerHeads = ZoneProperties.SprinklerHeads.NOT_SET;
            prop.slope = ZoneProperties.Slope.NOT_SET;
            prop.exposure = ZoneProperties.Exposure.NOT_SET;
            zonesProperties.add(prop);
        }
        return zonesProperties;
    }

    private ZoneProperties.VegetationType vegetationType(int vegetationType) {
        switch (vegetationType) {
            case 2:
                return ZoneProperties.VegetationType.LAWN;
            case 3:
                return ZoneProperties.VegetationType.FRUIT_TREES;
            case 4:
                return ZoneProperties.VegetationType.FLOWERS;
            case 5:
                return ZoneProperties.VegetationType.VEGETABLES;
            case 6:
                return ZoneProperties.VegetationType.CITRUS;
            case 7:
                return ZoneProperties.VegetationType.TREES_BUSHES;
            default:
                return ZoneProperties.VegetationType.NOT_SET;
        }
    }
}
