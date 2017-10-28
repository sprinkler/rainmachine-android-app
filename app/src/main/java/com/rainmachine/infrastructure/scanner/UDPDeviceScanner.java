package com.rainmachine.infrastructure.scanner;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.infrastructure.InfrastructureUtils;
import com.rainmachine.infrastructure.NetworkUtils;
import com.rainmachine.infrastructure.Sleeper;
import com.rainmachine.infrastructure.bus.BaseEvent;
import com.rainmachine.infrastructure.util.RainApplication;
import com.squareup.otto.Bus;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

import timber.log.Timber;

public class UDPDeviceScanner {

    // the port where the sprinklers are listening
    private static final int SPRINKLER_PORT = 15800;
    // the port where the app listens for sprinkler responses
    private static final int APP_PORT = 15900;
    // how long to sleep between sending udp broadcasts
    private static final int TIMEOUT_SLEEP_MILLIS = 3 * DateTimeConstants.MILLIS_PER_SECOND;
    private static final int TIMEOUT_SLEEP_MILLIS_INITIAL_SETUP = 500;
    // Show only the sprinklers that responded in the last x seconds
    private static final String SPRINKLER_RESPONSE_DELIMITER = "\\|\\|"; // it's || but it needs
    // the backspaces
    private static final String SPRINKLER_LOCAL_BROADCAST = "255.255.255.255";
    private static final int RECEIVER_BUFFER_LENGTH = 200;
    private static final int RECEIVER_PARTIAL_TIMEOUT = DateTimeConstants.MILLIS_PER_SECOND;

    private Bus bus;
    private PersistDeviceHandler persistDeviceHandler;

    private ReceiverThread receiverThread;
    private SenderThread senderThread;
    private Device targetDevice; // if must look for a specific device during provisioning

    public UDPDeviceScanner(Bus bus, PersistDeviceHandler persistDeviceHandler) {
        this.bus = bus;
        this.persistDeviceHandler = persistDeviceHandler;
    }

    public void start() {
        // Clear the target device in case the last scan wasn't stopped appropriately
        targetDevice = null;
        startInternal();
    }

    // Start looking for this device
    public void start(Device device) {
        if (InfrastructureUtils.shouldRouteNetworkTrafficToWiFi(device)) {
            NetworkUtils.routeNetworkTrafficToCurrentWiFi();
        }
        targetDevice = device;
        startInternal();
    }

    private void startInternal() {
        Timber.d("Start UDP scanner...");
        persistDeviceHandler.start();
        // The receiver thread does not depend on the Wi-Fi connected so we just need one
        if (receiverThread == null || !receiverThread.isAlive()) {
            receiverThread = new ReceiverThread();
            receiverThread.start();
        }
        // The sender thread depends on the Wi-Fi connected for the specific broadcast address
        stopSenderThread();
        senderThread = new SenderThread();
        senderThread.start();
    }

    public void stop() {
        stopSenderThread();
        if (receiverThread != null) {
            receiverThread.requestStop();
            receiverThread = null;
        }
        persistDeviceHandler.stop();
        targetDevice = null;
    }

    private void stopSenderThread() {
        if (senderThread != null) {
            senderThread.requestStop();
            senderThread = null;
        }
    }

    private class SenderThread extends Thread {

        private InetAddress broadcastAddress;
        private boolean requestedToStop;

        public SenderThread() {
            try {
                broadcastAddress = getBroadcastAddress();
            } catch (IOException ioe) {
                try {
                    broadcastAddress = InetAddress.getByName(SPRINKLER_LOCAL_BROADCAST);
                } catch (UnknownHostException uhe) {
                    Timber.w(ioe, "Could not get broadcast address");
                    Timber.w(uhe, "Could not get broadcast address by name");
                    requestStop();
                    return;
                }
            }

            Timber.d("The broadcast address: %s", broadcastAddress.getHostAddress());
        }

        @Override
        public void run() {
            DatagramSocket socket = null;
            Timber.d("Initialize the sender...");

            try {
                // We sleep here to be sure the receiver thread is already listening for messages
                Sleeper.sleep(DateTimeConstants.MILLIS_PER_SECOND);

                Timber.d("Sending UDP broadcasts...");
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                NetworkUtils.bindToWifiNetwork(socket);

                while (!Thread.currentThread().isInterrupted() && !requestedToStop) {
                    String s = "hello";
                    byte[] buffer = s.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                            broadcastAddress, SPRINKLER_PORT);
                    try {
                        if (RainApplication.isDebugLogging()) {
                            Timber.d("[%s] Send UDP packet to broadcast address %s", new DateTime
                                    ().toLocalTime().toString(), broadcastAddress.getHostAddress());
                        }
                        socket.send(packet);
                    } catch (InterruptedIOException ioee) {
                        Timber.d(ioee, ioee.getMessage());
                        break;
                    } catch (IOException ioe) {
                        Timber.d(ioe, ioe.getMessage());
                    }
                    try {
                        Sleeper.sleepThrow((targetDevice != null) ?
                                TIMEOUT_SLEEP_MILLIS_INITIAL_SETUP : TIMEOUT_SLEEP_MILLIS);
                    } catch (InterruptedException ie) {
                        Timber.d("The sender thread received interrupt request");
                        break;
                    }
                }
            } catch (SocketException se) {
                Timber.d(se, "Socket exception");
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }

            Timber.d("Finish the sender...");
        }

        public void requestStop() {
            Timber.d("Sender sends interrupt to itself");
            requestedToStop = true;
            interrupt();
        }

        private InetAddress getBroadcastAddress() throws IOException {
            WifiManager wifi = (WifiManager) RainApplication.get().getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifi.getDhcpInfo();
            if (dhcp == null) {
                throw new IOException();
            }

            int broadcast = dhcp.ipAddress | (~dhcp.netmask);
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++) {
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            }
            return InetAddress.getByAddress(quads);
        }
    }

    public class ReceiverThread extends Thread {

        private boolean requestedToStop;

        @Override
        public void run() {
            DatagramSocket socket = null;
            byte[] buffer;
            DatagramPacket dataIn;

            Timber.d("Initialize the receiver...");
            int count = 0;
            while (true) {
                try {
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.setSoTimeout(RECEIVER_PARTIAL_TIMEOUT);
                    socket.bind(new InetSocketAddress(APP_PORT));
                    break;
                } catch (SocketException se) {
                    Timber.d(se, "Socket exception");
                    count++;

                    if (count >= 10) {
                        if (socket != null) {
                            socket.close();
                        }
                        return;
                    } else {
                        // Sleep a bit and then try again
                        try {
                            Sleeper.sleepThrow(DateTimeConstants.MILLIS_PER_SECOND);
                        } catch (InterruptedException ie) {
                            Timber.d(ie, ie.getMessage());
                            if (socket != null) {
                                socket.close();
                            }
                            return;
                        }
                    }
                }
            }

            try {
                Timber.d("Waiting for sprinklers to answer...");
                NetworkUtils.bindToWifiNetwork(socket);
                while (!Thread.currentThread().isInterrupted() && !requestedToStop) {
                    buffer = new byte[RECEIVER_BUFFER_LENGTH];
                    dataIn = new DatagramPacket(buffer, buffer.length);

                    try {
                        socket.receive(dataIn);
                    } catch (SocketTimeoutException ste) {
                        continue;
                    } catch (IOException ioe) {
                        Timber.d(ioe, "IOException");
                        break;
                    }

                    final String sprinklerMsg = new String(dataIn.getData(), 0, dataIn.getLength());
                    if (RainApplication.isDebugLogging()) {
                        Timber.d("[%s] UDP received from %s Message: %s", new DateTime()
                                .toLocalTime().toString(), dataIn.getAddress(), sprinklerMsg);
                    }

                    saveDevice(sprinklerMsg);
                }
            } finally {
                Timber.d("The receiver thread received interrupt request");
                socket.close();
            }

            Timber.d("Finish the receiver...");
        }

        private void saveDevice(String sprinklerMsg) {
            String[] values = sprinklerMsg.split(SPRINKLER_RESPONSE_DELIMITER);
            if (values.length >= 4 && values[0].equals("SPRINKLER")) {
                Device device = new Device();
                // We use as device id the DEVICE_ID of the sprinkler
                device.deviceId = values[1].toLowerCase(Locale.ENGLISH);
                device.name = values[2];
                device.setUrl(values[3]);
                device.type = Device.SPRINKLER_TYPE_UDP;
                device.timestamp = new DateTime().getMillis();
                device.wizardHasRun = true; // old v3 sprinklers already ran wizard
                if (values.length >= 5) {
                    try {
                        device.wizardHasRun = Integer.parseInt(values[4]) != 0;
                    } catch (NumberFormatException nfe) {
                        Timber.w(nfe, nfe.getMessage());
                    }
                }
                device.cloudEmail = null;

                // If we scan for a particular device
                if (targetDevice != null && targetDevice.deviceId.equals(device.deviceId)) {
                    Timber.d("Found device %s", device.deviceId);
                    targetDevice.name = device.name;
                    targetDevice.setUrl(device.getUrl());
                    targetDevice.type = device.type;
                    targetDevice.timestamp = device.timestamp;
                    targetDevice.wizardHasRun = device.wizardHasRun;
                    persistDeviceHandler.addToQueue(device);
                    bus.post(new DeviceDiscoveredEvent(device));
                    requestStop();
                } else {
                    persistDeviceHandler.addToQueue(device);
                }
            }
        }

        public void requestStop() {
            requestedToStop = true;
            interrupt();
        }
    }

    public class DeviceDiscoveredEvent extends BaseEvent {
        public Device device;

        public DeviceDiscoveredEvent(Device device) {
            this.device = device;
        }
    }
}
