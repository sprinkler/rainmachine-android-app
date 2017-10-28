package com.rainmachine.presentation.screens.programdetailsold;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.rainmachine.R;
import com.rainmachine.domain.model.ProgramStartTime;
import com.rainmachine.presentation.activities.SprinklerActivity;

import net.simonvt.numberpicker.NumberPicker;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SunriseSunsetDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.number_picker)
    NumberPicker offsetPicker;
    @BindView(R.id.spinner_after_before)
    Spinner spinnerBeforeAfter;
    @BindView(R.id.spinner_sunrise_sunset)
    Spinner spinnerSunriseSunset;

    public interface Callback {
        void onDialogStartTimeSunriseSunsetPositiveClick(ProgramStartTime programStartTime);

        void onDialogStartTimeSunriseSunsetCancel();
    }

    public static SunriseSunsetDialogFragment newInstance(ProgramStartTime programStartTime) {
        SunriseSunsetDialogFragment fragment = new SunriseSunsetDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("programStartTime", Parcels.wrap(programStartTime));
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
        builder.setTitle(R.string.program_sunrise_sunset_set);

        View view = View.inflate(getContext(), R.layout.dialog_sunrise_sunset, null);
        ButterKnife.bind(this, view);
        setup();
        render(getProgramStartTime());
        builder.setView(view);

        builder.setPositiveButton(R.string.all_ok, (dialog, id) -> {
            ProgramStartTime programStartTime = getProgramStartTime();
            programStartTime.sunPosition = getCurrentStartTimeSunPosition();
            programStartTime.beforeAfter = getCurrentStartTimeBeforeAfter();
            programStartTime.offsetMinutes = getCurrentStartTimeOffsetMinutes();
            callback.onDialogStartTimeSunriseSunsetPositiveClick(programStartTime);
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogStartTimeSunriseSunsetCancel();
    }

    private int getCurrentStartTimeOffsetMinutes() {
        // Clear focus to get the latest value if the user entered a value via keyboard
        offsetPicker.clearFocus();
        return offsetPicker.getValue();
    }

    private ProgramStartTime.StartTimeSunPosition getCurrentStartTimeSunPosition() {
        return spinnerSunriseSunset.getSelectedItemPosition() == 0 ? ProgramStartTime
                .StartTimeSunPosition.SUNRISE : ProgramStartTime.StartTimeSunPosition.SUNSET;
    }

    private ProgramStartTime.StartTimeBeforeAfter getCurrentStartTimeBeforeAfter() {
        return spinnerBeforeAfter.getSelectedItemPosition() == 0 ? ProgramStartTime
                .StartTimeBeforeAfter.BEFORE : ProgramStartTime
                .StartTimeBeforeAfter.AFTER;
    }

    private ProgramStartTime getProgramStartTime() {
        return Parcels.unwrap(getArguments().getParcelable("programStartTime"));
    }

    private void setup() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array
                .program_details_before_after, R.layout.item_spinner_program_sunrise_sunset);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown_program_sunrise_sunset);
        spinnerBeforeAfter.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(getContext(), R.array
                .program_details_sunrise_sunset, R.layout.item_spinner_program_sunrise_sunset);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown_program_sunrise_sunset);
        spinnerSunriseSunset.setAdapter(adapter);

        offsetPicker.setMinValue(0);
        offsetPicker.setMaxValue(999);
    }

    private void render(ProgramStartTime startTime) {
        offsetPicker.setValue(startTime.offsetMinutes);

        int selection = startTime.isBefore() ? 0 : 1;
        spinnerBeforeAfter.setSelection(selection, false);

        selection = startTime.isSunrise() ? 0 : 1;
        spinnerSunriseSunset.setSelection(selection, false);
    }
}
