package com.rainmachine.presentation.screens.wizardtimezone;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SearchView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.PresentationUtils;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;

public class TimezoneDialogFragment extends DialogFragment {

    public interface Callback {
        void onTimezoneSelected(String timezoneId);
    }

    @Inject
    Callback callback;

    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.search_view)
    SearchView searchView;

    private TimezoneAdapter adapter;
    private Unbinder unbinder;

    public static TimezoneDialogFragment newInstance() {
        TimezoneDialogFragment fragment = new TimezoneDialogFragment();
        Bundle args = new Bundle();
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
        builder.setTitle(R.string.wizard_timezone_set_timezone);
        View view = View.inflate(getContext(), R.layout.dialog_timezone, null);
        unbinder = ButterKnife.bind(this, view);
        builder.setView(view);
        updateViews();
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // Do nothing special
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnItemClick(R.id.list)
    public void onClickTimezone(int position) {
        String timezoneId = adapter.getItem(position);
        callback.onTimezoneSelected(timezoneId);
        dismissAllowingStateLoss();
    }

    private void updateViews() {
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(onQueryTextListener);
        List<String> timezoneIds = Arrays.asList(getActivity().getResources().getStringArray(R
                .array.timezone_ids));
        adapter = new TimezoneAdapter(getActivity(), timezoneIds);
        list.setAdapter(adapter);
    }

    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView
            .OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String query) {
            PresentationUtils.hideSoftKeyboard(searchView);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            adapter.getFilter().filter(s);
            return true;
        }
    };
}
