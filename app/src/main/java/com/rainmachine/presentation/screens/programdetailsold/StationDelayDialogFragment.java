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

public class StationDelayDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.number_picker_minutes)
    NumberPicker numberPickerMinutes;
    @Nullable
    @BindView(R.id.number_picker_seconds)
    NumberPicker numberPickerSeconds;

    public interface Callback {
        void onDialogStationDelayPositiveClick(int duration);

        void onDialogStationDelayCancel();
    }

    public static StationDelayDialogFragment newInstance(int stationDelay /*in seconds*/,
                                                         boolean useMinutesSeconds) {
        StationDelayDialogFragment fragment = new StationDelayDialogFragment();
        Bundle args = new Bundle();
        args.putInt("stationDelay", stationDelay);
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
        final boolean useMinutesSeconds = getArguments().getBoolean("useMinutesSeconds");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(useMinutesSeconds ? R.string
                .program_details_set_duration_minutes_seconds : R.string
                .program_details_set_duration_minutes);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(useMinutesSeconds ? R.layout.dialog_station_delay : R.layout
                .dialog_station_delay3, null, false);
        ButterKnife.bind(this, view);
        int stationDelay = getArguments().getInt("stationDelay");
        if (useMinutesSeconds) {
            String[] displayedValues = getDisplayedValues();
            numberPickerMinutes.setMinValue(0);
            numberPickerMinutes.setMaxValue(displayedValues.length - 1);
            int minutes = stationDelay / DateTimeConstants.SECONDS_PER_MINUTE;
            // We may have a duration larger than 59 minutes and I do not know how to show this
            numberPickerMinutes.setValue(minutes <= (displayedValues.length - 1) ? minutes : 0);
            numberPickerMinutes.setDisplayedValues(getDisplayedValues());
            numberPickerSeconds.setValue(stationDelay % DateTimeConstants.SECONDS_PER_MINUTE);
            numberPickerSeconds.setDisplayedValues(getDisplayedValues());
        } else {
            int minutes = stationDelay / DateTimeConstants.SECONDS_PER_MINUTE;
            numberPickerMinutes.setValue(minutes);
            numberPickerMinutes.setMinValue(0);
            numberPickerMinutes.setMaxValue(300);
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
            int duration = minutes * DateTimeConstants.SECONDS_PER_MINUTE + seconds;
            callback.onDialogStationDelayPositiveClick(duration);
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogStationDelayCancel();
    }

    private String[] getDisplayedValues() {
        String[] values = new String[60];
        for (int i = 0; i < 60; i++) {
            values[i] = i < 10 ? "0" + i : "" + i;
        }
        return values;
    }
}
