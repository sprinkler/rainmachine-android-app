package com.rainmachine.presentation.screens.restrictions;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.widgets.SeekBarWithMin;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HotDaysDialogFragment extends DialogFragment {

    private static final String ARG_COEFFICIENT = "maxWateringCoefficient";
    private static final int COEFFICIENT_MIN_VALUE = 100;

    @Inject
    RestrictionsPresenter presenter;

    @BindView(R.id.seek_bar)
    SeekBarWithMin seekBar;

    public static HotDaysDialogFragment newInstance(int maxWateringCoefficient) {
        HotDaysDialogFragment fragment = new HotDaysDialogFragment();
        Bundle args = new Bundle();
        // The coefficient is between 100 and 200
        args.putInt(ARG_COEFFICIENT, maxWateringCoefficient);
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
        builder.setTitle(R.string.restrictions_watering_coefficient);

        View view = View.inflate(getContext(), R.layout.dialog_hot_days, null);
        ButterKnife.bind(this, view);
        seekBar.setMinValue(COEFFICIENT_MIN_VALUE);
        seekBar.setMax(100);
        updateSeekBar(getArguments().getInt(ARG_COEFFICIENT));
        builder.setView(view);

        builder.setPositiveButton(R.string.all_save, (dialog, id) -> {
            int maxWateringCoefficient = COEFFICIENT_MIN_VALUE + seekBar.getProgress();
            presenter.onSaveHotDaysMaxWatering(maxWateringCoefficient);
        });

        return builder.create();
    }

    private void updateSeekBar(int coefficient) {
        seekBar.setProgress(coefficient - COEFFICIENT_MIN_VALUE);
    }
}
