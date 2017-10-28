package com.rainmachine.domain.model;

public class ZoneImage {
    public long zoneId;
    public String imageUrl;
    public String imageLocalPath;

    public ZoneImage(long zoneId, String imageUrl) {
        this.zoneId = zoneId;
        this.imageUrl = imageUrl;
    }

    public ZoneImage(long zoneId, String imageUrl, String imageLocalPath) {
        this.zoneId = zoneId;
        this.imageUrl = imageUrl;
        this.imageLocalPath = imageLocalPath;
    }
}
