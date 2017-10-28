package com.rainmachine.data.remote.sprinkler.v4.mapper;

import com.rainmachine.domain.model.Parser;

import java.util.HashMap;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;


public class ParserParamsRequestMapper implements Function<Parser, HashMap<String, Object>> {

    private static volatile ParserParamsRequestMapper instance;

    private ParserParamsRequestMapper() {
    }

    public static ParserParamsRequestMapper instance() {
        if (instance == null) {
            instance = new ParserParamsRequestMapper();
        }
        return instance;
    }

    @Override
    public HashMap<String, Object> apply(@NonNull Parser parser) throws Exception {
        HashMap<String, Object> params = parser.params;
        if (parser.isWUnderground()) {
            params.put("apiKey", parser.wUndergroundParams.apiKey);
            params.put("useCustomStation", parser.wUndergroundParams.useCustomStation);
            params.put("customStationName", parser.wUndergroundParams.customStationName);
        } else if (parser.isNetatmo()) {
            params.put("username", parser.netatmoParams.username);
            params.put("password", parser.netatmoParams.password);
            params.put("useSpecifiedModules", parser.netatmoParams.useSpecifiedModules);

            String sSpecificModules;
            if (parser.netatmoParams.specificModules.size() == 0) {
                sSpecificModules = "";
            } else {
                StringBuilder sb = new StringBuilder("");
                for (Parser.NetatmoModule module : parser.netatmoParams.specificModules) {
                    sb.append(module.mac).append(",");
                }
                sSpecificModules = sb.substring(0, sb.length() - 1);
            }
            params.put("specificModules", sSpecificModules);
        }
        return params;
    }
}