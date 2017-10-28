package com.rainmachine.presentation.screens.remoteaccess;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import com.rainmachine.R;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Toasts;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChangePasswordDialogFragment extends DialogFragment {

    @Inject
    RemoteAccessPresenter presenter;

    @BindView(R.id.input_old)
    EditText inputOldPass;
    @BindView(R.id.input_new)
    EditText inputNewPass;
    @BindView(R.id.input_confirm)
    EditText inputConfirmPass;

    private boolean dialogReady;

    public static ChangePasswordDialogFragment newInstance() {
        ChangePasswordDialogFragment fragment = new ChangePasswordDialogFragment();
        Bundle args = new Bundle();
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
        builder.setTitle(R.string.remote_access_set_password);

        View view = View.inflate(getContext(), R.layout.dialog_change_password, null);
        ButterKnife.bind(this, view);
        setup();
        builder.setView(view);

        builder.setPositiveButton(R.string.all_save, (dialog, id) -> {
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

    private void onPositiveButton() {
        boolean isSuccess = true;
        String oldPass = inputOldPass.getText().toString();
        if (!presenter.canBeEmptyPassword()) {
            if (Strings.isBlank(oldPass)) {
                isSuccess = false;
                inputOldPass.setError(getContext().getString(R.string.all_error_required));
            }
        }

        String newPass = inputNewPass.getText().toString();
        if (Strings.isBlank(newPass)) {
            isSuccess = false;
            inputNewPass.setError(getContext().getString(R.string.all_error_required));
        }

        String confirmPass = inputConfirmPass.getText().toString();
        if (Strings.isBlank(confirmPass)) {
            isSuccess = false;
            inputNewPass.setError(getContext().getString(R.string.all_error_required));
        }

        if (Strings.isBlank(newPass) || !newPass.equals(confirmPass)) {
            isSuccess = false;
            inputNewPass.setError(getContext().getString(R.string.all_error_password_mismatch));
            inputConfirmPass.setError(getContext().getString(R.string.all_error_password_mismatch));
        }

        if (isSuccess) {
            presenter.changePassword(oldPass, newPass);
            dismissAllowingStateLoss();
        } else {
            Toasts.showLong(R.string.all_error_fill_in);
        }
    }

    public void setup() {
        inputConfirmPass.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onPositiveButton();
                return true;
            }
            return false;
        });
    }
}
