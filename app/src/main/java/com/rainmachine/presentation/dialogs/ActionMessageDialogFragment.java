package com.rainmachine.presentation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.rainmachine.presentation.activities.NonSprinklerActivity;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

public class ActionMessageDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    public interface Callback {
        void onDialogActionMessagePositiveClick(int dialogId);

        void onDialogActionMessageNegativeClick(int dialogId);

        void onDialogActionMessageCancel(int dialogId);
    }

    public static ActionMessageDialogFragment newInstance(int dialogId, String title,
                                                          String message, String positiveBtn,
                                                          String negativeBtn) {
        ActionMessageDialogFragment fragment = new ActionMessageDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putString("message", message);
        args.putString("positiveBtn", positiveBtn);
        args.putString("negativeBtn", negativeBtn);
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
        builder.setTitle(getArguments().getString("title"));
        builder.setMessage(getArguments().getString("message"));

        builder.setPositiveButton(getArguments().getString("positiveBtn"),
                (dialog, id) -> callback.onDialogActionMessagePositiveClick(getArguments().getInt
                        ("dialogId")));
        builder.setNegativeButton(getArguments().getString("negativeBtn"),
                (dialog, id) -> callback.onDialogActionMessageNegativeClick(getArguments().getInt
                        ("dialogId")));

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogActionMessageCancel(getArguments().getInt("dialogId"));
    }
}
