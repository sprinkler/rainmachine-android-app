package com.rainmachine.presentation.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.rainmachine.R;

import java.math.BigDecimal;

public class WaterPercentageView extends View {

    /* Visible attributes */
    private double percentage;
    private int waterColorFill;
    private int waterColorStroke;
    private int textColor;
    private int textSize;

    private Paint waterPaintFill;
    private Paint waterPaintStroke;
    private Paint textPaint;
    private String textPercentage;
    private float xText;
    private float yText;
    private Path wavyPath;
    private static final int STROKE_WIDTH = 2;

    public WaterPercentageView(Context context) {
        super(context);
        init(context, null);
    }

    public WaterPercentageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WaterPercentageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.WaterPercentageView, 0, 0);
            try {
                float percentage = a.getFloat(R.styleable.WaterPercentageView_percentage, 0.0f);
                setPercentage(percentage);
                waterColorFill = a.getColor(R.styleable.WaterPercentageView_waterColorFill, 0);
                waterColorStroke = a.getColor(R.styleable.WaterPercentageView_waterColorStroke, 0);
                textColor = a.getColor(R.styleable.WaterPercentageView_textColor, 0);
                textSize = a.getDimensionPixelOffset(R.styleable.WaterPercentageView_textSize, 0);
            } finally {
                a.recycle();
            }
        }

        waterPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        waterPaintFill.setStyle(Paint.Style.FILL);
        waterPaintFill.setColor(waterColorFill);
        waterPaintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        waterPaintStroke.setStyle(Paint.Style.STROKE);
        waterPaintStroke.setStrokeWidth(dpToPx(STROKE_WIDTH));
        waterPaintStroke.setColor(waterColorStroke);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(textSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (percentage > 0) {
            canvas.drawPath(wavyPath, waterPaintFill);
            canvas.drawPath(wavyPath, waterPaintStroke);
        }
        canvas.drawText(textPercentage, xText, yText, textPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        updateData();
    }

    private void updateData() {
        int padLeft = getPaddingLeft();
        int padRight = getPaddingRight();
        int xPad = padLeft + padRight;

        int padTop = getPaddingTop();
        int padBottom = getPaddingBottom();
        int yPad = padTop + padBottom;

        int effectiveWidth = getWidth() - xPad;
        int effectiveHeight = getHeight() - yPad;

        xText = padLeft + effectiveWidth / 2;
        yText = padTop + effectiveHeight / 2 - ((textPaint.descent() + textPaint.ascent() / 2));
        float percent = 1f / 10;
        float segment = percent * effectiveHeight;
        float waveOffsetX = segment / 2;
        float fillX = padLeft + (float) (effectiveWidth * percentage) - waveOffsetX;
        wavyPath = new Path();
        wavyPath.moveTo(fillX, padTop);
        wavyPath.lineTo(padLeft, padTop);
        wavyPath.lineTo(padLeft, getHeight() - padBottom);
        wavyPath.lineTo(fillX, getHeight() - padBottom);
        wavyPath.quadTo(fillX - waveOffsetX, padTop + 9 * segment, fillX, padTop + 8 * segment);
        wavyPath.quadTo(fillX + waveOffsetX, padTop + 7 * segment, fillX, padTop + 6 * segment);
        wavyPath.quadTo(fillX - waveOffsetX, padTop + 5 * segment, fillX, padTop + 4 * segment);
        wavyPath.quadTo(fillX + waveOffsetX, padTop + 3 * segment, fillX, padTop + 2 * segment);
        wavyPath.quadTo(fillX - waveOffsetX, padTop + 1 * segment, fillX, padTop);
    }

    public void setPercentage(double percentage) {
        percentage = round(percentage, 2);
        if (percentage < 0) {
            this.percentage = 0;
            textPercentage = (int) (this.percentage * 100) + "%";
        } else if (percentage > 1) {
            this.percentage = 1;
            textPercentage = (int) (percentage * 100) + "%";
        } else {
            this.percentage = percentage;
            textPercentage = (int) (percentage * 100) + "%";
        }
        invalidate();
    }

    public void setWaterColorFill(int color) {
        waterColorFill = color;
        waterPaintFill.setColor(waterColorFill);
        invalidate();
    }

    public void setWaterColorStroke(int color) {
        waterColorStroke = color;
        waterPaintStroke.setColor(waterColorStroke);
        invalidate();
    }

    public void setTextColor(int color) {
        textColor = color;
        textPaint.setColor(textColor);
        invalidate();
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        textPaint.setTextSize(textSize);
        updateData();
        invalidate();
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (dp * displayMetrics.density + 0.5f);
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
}
