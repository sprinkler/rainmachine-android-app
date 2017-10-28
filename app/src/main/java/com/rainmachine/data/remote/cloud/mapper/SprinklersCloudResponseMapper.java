package com.rainmachine.data.remote.cloud.mapper;

import com.rainmachine.data.remote.cloud.response.CloudResponse;
import com.rainmachine.data.remote.cloud.response.CloudSprinklerResponse;
import com.rainmachine.data.remote.cloud.response.CloudSprinklersResponse;
import com.rainmachine.domain.model.CloudDevice;
import com.rainmachine.domain.model.CloudEntry;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;


public class SprinklersCloudResponseMapper implements Function<CloudResponse, List<CloudEntry>> {

    private static volatile SprinklersCloudResponseMapper instance;

    public static SprinklersCloudResponseMapper instance() {
        if (instance == null) {
            instance = new SprinklersCloudResponseMapper();
        }
        return instance;
    }

    @Override
    public List<CloudEntry> apply(@NonNull CloudResponse response) throws Exception {
        List<CloudEntry> entries = new ArrayList<>();
        if (response.sprinklersByEmail != null && response.sprinklersByEmail.size() > 0) {
            for (CloudSprinklersResponse sprinklersResponse : response.sprinklersByEmail) {
                CloudEntry entry = new CloudEntry();
                entry.email = sprinklersResponse.email;
                entry.activeCount = sprinklersResponse.activeCount;
                entry.authCount = sprinklersResponse.authCount;
                entry.knownCount = sprinklersResponse.knownCount;
                entry.devices = new ArrayList<>(sprinklersResponse.sprinklers.size());
                for (CloudSprinklerResponse sprinklerResponse : sprinklersResponse.sprinklers) {
                    CloudDevice device = new CloudDevice();
                    device.sprinklerId = sprinklerResponse.sprinklerId;
                    device.sprinklerUrl = sprinklerResponse.sprinklerUrl;
                    device.mac = sprinklerResponse.mac;
                    device.name = sprinklerResponse.name;
                    entry.devices.add(device);
                }
                entries.add(entry);
            }
        }
        return entries;
    }
}
