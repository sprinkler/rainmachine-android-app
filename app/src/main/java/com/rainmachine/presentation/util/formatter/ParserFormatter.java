package com.rainmachine.presentation.util.formatter;


import android.content.Context;

import com.rainmachine.R;
import com.rainmachine.domain.model.Parser;

public class ParserFormatter {

    public String coverArea(Context ctx, Parser parser) {
        if (parser.getCoverArea() == Parser.CoverArea.NORTH_AMERICA) {
            return ctx.getString(R.string.all_north_america);
        }
        if (parser.getCoverArea() == Parser.CoverArea.US_CA) {
            return ctx.getString(R.string.all_us_ca);
        }
        if (parser.getCoverArea() == Parser.CoverArea.US_FL) {
            return ctx.getString(R.string.all_us_fl);
        }
        return ctx.getString(R.string.all_global);
    }

    public String lastRun(Context ctx, Parser parser) {
        String formattedLastRun = parser.lastRun == null ?
                ctx.getString(R.string.all_never) :
                parser.lastRun.toString("yyyy-MM-dd HH:mm:ss");
        return ctx.getString(R.string.all_last_run, formattedLastRun);
    }
}
