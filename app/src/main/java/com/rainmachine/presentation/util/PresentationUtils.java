package com.rainmachine.presentation.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.rainmachine.infrastructure.util.BaseApplication;

public class PresentationUtils {

    public static void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) BaseApplication.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) BaseApplication.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view.requestFocus()) {
            imm.showSoftInput(view, 0);
        }
    }
}
