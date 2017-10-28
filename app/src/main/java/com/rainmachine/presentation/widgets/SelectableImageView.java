package com.rainmachine.presentation.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.rainmachine.R;

public class SelectableImageView extends AppCompatImageView {

    private int cx;
    private int cy;
    private int radius;
    private Paint paint;
    private boolean drawCircle;

    public SelectableImageView(Context context) {
        super(context);
        init();
    }

    public SelectableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.green));
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        int[] states = getDrawableState();
        boolean selected = false;
        for (int state : states) {
            if (state == android.R.attr.state_selected) {
                selected = true;
                break;
            }
            if (state == android.R.attr.state_pressed) {
                selected = true;
                break;
            }
            if (state == android.R.attr.state_checked) {
                selected = true;
                break;
            }
        }

        drawCircle = selected;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cx = w / 2;
        cy = h / 2;
        int stroke = getPaddingLeft();
        radius = w / 2 - stroke / 2;
        paint.setStrokeWidth(stroke);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawCircle) {
            canvas.drawCircle(cx, cy, radius, paint);
        }
    }
}
