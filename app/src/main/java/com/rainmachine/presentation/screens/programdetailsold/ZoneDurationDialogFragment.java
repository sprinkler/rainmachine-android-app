package com.rainmachine.presentation.screens.programdetailsold;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import net.simonvt.numberpicker.NumberPicker;

import org.joda.time.DateTimeConstants;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ZoneDurationDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.number_picker_minutes)
    NumberPicker numberPickerMinutes;
    @Nullable
    @BindView(R.id.number_picker_seconds)
    NumberPicker numberPickerSeconds;

    public interface Callback {
        void onDialogZoneDurationPositiveClick(long zoneId, int duration);

        void onDialogZoneDurationCancel(long zoneId, int duration);
    }

    public static ZoneDurationDialogFragment newInstance(long zoneId, String zoneName, int
            zoneDuration, boolean useMinutesSeconds) {
        ZoneDurationDialogFragment fragment = new ZoneDurationDialogFragment();
        Bundle args = new Bundle();
        args.putLong("id", zoneId);
        args.putString("name", zoneName);
        args.putInt("duration", zoneDuration);
        args.putBoolean("useMinutesSeconds", useMinutesSeconds);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final boolean useMinutesSeconds = getArguments().getBoolean("useMinutesSeconds");
        builder.setTitle(useMinutesSeconds ? R.string
                .program_details_set_duration_minutes_seconds : R.string
                .program_details_set_duration);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(useMinutesSeconds ? R.layout.dialog_zone_duration : R.layout
                .dialog_zone_duration3, null, false);
        ButterKnife.bind(this, view);
        int duration = getArguments().getInt("duration");
        if (useMinutesSeconds) {
            String[] displayedValuesMinutes = getDisplayedValuesMinutes();
            numberPickerMinutes.setMinValue(0);
            numberPickerMinutes.setMaxValue(displayedValuesMinutes.length - 1);
            int minutes = duration / DateTimeConstants.SECONDS_PER_MINUTE;
            // We may have a duration larger than the maximum minutes (1440) and I do not know
            // how to show this
            numberPickerMinutes.setValue(minutes <= (displayedValuesMinutes.length - 1) ?
                    minutes : 0);
            numberPickerMinutes.setDisplayedValues(displayedValuesMinutes);
            numberPickerSeconds.setValue(duration % DateTimeConstants.SECONDS_PER_MINUTE);
            numberPickerSeconds.setDisplayedValues(getDisplayedValuesSeconds());
        } else {
            numberPickerMinutes.setValue(duration / DateTimeConstants.SECONDS_PER_MINUTE);
        }
        builder.setView(view);

        builder.setPositiveButton(R.string.all_ok, (dialog, id) -> {
            numberPickerMinutes.clearFocus();
            int minutes = numberPickerMinutes.getValue();
            int seconds = 0;
            if (useMinutesSeconds) {
                numberPickerSeconds.clearFocus();
                seconds = numberPickerSeconds.getValue();
            }
            int duration1 = minutes * DateTimeConstants.SECONDS_PER_MINUTE + seconds;
            long zoneId = getArguments().getLong("id");
            callback.onDialogZoneDurationPositiveClick(zoneId, duration1);
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        long zoneId = getArguments().getLong("id");
        int duration = getArguments().getInt("duration");
        callback.onDialogZoneDurationCancel(zoneId, duration);
    }

    private String[] getDisplayedValuesMinutes() {
        final int NUM_VALUES = 1440;
        String[] values = new String[NUM_VALUES];
        for (int i = 0; i < NUM_VALUES; i++) {
            values[i] = i < 10 ? "0" + i : "" + i;
        }
        return values;
    }

    private String[] getDisplayedValuesSeconds() {
        final int NUM_VALUES = 60;
        String[] values = new String[NUM_VALUES];
        for (int i = 0; i < NUM_VALUES; i++) {
            values[i] = i < 10 ? "0" + i : "" + i;
        }
        return values;
    }
}
