package com.rainmachine.presentation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.rainmachine.presentation.activities.NonSprinklerActivity;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

public class ActionMessageParcelableDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    public interface Callback {
        void onDialogActionMessageParcelablePositiveClick(int dialogId,
                                                          Parcelable parcelable);

        void onDialogActionMessageParcelableNegativeClick(int dialogId);

        void onDialogActionMessageParcelableCancel(int dialogId);
    }

    public static ActionMessageParcelableDialogFragment newInstance(int dialogId, String title,
                                                                    String message,
                                                                    String positiveBtn,
                                                                    String negativeBtn,
                                                                    Parcelable parcelable) {
        ActionMessageParcelableDialogFragment fragment = new
                ActionMessageParcelableDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putString("message", message);
        args.putString("positiveBtn", positiveBtn);
        args.putString("negativeBtn", negativeBtn);
        args.putParcelable("parcelable", parcelable);
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
                (dialog, id) -> {
                    int dialogId = getArguments().getInt("dialogId");
                    Parcelable parcelable = getArguments().getParcelable("parcelable");
                    callback.onDialogActionMessageParcelablePositiveClick(dialogId,
                            parcelable);
                });
        builder.setNegativeButton(getArguments().getString("negativeBtn"),
                (dialog, id) -> {
                    int dialogId = getArguments().getInt("dialogId");
                    callback.onDialogActionMessageParcelableNegativeClick(dialogId);
                });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogActionMessageParcelableCancel(getArguments().getInt("dialogId"));
    }
}
