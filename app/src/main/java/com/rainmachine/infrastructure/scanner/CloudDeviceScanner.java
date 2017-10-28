package com.rainmachine.infrastructure.scanner;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.boundary.data.CloudRepository;
import com.rainmachine.domain.model.CloudDevice;
import com.rainmachine.domain.model.CloudEntry;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.Sleeper;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import kotlin.Pair;
import timber.log.Timber;

public class CloudDeviceScanner {

    private static final int TIMEOUT_SCAN_CLOUD_MILLIS = 20 * DateTimeConstants.MILLIS_PER_SECOND;

    private final DatabaseRepositoryImpl databaseRepository;
    private final CloudRepository cloudRepository;

    private CloudScanThread cloudScanThread;

    public CloudDeviceScanner(DatabaseRepositoryImpl databaseRepository,
                              CloudRepository cloudRepository) {
        this.databaseRepository = databaseRepository;
        this.cloudRepository = cloudRepository;
    }

    public void start() {
        Timber.d("Start cloud scanner...");
        stop();
        cloudScanThread = new CloudScanThread();
        cloudScanThread.start();
    }

    public void stop() {
        if (cloudScanThread != null) {
            cloudScanThread.requestStop();
            cloudScanThread = null;
        }
    }

    public class CloudScanThread extends Thread {

        private boolean requestedToStop;

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted() && !requestedToStop) {
                    List<CloudInfo> cloudInfoList = databaseRepository.getCloudInfoList();
                    if (cloudInfoList.size() > 0) {
                        try {
                            List<CloudEntry> entries = Observable.fromIterable(cloudInfoList)
                                    .map(cloudInfo -> new Pair<>(cloudInfo.email,
                                            cloudInfo.password))
                                    .toList()
                                    .flatMap(emailPasswordPairs -> cloudRepository.cloudEntries
                                            (emailPasswordPairs))
                                    .blockingGet();
                            if (entries != null && entries.size() > 0) {
                                for (CloudEntry entry : entries) {
                                    for (CloudDevice cloudDevice : entry.devices) {
                                        Device device = new Device();
                                        device.deviceId = cloudDevice.mac != null ?
                                                cloudDevice.mac.toLowerCase(Locale
                                                        .getDefault()) : "";
                                        device.name = cloudDevice.name;
                                        if (!Strings.isBlank(cloudDevice.sprinklerUrl) &&
                                                !cloudDevice.sprinklerUrl.startsWith("https://")) {
                                            device.setUrl("https://" + cloudDevice.sprinklerUrl);
                                        } else {
                                            device.setUrl(cloudDevice.sprinklerUrl);
                                        }
                                        device.type = Device.SPRINKLER_TYPE_CLOUD;
                                        device.timestamp = new DateTime().getMillis();
                                        // If it's a cloud device, wizard has already run
                                        device.wizardHasRun = true;
                                        device.cloudEmail = entry.email;
                                        databaseRepository.saveDevice(device);
                                    }
                                    databaseRepository.updateCloudInfo(entry.email, entry
                                            .activeCount, entry.knownCount, entry.authCount);
                                }
                            }
                        } catch (Throwable t) {
                            Timber.i("Caught you little bugger %s", t.getMessage());
                            // Blocking Observable may throw when interrupting this thread
                        }
                    }
                    Sleeper.sleepThrow(TIMEOUT_SCAN_CLOUD_MILLIS);
                }
            } catch (InterruptedException ie) {
                Timber.d("The cloud scan thread received interrupt request");
            }
            Timber.d("Finish the cloud scan thread...");
        }

        public void requestStop() {
            requestedToStop = true;
            interrupt();
        }
    }
}
