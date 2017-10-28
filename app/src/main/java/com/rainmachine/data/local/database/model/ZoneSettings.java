package com.rainmachine.data.local.database.model;

import org.parceler.Parcel;

@Parcel
public class ZoneSettings {
    public long zoneId;
    public boolean showZoneImage;
    public String imageLocalPath; // local storage
    public String imageUrl; // cloud storage

    public ZoneSettings() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ZoneSettings that = (ZoneSettings) o;

        if (zoneId != that.zoneId) {
            return false;
        }
        if (showZoneImage != that.showZoneImage) {
            return false;
        }
        if (imageLocalPath != null ? !imageLocalPath.equals(that.imageLocalPath) : that
                .imageLocalPath != null) {
            return false;
        }
        return imageUrl != null ? imageUrl.equals(that.imageUrl) : that.imageUrl == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (zoneId ^ (zoneId >>> 32));
        result = 31 * result + (showZoneImage ? 1 : 0);
        result = 31 * result + (imageLocalPath != null ? imageLocalPath.hashCode() : 0);
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        return result;
    }

    public ZoneSettings(long id) {
        zoneId = id;
        showZoneImage = true;
        imageLocalPath = null;
        imageUrl = null;
    }

    public ZoneSettings cloneIt() {
        ZoneSettings zoneSettings = new ZoneSettings();
        zoneSettings.zoneId = zoneId;
        zoneSettings.showZoneImage = showZoneImage;
        zoneSettings.imageLocalPath = imageLocalPath;
        zoneSettings.imageUrl = imageUrl;
        return zoneSettings;
    }
}
