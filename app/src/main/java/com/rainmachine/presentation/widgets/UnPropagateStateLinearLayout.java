package com.rainmachine.presentation.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class UnPropagateStateLinearLayout extends LinearLayout {

    public UnPropagateStateLinearLayout(Context context) {
        super(context);
    }

    public UnPropagateStateLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnPropagateStateLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        // We do not notify the children that this was pressed in order to not trigger their
        // selectors
    }
}
