package com.rainmachine.presentation.screens.programdetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.screens.programdetailsadvanced.ProgramDetailsAdvancedActivity;
import com.rainmachine.presentation.screens.programdetailsfrequency.ProgramDetailsFrequencyActivity;
import com.rainmachine.presentation.screens.programdetailsstarttime.ProgramDetailsStartTimeActivity;
import com.rainmachine.presentation.screens.programdetailszones.ProgramDetailsZonesActivity;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProgramDetailsActivity extends SprinklerActivity implements ProgramDetailsContract
        .Container {

    public static final String EXTRA_PROGRAM_DETAILS = "extra_program_details";
    public static final String EXTRA_PROGRAM_MODIFIED_ZONES = "extra_program_modified_zones";
    public static final String EXTRA_PROGRAM_MODIFIED_FREQUENCY =
            "extra_program_modified_frequency";
    public static final String EXTRA_PROGRAM_MODIFIED_START_TIME =
            "extra_program_modified_start_time";
    public static final String EXTRA_PROGRAM_MODIFIED_ADVANCED =
            "extra_program_modified_advanced";

    private static final int DIALOG_ID_ACTION_MESSAGE_DISCARD = 1;

    private static final int REQ_CODE_PROGRAM_DETAILS_ZONES = 1;
    private static final int REQ_CODE_PROGRAM_DETAILS_FREQUENCY = 2;
    private static final int REQ_CODE_PROGRAM_DETAILS_START_TIME = 3;
    private static final int REQ_CODE_PROGRAM_DETAILS_ADVANCED = 4;

    @Inject
    ProgramDetailsContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private View customActionBarView;

    public static Intent getStartIntent(Context context, Program program,
                                        LocalDateTime sprinklerLocalDateTime, boolean
                                                isUnitsMetric, boolean use24HourFormat) {
        Intent intent = new Intent(context, ProgramDetailsActivity.class);
        ProgramDetailsExtra extra = new ProgramDetailsExtra();
        extra.program = program.cloneIt();
        extra.originalProgram = program;
        extra.sprinklerLocalDateTime = sprinklerLocalDateTime;
        extra.isUnitsMetric = isUnitsMetric;
        extra.use24HourFormat = use24HourFormat;
        intent.putExtra(EXTRA_PROGRAM_DETAILS, Parcels.wrap(extra));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_program_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        setupCustomActionBar();
    }

    @Override
    public Object getModule() {
        return new ProgramDetailsModule(this);
    }

    @Override
    public void onBackPressed() {
        presenter.onClickDiscardOrBack();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PROGRAM_DETAILS_ZONES) {
            // Fix a crash report although I do not know why the intent can be null
            if (data != null) {
                Program program = Parcels.unwrap(data.getParcelableExtra
                        (EXTRA_PROGRAM_MODIFIED_ZONES));
                presenter.onComingBackFromZones(program);
            }
        } else if (requestCode == REQ_CODE_PROGRAM_DETAILS_FREQUENCY) {
            Program program = Parcels.unwrap(data.getParcelableExtra
                    (EXTRA_PROGRAM_MODIFIED_FREQUENCY));
            presenter.onComingBackFromFrequency(program);
        } else if (requestCode == REQ_CODE_PROGRAM_DETAILS_START_TIME) {
            Program program = Parcels.unwrap(data.getParcelableExtra
                    (EXTRA_PROGRAM_MODIFIED_START_TIME));
            presenter.onComingBackFromStartTime(program);
        } else if (requestCode == REQ_CODE_PROGRAM_DETAILS_ADVANCED) {
            Program program = Parcels.unwrap(data.getParcelableExtra
                    (EXTRA_PROGRAM_MODIFIED_ADVANCED));
            presenter.onComingBackFromAdvanced(program);
        }
    }

    @Override
    public ProgramDetailsExtra getExtra() {
        return getParcelable(EXTRA_PROGRAM_DETAILS);
    }

    @Override
    public void showDiscardDialog() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance
                (DIALOG_ID_ACTION_MESSAGE_DISCARD,
                        getString(R.string.all_unsaved_changes),
                        getString(R.string.program_details_unsaved_changes_program),
                        getString(R.string.all_yes),
                        getString(R.string.all_no));
        showDialogSafely(dialog);
    }

    @Override
    public void closeScreen() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void toggleCustomActionBar(boolean makeVisible) {
        customActionBarView.setVisibility(makeVisible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void goToProgramZone(Program program, int positionInList, LocalDateTime
            sprinklerLocalDateTime) {
        Intent intent = ProgramDetailsZonesActivity.getStartIntent(this, program,
                positionInList, sprinklerLocalDateTime);
        startActivityForResult(intent, REQ_CODE_PROGRAM_DETAILS_ZONES);
    }

    @Override
    public void goToStartTimeScreen(Program program, LocalDateTime sprinklerLocalDateTime,
                                    boolean use24HourFormat) {
        Intent intent = ProgramDetailsStartTimeActivity.getStartIntent(this, program,
                use24HourFormat, sprinklerLocalDateTime);
        startActivityForResult(intent, REQ_CODE_PROGRAM_DETAILS_START_TIME);
    }

    @Override
    public void goToFrequencyScreen(Program program, LocalDateTime sprinklerLocalDateTime) {
        Intent intent = ProgramDetailsFrequencyActivity.getStartIntent(this, program,
                sprinklerLocalDateTime);
        startActivityForResult(intent, REQ_CODE_PROGRAM_DETAILS_FREQUENCY);
    }

    @Override
    public void goToAdvancedScreen(Program program, boolean isUnitsMetric) {
        Intent intent = ProgramDetailsAdvancedActivity.getStartIntent(this, program, isUnitsMetric);
        startActivityForResult(intent, REQ_CODE_PROGRAM_DETAILS_ADVANCED);
    }

    private void setupCustomActionBar() {
        customActionBarView = View.inflate(getSupportActionBar().getThemedContext(), R.layout
                .include_actionbar_discard_save, null);
        ButterKnife.findById(customActionBarView, R.id.actionbar_save).setOnClickListener(
                v -> {
                    // "Save"
                    presenter.onClickSave();
                }
        );
        ButterKnife.findById(customActionBarView, R.id.actionbar_discard).setOnClickListener(
                v -> presenter.onClickDiscardOrBack()
        );

        // Show the custom action bar view and hide the normal Home icon and title.
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar
                .DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setCustomView(customActionBarView, new ActionBar.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
}
