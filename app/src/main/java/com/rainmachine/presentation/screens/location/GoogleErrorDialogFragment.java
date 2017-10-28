package com.rainmachine.presentation.screens.location;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.common.GoogleApiAvailability;
import com.rainmachine.presentation.activities.NonSprinklerActivity;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

public class GoogleErrorDialogFragment extends DialogFragment {

    // Unique tag for the error dialog fragment
    public static final String DIALOG_ERROR = "dialog_error";

    @Inject
    Callback callback;

    public interface Callback {
        void onDialogGoogleErrorCancel();
    }

    public static GoogleErrorDialogFragment newInstance(int errorCode) {
        GoogleErrorDialogFragment fragment = new GoogleErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
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
        // Get the error code and retrieve the appropriate dialog
        int errorCode = getArguments().getInt(DIALOG_ERROR);
        return GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), errorCode,
                LocationActivity.REQ_CODE_RESOLVE_LOCATION);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogGoogleErrorCancel();
    }
}
