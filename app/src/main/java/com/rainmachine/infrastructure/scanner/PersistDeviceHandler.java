package com.rainmachine.infrastructure.scanner;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import timber.log.Timber;

public class PersistDeviceHandler {

    private DatabaseRepositoryImpl databaseRepository;

    private PersistThread persistThread;

    public PersistDeviceHandler(DatabaseRepositoryImpl databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public void start() {
        if (persistThread == null || !persistThread.isAlive()) {
            persistThread = new PersistThread();
            persistThread.start();
        }
    }

    public void stop() {
        if (persistThread != null) {
            persistThread.requestStop();
            persistThread = null;
        }
    }

    public void addToQueue(Device device) {
        if (persistThread == null) {
            start();
        }
        persistThread.addToQueue(device);
    }

    public class PersistThread extends Thread {

        private boolean requestedToStop;
        private BlockingQueue<Device> blockingQueue = new LinkedBlockingQueue<>();

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted() && !requestedToStop) {
                    Device device = blockingQueue.take();
                    databaseRepository.saveDevice(device);
                }
            } catch (InterruptedException ie) {
                Timber.d("The persist thread received interrupt request");
            }
            Timber.d("Finish the persist thread...");
        }

        public void addToQueue(Device device) {
            try {
                blockingQueue.add(device);
            } catch (Exception e) {
                Timber.w(e, "Could not add device to queue");
            }
        }

        public void requestStop() {
            requestedToStop = true;
            interrupt();
        }
    }
}
