package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.response.ZoneSubResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.ZonesResponse3;
import com.rainmachine.domain.model.Zone;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class ZonesResponseMapper3 implements Function<ZonesResponse3, List<Zone>> {

    private static volatile ZonesResponseMapper3 instance;

    public static ZonesResponseMapper3 instance() {
        if (instance == null) {
            instance = new ZonesResponseMapper3();
        }
        return instance;
    }

    @Override
    public List<Zone> apply(@NonNull ZonesResponse3 response3) throws Exception {
        List<Zone> list = new ArrayList<>();
        for (ZoneSubResponse3 zone3 : response3.zones) {
            Zone localZone = ZoneResponseMapper3.convertZone(zone3);
            list.add(localZone);
        }
        return list;
    }
}
