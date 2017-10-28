package com.rainmachine.presentation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.DatePicker;

import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

public class DatePickerDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    public interface Callback {
        void onDialogDatePickerPositiveClick(int dialogId, int year, int month, int day);

        void onDialogDatePickerCancel(int dialogId);
    }

    public static DatePickerDialogFragment newInstance(int dialogId, String positiveBtn, int
            year, int month, int day) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("positiveBtn", positiveBtn);
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((SprinklerActivity) getActivity()).inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = getArguments().getInt("year");
        int month = getArguments().getInt("month");
        int day = getArguments().getInt("day");
        String positiveBtn = getArguments().getString("positiveBtn");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final DatePicker datePicker = new DatePicker(getActivity());
        datePicker.init(year, month, day, null);
        setSpinnerViewWherePossible(datePicker);
        builder.setView(datePicker);
        builder.setPositiveButton(positiveBtn,
                (dialogInterface, i) -> {
                    datePicker.clearFocus();
                    callback.onDialogDatePickerPositiveClick(getArguments().getInt
                                    ("dialogId"), datePicker.getYear(), datePicker.getMonth(),
                            datePicker.getDayOfMonth());
                });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogDatePickerCancel(getArguments().getInt("dialogId"));
    }

    @SuppressWarnings("deprecation")
    private void setSpinnerViewWherePossible(DatePicker datePicker) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            datePicker.setCalendarViewShown(false);
        }
    }
}
