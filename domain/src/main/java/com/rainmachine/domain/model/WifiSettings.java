package com.rainmachine.domain.model;

public class WifiSettings {
    public static final int NETWORK_TYPE_DHCP = 1;
    public static final int NETWORK_TYPE_STATIC = 2;

    public String sSID;
    public String password;
    public boolean isWEP;
    public boolean isWPA;
    public boolean isWPA2;
    public int networkType;
    public String ipAddress;
    public String netmask;
    public String gateway;
    public String dns;
}
