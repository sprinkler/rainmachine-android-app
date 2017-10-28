package com.rainmachine.presentation.screens.mini8settings;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ListView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Mini8SettingsProgramsDialogFragment extends DialogFragment {

    @Inject
    Mini8SettingsContract.Presenter presenter;

    @BindView(R.id.list)
    ListView listView;

    public static Mini8SettingsProgramsDialogFragment newInstance(List<TouchProgramViewModel>
                                                                          items,
                                                                  TouchProgramViewModel
                                                                          selectedItem) {
        Mini8SettingsProgramsDialogFragment fragment = new Mini8SettingsProgramsDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("items", Parcels.wrap(items));
        args.putParcelable("selectedItem", Parcels.wrap(selectedItem));
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
        builder.setTitle(R.string.mini8_settings_select_program);
        View view = View.inflate(getContext(), R.layout.dialog_mini8_settings_programs, null);
        ButterKnife.bind(this, view);

        final List<TouchProgramViewModel> items = Parcels.unwrap(getArguments().getParcelable
                ("items"));
        TouchProgramViewModel selectedItem = Parcels.unwrap(getArguments().getParcelable
                ("selectedItem"));
        Mini8SettingsProgramAdapter adapter = new Mini8SettingsProgramAdapter(getActivity(),
                items, selectedItem);
        listView.setAdapter(adapter);
        builder.setView(view);

        builder.setPositiveButton(R.string.all_save, (dialog, id) -> {
            presenter.onSelectedProgram(adapter.getSelectedItem());
        });
        return builder.create();
    }
}
