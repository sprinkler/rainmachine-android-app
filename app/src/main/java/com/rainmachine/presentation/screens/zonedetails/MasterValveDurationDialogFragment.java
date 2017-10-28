package com.rainmachine.presentation.screens.zonedetails;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import net.simonvt.numberpicker.NumberPicker;

import org.joda.time.DateTimeConstants;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MasterValveDurationDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.number_picker_minutes)
    NumberPicker numberPickerMinutes;
    @BindView(R.id.number_picker_seconds)
    NumberPicker numberPickerSeconds;

    public interface Callback {
        void onDialogMasterValveDurationPositiveClick(int dialogId, int duration);
    }

    public static MasterValveDurationDialogFragment newInstance(int dialogId, int duration,
                                                                String title) {
        MasterValveDurationDialogFragment fragment = new MasterValveDurationDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putInt("duration", duration);
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
        builder.setTitle(getArguments().getString("title"));

        View view = View.inflate(getContext(), R.layout.dialog_master_valve_duration, null);
        ButterKnife.bind(this, view);
        int duration = getArguments().getInt("duration");
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
        builder.setView(view);

        builder.setPositiveButton(R.string.all_ok, (dialog, id) -> {
            numberPickerMinutes.clearFocus();
            int minutes1 = numberPickerMinutes.getValue();
            numberPickerSeconds.clearFocus();
            int seconds = numberPickerSeconds.getValue();
            int duration1 = minutes1 * DateTimeConstants.SECONDS_PER_MINUTE + seconds;
            int dialogId = getArguments().getInt("dialogId");
            callback.onDialogMasterValveDurationPositiveClick(dialogId, duration1);
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // Do nothing special
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
