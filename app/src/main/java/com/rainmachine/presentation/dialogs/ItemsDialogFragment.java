package com.rainmachine.presentation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

public class ItemsDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    public interface Callback {
        void onDialogItemsClick(int dialogId, String[] items, int clickedItemPosition);

        void onDialogItemsCancel(int dialogId);
    }

    public static ItemsDialogFragment newInstance(int dialogId, String title, String[] items) {
        ItemsDialogFragment fragment = new ItemsDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putStringArray("items", items);
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
        builder.setItems(items, (dialog, which) -> {
            String[] items1 = getArguments().getStringArray("items");
            callback.onDialogItemsClick(getArguments().getInt("dialogId"), items1, which);
        });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogItemsCancel(getArguments().getInt("dialogId"));
    }
}
