package com.rainmachine.presentation.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

import com.rainmachine.R;

import timber.log.Timber;

public class SeekBarWithIndicator extends AppCompatSeekBar {

    private static final int TEXT_MARGIN = 6;

    private int mPaddingLeft;
    private int mSeekWidth;
    private int mHeightText;

    private Paint mTextPaint;

    public SeekBarWithIndicator(Context context) {
        super(context);
        init();
    }

    public SeekBarWithIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SeekBarWithIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Align.LEFT);
        mTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.main));
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_large));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPaddingLeft = getPaddingLeft();
        mSeekWidth = w - mPaddingLeft - getPaddingRight();

        String testText = "100%";
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(testText, 0, testText.length(), bounds);
        mHeightText = bounds.height();

        int paddingTop = getPaddingTop();
        float textSize = mTextPaint.getTextSize();
        Timber.d("text height %d vs padding top %d", mHeightText, paddingTop);
        while (mHeightText > paddingTop) {
            textSize -= 1;
            mTextPaint.setTextSize(textSize);
            mTextPaint.getTextBounds(testText, 0, testText.length(), bounds);
            mHeightText = bounds.height();
        }
    }

    /*@Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = getMeasuredWidth();

        String testText = "100%";
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(testText, 0, testText.length(), bounds);
        mHeightText = bounds.height();
        int h = getMeasuredHeight() + mHeightText;

        setMeasuredDimension(w, h);
    }*/

    @Override
    protected synchronized void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        int progress = getProgress();
        int maxProgress = getMax();
        double percentProgress = (double) progress / (double) maxProgress;
        String overlayText = ((int) (percentProgress * 100)) + "%";
        float textWidth = mTextPaint.measureText(overlayText);

        int middleOfThumbControl = (int) (mPaddingLeft + (double) mSeekWidth * percentProgress);

        int x = middleOfThumbControl - (int) (textWidth / 2);
        // Correct a bit the x position
        if (progress <= maxProgress / 2) {
            x += TEXT_MARGIN;
        }

        canvas.drawText(overlayText, x, mHeightText, mTextPaint);
        canvas.restore();
    }
}
