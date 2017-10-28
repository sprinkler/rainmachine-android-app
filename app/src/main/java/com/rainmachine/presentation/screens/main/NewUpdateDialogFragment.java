package com.rainmachine.presentation.screens.main;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.rainmachine.R;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.NonSprinklerActivity;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

public class NewUpdateDialogFragment extends DialogFragment {

    @Inject
    MainPresenter presenter;

    public static NewUpdateDialogFragment newInstance(String newVersion) {
        NewUpdateDialogFragment fragment = new NewUpdateDialogFragment();
        Bundle args = new Bundle();
        args.putString("newVersion", newVersion);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof SprinklerActivity) {
            ((SprinklerActivity) getActivity()).inject(this);
        } else if (getActivity() instanceof NonSprinklerActivity) {
            ((NonSprinklerActivity) getActivity()).inject(this);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.main_firmware_update);

        String message;
        String newVersion = getArguments().getString("newVersion");
        if (!Strings.isBlank(newVersion)) {
            message = getString(R.string.main_please_update, newVersion);
        } else {
            message = getString(R.string.main_please_update_latest);
        }
        builder.setMessage(message);

        builder.setPositiveButton(R.string.all_update_now, (dialog, id) -> presenter
                .onClickUpdate());
        builder.setNeutralButton(R.string.main_see_whats_new, (dialog, which) -> presenter
                .onClickSeeUpdateChanges());
        builder.setNegativeButton(R.string.main_later, (dialog, id) -> {
            // Do nothing
        });
        return builder.create();
    }
}
