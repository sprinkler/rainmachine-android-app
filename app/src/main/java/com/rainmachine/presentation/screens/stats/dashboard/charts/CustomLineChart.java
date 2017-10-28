package com.rainmachine.presentation.screens.stats.dashboard.charts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.rainmachine.R;
import com.rainmachine.presentation.screens.stats.dashboard.DashboardView;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Tremend Software on 2/4/2015.
 */
public class CustomLineChart extends CustomBarLineChart<LineData> {

    private final float CIRCLE_SIZE = Utils.convertDpToPixel(3f);

    private Paint mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // For drawing the second line in the chart if it exists
    private Paint mRenderPaint2;
    private Paint mInnerCirclePaint2;
    private Paint mOuterCirclePaint2;

    public CustomLineChart(Context context) {
        super(context);

        mInnerCirclePaint.setStyle(Paint.Style.FILL);
        mInnerCirclePaint.setColor(Color.rgb(51, 153, 204));

        mOuterCirclePaint.setStyle(Paint.Style.STROKE);
        mOuterCirclePaint.setStrokeWidth(1);
        mOuterCirclePaint.setColor(Color.WHITE);
        mOuterCirclePaint.setStyle(Paint.Style.FILL);

        mRenderPaint.setStyle(Paint.Style.STROKE);
        mRenderPaint.setStrokeWidth(1);
        mRenderPaint.setColor(Color.WHITE);
        mRenderPaint2 = new Paint(mRenderPaint);
        mRenderPaint2.setColor(ContextCompat.getColor(context, R.color.gray_light));

        mInnerCirclePaint2 = new Paint(mInnerCirclePaint);

        mOuterCirclePaint2 = new Paint(mOuterCirclePaint);
        mOuterCirclePaint2.setColor(ContextCompat.getColor(context, R.color.gray_light));
    }

    @Override
    protected void drawData() {
        Path line1 = generateLinePath(entries1);
        mTrans.pathValueToPixel(line1);
        mDrawCanvas.drawPath(line1, mRenderPaint);

        if (entries2.size() > 0) {
            Path line2 = generateLinePath(entries2);
            mTrans.pathValueToPixel(line2);
            mDrawCanvas.drawPath(line2, mRenderPaint2);
        }
    }

    @Override
    protected void drawHighlights() {
    }

    @Override
    protected void drawValues() {
    }

    @Override
    protected void drawAdditional() {
    }

    private Path generateLinePath(ArrayList<Entry> entries) {
        final Path line = new Path();
        boolean initLine = false;

        for (Entry e : entries) {
            if (((CustomEntry) e).isValidEntry) {
                if (!initLine) {
                    line.moveTo(e.getXIndex(), e.getVal());
                    initLine = true;
                } else {
                    line.lineTo(e.getXIndex(), e.getVal());
                }
            } else {
                initLine = false;
            }
        }

        return line;
    }

    @Override
    protected void drawCustomData() {
        LocalDate prevDate = null;

        isLeftDate = false;

        float circleSizeRef = 1.3f;
        for (Entry e : entries1) {

            final float[] position = {e.getXIndex(), e.getVal()};

            mTrans.pointValuesToPixel(position);

            if (position[0] >= mOffsetLeft - LEFT_OFFSET && position[0] <= getWidth() -
                    mOffsetRight + RIGHT_OFFSET_EPS) {

                if (!viewType.equals("year") && position[0] >= mOffsetLeft && ((CustomEntry) e)
                        .isValidEntry) {
                    mDrawCanvas.drawCircle(position[0], position[1], CIRCLE_SIZE * (circleSizeRef
                            == 2.f ? .7f : 1), mOuterCirclePaint);
                    mDrawCanvas.drawCircle(position[0], position[1], CIRCLE_SIZE / circleSizeRef,
                            mInnerCirclePaint);
                }

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
                        }
                    }

                    drawLeftDate(currDate);
                    drawCustomText(label, position[0], getHeight() - OFFSET, mXLabelPaint,
                            mDrawCanvas);
                }
            }
        }

        if (entries2.size() > 0) {
            for (Entry e : entries2) {
                final float[] position = {e.getXIndex(), e.getVal()};
                mTrans.pointValuesToPixel(position);

                if (position[0] >= mOffsetLeft - LEFT_OFFSET && position[0] <= getWidth() -
                        mOffsetRight + RIGHT_OFFSET_EPS) {
                    if (!viewType.equals("year") && position[0] >= mOffsetLeft && ((CustomEntry)
                            e).isValidEntry) {
                        mDrawCanvas.drawCircle(position[0], position[1], CIRCLE_SIZE *
                                (circleSizeRef == 2.f ? .7f : 1), mOuterCirclePaint2);
                        mDrawCanvas.drawCircle(position[0], position[1], CIRCLE_SIZE /
                                circleSizeRef, mInnerCirclePaint2);
                    }
                }
            }
        }
    }

    public void setChartData(String viewType, ArrayList<LocalDate> xValues, ArrayList<Float>
            yValues1, ArrayList<Float> yValues2, float yMin, float yMax) {
        String firstYear = "" + xValues.get(0).getYear();
        String lastYear = "" + xValues.get(xValues.size() - 1).getYear();

        year = firstYear + (firstYear.equals(lastYear) ? "" : "\n" + lastYear);

        ArrayList<Entry> entries1 = new ArrayList<>();
        for (Float f : yValues1) {
            entries1.add(new CustomEntry(f, entries1.size()));
        }

        ArrayList<Entry> entries2 = new ArrayList<>();
        for (Float f : yValues2) {
            entries2.add(new CustomEntry(f, entries2.size()));
        }

        ArrayList<String> expectedXValues = new ArrayList<>();
        for (int i = 0; i < xValues.size(); i++) {
            expectedXValues.add(xValues.get(i).toString());
        }

        LineDataSet set1 = new LineDataSet(entries1, "LineDataSet1");
        LineDataSet set2 = new LineDataSet(entries2, "LineDataSet2");

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        dataSets.add(set2);

        int i = viewType.equals(DashboardView.WEEK) ? 0 : viewType.equals(DashboardView.MONTH) ?
                1 : 2;

        data.put(i, new LineData(expectedXValues, dataSets));

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

    private class CustomEntry extends Entry {

        private boolean isValidEntry = true;

        public CustomEntry(float val, int xIndex) {
            super(val, xIndex);

            if (val == Integer.MIN_VALUE) {
                setVal(0);
                isValidEntry = false;
            }
        }
    }
}