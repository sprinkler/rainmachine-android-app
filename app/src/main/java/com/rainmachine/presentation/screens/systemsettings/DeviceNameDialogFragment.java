package com.rainmachine.presentation.screens.systemsettings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.rainmachine.R;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Toasts;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceNameDialogFragment extends DialogFragment {

    @Inject
    SystemSettingsPresenter presenter;

    @BindView(R.id.input_device_name)
    EditText inputDeviceName;

    public static DeviceNameDialogFragment newInstance(String deviceName) {
        DeviceNameDialogFragment fragment = new DeviceNameDialogFragment();
        Bundle args = new Bundle();
        args.putString("deviceName", deviceName);
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
        builder.setTitle(R.string.system_settings_set_device_name);

        View view = View.inflate(getContext(), R.layout.dialog_device_name, null);
        ButterKnife.bind(this, view);
        String deviceName = getArguments().getString("deviceName");
        inputDeviceName.setText(deviceName);
        inputDeviceName.setSelection(inputDeviceName.length());
        builder.setView(view);

        builder.setPositiveButton(R.string.all_save, (dialog, id) -> {
            boolean isSuccess = true;
            String deviceName1 = inputDeviceName.getText().toString();
            if (Strings.isBlank(deviceName1)) {
                isSuccess = false;
                inputDeviceName.setError(getString(R.string.all_error_required));
            }

            if (isSuccess) {
                presenter.onSaveDeviceName(deviceName1);
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
