package com.rainmachine.presentation.widgets;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

public class SpinnerUserSelection implements AdapterView.OnItemSelectedListener, View
        .OnTouchListener {

    private Listener listener;
    private boolean userSelect = false;

    public interface Listener {
        void onUserSelection(int position);
    }

    public SpinnerUserSelection(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        userSelect = true;
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (userSelect) {
            listener.onUserSelection(position);
            userSelect = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }
}
