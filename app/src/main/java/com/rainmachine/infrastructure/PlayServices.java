package com.rainmachine.infrastructure;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;

public class PlayServices {

    private Context context;

    public PlayServices(Context context) {
        this.context = context;
    }

    public Observable<Response> check() {
        return Observable.create(emitter -> {
            GoogleApiClient apiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            emitter.onNext(Response.available());
                            emitter.onComplete();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            // Do nothing because it automatically starts to connect again and it
                            // will either succeed or fail and the corresponding callbacks will
                            // be called
                        }
                    })
                    .addOnConnectionFailedListener(connectionResult -> {
                        emitter.onNext(Response.unavailable(connectionResult.getErrorCode()));
                        emitter.onComplete();
                    })
                    .build();
            apiClient.connect();

            emitter.setCancellable(() -> apiClient.disconnect());
        });
    }

    public static class Response {
        public boolean isPlayServicesAvailable;
        public int errorCode;

        private Response(boolean isPlayServicesAvailable, int errorCode) {
            this.isPlayServicesAvailable = isPlayServicesAvailable;
            this.errorCode = errorCode;
        }

        static Response available() {
            return new Response(true, 0);
        }

        static Response unavailable(int errorCode) {
            return new Response(false, errorCode);
        }
    }
}
