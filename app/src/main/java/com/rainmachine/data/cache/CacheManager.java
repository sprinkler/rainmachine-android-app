package com.rainmachine.data.cache;


import com.rainmachine.domain.model.GlobalRestrictions;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.model.ZoneProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheManager {

    private Map<CacheEntryKey, CacheData<?>> cache = new HashMap<>(3);

    public CacheData<Provision> save(Provision provision) {
        CacheData<Provision> data = new CacheData<>(provision);
        cache.put(CacheEntryKey.PROVISION, data);
        return data;
    }

    public CacheData<String> save(String deviceName) {
        CacheData<String> data = new CacheData<>(deviceName);
        cache.put(CacheEntryKey.DEVICE_NAME, data);
        return data;
    }

    public CacheData<List<ZoneProperties>> save(List<ZoneProperties> zonesProperties) {
        CacheData<List<ZoneProperties>> data = new CacheData<>(zonesProperties);
        cache.put(CacheEntryKey.ZONES_PROPERTIES, data);
        return data;
    }

    public CacheData<GlobalRestrictions> save(GlobalRestrictions globalRestrictions) {
        CacheData<GlobalRestrictions> data = new CacheData<>(globalRestrictions);
        cache.put(CacheEntryKey.GLOBAL_RESTRICTIONS, data);
        return data;
    }

    public CacheData<List<HourlyRestriction>> saveRestrictions(List<HourlyRestriction>
                                                                       hourlyRestrictions) {
        CacheData<List<HourlyRestriction>> data = new CacheData<>(hourlyRestrictions);
        cache.put(CacheEntryKey.HOURLY_RESTRICTIONS, data);
        return data;
    }

    public CacheData<List<Program>> savePrograms(List<Program> programs) {
        CacheData<List<Program>> data = new CacheData<>(programs);
        cache.put(CacheEntryKey.PROGRAMS, data);
        return data;
    }

    public CacheData<Boolean> saveBetaUpdatesEnabled(boolean enabled) {
        CacheData<Boolean> data = new CacheData<>(enabled);
        cache.put(CacheEntryKey.BETA_UPDATES, data);
        return data;
    }

    public <T> CacheData<T> get(CacheEntryKey key) {
        CacheData<T> cacheData = (CacheData<T>) cache.get(key);
        if (cacheData == null) {
            cacheData = CacheData.NOT_FOUND;
        }
        return cacheData;
    }

    public void invalidate(CacheEntryKey key) {
        cache.remove(key);
    }

    public void invalidateAll() {
        invalidate(CacheEntryKey.DEVICE_NAME);
        invalidate(CacheEntryKey.PROVISION);
        invalidate(CacheEntryKey.ZONES_PROPERTIES);
        invalidate(CacheEntryKey.GLOBAL_RESTRICTIONS);
        invalidate(CacheEntryKey.HOURLY_RESTRICTIONS);
        invalidate(CacheEntryKey.PROGRAMS);
        invalidate(CacheEntryKey.BETA_UPDATES);
    }
}
