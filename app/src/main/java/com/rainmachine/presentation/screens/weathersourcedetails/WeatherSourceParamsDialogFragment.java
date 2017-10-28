package com.rainmachine.presentation.screens.weathersourcedetails;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.rainmachine.R;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Toasts;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class WeatherSourceParamsDialogFragment extends DialogFragment {

    public static final int VALUE_TYPE_INT = 0;
    public static final int VALUE_TYPE_STRING = 1;

    @Inject
    WeatherSourceDetailsPresenter presenter;

    @BindView(R.id.input_value)
    EditText inputParamValue;

    public static WeatherSourceParamsDialogFragment newInstance(String param, Object value,
                                                                int valueType) {
        WeatherSourceParamsDialogFragment fragment = new WeatherSourceParamsDialogFragment();
        Bundle args = new Bundle();
        args.putString("param", param);
        if (valueType == VALUE_TYPE_INT) {
            args.putInt("value", (Integer) value);
        } else if (valueType == VALUE_TYPE_STRING) {
            args.putString("value", (String) value);
        }
        args.putInt("valueType", valueType);
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
        builder.setTitle(getArguments().getString("param"));

        View view = View.inflate(getContext(), R.layout.dialog_data_source_param, null);
        ButterKnife.bind(this, view);
        int valueType = getArguments().getInt("valueType");
        if (valueType == VALUE_TYPE_INT) {
            inputParamValue.setInputType(InputType.TYPE_CLASS_NUMBER);
            int value = getArguments().getInt("value");
            inputParamValue.setText(String.format(Locale.ENGLISH, "%d", value));
            inputParamValue.setSelection(inputParamValue.length());
        } else if (valueType == VALUE_TYPE_STRING) {
            inputParamValue.setInputType(InputType.TYPE_CLASS_TEXT);
            String value = getArguments().getString("value");
            inputParamValue.setText(value);
            inputParamValue.setSelection(inputParamValue.length());
        }
        builder.setView(view);

        builder.setPositiveButton(R.string.all_save, (dialog, id) -> {
            boolean isSuccess = true;
            int valueType1 = getArguments().getInt("valueType");
            String sValue = inputParamValue.getText().toString();
            int valueInt = 0;
            if (Strings.isBlank(sValue)) {
                isSuccess = false;
                inputParamValue.setError(getString(R.string.all_error_required));
            } else {
                if (valueType1 == VALUE_TYPE_INT) {
                    try {
                        valueInt = Integer.parseInt(sValue);
                    } catch (NumberFormatException e) {
                        Timber.w(e, e.getMessage());
                        isSuccess = false;
                    }
                }
            }

            if (isSuccess) {
                String param = getArguments().getString("param");
                if (valueType1 == VALUE_TYPE_INT) {
                    presenter.onChangedIntParam(param, valueInt);
                } else if (valueType1 == VALUE_TYPE_STRING) {
                    presenter.onChangedStringParam(param, sValue);
                }
            } else {
                Toasts.showLong(R.string.all_error_fill_in);
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // Do nothing special
    }
}
