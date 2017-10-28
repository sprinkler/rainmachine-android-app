package com.rainmachine.presentation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

public class ClickableRadioOptionsDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    public interface Callback {
        void onDialogClickableRadioOptionsItem(int dialogId, String[] items,
                                               int checkedItemPosition);

        void onDialogClickableRadioOptionsCancel(int dialogId);
    }

    public static ClickableRadioOptionsDialogFragment newInstance(int dialogId, String title,
                                                                  String[] items,
                                                                  int checkedItemPosition) {
        ClickableRadioOptionsDialogFragment fragment = new ClickableRadioOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putStringArray("items", items);
        args.putInt("checkedItemPosition", checkedItemPosition);
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
        builder.setTitle(getArguments().getString("title"));

        String[] items = getArguments().getStringArray("items");
        int checkedItem = getArguments().getInt("checkedItemPosition");
        builder.setSingleChoiceItems(items, checkedItem, (dialogInterface, i) -> {
            callback.onDialogClickableRadioOptionsItem(getArguments().getInt("dialogId"),
                    getArguments().getStringArray("items"), i);
            dismissAllowingStateLoss();
        });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogClickableRadioOptionsCancel(getArguments().getInt("dialogId"));
    }
}
