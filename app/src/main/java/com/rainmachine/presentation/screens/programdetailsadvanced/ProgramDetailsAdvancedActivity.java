package com.rainmachine.presentation.screens.programdetailsadvanced;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.ClickableRadioOptionsDialogFragment;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;
import com.rainmachine.presentation.screens.programdetails.ProgramDetailsActivity;
import com.rainmachine.presentation.screens.programdetailsold.CycleSoakDialogFragment;
import com.rainmachine.presentation.screens.programdetailsold.StationDelayDialogFragment;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProgramDetailsAdvancedActivity extends SprinklerActivity implements
        ProgramDetailsAdvancedContract.Container {

    public static final String EXTRA_PROGRAM_DETAILS_ADVANCED = "extra_program_details_advanced";

    @Inject
    ProgramDetailsAdvancedContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context, Program program, boolean isUnitsMetric) {
        Intent intent = new Intent(context, ProgramDetailsAdvancedActivity.class);
        ProgramDetailsAdvancedExtra extra = new ProgramDetailsAdvancedExtra();
        extra.program = program;
        extra.isUnitsMetric = isUnitsMetric;
        intent.putExtra(EXTRA_PROGRAM_DETAILS_ADVANCED, Parcels.wrap(extra));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_program_details_advanced);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
    }

    @Override
    public Object getModule() {
        return new ProgramDetailsAdvancedModule(this);
    }

    @Override
    public void onBackPressed() {
        presenter.onClickBack();
    }

    @Override
    public ProgramDetailsAdvancedExtra getExtra() {
        return getParcelable(EXTRA_PROGRAM_DETAILS_ADVANCED);
    }

    @Override
    public void closeScreen(Program program) {
        Intent intent = new Intent();
        intent.putExtra(ProgramDetailsActivity.EXTRA_PROGRAM_MODIFIED_ADVANCED, Parcels.wrap
                (program));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void showDelayZonesDialog(int dialogId, Program program) {
        String[] items = new String[]{getString(R.string.all_custom), getString(R.string
                .program_details_off)};
        int checkedItemPosition = program.isDelayEnabled ? 0 : 1;
        DialogFragment dialog = ClickableRadioOptionsDialogFragment.newInstance(dialogId,
                getString(R.string
                        .program_details_station_delay), items, checkedItemPosition);
        showDialogSafely(dialog);
    }

    @Override
    public void showCycleSoakDialog(int dialogId, Program program) {
        String[] items = new String[]{getString(R.string.all_custom), getString(R.string
                .program_details_auto), getString(R.string.program_details_off)};
        int checkedItemPosition = program.isCycleSoakEnabled ? (program.isCycleSoakAuto() ? 1
                : 0) : 2;
        DialogFragment dialog = ClickableRadioOptionsDialogFragment.newInstance(dialogId,
                getString(R.string
                        .program_details_cycle_soak), items, checkedItemPosition);
        showDialogSafely(dialog);
    }

    @Override
    public void showCustomDelayZonesDialog(Program program) {
        DialogFragment dialog = StationDelayDialogFragment.newInstance(program.delaySeconds, true);
        showDialogSafely(dialog);
    }

    @Override
    public void showCustomCycleSoakDialog(Program program) {
        DialogFragment dialog = CycleSoakDialogFragment.newInstance(program.numCycles, program
                .soakSeconds);
        showDialogSafely(dialog);
    }

    @Override
    public void showDoNotRunDialog(int dialogId, Program program, boolean isUnitsMetric) {
        String[] items;
        int[] values = getResources().getIntArray(R.array.program_details_not_run_values);
        int checkedItemPosition = values.length; // default: the one after the last is Not set
        if (isUnitsMetric) {
            items = getResources().getStringArray(R.array.program_details_not_run_mm);
            for (int i = 0; i < values.length; i++) {
                int value = values[i];
                if (value == program.maxRainAmountMm) {
                    checkedItemPosition = i;
                    break;
                }
            }
        } else {
            items = getResources().getStringArray(R.array.program_details_not_run_inch);
            for (int i = 0; i < values.length; i++) {
                int value = values[i];
                if (value == program.maxRainAmountMm) {
                    checkedItemPosition = i;
                    break;
                }
            }
        }
        DialogFragment dialog = RadioOptionsDialogFragment.newInstance(dialogId, getString(R.string
                        .program_details_not_run_program), getString(R.string.all_ok), items,
                checkedItemPosition);
        showDialogSafely(dialog);
    }

    @Override
    public int getMaxRainAmount(int checkedItemPosition) {
        int[] values = getResources().getIntArray(R.array.program_details_not_run_values);
        if (checkedItemPosition >= 0 && checkedItemPosition <= values.length - 1) {
            return values[checkedItemPosition];
        } else {
            return 0;
        }
    }
}
