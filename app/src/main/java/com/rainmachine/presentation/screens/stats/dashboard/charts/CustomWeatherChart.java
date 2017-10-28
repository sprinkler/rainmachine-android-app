package com.rainmachine.presentation.screens.stats.dashboard.charts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.LruCache;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.Utils;
import com.rainmachine.R;
import com.rainmachine.presentation.screens.stats.dashboard.DashboardView;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created from CustomBarChart
 */
public class CustomWeatherChart extends CustomChart<BarData> {

    private static final int MAX_MEMORY = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static final int CACHE_SIZE = MAX_MEMORY / 8;

    private ArrayList<String> temperatureMaxTextValues = new ArrayList<>();
    private ArrayList<String> temperatureMinTextValues = new ArrayList<>();
    private ArrayList<Boolean> temperatureMinLessOrEqualToFreezeProtect = new ArrayList<>();
    private ArrayList<String> rainTextValues = new ArrayList<>();
    private String unitTemperature = "\u2103";
    private String unitRain = "in";
    private LruCache<Integer, Object> iconBitmaps;
    private Paint generalPaint;
    private Paint temperatureMinPaint;
    private Paint temperatureMinFreezeProtectPaint;
    private int unitWidth;
    private int textHeight;
    private int weatherIconSize;
    private int width;
    private float rowHeight;

    /*private Paint paint1;
    private Paint paint2;
    private Paint paint3;
    private Paint paint4;*/

    public CustomWeatherChart(Context context) {
        super(context);

        generalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        generalPaint.setTextAlign(Paint.Align.CENTER);
        generalPaint.setTypeface(robotoTypeface);
        generalPaint.setTextSize(Utils.convertDpToPixel(10));
        generalPaint.setColor(Color.WHITE);

        temperatureMinPaint = new Paint(generalPaint);
        temperatureMinPaint.setColor(ContextCompat.getColor(context, R.color.gray_light));

        temperatureMinFreezeProtectPaint = new Paint(temperatureMinPaint);
        temperatureMinFreezeProtectPaint.setColor(ContextCompat.getColor(context, R.color.red));
        temperatureMinFreezeProtectPaint.setStyle(Paint.Style.FILL);

        textHeight = Utils.calcTextHeight(generalPaint, "Wed");
        weatherIconSize = (int) (1.5 * Utils.calcTextWidth(mXLabelPaint, "Aaa"));

        /*paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setColor(Color.RED);
        paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setColor(Color.GREEN);
        paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint3.setColor(Color.CYAN);
        paint4 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint4.setColor(Color.BLACK);*/
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDataNotSet) {
            return;
        }

        mData = getData();
        DataSet dataSet = mData.getDataSets().get(0);
        entries1 = dataSet.getYVals();

        if (viewType.equals(DashboardView.WEEK)) {
            drawAllValuesAndIcons();
            drawHeader();
        }
        canvas.drawBitmap(mDrawBitmap, 0, 0, mDrawPaint);

        if (onLoad) {
            onLoad = false;
            mTrans.refresh(mTrans.getTouchMatrix(), this);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getWidth();

        float contentHeight = getHeight() - getYPositionHeaderLine();
        rowHeight = contentHeight / 5;
    }

    @Override
    public void prepare() {
        if (mDataNotSet) {
            return;
        }
        calcMinMax(false);
        calculateOffsets();
    }

    @Override
    protected void drawData() {
    }

    @Override
    protected void drawValues() {
    }

    @Override
    protected void drawAdditional() {
    }

    @Override
    protected void drawHighlights() {
    }

    private void drawAllValuesAndIcons() {
        /*mDrawCanvas.drawRect(0, getYPositionHeaderLine(), width, getYPositionHeaderLine() +
                rowHeight, paint1);
        mDrawCanvas.drawRect(0, getYPositionHeaderLine() + rowHeight, width,
                getYPositionHeaderLine() + 2 * rowHeight, paint2);
        mDrawCanvas.drawRect(0, getYPositionHeaderLine() + 2 * rowHeight, width,
                getYPositionHeaderLine() + 3 * rowHeight, paint3);
        mDrawCanvas.drawRect(0, getYPositionHeaderLine() + 3 * rowHeight, width,
                getYPositionHeaderLine() + 4 * rowHeight, paint4);*/

        for (Entry e : entries1) {
            final float[] position = {e.getXIndex(), e.getVal()};
            mTrans.pointValuesToPixel(position);
            if (position[0] >= mOffsetLeft - LEFT_OFFSET && position[0] <= width - mOffsetRight +
                    RIGHT_OFFSET_EPS) {
                if (e.getXIndex() % xModulus == 0) {
                    String currDate;
                    LocalDate localDate;
                    try {
                        currDate = mData.getXVals().get(e.getXIndex());
                        localDate = LocalDate.parse(currDate);
                    } catch (IndexOutOfBoundsException exception) {
                        return;
                    }

                    drawHighlight(localDate, position[0], e);

                    String dayOfWeek = localDate.dayOfWeek().getAsShortText(Locale.ENGLISH);
                    mDrawCanvas.drawText(dayOfWeek, position[0], getYPositionBaselineDayOfWeek(),
                            generalPaint);

                    Object bitmap = iconBitmaps.get(e.getXIndex());
                    if (bitmap instanceof Bitmap) {
                        float xPositionLeft = position[0] - ((Bitmap) bitmap).getWidth() / 2;
                        mDrawCanvas.drawBitmap((Bitmap) bitmap, xPositionLeft,
                                getYPositionTopIcon(), generalPaint);
                    }

                    String temperatureMaxValue = temperatureMaxTextValues.get(e.getXIndex());
                    mDrawCanvas.drawText(temperatureMaxValue, position[0],
                            getYPositionBaselineTemperatureMaxValue(), generalPaint);

                    String temperatureMinValue = temperatureMinTextValues.get(e.getXIndex());
                    Paint paint;
                    if (temperatureMinLessOrEqualToFreezeProtect.get(e.getXIndex())) {
                        paint = generalPaint;
                        float left = position[0] - Utils.convertDpToPixel(10);
                        float top = getYPositionBaselineTemperatureMinValue() - Utils
                                .convertDpToPixel(10);
                        float right = position[0] + Utils.convertDpToPixel(10);
                        float bottom = getYPositionBaselineTemperatureMinValue() + Utils
                                .convertDpToPixel(3);
                        RectF rect = new RectF(left, top, right, bottom);
                        mDrawCanvas.drawRoundRect(rect, 5, 5, temperatureMinFreezeProtectPaint);
                    } else {
                        paint = temperatureMinPaint;
                    }
                    mDrawCanvas.drawText(temperatureMinValue, position[0],
                            getYPositionBaselineTemperatureMinValue(), paint);

                    String rainValue = rainTextValues.get(e.getXIndex());
                    mDrawCanvas.drawText(rainValue, position[0], getYPositionBaselineRainValue(),
                            generalPaint);
                }
            }
        }

        mDrawCanvas.drawText(unitTemperature, getXPositionCenterUnit(unitTemperature),
                getYPositionBaselineTemperatureMaxValue(), generalPaint);
        mDrawCanvas.drawText(unitTemperature, getXPositionCenterUnit(unitTemperature),
                getYPositionBaselineTemperatureMinValue(), temperatureMinPaint);
        mDrawCanvas.drawText(unitRain, getXPositionCenterUnit(unitRain),
                getYPositionBaselineRainValue(), generalPaint);

        String label = "Max";
        mDrawCanvas.drawText(label, getXPositionCenterLabel(label),
                getYPositionBaselineTemperatureMaxValue(), generalPaint);
        label = "Min";
        mDrawCanvas.drawText(label, getXPositionCenterLabel(label),
                getYPositionBaselineTemperatureMinValue(), temperatureMinPaint);
        label = "Rain";
        mDrawCanvas.drawText(label, getXPositionCenterLabel(label), getYPositionBaselineRainValue(),
                generalPaint);
    }

    public void setChartData(String viewType, ArrayList<LocalDate> xDatesVals, ArrayList<Float>
            yVals) {
        float yMin = 0.0f; // default values so as not to break stuff
        float yMax = 1.0f; // needs to be different from yMin so as not to break stuff
        String firstYear = "" + xDatesVals.get(0).getYear();
        String lastYear = "" + xDatesVals.get(xDatesVals.size() - 1).getYear();

        year = firstYear + (firstYear.equals(lastYear) ? "" : "\n" + lastYear);

        ArrayList<BarEntry> entries = new ArrayList<>();

        for (Float f : yVals) {
            entries.add(new BarEntry(f, entries.size()));
        }

        ArrayList<String> expectedXVals = new ArrayList<>();

        for (int i = 0; i < xDatesVals.size(); i++) {
            expectedXVals.add(xDatesVals.get(i).toString());
        }

        BarDataSet set = new BarDataSet(entries, "BarDataSet");

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        int i = viewType.equals(DashboardView.WEEK) ? 0 : viewType.equals(DashboardView.MONTH) ?
                1 : 2;

        data.put(i, new BarData(expectedXVals, dataSets));

        switch (viewType) {
            case DashboardView.WEEK:
                yAxisMinMax[0][0] = yMin;
                yAxisMinMax[0][1] = yMax;
                break;
            case DashboardView.MONTH:
                yAxisMinMax[1][0] = yMin;
                yAxisMinMax[1][1] = yMax;
                break;
            case DashboardView.YEAR:
                yAxisMinMax[2][0] = yMin;
                yAxisMinMax[2][1] = yMax;
                break;
        }

        if (viewType.equals(DashboardView.WEEK) && mDataNotSet) {
            setData(DashboardView.WEEK);
        }
    }

    public void setIcons(ArrayList<Integer> icons) {
        iconBitmaps = new LruCache<Integer, Object>(CACHE_SIZE) {
            @Override
            protected int sizeOf(Integer key, Object bitmap) {
                return (bitmap instanceof Bitmap) ? ((Bitmap) bitmap).getByteCount() / 1024 : 0;
            }
        };

        for (int i : icons) {
            Bitmap initialBitmap = BitmapFactory.decodeResource(getResources(), i);
            Bitmap scaledBitmap = null;
            if (initialBitmap != null) {
                scaledBitmap = Bitmap.createScaledBitmap(initialBitmap, weatherIconSize,
                        weatherIconSize, true);
            }
            iconBitmaps.put(iconBitmaps.putCount(), scaledBitmap != null ? scaledBitmap : NO_ID);

            if (initialBitmap != null && initialBitmap != scaledBitmap) {
                initialBitmap.recycle();
            }
        }
    }

    public void setTemperatureMaxTextValues(ArrayList<String> textValues, String unit) {
        this.unitTemperature = unit;
        int unitWidth = Utils.calcTextWidth(generalPaint, unit);
        if (this.unitWidth < unitWidth) {
            this.unitWidth = unitWidth;
        }
        for (String value : textValues) {
            this.temperatureMaxTextValues.add(value);
        }
    }

    public void setTemperatureMinTextValues(ArrayList<String> textValues, String unit,
                                            ArrayList<Boolean>
                                                    temperatureMinLessOrEqualToFreezeProtect) {
        this.unitTemperature = unit;
        int unitWidth = Utils.calcTextWidth(generalPaint, unit);
        if (this.unitWidth < unitWidth) {
            this.unitWidth = unitWidth;
        }
        for (String value : textValues) {
            this.temperatureMinTextValues.add(value);
        }
        this.temperatureMinLessOrEqualToFreezeProtect = temperatureMinLessOrEqualToFreezeProtect;
    }

    public void setRainTextValues(ArrayList<String> textValues, String unit) {
        this.unitRain = unit;
        int unitWidth = Utils.calcTextWidth(generalPaint, unit);
        if (this.unitWidth < unitWidth) {
            this.unitWidth = unitWidth;
        }
        for (String value : textValues) {
            this.rainTextValues.add(value);
        }
    }

    private float getYPositionBaselineDayOfWeek() {
        return getYPositionHeaderLine() + rowHeight / 2 + textHeight / 2;
    }

    private float getYPositionTopIcon() {
        float gridTop = getYPositionHeaderLine() + rowHeight;
        // The icon should be drawn centered vertically in its allotted row
        return gridTop + rowHeight / 2 - weatherIconSize / 2;
    }

    private float getYPositionBaselineTemperatureMaxValue() {
        return getYPositionHeaderLine() + 2 * rowHeight + rowHeight / 2 + textHeight / 2;
    }

    private float getYPositionBaselineTemperatureMinValue() {
        return getYPositionHeaderLine() + 3 * rowHeight + rowHeight / 2 + textHeight / 2;
    }

    private float getYPositionBaselineRainValue() {
        return getYPositionHeaderLine() + 4 * rowHeight + rowHeight / 2 + textHeight / 2;
    }

    private float getXPositionCenterUnit(String text) {
        int textWidth = Utils.calcTextWidth(generalPaint, text);
        return getXPositionPaddingRight() - textWidth / 2f;
    }

    private float getXPositionCenterLabel(String text) {
        int textWidth = Utils.calcTextWidth(generalPaint, text);
        return getXPositionPaddingLeft() + textWidth / 2;
    }
}
