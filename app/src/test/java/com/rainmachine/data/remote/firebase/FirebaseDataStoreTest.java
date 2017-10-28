package com.rainmachine.data.remote.firebase;

import com.rainmachine.domain.model.LatLong;

import org.junit.Assert;
import org.junit.Test;

public class FirebaseDataStoreTest {

    @Test
    public void zoneFileName() {
        long zoneId = 3;
        LatLong coordinates = new LatLong(23.456879, -176.2);
        String filename = FirebaseDataStore.zoneFileName(zoneId, coordinates);
        Assert.assertEquals(null, "zone3_23.456_-176.200.jpg", filename);

        coordinates = new LatLong(-46, -176.2859);
        filename = FirebaseDataStore.zoneFileName(zoneId, coordinates);
        Assert.assertEquals(null, "zone3_-46.000_-176.285.jpg", filename);

        coordinates = new LatLong(-2.39991, 12.435);
        filename = FirebaseDataStore.zoneFileName(zoneId, coordinates);
        Assert.assertEquals(null, "zone3_-2.399_12.435.jpg", filename);

        coordinates = new LatLong(27.7053, 120);
        filename = FirebaseDataStore.zoneFileName(zoneId, coordinates);
        Assert.assertEquals(null, "zone3_27.705_120.000.jpg", filename);
    }
}
