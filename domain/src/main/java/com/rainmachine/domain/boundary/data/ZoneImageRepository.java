package com.rainmachine.domain.boundary.data;

import com.rainmachine.domain.model.LatLong;
import com.rainmachine.domain.model.ZoneImage;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface ZoneImageRepository {

    Single<List<ZoneImage>> getRemoteZoneImages(String mac, LatLong coordinates);

    Single<ZoneImage> getLocalZoneImage(String mac, long zoneId);

    Single<String> uploadZoneImage(String mac, long zoneId, LatLong coordinates,
                                   final byte[] bytes);

    Completable deleteZoneImage(String mac, long zoneId, LatLong coordinates);

    Completable updateLocalZoneImage(String deviceId, ZoneImage zoneImage);
}
