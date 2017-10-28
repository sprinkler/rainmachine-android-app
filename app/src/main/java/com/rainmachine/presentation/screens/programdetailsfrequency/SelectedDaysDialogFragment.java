package com.rainmachine.presentation.screens.programdetailsfrequency;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ListView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import org.joda.time.DateTimeConstants;
import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectedDaysDialogFragment extends DialogFragment {

    @Inject
    Callback callback;

    @BindView(R.id.list)
    ListView listView;

    public interface Callback {
        void onDialogSelectedDaysPositiveClick(boolean[] checkedItemPositions);

        void onDialogSelectedDaysCancel();
    }

    public static SelectedDaysDialogFragment newInstance(int dialogId, String title,
                                                         String positiveBtn, ItemsSelectedDays
                                                                 items) {
        SelectedDaysDialogFragment fragment = new SelectedDaysDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogId", dialogId);
        args.putString("title", title);
        args.putString("positiveBtn", positiveBtn);
        args.putParcelable("items", Parcels.wrap(items));
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
        View view = View.inflate(getContext(), R.layout.dialog_program_details_selected_days, null);
        ButterKnife.bind(this, view);

        final ItemsSelectedDays items = Parcels.unwrap(getArguments().getParcelable("items"));
        SelectedDayAdapter adapter = new SelectedDayAdapter(getActivity(), items.items);
        listView.setAdapter(adapter);
        builder.setView(view);

        String positiveBtn = getArguments().getString("positiveBtn");
        builder.setPositiveButton(positiveBtn, (dialog, id) -> {
            boolean[] checkedItemPositions = new boolean[DateTimeConstants.DAYS_PER_WEEK];
            for (int i = 0; i < checkedItemPositions.length; i++) {
                checkedItemPositions[i] = items.items.get(i).isChecked;
            }
            callback.onDialogSelectedDaysPositiveClick(checkedItemPositions);
        });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.onDialogSelectedDaysCancel();
    }
}
