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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MinutesDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.number_picker)
    NumberPicker numberPicker;

    public interface Callback {
        void onDialogMinutesPositiveClick(int dialogId, int minutes);
    }

    public static MinutesDialogFragment newInstance(int dialogId, String title, String positiveBtn,
                                                    int minutes) {
        MinutesDialogFragment fragment = new MinutesDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putString("positiveBtn", positiveBtn);
        args.putInt("minutes", minutes);
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

        View view = View.inflate(getContext(), R.layout.dialog_minutes, null);
        ButterKnife.bind(this, view);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(300);
        numberPicker.setValue(getArguments().getInt("minutes"));
        builder.setView(view);

        builder.setPositiveButton(getArguments().getString("positiveBtn"),
                (dialog, id) -> {
                    numberPicker.clearFocus();
                    int minutes = numberPicker.getValue();
                    callback.onDialogMinutesPositiveClick(getArguments().getInt
                            ("dialogId"), minutes);
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
}
