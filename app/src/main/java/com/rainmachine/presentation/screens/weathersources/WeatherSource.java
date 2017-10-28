package com.rainmachine.presentation.screens.weathersources;

import com.rainmachine.domain.model.Parser;

public class WeatherSource {
    public Parser parser;

    public static class Comparator implements java.util.Comparator<WeatherSource> {

        @Override
        public int compare(WeatherSource lhs, WeatherSource rhs) {
            if (lhs.parser.isNOAA()) {
                return -1;
            }
            if (rhs.parser.isNOAA()) {
                return 1;
            }
            if (lhs.parser.isMETNO()) {
                return -1;
            }
            if (rhs.parser.isMETNO()) {
                return 1;
            }
            if (lhs.parser.isWUnderground()) {
                return -1;
            }
            if (rhs.parser.isWUnderground()) {
                return 1;
            }
            if (lhs.parser.isForecastIO()) {
                return -1;
            }
            if (rhs.parser.isForecastIO()) {
                return 1;
            }
            if (lhs.parser.isNetatmo()) {
                return -1;
            }
            if (rhs.parser.isNetatmo()) {
                return 1;
            }
            if (lhs.parser.enabled) {
                return -1;
            }
            if (rhs.parser.enabled) {
                return 1;
            }
            return 0;
        }
    }
}
