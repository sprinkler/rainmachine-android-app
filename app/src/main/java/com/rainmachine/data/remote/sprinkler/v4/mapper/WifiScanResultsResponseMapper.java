package com.rainmachine.data.remote.sprinkler.v4.mapper;

import com.rainmachine.data.remote.sprinkler.v4.response.WifiScanResultsResponse;
import com.rainmachine.domain.model.WifiScan;
import com.rainmachine.domain.util.Strings;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class WifiScanResultsResponseMapper implements Function<WifiScanResultsResponse,
        List<WifiScan>> {

    private static volatile WifiScanResultsResponseMapper instance;

    private WifiScanResultsResponseMapper() {
    }

    public static WifiScanResultsResponseMapper instance() {
        if (instance == null) {
            instance = new WifiScanResultsResponseMapper();
        }
        return instance;
    }

    @Override
    public List<WifiScan> apply(@NonNull WifiScanResultsResponse response) throws Exception {
        List<WifiScan> list = new ArrayList<>();
        if (response.scanResults != null) {
            for (WifiScanResultsResponse.WifiScanResult resp : response.scanResults) {
                WifiScan wifiScan = new WifiScan();
                wifiScan.sSID = resp.sSID;
                wifiScan.isHidden = Strings.isBlank(resp.sSID);
                wifiScan.isEncrypted = resp.isEncrypted;
                wifiScan.rSSI = resp.signal;
                wifiScan.isWEP = resp.isWEP;
                wifiScan.bSS = resp.bSS;
                wifiScan.isWPA = resp.isWPA;
                wifiScan.isWPA2 = resp.isWPA2;
                wifiScan.channel = resp.channel;
                list.add(wifiScan);
            }
        }
        return list;
    }
}
