package com.rainmachine.data.local.database.model;

import org.parceler.Parcel;

@Parcel
public class CloudInfo {

    public static final CloudInfo NOT_FOUND = new CloudInfo();

    public Long _id;
    public String email;
    public String password;
    public int activeCount;
    public int knownCount;
    public int authCount;
}
