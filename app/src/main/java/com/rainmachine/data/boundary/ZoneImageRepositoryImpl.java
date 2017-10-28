package com.rainmachine.data.boundary;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.ZoneSettings;
import com.rainmachine.data.remote.firebase.FirebaseDataStore;
import com.rainmachine.domain.boundary.data.ZoneImageRepository;
import com.rainmachine.domain.model.LatLong;
import com.rainmachine.domain.model.ZoneImage;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.Strings;

import java.io.File;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public class ZoneImageRepositoryImpl implements ZoneImageRepository {

    private FirebaseDataStore firebaseDataStore;
    private DatabaseRepositoryImpl databaseRepository;

    public ZoneImageRepositoryImpl(FirebaseDataStore firebaseDataStore,
                                   DatabaseRepositoryImpl databaseRepository) {
        this.firebaseDataStore = firebaseDataStore;
        this.databaseRepository = databaseRepository;
    }

    @Override
    public Single<List<ZoneImage>> getRemoteZoneImages(String mac, LatLong coordinates) {
        // TODO: 15/09/2017 if migration flag not set, 1. fetch old zone images 2. upload them
        // with new format 3. delete the old images 4. set migration flag
        return firebaseDataStore.getZoneImagesOld(mac);
    }

    @Override
    public Single<ZoneImage> getLocalZoneImage(String mac, long zoneId) {
        return getZoneSettings(mac, zoneId)
                .map(zoneSettings -> new ZoneImage(zoneSettings.zoneId, zoneSettings.imageUrl,
                        zoneSettings.imageLocalPath));
    }

    @Override
    public Single<String> uploadZoneImage(String mac, long zoneId, LatLong coordinates,
                                          byte[] bytes) {
        return firebaseDataStore.uploadZoneImageOld(mac, zoneId, bytes);
    }

    @Override
    public Completable deleteZoneImage(String mac, long zoneId, LatLong coordinates) {
        return getZoneSettings(mac, zoneId)
                .flatMap(zoneSettings ->
                        Single.zip(
                                deleteLocalZoneImage(zoneSettings.imageLocalPath),
                                firebaseDataStore.deleteZoneImageOld(mac, zoneId),
                                (irrelevant1, irrelevant2) -> Irrelevant.INSTANCE)
                                .flatMap(irrelevant -> {
                                    zoneSettings.imageLocalPath = null;
                                    zoneSettings.imageUrl = null;
                                    return saveZoneSettings(mac, zoneSettings);
                                }))
                .toCompletable();
    }

    @Override
    public Completable updateLocalZoneImage(String deviceId, ZoneImage zoneImage) {
        return databaseRepository
                .deviceSettings(deviceId)
                .flatMapCompletable(deviceSettings -> {
                    ZoneSettings zoneSettings = deviceSettings.zones.get(zoneImage.zoneId);
                    if (zoneSettings == null) {
                        zoneSettings = new ZoneSettings(zoneImage.zoneId);
                    }
                    zoneSettings.imageUrl = zoneImage.imageUrl;
                    deviceSettings.zones.put(zoneImage.zoneId, zoneSettings);
                    databaseRepository.saveDeviceSettings(deviceSettings);
                    return Completable.complete();
                });
    }

    private Single<ZoneSettings> getZoneSettings(String mac, long zoneId) {
        return databaseRepository
                .deviceSettings(mac)
                .map(deviceSettings -> {
                    ZoneSettings zoneSettings = deviceSettings.zones.get(zoneId);
                    if (zoneSettings == null) {
                        zoneSettings = new ZoneSettings(zoneId);
                    }
                    return zoneSettings;
                });
    }

    private Single<Irrelevant> saveZoneSettings(String mac, ZoneSettings zoneSettings) {
        return databaseRepository
                .deviceSettings(mac)
                .flatMap(deviceSettings -> {
                    deviceSettings.zones.put(zoneSettings.zoneId, zoneSettings);
                    databaseRepository.saveDeviceSettings(deviceSettings);
                    return Single.just(Irrelevant.INSTANCE);
                });
    }

    private Single<Irrelevant> deleteLocalZoneImage(final String localPath) {
        return Single.fromCallable(() -> {
            if (!Strings.isBlank(localPath)) {
                File file = new File(localPath);
                if (file.exists()) {
                    file.delete();
                }
            }
            return Irrelevant.INSTANCE;
        });
    }
}
