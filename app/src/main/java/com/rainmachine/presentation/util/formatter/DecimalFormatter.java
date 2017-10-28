package com.rainmachine.presentation.util.formatter;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class DecimalFormatter {

    public String lengthUnitsDecimals(float value, boolean isUnitsMetric) {
        DecimalFormat df = isUnitsMetric ? new DecimalFormat("#.#") : new DecimalFormat("#.##");
        return df.format(value);
    }

    public String limitedDecimals(float value, int numDecimals) {
        StringBuilder sb = new StringBuilder();
        sb.append("#.");
        for (int i = 0; i < numDecimals; i++) {
            sb.append("#");
        }
        DecimalFormat df = new DecimalFormat(sb.toString());
        return df.format(value);
    }

    public float toFloat(String s) throws ParseException {
        NumberFormat numberFormat = NumberFormat.getInstance();
        Number number = numberFormat.parse(s);
        return number.floatValue();
    }

    public double toDouble(String s) throws ParseException {
        NumberFormat numberFormat = NumberFormat.getInstance();
        Number number = numberFormat.parse(s);
        return number.doubleValue();
    }

    public char getDecimalSeparator() {
        return new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
    }
}
