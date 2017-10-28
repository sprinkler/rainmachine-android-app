package com.rainmachine.presentation.screens.cloudaccounts;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.domain.util.EmailAddress;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.NonSprinklerActivity;
import com.rainmachine.presentation.util.Toasts;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CloudAccountsDialogFragment extends DialogFragment {

    @Inject
    CloudAccountsPresenter presenter;

    @BindView(R.id.input_email)
    EditText inputEmail;
    @BindView(R.id.input_password)
    EditText inputPassword;

    public static CloudAccountsDialogFragment newInstance(String title) {
        CloudAccountsDialogFragment fragment = new CloudAccountsDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    public static CloudAccountsDialogFragment newInstance(String title, CloudInfo cloudInfo) {
        CloudAccountsDialogFragment fragment = new CloudAccountsDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("email", cloudInfo.email);
        args.putString("password", cloudInfo.password);
        args.putLong("_id", cloudInfo._id);
        args.putBoolean("update", true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((NonSprinklerActivity) getActivity()).inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString("title"));
        View view = View.inflate(getContext(), R.layout.dialog_cloud_account, null);
        ButterKnife.bind(this, view);
        inputEmail.setText(getArguments().getString("email", ""));
        inputEmail.setSelection(inputEmail.length());
        inputPassword.setText(getArguments().getString("password", ""));
        inputPassword.setSelection(inputPassword.length());
        inputPassword.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSave();
                return true;
            }
            return false;
        });
//        updateViews();
        builder.setView(view);
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

    @OnClick(R.id.btn_save)
    public void onSave() {
        saveCloudEmail();
    }

    private void saveCloudEmail() {
        boolean isSuccess = true;
        String email = inputEmail.getText().toString().trim();
        if (Strings.isBlank(email)) {
            isSuccess = false;
            inputEmail.setError(getString(R.string.all_error_required));
        } else if (!EmailAddress.isValid(email)) {
            isSuccess = false;
            inputEmail.setError(getString(R.string.cloud_accounts_error_email_invalid));
        }

        String password = inputPassword.getText().toString();

        if (isSuccess) {
            boolean update = getArguments().getBoolean("update", false);
            if (update) {
                Long _id = getArguments().getLong("_id");
                presenter.onUpdateCloudAccount(_id, email, password);
            } else {
                presenter.onCreateCloudAccount(email, password);
            }
            dismiss();
        } else {
            Toasts.showLong(R.string.all_error_fill_in);
        }
    }
}
