package com.rainmachine.presentation.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ThinDividerView extends View {

    private float width;
    private float middle;
    private Paint paint;
    private float offsetLeft;
    private float offsetRight;

    public ThinDividerView(Context context) {
        super(context);
        init();
    }

    public ThinDividerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThinDividerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        middle = (float) h / 2;
        updateData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(offsetLeft, middle, width - offsetRight, middle, paint);
    }

    private void updateData() {
        offsetLeft = getPaddingLeft();
        offsetRight = getPaddingRight();
    }
}
