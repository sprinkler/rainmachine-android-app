package com.rainmachine.presentation.screens.stats.dashboard.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;

import com.github.mikephil.charting.data.BarLineScatterCandleData;
import com.github.mikephil.charting.data.BarLineScatterCandleRadarDataSet;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.LimitLine;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

public abstract class CustomBarLineChart<T extends BarLineScatterCandleData<? extends
        BarLineScatterCandleRadarDataSet<? extends Entry>>> extends CustomChart<T> {

    private Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CustomBarLineChart(Context context) {
        super(context);

        mGridPaint.setColor(Color.argb(100, 255, 255, 255));
        mGridPaint.setStrokeWidth(1f);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
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

        // Some charts may have an extra data set to be drawn
        if (mData.getDataSets().size() == 2) {
            dataSet = mData.getDataSets().get(1);
            entries2 = dataSet.getYVals();
        }

        prepareYLabels();

        int clipRestoreCount = mDrawCanvas.save();
        mDrawCanvas.clipRect(mContentRect);

        drawData();
        drawLimitLines();

        mDrawCanvas.restoreToCount(clipRestoreCount);

        drawCustomData();
        drawHeader();
        drawYLabels();
        drawCustomHorizontalGrid();

        canvas.drawBitmap(mDrawBitmap, 0, 0, mDrawPaint);

        if (onLoad) {
            onLoad = false;
            mTrans.refresh(mTrans.getTouchMatrix(), this);
        }
    }

    @Override
    public void prepare() {
        if (mDataNotSet) {
            return;
        }
        calcMinMax(false);
        prepareYLabels();
        calculateOffsets();
    }

    protected void drawCustomData() {
    }

    private void drawYLabels() {
        float[] positions = new float[mYLabels.mEntryCount * 2];

        for (int i = 0; i < positions.length; i += 2) {
            positions[i + 1] = mYLabels.mEntries[i / 2];
        }

        mTrans.pointValuesToPixel(positions);

        for (int i = 0; i < mYLabels.mEntryCount; i++) {
            String text = mYLabels.mEntries[i] == (long) mYLabels.mEntries[i]
                    ? String.format(Locale.ENGLISH, "%d", (long) mYLabels.mEntries[i])
                    : String.format(Locale.ENGLISH, "%.1f", mYLabels.mEntries[i]);

            mDrawCanvas.drawText(text, yLabelGuideSize / 2f, positions[i * 2 + 1] + yOffset,
                    mYLabelPaint);
        }
    }

    private void drawCustomHorizontalGrid() {
        float[] position = new float[2];
        for (int i = 0; i < mYLabels.mEntryCount; i++) {
            position[1] = mYLabels.mEntries[i];
            mTrans.pointValuesToPixel(position);
            mDrawCanvas.drawLine(0, position[1], getWidth(), position[1], mGridPaint);
        }
    }

    private void drawLimitLines() {
        ArrayList<LimitLine> limitLines = mData.getLimitLines();
        if (limitLines == null) {
            return;
        }

        float[] pts = new float[4];
        for (int i = 0; i < limitLines.size(); i++) {

            LimitLine l = limitLines.get(i);

            pts[1] = l.getLimit();
            pts[3] = l.getLimit();

            mTrans.pointValuesToPixel(pts);

            pts[0] = 0;
            pts[2] = getWidth();

            mLimitLinePaint.setColor(l.getLineColor());
            mLimitLinePaint.setPathEffect(l.getDashPathEffect());
            mLimitLinePaint.setStrokeWidth(l.getLineWidth());

            mDrawCanvas.drawLines(pts, mLimitLinePaint);

            if (l.isDrawValueEnabled()) {

                PointF pos = getPosition(new Entry(l.getLimit(), 0));

                Paint.Align align = mValuePaint.getTextAlign();

                float xOffset = Utils.convertDpToPixel(4f);
                float yOffset = l.getLineWidth() + xOffset;
                String label = mValueFormatter.getFormattedValue(l.getLimit());

                if (mDrawUnitInChart) {
                    label += mUnit;
                }

                if (l.getLabelPosition() == LimitLine.LimitLabelPosition.RIGHT) {

                    mValuePaint.setTextAlign(Paint.Align.RIGHT);
                    mDrawCanvas.drawText(label, getWidth() - mOffsetRight
                                    - xOffset,
                            pos.y - yOffset, mValuePaint);
                } else {
                    mValuePaint.setTextAlign(Paint.Align.LEFT);
                    mDrawCanvas.drawText(label, mOffsetLeft
                                    + xOffset,
                            pos.y - yOffset, mValuePaint);
                }

                mValuePaint.setTextAlign(align);
            }
        }
    }
}
