package com.rainmachine.data.local.database.model;

import org.parceler.Parcel;

@Parcel
public class CloudServers {
    public Long _id;
    public String key;
    public String urlProxy;
    public String urlValidator;
    public String urlPush;

    public CloudServers() {
    }

    public CloudServers(String key, String urlProxy, String urlValidator, String urlPush) {
        this.key = key;
        this.urlProxy = urlProxy;
        this.urlValidator = urlValidator;
        this.urlPush = urlPush;
    }

    @Override
    public String toString() {
        return key;
    }
}
