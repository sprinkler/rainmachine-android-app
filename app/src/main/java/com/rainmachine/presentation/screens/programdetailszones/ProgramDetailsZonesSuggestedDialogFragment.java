package com.rainmachine.presentation.screens.programdetailszones;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.ProgramFormatter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProgramDetailsZonesSuggestedDialogFragment extends DialogFragment implements
        ProgramDetailsZonesContract.SuggestedDialog {

    @Inject
    ProgramDetailsZonesContract.Presenter presenter;
    @Inject
    CalendarFormatter calendarFormatter;
    @Inject
    ProgramFormatter programFormatter;

    @BindView(R.id.tv_default_zone_properties)
    TextView tvDefaultZoneProperties;
    @BindView(R.id.tv_determined_duration)
    TextView tvDeterminedDuration;
    @BindView(R.id.tv_user_percentage)
    TextView tvUserPercentage;

    public static ProgramDetailsZonesSuggestedDialogFragment newInstance() {
        ProgramDetailsZonesSuggestedDialogFragment fragment = new
                ProgramDetailsZonesSuggestedDialogFragment();
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
        builder.setTitle(R.string.program_details_suggested);

        View view = View.inflate(getContext(), R.layout
                .dialog_program_details_suggested_duration, null);
        ButterKnife.bind(this, view);
        builder.setView(view);
        builder.setPositiveButton(R.string.all_ok,
                (dialog, id) -> {
                    // Do nothing because the underlying view is already updated
                });
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onShownSuggestedDialog(this);
    }

    @Override
    public void render(Program program, ProgramWateringTimes programWateringTimes) {
        tvDefaultZoneProperties.setVisibility(programWateringTimes.hasDefaultAdvancedSettings ? View
                .VISIBLE : View.GONE);

        List<ProgramWateringTimes.SelectedDayDuration> values = ProgramWateringTimes
                .suggestedProgramWateringDurations(programWateringTimes, program);
        tvDeterminedDuration.setText(programFormatter.wateringTimesDuration(program, values));

        int percentage = (int) (programWateringTimes.userPercentage * 100);
        tvUserPercentage.setText(getResources().getString(R.string.all_percentage, percentage));
    }

    @OnClick({R.id.btn_minus, R.id.btn_plus, R.id.tv_default_zone_properties})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_minus:
                presenter.onClickMinus();
                break;
            case R.id.btn_plus:
                presenter.onClickPlus();
                break;
            case R.id.tv_default_zone_properties:
                presenter.onClickDefaultZone();
                break;
        }
    }
}
