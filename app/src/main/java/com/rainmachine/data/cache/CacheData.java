package com.rainmachine.data.cache;

import org.joda.time.DateTimeConstants;

import java.util.Random;

public class CacheData<T> {

    public static final CacheData NOT_FOUND = new CacheData<>(null);
    private static final Random random = new Random();

    private T data;
    private long timestamp;

    CacheData(T data) {
        this.data = data;
        timestamp = System.currentTimeMillis();
    }

    public T getData() {
        return data;
    }

    public boolean isUpToDate(boolean isLocal) {
        long cacheAliveSecondsLocal = DateTimeConstants.SECONDS_PER_MINUTE;
        long cacheAliveSecondsRemote = 30 * DateTimeConstants.SECONDS_PER_MINUTE;
        long cacheAliveSeconds = isLocal ? cacheAliveSecondsLocal : cacheAliveSecondsRemote;
        // Use a bit of randomness so that the cached data does not become stale all at once
        cacheAliveSeconds += random.nextInt(20);
        return (System.currentTimeMillis() - timestamp) < cacheAliveSeconds * DateTimeConstants
                .MILLIS_PER_SECOND;
    }
}
