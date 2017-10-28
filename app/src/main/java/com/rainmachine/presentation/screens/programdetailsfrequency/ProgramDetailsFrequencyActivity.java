package com.rainmachine.presentation.screens.programdetailsfrequency;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.DatePickerDialogFragment;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;
import com.rainmachine.presentation.screens.programdetails.ProgramDetailsActivity;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProgramDetailsFrequencyActivity extends SprinklerActivity implements
        ProgramDetailsFrequencyContract.Container {

    public static final String EXTRA_PROGRAM_DETAILS_FREQUENCY = "extra_program_details_frequency";

    @Inject
    ProgramDetailsFrequencyContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context, Program program,
                                        LocalDateTime sprinklerLocalDateTime) {
        Intent intent = new Intent(context, ProgramDetailsFrequencyActivity.class);
        ProgramDetailsFrequencyExtra extra = new ProgramDetailsFrequencyExtra();
        extra.program = program;
        extra.sprinklerLocalDateTime = sprinklerLocalDateTime;
        intent.putExtra(EXTRA_PROGRAM_DETAILS_FREQUENCY, Parcels.wrap(extra));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_program_details_frequency);
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
        return new ProgramDetailsFrequencyModule(this);
    }

    @Override
    public void onBackPressed() {
        presenter.onClickBack();
    }

    @Override
    public ProgramDetailsFrequencyExtra getExtra() {
        return getParcelable(EXTRA_PROGRAM_DETAILS_FREQUENCY);
    }

    @Override
    public void closeScreen(Program program) {
        Intent intent = new Intent();
        intent.putExtra(ProgramDetailsActivity.EXTRA_PROGRAM_MODIFIED_FREQUENCY, Parcels.wrap
                (program));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void showWeekDaysDialog(Program program) {
        ItemsSelectedDays items = new ItemsSelectedDays();
        items.items = new ArrayList<>();
        String[] weekDays = getResources().getStringArray(R.array.all_week_days);
        for (int i = 0; i < program.frequencyWeekDays().length; i++) {
            ItemSelectedDay item = new ItemSelectedDay(i, weekDays[i], program
                    .frequencyWeekDays()[i]);
            items.items.add(item);
        }
        DialogFragment dialog = SelectedDaysDialogFragment.newInstance(0,
                getString(R.string.program_details_selected_days),
                getString(R.string.all_ok), items);
        showDialogSafely(dialog);
    }

    @Override
    public void showEveryNDaysDialog(int dialogId, Program program) {
        String[] items = getResources().getStringArray(R.array.program_details_every_n_days);
        int checkedPosition = program.frequencyNumDays() - 2;
        DialogFragment dialog = RadioOptionsDialogFragment.newInstance(dialogId, getString(R
                .string.all_frequency), getString(R.string.all_ok), items, checkedPosition);
        showDialogSafely(dialog);
    }

    @Override
    public void showNextRunDialog(LocalDate date) {
        DialogFragment dialog = DatePickerDialogFragment.newInstance(0, getString(R.string
                .all_save), date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
        showDialogSafely(dialog);
    }
}
