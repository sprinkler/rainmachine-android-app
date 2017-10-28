package com.rainmachine.presentation.screens.programdetailsold;

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

public class CycleSoakDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.number_picker_cycles)
    NumberPicker numberPickerCycles;
    @BindView(R.id.number_picker_soak)
    NumberPicker numberPickerSoak;

    public interface Callback {
        void onDialogCycleSoakPositiveClick(int cycles, int soak);

        void onDialogCycleSoakCancel();
    }

    public static CycleSoakDialogFragment newInstance(int cycles, int soak /*in seconds*/) {
        CycleSoakDialogFragment fragment = new CycleSoakDialogFragment();
        Bundle args = new Bundle();
        args.putInt("numCycles", cycles);
        args.putInt("soakSeconds", soak);
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
        builder.setTitle(R.string.program_details_set_duration);

        View view = View.inflate(getContext(), R.layout.dialog_cycle_soak, null);
        ButterKnife.bind(this, view);
        numberPickerCycles.setValue(getArguments().getInt("numCycles"));
        numberPickerCycles.setMinValue(2);
        numberPickerCycles.setMaxValue(300);
        int soakMinutes = getArguments().getInt("soakSeconds") / DateTimeConstants
                .SECONDS_PER_MINUTE;
        numberPickerSoak.setValue(soakMinutes);
        numberPickerSoak.setMinValue(0);
        numberPickerSoak.setMaxValue(300);
        builder.setView(view);

        builder.setPositiveButton(R.string.all_ok, (dialog, id) -> {
            numberPickerCycles.clearFocus();
            int cycles = numberPickerCycles.getValue();
            numberPickerSoak.clearFocus();
            int soak = numberPickerSoak.getValue();
            int soakSeconds = soak * DateTimeConstants.SECONDS_PER_MINUTE;
            callback.onDialogCycleSoakPositiveClick(cycles, soakSeconds);
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogCycleSoakCancel();
    }
}
