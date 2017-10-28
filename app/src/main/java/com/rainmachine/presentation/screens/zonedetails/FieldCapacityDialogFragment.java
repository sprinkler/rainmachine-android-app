package com.rainmachine.presentation.screens.zonedetails;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.DecimalFormatter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FieldCapacityDialogFragment extends DialogFragment {

    @Inject
    ZoneDetailsAdvancedPresenter presenter;
    @Inject
    DecimalFormatter decimalFormatter;

    @BindView(R.id.tv_amount)
    TextView tvAmount;
    @BindView(R.id.tv_percentage)
    TextView tvPercentage;

    public static FieldCapacityDialogFragment newInstance(int savingsPercentage, float
            currentFieldCapacity, boolean isUnitsMetric) {
        FieldCapacityDialogFragment fragment = new FieldCapacityDialogFragment();
        Bundle args = new Bundle();
        args.putInt("savings", savingsPercentage);
        args.putFloat("currentFieldCapacity", currentFieldCapacity);
        args.putBoolean("isUnitsMetric", isUnitsMetric);
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
        builder.setTitle(R.string.zone_details_field_capacity);

        View view = View.inflate(getContext(), R.layout.dialog_field_capacity, null);
        ButterKnife.bind(this, view);
        builder.setView(view);

        builder.setPositiveButton(R.string.all_ok, (dialog, id) -> presenter.onSaveFieldCapacity
                (getSavingsPercentage()));
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        setup();
    }

    @OnClick({R.id.btn_minus, R.id.btn_plus})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_minus) {
            int savingsPercentage = getSavingsPercentage();
            if (savingsPercentage > 50) {
                savingsPercentage -= 10;
                setSavingsPercentage(savingsPercentage);
                updatePercentage();
                updateFieldCapacity();
            }
        } else if (id == R.id.btn_plus) {
            int savingsPercentage = getSavingsPercentage();
            if (savingsPercentage < 200) {
                savingsPercentage += 10;
                setSavingsPercentage(savingsPercentage);
                updatePercentage();
                updateFieldCapacity();
            }
        }
    }

    private void updateFieldCapacity() {
        boolean isUnitsMetric = isUnitsMetric();
        float fieldCapacity = (getSavingsPercentage() / 100.0f) * getCurrentFieldCapacity();
        if (fieldCapacity >= 0) {
            String sFieldCapacity = decimalFormatter.lengthUnitsDecimals(fieldCapacity,
                    isUnitsMetric) + " " + (isUnitsMetric ? getContext()
                    .getString(R.string.all_mm) : getContext().getString(R.string.all_inch));
            tvAmount.setText(sFieldCapacity);
        } else {
            tvAmount.setText(R.string.zone_details_advanced_invalid_values);
        }
    }

    private void updatePercentage() {
        tvPercentage.setText(getString(R.string.all_percentage, getSavingsPercentage() - 100));
    }

    private void setup() {
        updateFieldCapacity();
        updatePercentage();
    }

    private int getSavingsPercentage() {
        return getArguments().getInt("savings");
    }

    private void setSavingsPercentage(int savingsPercentage) {
        getArguments().putInt("savings", savingsPercentage);
    }

    private float getCurrentFieldCapacity() {
        return getArguments().getFloat("currentFieldCapacity");
    }

    private boolean isUnitsMetric() {
        return getArguments().getBoolean("isUnitsMetric");
    }
}
