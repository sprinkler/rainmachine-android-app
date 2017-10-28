package com.rainmachine.presentation.screens.hiddendrawer;

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

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.CloudServers;
import com.rainmachine.data.remote.util.RemoteUtils;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.util.Toasts;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditCloudServersDialog extends DialogFragment {

    @BindView(R.id.input_key)
    EditText inputKey;
    @BindView(R.id.input_proxy)
    EditText inputProxy;
    @BindView(R.id.input_validator)
    EditText inputValidator;
    @BindView(R.id.input_push)
    EditText inputPush;

    private Callback callback;
    private boolean dialogReady;

    public interface Callback {
        void onDialogEditPositiveClick(CloudServers cloudServers);
    }

    public static EditCloudServersDialog newInstance(CloudServers cloudServers, Callback callback) {
        EditCloudServersDialog dialog = new EditCloudServersDialog();
        Bundle args = new Bundle();
        args.putParcelable("cloudServers", Parcels.wrap(cloudServers));
        dialog.setArguments(args);
        dialog.setCallback(callback);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.hidden_drawer_edit_cloud_servers);

        View view = View.inflate(getContext(), R.layout.dialog_add_edit_cloud_servers, null);
        ButterKnife.bind(this, view);
        CloudServers cloudServers = cloudServers();
        inputKey.setText(cloudServers.key);
        inputProxy.setText(cloudServers.urlProxy);
        inputValidator.setText(cloudServers.urlValidator);
        inputPush.setText(cloudServers.urlPush);
        builder.setView(view);

        builder.setNegativeButton(R.string.all_cancel, (dialog, id) -> {
        });
        builder.setPositiveButton(R.string.all_save, (dialog, id) -> {
            // This is replaced in onShowListener
        });
        Dialog dialog = builder.create();

        dialog.setOnShowListener(dialog1 -> {
            if (!dialogReady) {
                Button button = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(v -> onPositiveButton());
                dialogReady = true;
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    private CloudServers cloudServers() {
        return Parcels.unwrap(getArguments().getParcelable("cloudServers"));
    }

    private void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void onPositiveButton() {
        boolean isSuccess = true;
        String key = inputKey.getText().toString().trim();
        if (Strings.isBlank(key)) {
            isSuccess = false;
            inputKey.setError(getString(R.string.all_error_required));
        }

        String proxy = inputProxy.getText().toString().trim();
        if (Strings.isBlank(proxy)) {
            isSuccess = false;
            inputProxy.setError(getString(R.string.all_error_required));
        } else if (!RemoteUtils.isValidInternetUrl(proxy)) {
            isSuccess = false;
            inputProxy.setError(getString(R.string.all_invalid_url));
        }

        String validator = inputValidator.getText().toString().trim();
        if (Strings.isBlank(validator)) {
            isSuccess = false;
            inputValidator.setError(getString(R.string.all_error_required));
        } else if (!RemoteUtils.isValidInternetUrl(validator)) {
            isSuccess = false;
            inputValidator.setError(getString(R.string.all_invalid_url));
        }

        String push = inputPush.getText().toString().trim();
        if (Strings.isBlank(push)) {
            isSuccess = false;
            inputPush.setError(getString(R.string.all_error_required));
        } else if (!RemoteUtils.isValidInternetUrl(push)) {
            isSuccess = false;
            inputPush.setError(getString(R.string.all_invalid_url));
        }

        if (isSuccess) {
            CloudServers cloudServers = cloudServers();
            cloudServers.key = key;
            cloudServers.urlProxy = proxy;
            cloudServers.urlValidator = validator;
            cloudServers.urlPush = push;
            callback.onDialogEditPositiveClick(cloudServers);
            dismissAllowingStateLoss();
        } else {
            Toasts.showLong(R.string.all_error_fill_in);
        }
    }
}
