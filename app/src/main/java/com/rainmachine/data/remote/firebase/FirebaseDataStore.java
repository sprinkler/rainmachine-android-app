package com.rainmachine.data.remote.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rainmachine.BuildConfig;
import com.rainmachine.domain.model.LatLong;
import com.rainmachine.domain.model.ZoneImage;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.Strings;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import durdinapps.rxfirebase2.RxFirebaseAuth;
import durdinapps.rxfirebase2.RxFirebaseStorage;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class FirebaseDataStore {

    private static final String EMAIL = BuildConfig.FIREBASE_EMAIL;
    private static final String PASSWORD = BuildConfig.FIREBASE_PASSWORD;

    private FirebaseAuth auth;
    private StorageReference storageRef;

    public FirebaseDataStore() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        storageRef = storage.getReferenceFromUrl(BuildConfig.FIREBASE_URL);
    }

    public Single<String> uploadZoneImage(String mac, long zoneId, LatLong coordinates,
                                          final byte[] bytes) {
        return uploadZoneImage(mac, zoneFileName(zoneId, coordinates), bytes);
    }

    public Single<String> uploadZoneImageOld(String mac, long zoneId, final byte[] bytes) {
        return uploadZoneImage(mac, "zone" + zoneId + ".jpg", bytes);
    }

    private Single<String> uploadZoneImage(String mac, String fileName, final byte[] bytes) {
        if (Strings.isBlank(mac)) {
            return Single.error(new Throwable("MAC is empty for some unknown reason"));
        }
        final StorageReference imageRef = storageRef.child("devices/" + mac.toLowerCase(Locale
                .ENGLISH) + "/images/" + fileName);
        return RxFirebaseAuth
                .signInWithEmailAndPassword(auth, EMAIL, PASSWORD)
                .flatMapSingle(ignored -> RxFirebaseStorage.putBytes(imageRef, bytes)
                        .toSingle())
                .flatMap(taskSnapshot -> Single.just(taskSnapshot.getDownloadUrl().toString()));
    }

    public Single<List<ZoneImage>> getZoneImages(String mac, LatLong coordinates) {
        return getZoneImages(mac, zoneId -> zoneFileName(zoneId, coordinates));
    }

    public Single<List<ZoneImage>> getZoneImagesOld(String mac) {
        return getZoneImages(mac, zoneId -> "zone" + zoneId + ".jpg");
    }

    private Single<List<ZoneImage>> getZoneImages(String mac,
                                                  Function<Integer, String> function) {
        if (Strings.isBlank(mac)) {
            return Single.error(new Throwable("MAC is empty for some unknown reason"));
        }
        final StorageReference parentRef = storageRef.child("devices/" + mac.toLowerCase(Locale
                .ENGLISH) + "/images/");
        return RxFirebaseAuth.signInWithEmailAndPassword(auth, EMAIL, PASSWORD)
                .flatMapObservable(ignored -> Observable.range(1, 16))
                .flatMapMaybe(zoneId -> {
                    final StorageReference imageRef = parentRef.child(function.apply(zoneId));
                    return RxFirebaseStorage.getMetadata(imageRef)
                            .map(storageMetadata -> new ZoneImage(zoneId,
                                    storageMetadata.getDownloadUrl().toString()))
                            .onErrorResumeNext(Maybe.empty());
                })
                .toList();
    }

    public Single<Irrelevant> deleteZoneImage(String mac, long zoneId, LatLong coordinates) {
        return deleteZoneImage(mac, zoneFileName(zoneId, coordinates));
    }

    public Single<Irrelevant> deleteZoneImageOld(String mac, long zoneId) {
        return deleteZoneImage(mac, "zone" + zoneId + ".jpg");
    }

    private Single<Irrelevant> deleteZoneImage(String mac, String fileName) {
        if (Strings.isBlank(mac)) {
            return Single.error(new Throwable("MAC is empty for some unknown reason"));
        }
        final StorageReference imageRef = storageRef.child("devices/" + mac.toLowerCase(Locale
                .ENGLISH) + "/images/" + fileName);
        return RxFirebaseAuth
                .signInWithEmailAndPassword(auth, EMAIL, PASSWORD)
                .flatMapCompletable(ignored -> RxFirebaseStorage.delete(imageRef))
                .andThen(Single.just(Irrelevant.INSTANCE));
    }

    static String zoneFileName(long zoneId, LatLong coordinates) {
        DecimalFormat decimalFormat = new DecimalFormat("#.000");
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        String sLatitude = decimalFormat.format(coordinates.getLatitude());
        String sLongitude = decimalFormat.format(coordinates.getLongitude());
        return String.format(Locale.getDefault(), "zone%d_%s_%s.jpg", zoneId, sLatitude,
                sLongitude);
    }
}
