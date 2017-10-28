package com.rainmachine.presentation.screens.directaccess;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.remote.util.RemoteUtils;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.NonSprinklerActivity;
import com.rainmachine.presentation.util.Toasts;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DirectAccessDialogFragment extends DialogFragment {

    @Inject
    DirectAccessPresenter presenter;

    @BindView(R.id.input_name)
    EditText inputName;
    @BindView(R.id.input_url)
    EditText inputUrl;

    public static DirectAccessDialogFragment newInstance(String title) {
        DirectAccessDialogFragment fragment = new DirectAccessDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    public static DirectAccessDialogFragment newInstance(String title, Device device) {
        DirectAccessDialogFragment fragment = new DirectAccessDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("name", device.name);
        args.putString("url", device.getUrl());
        args.putLong("_id", device._id);
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
        View view = View.inflate(getContext(), R.layout.dialog_direct_access, null);
        ButterKnife.bind(this, view);
        inputName.setText(getArguments().getString("name", ""));
        inputName.setSelection(inputName.length());
        inputUrl.setText(getArguments().getString("url", ""));
        inputUrl.setSelection(inputUrl.length());
        inputUrl.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSave();
                return true;
            }
            return false;
        });
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @OnClick(R.id.btn_save)
    public void onSave() {
        saveManualDevice();
    }

    private void saveManualDevice() {
        boolean isSuccess = true;
        String name = inputName.getText().toString();
        if (Strings.isBlank(name)) {
            isSuccess = false;
            inputName.setError(getString(R.string.all_error_required));
            inputName.requestFocus();
        }

        String url = inputUrl.getText().toString();
        if (Strings.isBlank(url)) {
            isSuccess = false;
            inputUrl.setError(getString(R.string.all_error_required));
            inputUrl.requestFocus();
        } else {
            if (RemoteUtils.isValidMacAddress(url)) {
                isSuccess = false;
                inputUrl.setError(getString(R.string.direct_access_url_or_ip));
                inputUrl.requestFocus();
            } else {
                url = url.trim().replaceAll("http://", "https://");
                if (!url.startsWith("https://")) {
                    url = "https://" + url;
                }

                if (!RemoteUtils.isValidInternetUrl(url)) {
                    isSuccess = false;
                    inputUrl.setError(getString(R.string.all_invalid_url));
                    inputUrl.requestFocus();
                }
            }
        }

        if (isSuccess) {
            Long _id = null;
            boolean update = getArguments().getBoolean("update", false);
            if (update) {
                _id = getArguments().getLong("_id");
            }
            presenter.saveManualDevice(_id, name, url);
            dismiss();
            Toasts.show(R.string.direct_access_device_added);
        } else {
            Toasts.showLong(R.string.all_error_fill_in);
        }
    }
}
