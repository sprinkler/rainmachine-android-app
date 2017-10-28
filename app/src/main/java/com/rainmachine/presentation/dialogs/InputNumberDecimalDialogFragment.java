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

public class InputNumberDecimalDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.input_value)
    EditText inputValue;
    @BindView(R.id.number_text_after)
    TextView textAfter;

    private boolean dialogReady;

    public interface Callback {
        void onDialogInputNumberDecimalPositiveClick(int dialogId, float value);

        void onDialogInputNumberDecimalCancel(int dialogId);
    }

    public static InputNumberDecimalDialogFragment newInstance(int dialogId, String title,
                                                               String positiveBtn, float value,
                                                               String textAfter) {
        InputNumberDecimalDialogFragment fragment = new InputNumberDecimalDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putString("positiveBtn", positiveBtn);
        args.putFloat("value", value);
        args.putString("textAfter", textAfter);
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

        View view = View.inflate(getContext(), R.layout.dialog_input_number_decimal, null);
        ButterKnife.bind(this, view);

        inputValue.setText(String.format(Locale.ENGLISH, "%f", getArguments().getFloat("value")));
        inputValue.setSelection(0, inputValue.length());
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
        callback.onDialogInputNumberDecimalCancel(getArguments().getInt("dialogId"));
    }

    private void onPositiveButton() {
        try {
            float value = Float.parseFloat(inputValue.getText().toString().trim());
            callback.onDialogInputNumberDecimalPositiveClick(getArguments().getInt("dialogId"),
                    value);
            dismissAllowingStateLoss();
        } catch (NumberFormatException nfe) {
            inputValue.setError(getString(R.string.all_error_invalid));
        }
    }
}
