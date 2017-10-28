package com.rainmachine.presentation.screens.zonedetails;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Truss;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SummerTimeDialogFragment extends DialogFragment {

    @Inject
    ZoneDetailsAdvancedPresenter presenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.tv_summer_time_desc)
    TextView tvSummerTimeDesc;

    public static SummerTimeDialogFragment newInstance(int referenceTime) {
        SummerTimeDialogFragment fragment = new SummerTimeDialogFragment();
        Bundle args = new Bundle();
        args.putInt("referenceTime", referenceTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((SprinklerActivity) getActivity()).inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.zone_details_summer_time);

        View view = View.inflate(getContext(), R.layout.dialog_summer_time, null);
        ButterKnife.bind(this, view);
        builder.setView(view);

        builder.setPositiveButton(R.string.all_ok, (dialog, id) -> {
        });

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return dialog;
    }

    private void render() {
        Truss truss = new Truss()
                .pushSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen
                        .text_summer_time)))
                .append(formatter.hourMinSecLabel(getArguments().getInt("referenceTime")))
                .popSpan()
                .append("\n")
                .append(getString(R.string.zone_details_summer_time_hint))
                .append('\n')
                .append('\n')
                .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color
                        .text_primary)))
                .append(getString(R.string.zone_details_summer_time_desc))
                .popSpan();
        tvSummerTimeDesc.setText(truss.build());
    }
}
