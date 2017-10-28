package com.rainmachine.presentation.screens.stats;

import android.support.v4.util.ArrayMap;

import com.rainmachine.R;

class WeatherIconMapper {

    private static ArrayMap<String, Integer> map3;

    static int get(String iconName) {
        if (map3 == null) {
            buildMap();
        }
        Integer iconId = map3.get(iconName);
        if (iconId == null) {
            iconId = 0;
        }
        return iconId;
    }

    private static void buildMap() {
        map3 = new ArrayMap<>();
        map3.put("bkn", R.drawable.ic_weather_bkn_gray);
        map3.put("blizzard", R.drawable.ic_weather_blizzard_gray);
        map3.put("cold", R.drawable.ic_weather_cold_gray);
        map3.put("du", R.drawable.ic_weather_du_gray);
        map3.put("few", R.drawable.ic_weather_few_gray);
        map3.put("fg", R.drawable.ic_weather_fg_gray);
        map3.put("frza", R.drawable.ic_weather_frza_gray);
        map3.put("fu", R.drawable.ic_weather_fu_gray);
        map3.put("fzrara", R.drawable.ic_weather_fzrara_gray);
        map3.put("hi_nshwrs", R.drawable.ic_weather_hi_nshwrs_gray);
        map3.put("hi_ntsra", R.drawable.ic_weather_hi_ntsra_gray);
        map3.put("hi_shwrs", R.drawable.ic_weather_hi_shwrs_gray);
        map3.put("hi_tsra", R.drawable.ic_weather_hi_tsra_gray);
        map3.put("hot", R.drawable.ic_weather_hot_gray);
        map3.put("ip", R.drawable.ic_weather_ip_gray);
        map3.put("mist", R.drawable.ic_weather_mist_gray);
        map3.put("mix", R.drawable.ic_weather_mix_gray);
        map3.put("na", R.drawable.ic_weather_na_gray);
        map3.put("nbkn", R.drawable.ic_weather_nbkn_gray);
        map3.put("nbknfg", R.drawable.ic_weather_nbknfg_gray);
        map3.put("ndu", R.drawable.ic_weather_ndu_gray);
        map3.put("nfew", R.drawable.ic_weather_nfew_gray);
        map3.put("nfg", R.drawable.ic_weather_nfg_gray);
        map3.put("nfu", R.drawable.ic_weather_nfu_gray);
        map3.put("nmix", R.drawable.ic_weather_nmix_gray);
        map3.put("novc", R.drawable.ic_weather_novc_gray);
        map3.put("nra", R.drawable.ic_weather_nra_gray);
        map3.put("nra1", R.drawable.ic_weather_nra1_gray);
        map3.put("nraip", R.drawable.ic_weather_nraip_gray);
        map3.put("nrasn", R.drawable.ic_weather_nrasn_gray);
        map3.put("nsct", R.drawable.ic_weather_nsct_gray);
        map3.put("nscttsra", R.drawable.ic_weather_nscttsra_gray);
        map3.put("nshra", R.drawable.ic_weather_nshra_gray);
        map3.put("nskc", R.drawable.ic_weather_nskc_gray);
        map3.put("nsn", R.drawable.ic_weather_nsn_gray);
        map3.put("nsvrtsra", R.drawable.ic_weather_nsvrtsra_gray);
        map3.put("ntsra", R.drawable.ic_weather_ntsra_gray);
        map3.put("nwind", R.drawable.ic_weather_nwind_gray);
        map3.put("ovc", R.drawable.ic_weather_ovc_gray);
        map3.put("ra", R.drawable.ic_weather_ra_gray);
        map3.put("ra1", R.drawable.ic_weather_ra1_gray);
        map3.put("raip", R.drawable.ic_weather_raip_gray);
        map3.put("rasn", R.drawable.ic_weather_rasn_gray);
        map3.put("sct", R.drawable.ic_weather_sct_gray);
        map3.put("sctfg", R.drawable.ic_weather_sctfg_gray);
        map3.put("scttsra", R.drawable.ic_weather_scttsra_gray);
        map3.put("shra", R.drawable.ic_weather_shra_gray);
        map3.put("shra1", R.drawable.ic_weather_shra1_gray);
        map3.put("shra2", R.drawable.ic_weather_shra2_gray);
        map3.put("skc", R.drawable.ic_weather_skc_gray);
        map3.put("smoke", R.drawable.ic_weather_smoke_gray);
        map3.put("sn", R.drawable.ic_weather_sn_gray);
        map3.put("tsra", R.drawable.ic_weather_tsra_gray);
        map3.put("wind", R.drawable.ic_weather_wind_gray);
    }

    static int get(int icon) {
        switch (icon) {
            case 0:
                return R.drawable.ic_weather_ovc; // MostlyCloudy
            case 1:
                return R.drawable.ic_weather_skc; // Fair
            case 2:
                return R.drawable.ic_weather_few; // A Few Clouds
            case 3:
                return R.drawable.ic_weather_sct; // Partly Cloudy
            case 4:
                return R.drawable.ic_weather_bkn; // Overcast
            case 5:
                return R.drawable.ic_weather_fg; // Fog
            case 6:
                return R.drawable.ic_weather_smoke; // Smoke
            case 7:
                return R.drawable.ic_weather_fzrara; // Freezing Rain
            case 8:
                return R.drawable.ic_weather_ip; // Ice Pellets
            case 9:
                return R.drawable.ic_weather_raip; // Rain Ice Pellets
            case 10:
                return R.drawable.ic_weather_rasn; // Rain Snow
            case 11:
                return R.drawable.ic_weather_hi_shwrs; // Rain Showers
            case 12:
                return R.drawable.ic_weather_tsra; // Thunderstorms
            case 13:
                return R.drawable.ic_weather_sn; // Snow
            case 14:
                return R.drawable.ic_weather_blizzard; // Windy
            case 15:
                return R.drawable.ic_weather_ra; // Showers in vicinity
            case 16:
                return R.drawable.ic_weather_mix; // Heavy Freezing Rain
            case 17:
                return R.drawable.ic_weather_scttsra; // Thunderstorms in Vicinity
            case 18:
                return R.drawable.ic_weather_ra1;  // Light Rain
            case 19:
                return R.drawable.ic_weather_shra; // Heavy Rain
            case 20:
                return R.drawable.ic_weather_nsvrtsra;  // Funnel Cloud in Vicinity
            case 21:
                return R.drawable.ic_weather_du; // Dust
            case 22:
                return R.drawable.ic_weather_mist; // Mist
            case 23:
                return R.drawable.ic_weather_hot; // Hot
            case 24:
                return R.drawable.ic_weather_cold; // Cold
            default:
                return R.drawable.ic_weather_na; // Not Available
        }
    }

    static int getDefaultWeatherIcon() {
        return R.drawable.ic_weather_na;
    }
}
