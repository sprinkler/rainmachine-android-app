package com.rainmachine.domain.model;

public class WifiScan {
    public String sSID;
    public boolean isEncrypted;
    public int rSSI; // in dbm
    public boolean isWEP;
    public String bSS;
    public boolean isWPA;
    public boolean isWPA2;
    public int channel;
    public boolean isHidden;
}
