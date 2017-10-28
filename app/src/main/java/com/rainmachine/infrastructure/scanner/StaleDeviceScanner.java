package com.rainmachine.infrastructure.scanner;

import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.infrastructure.Sleeper;
import com.rainmachine.injection.Injector;

import org.joda.time.DateTimeConstants;

import javax.inject.Inject;
import javax.inject.Named;

import timber.log.Timber;

public class StaleDeviceScanner {

    private static final int TIMEOUT_UPDATE_SPRINKLER_DATA_MILLIS = 5 * DateTimeConstants
            .MILLIS_PER_SECOND;

    private DeviceRepository deviceRepository;

    @Inject
    @Named("device_cache_timeout")
    int deviceCacheTimeout;

    private CleanStaleDataThread cleanStaleDataThread;
    private int initialDelaySeconds;

    public StaleDeviceScanner(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
        Injector.inject(this);
    }

    public void start(int initialDelaySeconds) {
        this.initialDelaySeconds = initialDelaySeconds;
        stop();
        cleanStaleDataThread = new CleanStaleDataThread();
        cleanStaleDataThread.start();
    }

    public void stop() {
        if (cleanStaleDataThread != null) {
            cleanStaleDataThread.requestStop();
            cleanStaleDataThread = null;
        }
    }

    private class CleanStaleDataThread extends Thread {

        private boolean requestedToStop;

        @Override
        public void run() {
            try {
                // Sleep a bit so that the already listed sprinklers get a chance to respond so
                // we don't remove them prematurely
                if (initialDelaySeconds > 0) {
                    Sleeper.sleepThrow(initialDelaySeconds * DateTimeConstants.MILLIS_PER_SECOND);
                }
                while (!Thread.currentThread().isInterrupted() && !requestedToStop) {
                    deviceRepository.deleteStaleLocalDiscoveredDevices(deviceCacheTimeout);
                    deviceRepository.markStaleCloudDevicesAsOffline(deviceCacheTimeout);
                    Sleeper.sleepThrow(TIMEOUT_UPDATE_SPRINKLER_DATA_MILLIS);
                }
            } catch (InterruptedException ie) {
                Timber.d("The clean stale data thread received interrupt request");
            }
            Timber.d("Finish the clean stale data...");
        }

        void requestStop() {
            requestedToStop = true;
            interrupt();
        }
    }
}
