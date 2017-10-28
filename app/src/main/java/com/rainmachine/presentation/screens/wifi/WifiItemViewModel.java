package com.rainmachine.presentation.screens.wifi;

import org.parceler.Parcel;

@Parcel
class WifiItemViewModel {
    static final int NUM_SIGNAL_LEVELS = 4;

    String sSID;
    boolean isEncrypted;
    int level;
    int rSSI; // in dbm
    boolean isWEP;
    boolean isWPA;
    boolean isWPA2;
    boolean isHomeWifi;
    boolean isHidden;
}
