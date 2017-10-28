package com.rainmachine.presentation.screens.stats.dashboard.charts;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarLineScatterCandleData;
import com.github.mikephil.charting.data.BarLineScatterCandleRadarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.SelInfo;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.XLabels;
import com.github.mikephil.charting.utils.YLabels;
import com.rainmachine.presentation.screens.stats.dashboard.ChartUtils;
import com.rainmachine.presentation.screens.stats.dashboard.DashboardView;
import com.rainmachine.presentation.screens.stats.dashboard.charts.utils.OnChartClickListener;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Tremend Software on 5/15/2015.
 */
public abstract class CustomChart<T extends BarLineScatterCandleData<? extends
        BarLineScatterCandleRadarDataSet<? extends Entry>>>
        extends Chart<T> {

    protected final float LEFT_OFFSET = Utils.convertDpToPixel(15);
    protected final float RIGHT_OFFSET_EPS = .001f;
    protected final float OFFSET = Utils.convertDpToPixel(6);
    protected final float CHART_MIN_OFFSET = Utils.convertDpToPixel(11);

    protected HashMap<Integer, T> data = new HashMap<>(3);
    protected ArrayList<Entry> entries1 = new ArrayList<>();
    protected ArrayList<Entry> entries2 = new ArrayList<>();

    protected String viewType = DashboardView.WEEK;
    protected String year = "";
    protected String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
            "Aug", "Sep", "Oct", "Nov", "Dec"};

    protected YLabels mYLabels = new YLabels();
    protected XLabels mXLabels = new XLabels();

    protected float[][] yAxisMinMax = new float[3][2];

    protected int iconSize = 0;
    protected int yHeaderTextHeight = 0;
    protected int xModulus = 1;

    protected boolean onLoad = true;
    protected boolean isLeftDate = false;

    private OnChartClickListener mChartClickListener = null;

    private Paint mHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mHighLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Entry lastEntry = null;

    private String title = "Title";
    private String chartUnit = "Unit";
    private String forecast = "Forecast";

    private int monthMaxHeight = 0;
    private int highlightWidth = 0;
    private int chartUnitWidth = 0;
    private int forecastWidth = 0;

    protected float yLabelGuideSize = 0;
    private float customZoom;
    private float zoomFactor = 1;
    protected float yOffset = 0;

    private boolean showForecast = false;
    protected Typeface robotoTypeface;

    public CustomChart(Context context) {
        super(context);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Point screenSize = screenSize();
            zoomFactor = (float) screenSize.y / screenSize.x;
        } else {
            zoomFactor = 1;
        }

        mXLabels.setPosition(XLabels.XLabelPosition.BOTTOM);

        mYLabels.setLabelCount(3);

        robotoTypeface = Typeface.createFromAsset(getResources().getAssets(), "fonts/Roboto-Bold" +
                ".ttf");
        mXLabelPaint.setTypeface(robotoTypeface);
        mXLabelPaint.setTextSize(mXLabels.getTextSize());
        mXLabelPaint.setColor(Color.WHITE);
        mXLabelPaint.setTextAlign(Paint.Align.CENTER);

        mYLabelPaint.setTypeface(robotoTypeface);
        mYLabelPaint.setTextSize(mYLabels.getTextSize());
        mYLabelPaint.setColor(Color.WHITE);
        mYLabelPaint.setTextAlign(Paint.Align.CENTER);

        mHeaderPaint.setTextSize(Utils.convertDpToPixel(14));
        mHeaderPaint.setColor(Color.WHITE);

        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(1);
        mLinePaint.setColor(Color.WHITE);

        mHighLightPaint.setColor(Color.argb(100, 255, 255, 255));

        computeYHeaderTextHeight();

        monthMaxHeight = Utils.calcTextHeight(mXLabelPaint, "Ag");
        iconSize = Utils.calcTextWidth(mXLabelPaint, "Aaa");
        yLabelGuideSize = Utils.calcTextWidth(mYLabelPaint, "WWW");
        highlightWidth = Utils.calcTextWidth(mYLabelPaint, "Www");
        forecastWidth = Utils.calcTextWidth(mHeaderPaint, forecast);
        yOffset = -monthMaxHeight / 2.5f;

        setBackgroundColor(Color.argb(0, 0, 0, 0));
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setZoomOnOrientationChanged(newConfig);
    }

    @Override
    public void notifyDataSetChanged() {
        prepare();
        mTrans.prepareMatrixValuePx(this);
    }

    @Override
    protected void calculateOffsets() {
        mOffsetBottom = CHART_MIN_OFFSET + 1.5f * OFFSET + monthMaxHeight;
        mOffsetTop = CHART_MIN_OFFSET + OFFSET + getYPositionHeaderLine();
        mOffsetLeft = CHART_MIN_OFFSET + OFFSET + 3 / 2f * yLabelGuideSize;
        mOffsetRight = viewType.equals(DashboardView.WEEK) ? CustomBarChart.UNIT_LENGTH + iconSize *
                2 / 3f + OFFSET : CHART_MIN_OFFSET;

        prepareContentRect();
        prepareMatrix();
    }

    @Override
    protected void calcMinMax(boolean fixedValues) {
        switch (viewType) {
            case DashboardView.WEEK:
                mYChartMin = yAxisMinMax[0][0];
                mYChartMax = yAxisMinMax[0][1];
                break;
            case DashboardView.MONTH:
                mYChartMin = yAxisMinMax[1][0];
                mYChartMax = yAxisMinMax[1][1];
                break;
            case DashboardView.YEAR:
                mYChartMin = yAxisMinMax[2][0];
                mYChartMax = yAxisMinMax[2][1];
                break;
        }

        mDeltaX = mData.getXVals().size() - 1;
        mDeltaY = Math.abs(mYChartMax - mYChartMin);
    }

    protected void prepareYLabels() {
        int labelCount = 3;

        mYLabels.mEntryCount = labelCount;
        mYLabels.mEntries = new float[labelCount];
        mYLabels.mEntries[0] = mYChartMin;
        mYLabels.mEntries[1] = (mYChartMin + mYChartMax) / 2;
        mYLabels.mEntries[2] = mYChartMax;

        for (int i = 0; i < mYLabels.mEntryCount; i++) {
            String text = mYLabels.getFormattedLabel(i);
            int labelWidth = Utils.calcTextWidth(mYLabelPaint, text);
            if (yLabelGuideSize < labelWidth) {
                yLabelGuideSize = labelWidth;
            }
        }
    }

    protected void drawHeader() {
        mDrawCanvas.drawText(title, getXPositionPaddingLeft(), getYPositionHeader(), mHeaderPaint);
        mDrawCanvas.drawText(chartUnit, getXPositionPaddingRight() - chartUnitWidth,
                getYPositionHeader(), mHeaderPaint);

        if (viewType.equals(DashboardView.WEEK) && showForecast) {
            mDrawCanvas.drawText(forecast, getXPositionPaddingRight() - chartUnitWidth - OFFSET -
                    2 * forecastWidth, getYPositionHeader(), mHeaderPaint);
        }

        mDrawCanvas.drawLine(getXPositionPaddingLeft(), getYPositionHeaderLine(),
                getXPositionPaddingRight(), getYPositionHeaderLine(), mLinePaint);
    }

    private Highlight getHighlightByTouchPoint(float x, float y) {
        if (mDataNotSet || mData == null) {
            return null;
        }

        float[] pts = new float[2];
        pts[0] = x;
        pts[1] = y;

        mTrans.pixelsToValue(pts);

        double xTouchVal = pts[0];
        double yTouchVal = pts[1];
        double base = Math.floor(xTouchVal);

        double touchOffset = mDeltaX * 0.025;

        if (xTouchVal < -touchOffset || xTouchVal > mDeltaX + touchOffset) {
            return null;
        }

        if (base < 0) {
            base = 0;
        }
        if (base >= mDeltaX) {
            base = mDeltaX - 1;
        }

        int xIndex = (int) base;

        int dataSetIndex;

        if (xTouchVal - base > 0.5) {
            xIndex = (int) base + 1;
        }

        ArrayList<SelInfo> valsAtIndex = getYValsAtIndex(xIndex);

        dataSetIndex = Utils.getClosestDataSetIndex(valsAtIndex, (float) yTouchVal);

        if (dataSetIndex == -1) {
            return null;
        }

        return new Highlight(xIndex, dataSetIndex);
    }

    private void setZoomOnOrientationChanged(Configuration config) {
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Point screenSize = screenSize();
            zoomFactor = (float) screenSize.y / screenSize.x;
        } else {
            zoomFactor = 1;
        }

        fitScreen();
        zoom(customZoom * zoomFactor, 1, 0, 0);
        centerViewPort(lastEntry.getXIndex(), lastEntry.getVal());
    }

    private Point screenSize() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context
                .WINDOW_SERVICE);
        Point screenSize = new Point();
        windowManager.getDefaultDisplay().getSize(screenSize);
        return screenSize;
    }

    public void setZoom() {
        switch (viewType) {
            case DashboardView.WEEK:
                customZoom = mData.getXValCount() * .15f;
                break;
            case DashboardView.MONTH:
                customZoom = 10;
                break;
            case DashboardView.YEAR:
                customZoom = 1;
                break;
        }

        xModulus = viewType.equals(DashboardView.MONTH) ? 5 : 1;

        onLoad = true;

        fitScreen();
        zoom(customZoom * zoomFactor, 1, 0, 0);
        if (!viewType.equals(DashboardView.YEAR)) {
            lastEntry = getEntry(mData.getXValCount() - 1);
            centerViewPort(lastEntry.getXIndex(), lastEntry.getVal());
        }
    }

    protected void prepareMatrix() {
        mTrans.prepareMatrixValuePx(this);
        mTrans.prepareMatrixOffset(this);
    }

    protected void drawLeftDate(String currDate) {
        if (!isLeftDate) {
            isLeftDate = true;

            String leftDate;

            if (viewType.equals("year")) {
                leftDate = year;
            } else {
                String month = months[LocalDate.parse(currDate).getMonthOfYear() - 1];
                leftDate = month + "\n" + LocalDate.parse(currDate).getYear();
            }

            drawCustomText(leftDate, yLabelGuideSize / 2f, getHeight() - OFFSET, mYLabelPaint,
                    mDrawCanvas);
        }
    }

    protected void drawHighlight(LocalDate localDate, float position, Entry e) {
        if (ChartUtils.isCurrentDate(localDate)) {
            float leftPos = Math.max(mOffsetLeft - LEFT_OFFSET, position - highlightWidth / 2f);
            float rightPos = e.getXIndex() == mData.getXValCount() - 1 ?
                    position + highlightWidth / 2f :
                    Math.min(getWidth() - mOffsetRight, position + highlightWidth / 2f);

            showForecast = rightPos < getWidth() - OFFSET - chartUnitWidth - OFFSET - 2 *
                    forecastWidth;

            mDrawCanvas.drawRect(leftPos, 0, rightPos, getHeight(), mHighLightPaint);
        }
    }

    protected void drawCustomText(String str, float x, float y, Paint paint, Canvas canvas) {
        String[] lines = str.split("\n");

        if (lines.length > 1) {
            for (int i = 0; i < lines.length; i++) {
                canvas.drawText(lines[i], x, y + (i - 1) * monthMaxHeight, paint);
            }
        } else {
            canvas.drawText(str, x, y - monthMaxHeight / 2, paint);
        }
    }

    public Entry getEntryByTouchPoint(float x, float y) {
        Highlight h = getHighlightByTouchPoint(x, y);
        if (h != null) {
            return mData.getEntryForHighlight(h);
        }
        return null;
    }

    public void zoom(float scaleX, float scaleY, float x, float y) {
        Matrix save = mTrans.zoom(scaleX, scaleY, x, -y);
        mTrans.refresh(save, this);
    }

    public void fitScreen() {
        Matrix save = mTrans.fitScreen();
        mTrans.refresh(save, this);
    }

    public synchronized void centerViewPort(final int xIndex, final float yVal) {
        float indicesInView = mDeltaX / mTrans.getScaleX();
        float valsInView = mDeltaY / mTrans.getScaleY();
        float[] pts = new float[]{
                xIndex - indicesInView / 2f, yVal + valsInView / 2f
        };

        mTrans.centerViewPort(pts, this);
    }

    public PointF getPosition(Entry e) {
        if (e == null) {
            return null;
        }

        float[] vals = new float[]{e.getXIndex(), e.getVal()};

        if (this instanceof CustomBarChart) {
            BarDataSet set = (BarDataSet) mData.getDataSetForEntry(e);
            if (set != null) {
                vals[0] += set.getBarSpace() / 2f;
            }
        }
        mTrans.pointValuesToPixel(vals);
        return new PointF(vals[0], vals[1]);
    }

    public float getScaleX() {
        return mTrans.getScaleX();
    }

    public float getScaleY() {
        return mTrans.getScaleY();
    }

    public void setLastEntry(Entry lastEntry) {
        this.lastEntry = lastEntry;
    }

    public void setHeader(String title, String unit) {
        this.title = title;
        this.chartUnit = unit;
        chartUnitWidth = Utils.calcTextWidth(mHeaderPaint, chartUnit);
        computeYHeaderTextHeight();
    }

    private void computeYHeaderTextHeight() {
        yHeaderTextHeight = Utils.calcTextHeight(mHeaderPaint, "Wj") > Utils.calcTextHeight
                (mHeaderPaint, chartUnit) ?
                Utils.calcTextHeight(mHeaderPaint, "Wj") :
                Utils.calcTextHeight(mHeaderPaint, chartUnit);
    }

    public void setData(String viewType) {
        this.viewType = viewType;
        T specificData = data.get(viewType.equals(DashboardView.WEEK) ? 0 : viewType.equals
                (DashboardView.MONTH) ? 1 : 2);
        setData(specificData);
        setZoom();
    }

    public void setOnChartClickListener(OnChartClickListener listener) {
        this.mChartClickListener = listener;
    }

    public OnChartClickListener getChartClickListener() {
        return mChartClickListener;
    }

    protected float getYPositionHeader() {
        return yHeaderTextHeight;
    }

    protected float getYPositionHeaderLine() {
        return getYPositionHeader() + OFFSET;
    }

    protected float getXPositionPaddingLeft() {
        return OFFSET;
    }

    protected float getXPositionPaddingRight() {
        return getWidth() - OFFSET;
    }
}
