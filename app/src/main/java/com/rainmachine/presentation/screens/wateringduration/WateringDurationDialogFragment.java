package com.rainmachine.presentation.screens.wateringduration;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.BaseDialogFragment;

import net.simonvt.numberpicker.NumberPicker;

import org.joda.time.DateTimeConstants;
import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WateringDurationDialogFragment extends BaseDialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.number_picker_minutes)
    NumberPicker numberPickerMinutes;
    @BindView(R.id.number_picker_seconds)
    NumberPicker numberPickerSeconds;

    public interface Callback {
        void onDialogWateringDurationPositiveClick(ZoneViewModel zone);
    }

    public static WateringDurationDialogFragment newInstance(ZoneViewModel zone) {
        WateringDurationDialogFragment fragment = new WateringDurationDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("extra", Parcels.wrap(zone));
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

        builder.setTitle(R.string
                .program_details_set_duration_minutes_seconds);
        View view = View.inflate(getContext(), R.layout.dialog_zone_duration, null);
        ButterKnife.bind(this, view);
        ZoneViewModel zone = getParcelable("extra");
        String[] displayedValuesMinutes = getDisplayedValuesMinutes();
        numberPickerMinutes.setMinValue(0);
        numberPickerMinutes.setMaxValue(displayedValuesMinutes.length - 1);
        int minutes = (int) zone.durationSeconds / DateTimeConstants.SECONDS_PER_MINUTE;
        // We may have a duration larger than the maximum minutes (1440) and I do not know
        // how to show this
        numberPickerMinutes.setValue(minutes <= (displayedValuesMinutes.length - 1) ?
                minutes : 0);
        numberPickerMinutes.setDisplayedValues(displayedValuesMinutes);
        numberPickerSeconds.setValue((int) zone.durationSeconds % DateTimeConstants
                .SECONDS_PER_MINUTE);
        numberPickerSeconds.setDisplayedValues(getDisplayedValuesSeconds());
        builder.setView(view);

        builder.setPositiveButton(R.string.all_ok, (dialog, id) -> {
            numberPickerMinutes.clearFocus();
            int minutesSelected = numberPickerMinutes.getValue();
            numberPickerSeconds.clearFocus();
            int seconds = numberPickerSeconds.getValue();
            zone.durationSeconds = minutesSelected * DateTimeConstants.SECONDS_PER_MINUTE + seconds;
            callback.onDialogWateringDurationPositiveClick(zone);
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return dialog;
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
