package com.rainmachine.infrastructure;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.rainmachine.domain.model.LocationInfo;
import com.rainmachine.infrastructure.bus.BaseEvent;
import com.squareup.otto.Bus;

import org.joda.time.DateTimeConstants;

import timber.log.Timber;

public class LocationHandler implements GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    private Context context;
    private Bus bus;
    private GoogleApiClient googleApiClient;

    public LocationHandler(Context context, Bus bus) {
        this.context = context;
        this.bus = bus;
        googleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Timber.d("Location client connected");
        requestLocationUpdates(null);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.d("Location client connection suspended");
        MAIN_THREAD.post(runLocationNotFound);
    }

    @Override
    public void onLocationChanged(Location location) {
        Timber.d("Location changed!");
        stopLocationUpdates(null);
        LocationInfo localLocation = new LocationInfo();
        localLocation.latitude = location.getLatitude();
        localLocation.longitude = location.getLongitude();
        localLocation.isCompleteInfo = false;
        bus.post(new MapLocationEvent(localLocation));
    }

    public void requestLocationUpdates(GoogleApiClient.OnConnectionFailedListener
                                               connectionFailedListener) {
        if (googleApiClient.isConnected()) {
            int expirationDuration = 12 * DateTimeConstants.MILLIS_PER_SECOND;
            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setInterval(2 * DateTimeConstants.MILLIS_PER_SECOND);
            request.setFastestInterval(DateTimeConstants.MILLIS_PER_SECOND);
            request.setExpirationDuration(expirationDuration);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request,
                    this);
            MAIN_THREAD.postDelayed(runLocationNotFound, expirationDuration);
        } else {
            Timber.w("Location client is not connected");
            googleApiClient.registerConnectionFailedListener(connectionFailedListener);
            googleApiClient.connect();
        }
    }

    public void stopLocationUpdates(GoogleApiClient.OnConnectionFailedListener
                                            connectionFailedListener) {
        if (connectionFailedListener != null && googleApiClient
                .isConnectionFailedListenerRegistered(connectionFailedListener)) {
            googleApiClient.unregisterConnectionFailedListener(connectionFailedListener);
        }
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }

        MAIN_THREAD.removeCallbacks(runLocationNotFound);
    }

    public boolean isLocationServicesEnabled() {
        LocationManager manager = (LocationManager) context.getSystemService(Context
                .LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private Runnable runLocationNotFound = new Runnable() {
        @Override
        public void run() {
            stopLocationUpdates(null);
            bus.post(new NoMapLocationFound());
        }
    };

    public static class NoMapLocationFound {
    }

    public static class MapLocationEvent extends BaseEvent {
        public LocationInfo data;

        public MapLocationEvent(LocationInfo location) {
            this.data = location;
        }
    }
}
