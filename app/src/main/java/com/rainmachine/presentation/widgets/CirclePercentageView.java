package com.rainmachine.presentation.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.rainmachine.R;
import com.rainmachine.presentation.util.ViewUtils;

import java.math.BigDecimal;

public class CirclePercentageView extends View {

    private static final int STROKE_WIDTH_CIRCLE = 12; // dp
    private static final int TEXT_SIZE = 16; // dp
    private static final int ANIMATION_DURATION = 3000; // ms

    private int width;
    private int height;
    private int radius;
    private Paint textPaint;
    private Paint circlePaint;
    private Paint circleFillPaint;
    private String textPercentage;
    private float xText;
    private float yText;
    private float cx;
    private float cy;
    private RectF circleRect;
    private float sweepAngleToDraw;
    private ValueAnimator animator;

    public CirclePercentageView(Context context) {
        super(context);
        init();
    }

    public CirclePercentageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CirclePercentageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(ViewUtils.dpToPixels(TEXT_SIZE, getContext()));
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(ContextCompat.getColor(getContext(), R.color.bg_circle_percentage));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(ViewUtils.dpToPixels(STROKE_WIDTH_CIRCLE, getContext()));
        circleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleFillPaint.setColor(Color.WHITE);
        circleFillPaint.setStyle(Paint.Style.STROKE);
        circleFillPaint.setStrokeWidth(ViewUtils.dpToPixels(STROKE_WIDTH_CIRCLE - 4,
                getContext()));
        circleFillPaint.setStrokeCap(Paint.Cap.ROUND);
        setPercentage(0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        updateData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(cx, cy, radius, circlePaint);
        canvas.drawArc(circleRect, 270, sweepAngleToDraw, false, circleFillPaint);
        canvas.drawText(textPercentage, xText, yText, textPaint);
    }

    private void updateData() {
        int padLeft = getPaddingLeft();
        int padRight = getPaddingRight();
        int xPad = padLeft + padRight;

        int padTop = getPaddingTop();
        int padBottom = getPaddingBottom();
        int yPad = padTop + padBottom;

        int effectiveWidth = width - xPad;
        int effectiveHeight = height - yPad;

        xText = padLeft + effectiveWidth / 2;
        yText = padTop + effectiveHeight / 2 - ((textPaint.descent() + textPaint.ascent() / 2));
        cx = padLeft + effectiveWidth / 2;
        cy = padTop + effectiveHeight / 2;
        if (effectiveWidth < effectiveHeight) {
            radius = effectiveWidth / 2;
        } else {
            radius = effectiveHeight / 2;
        }
        radius = radius - (int) (circlePaint.getStrokeWidth() / 2);

        circleRect = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
    }

    public void setPercentage(float percentage) {
        percentage = round(percentage, 2);
        float cleanPercentage;
        if (percentage < 0) {
            cleanPercentage = 0;
            textPercentage = (int) (cleanPercentage * 100) + "%";
        } else if (percentage > 1) {
            cleanPercentage = 1;
            textPercentage = (int) (cleanPercentage * 100) + "%";
        } else {
            cleanPercentage = percentage;
            textPercentage = (int) (cleanPercentage * 100) + "%";
        }
        float sweepAngle = cleanPercentage * 360;

        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0, sweepAngle);
        long duration = (long) (ANIMATION_DURATION * (sweepAngle / 360f));
        animator.setDuration(duration);
        animator.addUpdateListener(valueAnimator -> {
            sweepAngleToDraw = (float) valueAnimator.getAnimatedValue();
            CirclePercentageView.this.invalidate();
        });
        animator.start();
    }

    private static float round(float value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
