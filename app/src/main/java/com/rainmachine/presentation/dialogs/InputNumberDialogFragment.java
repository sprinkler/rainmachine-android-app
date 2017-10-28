package com.rainmachine.presentation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InputNumberDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.input_value)
    EditText inputValue;
    @BindView(R.id.number_text_after)
    TextView textAfter;

    private boolean dialogReady;

    public interface Callback {
        void onDialogInputNumberPositiveClick(int dialogId, int value);

        void onDialogInputNumberCancel(int dialogId);
    }

    public static InputNumberDialogFragment newInstance(int dialogId, String title,
                                                        String positiveBtn, int value,
                                                        String textAfter, int minValue, int
                                                                maxValue) {
        InputNumberDialogFragment fragment = new InputNumberDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putString("positiveBtn", positiveBtn);
        args.putInt("value", value);
        args.putString("textAfter", textAfter);
        args.putInt("minValue", minValue);
        args.putInt("maxValue", maxValue);
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

        View view = View.inflate(getContext(), R.layout.dialog_input_number, null);
        ButterKnife.bind(this, view);

        inputValue.setText(String.format(Locale.ENGLISH, "%d", getArguments().getInt("value")));
        inputValue.setSelection(0, inputValue.length());

        /*int minValue = getArguments().getInt("minValue");
        int maxValue = getArguments().getInt("maxValue");
        inputValue.setFilters(new InputFilter[]{new InputFilterMinMax(minValue, maxValue)});*/
        textAfter.setText(getArguments().getString("textAfter"));
        builder.setView(view);

        builder.setPositiveButton(getArguments().getString("positiveBtn"), (dialog, id) -> {
            // This is replaced in onShowListener
        });
        Dialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            if (!dialogReady) {
                Button button = ((AlertDialog) dialog1).getButton(DialogInterface
                        .BUTTON_POSITIVE);
                button.setOnClickListener(v -> onPositiveButton());
                dialogReady = true;
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogInputNumberCancel(getArguments().getInt("dialogId"));
    }

    private void onPositiveButton() {
        try {
            int value = Integer.parseInt(inputValue.getText().toString().trim());
            int minValue = getArguments().getInt("minValue");
            int maxValue = getArguments().getInt("maxValue");
            if (value >= minValue && value <= maxValue) {
                callback.onDialogInputNumberPositiveClick(getArguments().getInt("dialogId"), value);
                dismissAllowingStateLoss();
            } else {
                inputValue.setError(getString(R.string.all_error_min_max, minValue, maxValue));
            }
        } catch (NumberFormatException nfe) {
            inputValue.setError(getString(R.string.all_error_invalid));
        }
    }
}
