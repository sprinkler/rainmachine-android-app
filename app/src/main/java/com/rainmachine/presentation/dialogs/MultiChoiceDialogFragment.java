package com.rainmachine.presentation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

public class MultiChoiceDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    public interface Callback {
        void onDialogMultiChoicePositiveClick(int dialogId, String[] items,
                                              boolean[] checkedItemPositions);

        void onDialogMultiChoiceCancel(int dialogId);
    }

    public static MultiChoiceDialogFragment newInstance(int dialogId, String title,
                                                        String positiveBtn, String[] items,
                                                        boolean[] checkedItemPositions) {
        MultiChoiceDialogFragment fragment = new MultiChoiceDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putString("positiveBtn", positiveBtn);
        args.putStringArray("items", items);
        args.putBooleanArray("checkedItemPositions", checkedItemPositions);
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
        final boolean[] checkedItems = getArguments().getBooleanArray("checkedItemPositions");
        builder.setMultiChoiceItems(items, checkedItems,
                (dialogInterface, i, checked) -> {
                    getArguments().getBooleanArray("checkedItemPositions")[i] = checked;
                }
        );
        String positiveBtn = getArguments().getString("positiveBtn");
        builder.setPositiveButton(positiveBtn, (dialog, id) -> {
            String[] items1 = getArguments().getStringArray("items");
            boolean[] checkedItemPositions = getArguments().getBooleanArray
                    ("checkedItemPositions");
            callback.onDialogMultiChoicePositiveClick(getArguments().getInt("dialogId")
                    , items1, checkedItemPositions);
        });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogMultiChoiceCancel(getArguments().getInt("dialogId"));
    }
}
