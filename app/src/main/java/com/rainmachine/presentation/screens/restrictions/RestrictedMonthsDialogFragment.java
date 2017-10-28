package com.rainmachine.presentation.screens.restrictions;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestrictedMonthsDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.list)
    ListView listView;

    public interface Callback {
        void onDialogMultiChoicePositiveClick(int dialogId, String[] items,
                                              boolean[] checkedItemPositions);

        void onDialogMultiChoiceCancel(int dialogId);
    }

    public static RestrictedMonthsDialogFragment newInstance(int dialogId, String title,
                                                             String positiveBtn, String[] items,
                                                             boolean[] checkedItemPositions) {
        RestrictedMonthsDialogFragment fragment = new RestrictedMonthsDialogFragment();
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
        View view = View.inflate(getContext(), R.layout.dialog_restricted_months, null);
        ButterKnife.bind(this, view);

        final String[] items = getArguments().getStringArray("items");
        RestrictedMonthAdapter adapter = new RestrictedMonthAdapter(getActivity(), Arrays.asList
                (items));
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        final boolean[] checkedItems = getArguments().getBooleanArray("checkedItemPositions");
        for (int i = 0; i < checkedItems.length; i++) {
            if (checkedItems[i]) {
                listView.setItemChecked(i, true);
            }
        }
        builder.setView(view);

        String positiveBtn = getArguments().getString("positiveBtn");
        builder.setPositiveButton(positiveBtn, (dialog, id) -> {
            String[] items1 = getArguments().getStringArray("items");
            boolean[] checkedItemPositions = new boolean[items1.length];
            SparseBooleanArray checkedItems1 = listView.getCheckedItemPositions();
            for (int i = 0; i < checkedItems1.size(); i++) {
                if (checkedItems1.valueAt(i)) {
                    checkedItemPositions[checkedItems1.keyAt(i)] = true;
                }
            }
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
