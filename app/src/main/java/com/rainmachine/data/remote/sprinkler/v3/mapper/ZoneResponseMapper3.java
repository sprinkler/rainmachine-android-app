package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.response.ZoneResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.ZoneSubResponse3;
import com.rainmachine.domain.model.Zone;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class ZoneResponseMapper3 implements Function<ZoneResponse3, Zone> {

    private static volatile ZoneResponseMapper3 instance;

    public static ZoneResponseMapper3 instance() {
        if (instance == null) {
            instance = new ZoneResponseMapper3();
        }
        return instance;
    }

    @Override
    public Zone apply(@NonNull ZoneResponse3 response3) throws Exception {
        return convertZone(response3.zone);
    }

    static Zone convertZone(ZoneSubResponse3 zoneSubResponse3) {
        Zone zone = new Zone();
        zone.id = zoneSubResponse3.id;
        zone.name = zoneSubResponse3.name;
        if (ZoneSubResponse3.STATE_WATERING.equalsIgnoreCase(zoneSubResponse3.state)) {
            zone.setWateringState();
        } else if (ZoneSubResponse3.STATE_PENDING.equalsIgnoreCase(zoneSubResponse3.state)) {
            zone.setPendingState();
        } else {
            zone.setIdleState();
        }
        zone.counter = zoneSubResponse3.counter;
        return zone;
    }
}
