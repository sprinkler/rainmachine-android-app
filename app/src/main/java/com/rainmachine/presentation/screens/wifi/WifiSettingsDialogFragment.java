package com.rainmachine.presentation.screens.wifi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.rainmachine.R;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.BaseDialogFragment;
import com.rainmachine.presentation.util.Toasts;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WifiSettingsDialogFragment extends BaseDialogFragment implements CompoundButton
        .OnCheckedChangeListener {

    private static final String EXTRA_AP = "ap";

    @Inject
    WifiContract.Presenter presenter;

    @BindView(R.id.input_password)
    EditText inputPassword;
    @BindView(R.id.check_show_password)
    CheckBox checkShowPassword;

    public static WifiSettingsDialogFragment newInstance(WifiItemViewModel wifiItemViewModel) {
        WifiSettingsDialogFragment fragment = new WifiSettingsDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_AP, Parcels.wrap(wifiItemViewModel));
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
        final WifiItemViewModel wifiItemViewModel = getParcelable(EXTRA_AP);
        builder.setTitle(wifiItemViewModel.sSID);

        View view = View.inflate(getContext(), R.layout.dialog_wifi_settings, null);
        ButterKnife.bind(this, view);
        checkShowPassword.setOnCheckedChangeListener(this);
        checkShowPassword.setChecked(true);
        builder.setView(view);

        builder.setPositiveButton(R.string.wifi_connect, (dialog, id) -> {
            boolean isSuccess = true;
            String password = inputPassword.getText().toString();
            if (Strings.isBlank(password)) {
                isSuccess = false;
                inputPassword.setError(getString(R.string.all_error_required));
            }

            if (isSuccess) {
                presenter.onClickConnectWifi(wifiItemViewModel, password);
            } else {
                Toasts.showLong(R.string.all_error_fill_in);
            }
        });
        builder.setNegativeButton(R.string.all_cancel, (dialog, which) -> {
            // Do nothing
            dialog.cancel();
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.check_show_password) {
            inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | (isChecked ? InputType
                    .TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType
                    .TYPE_TEXT_VARIATION_PASSWORD));
            inputPassword.setSelection(inputPassword.getText().length());
        }
    }
}
