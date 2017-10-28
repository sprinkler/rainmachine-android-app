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
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.rainmachine.R;
import com.rainmachine.domain.util.EmailAddress;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Toasts;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CloudEmailDialogFragment extends DialogFragment {

    private static final int THRESHOLD = 1; // characters

    @Inject
    RemoteAccessPresenter presenter;

    @BindView(R.id.input_cloud_email)
    AutoCompleteTextView autoComplete;

    private boolean dialogReady;

    public static CloudEmailDialogFragment newInstance(String cloudEmail,
                                                       ArrayList<String> knownEmails) {
        CloudEmailDialogFragment fragment = new CloudEmailDialogFragment();
        Bundle args = new Bundle();
        args.putString("cloudEmail", cloudEmail);
        args.putStringArrayList("knownEmails", knownEmails);
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
        builder.setTitle(R.string.remote_access_set_cloud_email);

        View view = View.inflate(getContext(), R.layout.dialog_cloud_email, null);
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

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        presenter.onDialogCloudEmailCancel();
    }

    private void onPositiveButton() {
        boolean isSuccess = true;
        String cloudEmail = autoComplete.getText().toString().trim();
        if (Strings.isBlank(cloudEmail)) {
            isSuccess = false;
            autoComplete.setError(getString(R.string.all_error_required));
        } else if (!EmailAddress.isValid(cloudEmail)) {
            isSuccess = false;
            autoComplete.setError(getString(R.string.cloud_accounts_error_email_invalid));
        }
        if (isSuccess) {
            presenter.onSaveCloudEmail(cloudEmail);
            dismissAllowingStateLoss();
        } else {
            Toasts.showLong(R.string.all_error_fill_in);
        }
    }

    private void setup() {
        List<String> knownEmails = getArguments().getStringArrayList("knownEmails");
        EmailAutocompleteAdapter adapter = new EmailAutocompleteAdapter(getActivity(), knownEmails);
        autoComplete.setAdapter(adapter);
        autoComplete.setThreshold(THRESHOLD);
        String cloudEmail = getArguments().getString("cloudEmail");
        if (!Strings.isBlank(cloudEmail)) {
            autoComplete.setText(cloudEmail);
            autoComplete.setSelection(autoComplete.length());
        } else if (knownEmails.size() > 0) {
            // Pre-fill with first email available
            autoComplete.setText(knownEmails.get(0));
            autoComplete.setSelection(autoComplete.length());
        }
        autoComplete.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onPositiveButton();
                return true;
            }
            return false;
        });
    }
}
