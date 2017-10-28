package com.rainmachine.presentation.screens.restrictions;

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
import timber.log.Timber;

public class FreezeProtectDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.input_temperature)
    EditText inputTemperature;
    @BindView(R.id.number_text_after)
    TextView textAfter;

    private boolean dialogReady;

    public interface Callback {
        void onDialogFreezeProtectPositiveClick(int dialogId, int value);

        void onDialogFreezeProtectCancel(int dialogId);
    }

    public static FreezeProtectDialogFragment newInstance(int dialogId, String title,
                                                          String positiveBtn, int value,
                                                          boolean isUnitsMetric) {
        FreezeProtectDialogFragment fragment = new FreezeProtectDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putString("positiveBtn", positiveBtn);
        args.putInt("value", value);
        args.putBoolean("isUnitsMetric", isUnitsMetric);
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

        View view = View.inflate(getContext(), R.layout.dialog_freeze_protect, null);
        ButterKnife.bind(this, view);

        inputTemperature.setText(String.format(Locale.ENGLISH, "%d", getArguments().getInt
                ("value")));
        inputTemperature.setSelection(inputTemperature.length());

        boolean isUnitsMetric = getArguments().getBoolean("isUnitsMetric");
        String temperatureUnit = isUnitsMetric ? getString(R.string.all_temperature_unit_celsius) :
                getString(R.string.all_temperature_unit_fahrenheit);
        textAfter.setText(temperatureUnit);
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
        callback.onDialogFreezeProtectCancel(getArguments().getInt("dialogId"));
    }

    private void onPositiveButton() {
        try {
            int value = Integer.parseInt(inputTemperature.getText().toString().trim());
            Timber.d("Selected value is %d", value);
            callback.onDialogFreezeProtectPositiveClick(getArguments().getInt
                    ("dialogId"), value);
            dismissAllowingStateLoss();
        } catch (NumberFormatException nfe) {
            inputTemperature.setError(getString(R.string.all_error_invalid));
        }
    }
}
