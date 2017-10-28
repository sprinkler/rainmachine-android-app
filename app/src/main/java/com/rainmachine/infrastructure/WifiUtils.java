package com.rainmachine.infrastructure;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import com.rainmachine.infrastructure.util.BaseApplication;

import org.joda.time.DateTimeConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class WifiUtils {

    public static final int WIFI_SECURITY_OPEN = 0;
    public static final int WIFI_SECURITY_WEP = 1;
    public static final int WIFI_SECURITY_WPA = 2;

    private static String homeWifiSSID;
    private static final int WAIT_WIFI_CONNECTION = 30; // seconds

    /**
     * Move to an already existing Wi-Fi configuration
     **/
    public static boolean moveToAlreadyEstablishedWifiConfiguration(String ssid) {
        if (ssid == null) {
            Timber.w("SSID is null");
            return false;
        }
        if (!isWifiEnabled()) {
            Timber.w("Wifi is disabled");
            return false;
        }
        String sanitizedSSID = getSanitizedSSID(ssid);
        WifiManager wifiManager = wifiManager();
        boolean isSuccess = false;
        List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
        if (networks != null) {
            for (WifiConfiguration network : networks) {
                if (sanitizedSSID.equals(network.SSID)) {
                    Timber.d("Activate wifi %s", ssid);
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(network.networkId, true);
                    wifiManager.reconnect();

                    // Wait a bit for the current Wi-Fi to disconnect
                    Sleeper.sleep(2 * DateTimeConstants.MILLIS_PER_SECOND);

                    // Wait a bit so that it connects to the wifi network
                    int count = 0;
                    boolean wifiConnected = true;
                    while (!isWifiConnected()) {
                        if (count >= WAIT_WIFI_CONNECTION) {
                            Timber.w("Took too long to activate Wifi");
                            wifiConnected = false;
                            break;
                        }

                        Sleeper.sleep(DateTimeConstants.MILLIS_PER_SECOND);
                        count++;
                    }
                    if (!wifiConnected) {
                        continue;
                    }

                    // It might connect to a previously configured wifi network
                    if (!sanitizedSSID.equals(getCurrentSSID())) {
                        Timber.w("Not connected to the right network");
                        isSuccess = false;
                        continue;
                    } else {
                        isSuccess = true;
                        break;
                    }
                }
            }
        }
        return isSuccess;
    }

    /**
     * Move to a new Wi-Fi by creating a new Wi-Fi configuration
     **/
    public static boolean moveToNewWifiConfiguration(String ssid, String password, int
            wifiSecurity) {
        if (ssid == null) {
            Timber.w("SSID is null");
            return false;
        }
        if (!isWifiEnabled()) {
            Timber.w("Wifi is disabled");
            return false;
        }

        boolean isSuccess = false;
        WifiConfiguration conf = constructWifiConfiguration(ssid, password, wifiSecurity);
        WifiManager wifiManager = wifiManager();
        int netId = wifiManager.addNetwork(conf);
        if (netId != -1) {
            List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
            if (networks != null) {
                for (WifiConfiguration network : networks) {
                    if (netId == network.networkId) {
                        Timber.d("Enable network %s", network.SSID);
                        wifiManager.disconnect();
                        boolean success = wifiManager.enableNetwork(network.networkId, true);
                        if (!success) {
                            Timber.w("Error enabling network");
                            isSuccess = false;
                            break;
                        }
                        wifiManager.reconnect();

                        // Wait a bit for the current Wi-Fi to disconnect
                        Sleeper.sleep(2 * DateTimeConstants.MILLIS_PER_SECOND);

                        // Wait a bit so that it connects to the wifi network
                        int count = 0;
                        while (!isWifiConnected()) {
                            if (count >= WAIT_WIFI_CONNECTION) {
                                Timber.w("Took too long to enable Wifi");
                                success = false;
                                break;
                            }

                            Sleeper.sleep(DateTimeConstants.MILLIS_PER_SECOND);
                            count++;
                        }

                        if (!success) {
                            isSuccess = false;
                            break;
                        }

                        // It might connect to a previously configured wifi network
                        WifiInfo wifiInfo = wifiManager().getConnectionInfo();
                        success = (wifiInfo != null && netId == wifiInfo.getNetworkId());
                        if (!success) {
                            Timber.w("Not connected to the right network %s", wifiInfo != null ?
                                    wifiInfo.getSSID() : "wifiInfo is null");
                            isSuccess = false;
                            break;
                        } else {
                            isSuccess = true;
                            break;
                        }
                    }
                }
            }
            if (!isSuccess) {
                wifiManager.removeNetwork(netId);
                wifiManager.saveConfiguration();
            }
        } else {
            Timber.w("Error adding network to WifiManager");
            isSuccess = false;
        }
        return isSuccess;
    }

    /* Test connection to a Wi-Fi by creating a new Wi-Fi configuration and then moving back to
    the previous Wi-Fi*/
    public static boolean testWifi(String ssid, String password, int wifiSecurity, String
            previousSSID) {
        if (ssid == null) {
            Timber.w("SSID is null");
            return false;
        }
        if (!isWifiEnabled()) {
            Timber.w("Wifi is disabled");
            return false;
        }
        List<Integer> networkIds = WifiUtils.disableAllEnabledWifiConfigurations();

        boolean isSuccess = false;
        WifiConfiguration conf = constructWifiConfiguration(ssid, password, wifiSecurity);
        WifiManager wifiManager = wifiManager();
        int netId = wifiManager.addNetwork(conf);
        if (netId != -1) {
            List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
            if (networks != null) {
                for (WifiConfiguration network : networks) {
                    if (netId == network.networkId) {
                        Timber.d("Enable network %s", network.SSID);
                        wifiManager.disconnect();
                        boolean success = wifiManager.enableNetwork(network.networkId, true);
                        if (!success) {
                            isSuccess = false;
                            break;
                        }
                        wifiManager.reconnect();

                        // Wait a bit for the current Wi-Fi to disconnect
                        Sleeper.sleep(2 * DateTimeConstants.MILLIS_PER_SECOND);

                        // Wait a bit so that it connects to the wifi network
                        int count = 0;
                        while (!isWifiConnected()) {
                            if (count >= WAIT_WIFI_CONNECTION) {
                                Timber.w("Took too long to enable Wifi");
                                success = false;
                                break;
                            }
                            Sleeper.sleep(DateTimeConstants.MILLIS_PER_SECOND);
                            count++;
                        }

                        if (!success) {
                            isSuccess = false;
                            break;
                        }

                        // It might connect to a previously configured wifi network (although they
                        // are temporarily disabled)
                        WifiInfo wifiInfo = wifiManager().getConnectionInfo();
                        success = (wifiInfo != null && netId == wifiInfo.getNetworkId());
                        if (!success) {
                            Timber.w("Not connected to the right network %s", wifiInfo != null ?
                                    wifiInfo.getSSID() : "wifiInfo is null");
                            isSuccess = false;
                            break;
                        } else {
                            isSuccess = true;
                            break;
                        }
                    }
                }
            }
        } else {
            Timber.w("Error adding network to WifiManager");
            isSuccess = false;
        }

        WifiUtils.enableWifiConfigurations(networkIds);

        if (!isSuccess) {
            Timber.d("Connection failed. Connect back to previous wifi %s", previousSSID);
            if (netId != -1) {
                wifiManager.removeNetwork(netId);
                wifiManager.saveConfiguration();
            }
            WifiUtils.moveToAlreadyEstablishedWifiConfiguration(previousSSID);
            return false;
        } else {
            Timber.d("Connection succeeded. Connect back to previous wifi %s", previousSSID);
            WifiUtils.moveToAlreadyEstablishedWifiConfiguration(previousSSID);
            return true;
        }
    }

    private static WifiConfiguration constructWifiConfiguration(String ssid, String password, int
            wifiSecurity) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = getSanitizedSSID(ssid);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        if (wifiSecurity == WIFI_SECURITY_OPEN) {
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedAuthAlgorithms.clear();
        } else if (wifiSecurity == WIFI_SECURITY_WEP) {
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            if (isHexKey(password)) {
                conf.wepKeys[0] = password;
            } else {
                conf.wepKeys[0] = "\"" + password + "\"";
            }
            conf.wepTxKeyIndex = 0;
        } else if (wifiSecurity == WIFI_SECURITY_WPA) {
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            conf.preSharedKey = "\"" + password + "\"";
        }
        return conf;
    }

    public static void removeWifiConfigurations(String ssid) {
        if (ssid == null) {
            Timber.w("SSID is null");
            return;
        }
        if (!isWifiEnabled()) {
            Timber.w("Wifi is disabled");
            return;
        }
        String sanitizedSSID = getSanitizedSSID(ssid);
        WifiManager wifiManager = wifiManager();
        List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
        if (networks != null) {
            for (WifiConfiguration network : networks) {
                if (sanitizedSSID.equals(network.SSID)) {
                    wifiManager.removeNetwork(network.networkId);
                    Timber.d("Removed wifi configuration for %s", ssid);
                }
            }
        }
        wifiManager.saveConfiguration();
    }

    private static List<Integer> disableAllEnabledWifiConfigurations() {
        List<Integer> networkIds = new ArrayList<>();
        if (!isWifiEnabled()) {
            return networkIds;
        }
        WifiManager wifiManager = wifiManager();
        List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
        if (networks != null) {
            for (WifiConfiguration network : networks) {
                if (network.status == WifiConfiguration.Status.ENABLED) {
                    wifiManager.disableNetwork(network.networkId);
                    networkIds.add(network.networkId);
                    Timber.d("Disabled wifi configuration for %s", network.SSID);
                }
            }
        }
        wifiManager.saveConfiguration();
        return networkIds;
    }

    private static void enableWifiConfigurations(List<Integer> networkIds) {
        if (!isWifiEnabled()) {
            return;
        }
        WifiManager wifiManager = wifiManager();
        for (Integer networkId : networkIds) {
            wifiManager.enableNetwork(networkId, false);
            Timber.d("Enabled wifi configuration for %d", networkId);
        }
        wifiManager.saveConfiguration();
    }

    public static String getSanitizedSSID(String ssid) {
        if (ssid == null) {
            return null;
        }
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid;
        } else {
            return "\"" + ssid + "\"";
        }
    }

    public static String getCurrentSSID() {
        WifiInfo wifiInfo = wifiManager().getConnectionInfo();
        if (wifiInfo != null) {
            return getSanitizedSSID(wifiInfo.getSSID());
        }
        return null;
    }

    public static String getCurrentWifiMac() {
        WifiInfo wifiInfo = wifiManager().getConnectionInfo();
        if (wifiInfo != null) {
            return wifiInfo.getBSSID();
        }
        return null;
    }

    public static boolean isCurrentlyActiveSSID(String ssid) {
        String sanitizedCurrentSSID = WifiUtils.getCurrentSSID();
        String sanitizedSSID = WifiUtils.getSanitizedSSID(ssid);
        Timber.d("current SSID: %s vs inquired SSID: %s", sanitizedCurrentSSID, sanitizedSSID);
        return sanitizedSSID != null && sanitizedSSID.equals(sanitizedCurrentSSID);
    }

    public static boolean isWifiConnected() {
        Context ctx = BaseApplication.getContext();
        ConnectivityManager connectivityManager = ((ConnectivityManager) ctx.getSystemService
                (Context.CONNECTIVITY_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            if (networks == null) {
                return false;
            }
            boolean isWifiConnected = false;
            for (Network network : networks) {
                NetworkInfo info = connectivityManager.getNetworkInfo(network);
                if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (info.isAvailable() && info.isConnected()) {
                        isWifiConnected = true;
                        break;
                    }
                }
            }
            return isWifiConnected;
        } else {
            //noinspection deprecation
            NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return info != null && info.isAvailable() && info.isConnected();
        }
    }

    public static boolean isWifiEnabled() {
        return wifiManager().isWifiEnabled();
    }

    public static void enableWifi() {
        wifiManager().setWifiEnabled(true);
    }

    public static void disableWifi() {
        wifiManager().setWifiEnabled(false);
    }

    public static void setHomeWifiSSID(String ssid) {
        homeWifiSSID = ssid;
    }

    public static String getHomeWifiSSID() {
        return getSanitizedSSID(homeWifiSSID);
    }

    public static String getDesanitizedHomeWifiSSID() {
        if (homeWifiSSID == null) {
            return null;
        }
        if (homeWifiSSID.startsWith("\"") && homeWifiSSID.endsWith("\"")) {
            return homeWifiSSID.substring(1, homeWifiSSID.length() - 1);
        }
        return homeWifiSSID;
    }

    private static WifiManager wifiManager() {
        Context ctx = BaseApplication.getContext();
        return (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * WEP has two kinds of password, a hex value that specifies the key or
     * a character string used to generate the real hex. This checks what kind of
     * password has been supplied. The checks correspond to WEP40, WEP104 & WEP232
     */
    private static boolean isHexKey(String s) {
        if (s == null) {
            return false;
        }

        int len = s.length();
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }

        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Checks whether the "Avoid poor networks" setting (named "Auto network switch" on
     * some Samsung devices) is enabled, which can in some instances interfere with Wi-Fi.
     *
     * @return true if the "Avoid poor networks" or "Auto network switch" setting is enabled
     */
    public static boolean isPoorNetworkAvoidanceEnabled(Context ctx) {
        final int SETTING_UNKNOWN = -1;
        final int SETTING_ENABLED = 1;
        final String AVOID_POOR = "wifi_watchdog_poor_network_test_enabled";
        final String WATCHDOG_CLASS = "android.net.wifi.WifiWatchdogStateMachine";
        final String DEFAULT_ENABLED = "DEFAULT_POOR_NETWORK_AVOIDANCE_ENABLED";
        final ContentResolver cr = ctx.getContentResolver();

        int result;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //Setting was moved from Secure to Global as of JB MR1
            result = Settings.Global.getInt(cr, AVOID_POOR, SETTING_UNKNOWN);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            result = Settings.Secure.getInt(cr, AVOID_POOR, SETTING_UNKNOWN);
        } else {
            //Poor network avoidance not introduced until ICS MR1
            //See android.provider.Settings.java
            return false;
        }

        //Exit here if the setting value is known
        if (result != SETTING_UNKNOWN) {
            return (result == SETTING_ENABLED);
        }

        //Setting does not exist in database, so it has never been changed.
        //It will be initialized to the default value.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //As of JB MR1, a constant was added to WifiWatchdogStateMachine to determine
            //the default behavior of the Avoid Poor Networks setting.
            try {
                //In the case of any failures here, take the safe route and assume the
                //setting is disabled to avoid disrupting the user with false information
                Class wifiWatchdog = Class.forName(WATCHDOG_CLASS);
                Field defValue = wifiWatchdog.getField(DEFAULT_ENABLED);
                if (!defValue.isAccessible()) {
                    defValue.setAccessible(true);
                }
                return defValue.getBoolean(null);
            } catch (IllegalAccessException ex) {
                return false;
            } catch (NoSuchFieldException ex) {
                return false;
            } catch (ClassNotFoundException ex) {
                return false;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        } else {
            //Prior to JB MR1, the default for the Avoid Poor Networks setting was
            //to enable it unless explicitly disabled
            return true;
        }
    }
}
