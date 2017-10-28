package com.rainmachine.presentation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.TimePicker;

import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

public class TimePickerDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    public interface Callback {
        void onDialogTimePickerPositiveClick(int dialogId, int hourOfDay, int minute);

        void onDialogTimePickerCancel(int dialogId);
    }

    public static TimePickerDialogFragment newInstance(int dialogId, String positiveBtn,
                                                       int hourOfDay, int minute,
                                                       boolean use24HourFormat) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("positiveBtn", positiveBtn);
        args.putInt("hourOfDay", hourOfDay);
        args.putInt("minute", minute);
        args.putBoolean("use24HourFormat", use24HourFormat);
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
        int hour = getArguments().getInt("hourOfDay");
        int minute = getArguments().getInt("minute");
        boolean use24HourFormat = getArguments().getBoolean("use24HourFormat");
        String positiveBtn = getArguments().getString("positiveBtn");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final TimePicker timePicker = new TimePicker(getActivity());
        setHourAndMinute(timePicker, hour, minute);
        timePicker.setIs24HourView(use24HourFormat);
        builder.setView(timePicker);

        builder.setPositiveButton(positiveBtn,
                (dialogInterface, i) -> {
                    timePicker.clearFocus();
                    callback.onDialogTimePickerPositiveClick(getArguments().getInt
                            ("dialogId"), getHour(timePicker), getMinute(timePicker));
                }
        );
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogTimePickerCancel(getArguments().getInt("dialogId"));
    }

    @SuppressWarnings("deprecation")
    private void setHourAndMinute(TimePicker timePicker, int hour, int minute) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
        } else {
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(minute);
        }
    }

    @SuppressWarnings("deprecation")
    private int getHour(TimePicker timePicker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return timePicker.getHour();
        } else {
            return timePicker.getCurrentHour();
        }
    }

    @SuppressWarnings("deprecation")
    private int getMinute(TimePicker timePicker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return timePicker.getMinute();
        } else {
            return timePicker.getCurrentMinute();
        }
    }
}
