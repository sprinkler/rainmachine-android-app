package com.rainmachine.presentation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

public class RadioOptionsDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    public interface Callback {
        void onDialogRadioOptionsPositiveClick(int dialogId, String[] items,
                                               int checkedItemPosition);

        void onDialogRadioOptionsCancel(int dialogId);
    }

    public static RadioOptionsDialogFragment newInstance(int dialogId, String title,
                                                         String positiveBtn, String[] items,
                                                         int checkedItemPosition) {
        RadioOptionsDialogFragment fragment = new RadioOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putString("positiveBtn", positiveBtn);
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
        builder.setSingleChoiceItems(items, checkedItem, (dialogInterface, i) -> getArguments()
                .putInt("checkedItemPosition", i));
        String positiveBtn = getArguments().getString("positiveBtn");
        builder.setPositiveButton(positiveBtn, (dialog, id) -> {
            String[] items1 = getArguments().getStringArray("items");
            int checkedItem1 = getArguments().getInt("checkedItemPosition");
            callback.onDialogRadioOptionsPositiveClick(getArguments().getInt("dialogId")
                    , items1, checkedItem1);
        });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogRadioOptionsCancel(getArguments().getInt("dialogId"));
    }
}
