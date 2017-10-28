package com.rainmachine.presentation.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

    private int offsetLeft;
    private int offsetTop;
    private int offsetRight;
    private int offsetBottom;

    public ItemOffsetDecoration(int offset) {
        this.offsetLeft = offset;
        this.offsetTop = offset;
        this.offsetRight = offset;
        this.offsetBottom = offset;
    }

    public ItemOffsetDecoration(int left, int top, int right, int bottom) {
        offsetLeft = left;
        offsetTop = top;
        offsetRight = right;
        offsetBottom = bottom;
    }

    public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
        this(context.getResources().getDimensionPixelSize(itemOffsetId));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(offsetLeft, offsetTop, offsetRight, offsetBottom);
    }
}