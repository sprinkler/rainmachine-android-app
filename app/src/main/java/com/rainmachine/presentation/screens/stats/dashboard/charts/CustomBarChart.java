package com.rainmachine.presentation.screens.stats.dashboard.charts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.LruCache;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.Utils;
import com.rainmachine.presentation.screens.stats.dashboard.ChartWrapperView;
import com.rainmachine.presentation.screens.stats.dashboard.DashboardView;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Tremend Software on 2/5/2015.
 */
public class CustomBarChart extends CustomBarLineChart<BarData> {

    public static int UNIT_LENGTH = 0;

    private final int MAX_MEMORY = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private final int CACHE_SIZE = MAX_MEMORY / 8;

    private ArrayList<String> textLabels = new ArrayList<>();

    private String unit = "\u2103";

    private LruCache<Integer, Object> iconsBitmaps;

    private RectF mBarRect = new RectF();

    private int initParentHeight = -1;
    private int unitWidth = 0;
    private int unitHeight = 0;

    private boolean hasIcons = false;
    private boolean hasTextLabels = false;

    public CustomBarChart(Context context) {
        super(context);

        Paint mTextLabelsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextLabelsPaint.setColor(Color.BLACK);
        mTextLabelsPaint.setTextAlign(Paint.Align.CENTER);
        mTextLabelsPaint.setTextSize(Utils.convertDpToPixel(10f));
        mTextLabelsPaint.setTypeface(mXLabels.getTypeface());
        mTextLabelsPaint.setTextSize(mXLabels.getTextSize());
        mTextLabelsPaint.setColor(mXLabels.getTextColor());

        mRenderPaint.setColor(Color.WHITE);
    }

    @Override
    public void calculateOffsets() {
        super.calculateOffsets();

        if (viewType.equals(DashboardView.WEEK)) {
            if (hasIcons) {
                mOffsetTop += iconSize + OFFSET;
            }

            if (hasTextLabels) {
                mOffsetTop += unitHeight + (hasIcons ? 2 : 1) * OFFSET;
            }

            prepareContentRect();
            prepareMatrix();
        }
    }

    @Override
    public void setZoom() {
        super.setZoom();
        updateLayoutHeight();
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

    @Override
    protected void drawCustomData() {
        LocalDate prevDate = null;

        boolean isUnit = false;

        isLeftDate = false;

        for (Entry e : entries1) {
            final float[] position = {e.getXIndex(), e.getVal()};

            mTrans.pointValuesToPixel(position);

            if (position[0] >= mOffsetLeft - LEFT_OFFSET && position[0] <= getWidth() -
                    mOffsetRight + RIGHT_OFFSET_EPS) {

                drawBars((CustomBarEntry) e, position);

                if (e.getXIndex() % xModulus == 0) {
                    String label;
                    String currDate;
                    LocalDate localDate;

                    try {
                        currDate = mData.getXVals().get(e.getXIndex());
                        localDate = LocalDate.parse(currDate);
                    } catch (IndexOutOfBoundsException exception) {
                        return;
                    }

                    if (viewType.equals(DashboardView.YEAR)) {
                        if (prevDate == null || localDate.getMonthOfYear() != prevDate
                                .getMonthOfYear()) {
                            prevDate = localDate;

                            if ((e.getXIndex() == 0 && localDate.getDayOfMonth() >= 5)) {
                                continue;
                            }

                            label = months[LocalDate.parse(currDate).getMonthOfYear() - 1];
                        } else {
                            continue;
                        }
                    } else {

                        String dayOfWeek = localDate.dayOfWeek().getAsShortText(Locale.ENGLISH);
                        String dayOfMonth = localDate.toString("dd");
                        label = dayOfMonth + "\n" + dayOfWeek;

                        if (viewType.equals(DashboardView.WEEK)) {
                            drawHighlight(localDate, position[0], e);

                            if (hasIcons) {
                                Object bitmap = iconsBitmaps.get(e.getXIndex());

                                if (bitmap instanceof Bitmap) {
                                    mDrawCanvas.drawBitmap((Bitmap) bitmap, position[0] - (
                                                    (Bitmap) bitmap).getWidth() / 2,
                                            getYPositionHeaderLine() + OFFSET, mXLabelPaint);
                                }
                            }

                            if (hasTextLabels) {
                                if (!isUnit) {
                                    isUnit = true;

                                    mDrawCanvas.drawText(unit,
                                            getWidth() - OFFSET - unitWidth / 2f,
                                            getYPositionHeaderLine() + OFFSET + (hasIcons ?
                                                    iconSize + OFFSET : 0) + unitHeight,
                                            mXLabelPaint);
                                }

                                String textLabel = textLabels.get(e.getXIndex());
                                mDrawCanvas.drawText(textLabel,
                                        position[0],
                                        getYPositionHeaderLine() + OFFSET + (hasIcons ? iconSize
                                                + OFFSET : 0) + unitHeight,
                                        mXLabelPaint);
                            }
                        }
                    }

                    drawLeftDate(currDate);
                    drawCustomText(label, position[0], getHeight() - OFFSET, mXLabelPaint,
                            mDrawCanvas);
                }
            }
        }
    }

    private void drawBars(CustomBarEntry e, float[] position) {
        if (e.isValidEntry) {
            prepareBar(e.getXIndex(), e.getVal());

            float barLength = viewType.equals(DashboardView.WEEK) ? .5f * (iconSize - OFFSET) :
                    (mBarRect.right - mBarRect.left) / 2;
            float leftPos = Math.max(mOffsetLeft - LEFT_OFFSET, position[0] - barLength);
            float rightPos = e.getXIndex() == mData.getXValCount() - 1 ? position[0] + barLength
                    : Math.min(getWidth() - mOffsetRight, position[0] + barLength);

            mBarRect = new RectF(leftPos, mBarRect.top, rightPos, mBarRect.bottom);

            mDrawCanvas.drawRect(mBarRect, mRenderPaint);
        }
    }

    public void setChartData(String viewType, ArrayList<LocalDate> xDatesVals, ArrayList<Float>
            yVals, float yMin, float yMax) {
        String firstYear = "" + xDatesVals.get(0).getYear();
        String lastYear = "" + xDatesVals.get(xDatesVals.size() - 1).getYear();

        year = firstYear + (firstYear.equals(lastYear) ? "" : "\n" + lastYear);

        ArrayList<BarEntry> entries = new ArrayList<>();

        for (Float f : yVals) {
            entries.add(new CustomBarEntry(f, entries.size()));
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

    public void setIcons(ArrayList<Integer> icons, boolean showIcons) {
        iconsBitmaps = new LruCache<Integer, Object>(CACHE_SIZE) {
            @Override
            protected int sizeOf(Integer key, Object bitmap) {
                return (bitmap instanceof Bitmap) ? ((Bitmap) bitmap).getByteCount() / 1024 : 0;
            }
        };

        for (int i : icons) {
            Bitmap initialBitmap = BitmapFactory.decodeResource(getResources(), i);
            Bitmap scaledBitmap = null;
            if (initialBitmap != null) {
                scaledBitmap = Bitmap.createScaledBitmap(initialBitmap, iconSize, iconSize, true);
            }
            iconsBitmaps.put(iconsBitmaps.putCount(), scaledBitmap != null ? scaledBitmap : NO_ID);

            if (initialBitmap != null && initialBitmap != scaledBitmap) {
                initialBitmap.recycle();
            }
        }

        this.hasIcons = showIcons;
    }

    public void setTextLabels(ArrayList<String> textLabels, String unit, boolean showTextLabels) {
        this.unit = unit;

        unitWidth = Utils.calcTextWidth(mXLabelPaint, unit);
        unitHeight = Utils.calcTextHeight(mXLabelPaint, unit);

        if (UNIT_LENGTH < unitWidth) {
            UNIT_LENGTH = unitWidth;
        }

        for (String label : textLabels) {
            this.textLabels.add(label);
        }

        this.hasTextLabels = showTextLabels;
    }

    public void updateLayoutHeight() {
        ChartWrapperView chartWrapperView = (ChartWrapperView) getParent();
        LayoutParams params = chartWrapperView.getLayoutParams();

        if (initParentHeight == -1) {
            initParentHeight = params.height;
        }

        params.height = initParentHeight;

        if (viewType.equals(DashboardView.WEEK)) {
            if (hasIcons) {
                params.height += iconSize + OFFSET;
            }

            if (hasTextLabels) {
                params.height += unitHeight + (hasIcons ? 2 : 1) * OFFSET;
            }
        }
        chartWrapperView.setLayoutParams(params);
    }

    private class CustomBarEntry extends BarEntry {
        private boolean isValidEntry = true;

        public CustomBarEntry(float val, int xIndex) {
            super(val, xIndex);

            if (val == Integer.MIN_VALUE) {
                setVal(0);
                isValidEntry = false;
            }
        }
    }

    private void prepareBar(float x, float y) {
        float spaceHalf = .35f / 2f;
        float left = x + spaceHalf;
        float right = x + 1f - spaceHalf;
        float top = y >= 0 ? y : 0;
        float bottom = y <= 0 ? y : 0;

        mBarRect.set(left, top, right, bottom);
        mTrans.rectValueToPixel(mBarRect, mPhaseY);
    }
}
