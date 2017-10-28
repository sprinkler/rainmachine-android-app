package com.rainmachine.presentation.screens.programdetailsstarttime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramStartTime;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.TimePickerDialogFragment;
import com.rainmachine.presentation.screens.programdetails.ProgramDetailsActivity;
import com.rainmachine.presentation.screens.programdetailsold.SunriseSunsetDialogFragment;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProgramDetailsStartTimeActivity extends SprinklerActivity implements
        ProgramDetailsStartTimeContract.Container {

    public static final String EXTRA_PROGRAM_DETAILS_START_TIME =
            "extra_program_details_start_time";

    @Inject
    ProgramDetailsStartTimeContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context, Program program,
                                        boolean use24HourFormat, LocalDateTime
                                                sprinklerLocalDateTime) {
        Intent intent = new Intent(context, ProgramDetailsStartTimeActivity.class);
        ProgramDetailsStartTimeExtra extra = new ProgramDetailsStartTimeExtra();
        extra.program = program;
        extra.use24HourFormat = use24HourFormat;
        extra.sprinklerLocalDateTime = sprinklerLocalDateTime;
        intent.putExtra(EXTRA_PROGRAM_DETAILS_START_TIME, Parcels.wrap(extra));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_program_details_start_time);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            presenter.onClickBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Object getModule() {
        return new ProgramDetailsStartTimeModule(this);
    }

    @Override
    public void onBackPressed() {
        presenter.onClickBack();
    }

    @Override
    public ProgramDetailsStartTimeExtra getExtra() {
        return getParcelable(EXTRA_PROGRAM_DETAILS_START_TIME);
    }

    @Override
    public void closeScreen(Program program) {
        Intent intent = new Intent();
        intent.putExtra(ProgramDetailsActivity.EXTRA_PROGRAM_MODIFIED_START_TIME, Parcels.wrap
                (program));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void showSunriseSunsetDialog(ProgramStartTime programStartTime) {
        DialogFragment dialog = SunriseSunsetDialogFragment.newInstance(programStartTime);
        showDialogSafely(dialog);
    }

    @Override
    public void showTimeOfDayDialog(Program program, boolean use24HourFormat) {
        LocalDateTime dateTime = program.startTime.localDateTime;
        DialogFragment dialog = TimePickerDialogFragment.newInstance(0, getString(R.string
                .all_done), dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), use24HourFormat);
        showDialogSafely(dialog);
    }
}
