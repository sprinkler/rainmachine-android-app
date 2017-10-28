package com.rainmachine.presentation.screens.programdetailsold;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.programdetails.ProgramDetailsExtra;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProgramDetailsOldActivity extends SprinklerActivity {

    public static final String EXTRA_PROGRAM_DETAILS = "extra_program_details";

    @Inject
    ProgramDetailsOldPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private View customActionBarView;

    public static Intent getStartIntent(Context context, Program program,
                                        LocalDateTime sprinklerLocalDateTime, boolean
                                                use24HourFormat) {
        Intent intent = new Intent(context, ProgramDetailsOldActivity.class);
        ProgramDetailsExtra extra = new ProgramDetailsExtra();
        extra.program = program.cloneIt();
        extra.originalProgram = program;
        extra.sprinklerLocalDateTime = sprinklerLocalDateTime;
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
        setContentView(R.layout.activity_program_details_old);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        setupCustomActionBar();
    }

    @Override
    public void onBackPressed() {
        presenter.onClickDiscardOrBack();
    }

    public Object getModule() {
        return new ProgramDetailsOldModule(this);
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

    public void toggleCustomActionBar(boolean makeVisible) {
        customActionBarView.setVisibility(makeVisible ? View.VISIBLE : View.INVISIBLE);
    }
}
