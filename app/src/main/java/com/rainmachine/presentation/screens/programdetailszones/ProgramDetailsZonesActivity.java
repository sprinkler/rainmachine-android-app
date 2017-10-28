package com.rainmachine.presentation.screens.programdetailszones;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.programdetails.ProgramDetailsActivity;
import com.rainmachine.presentation.screens.programdetailsold.ZoneDurationDialogFragment;
import com.rainmachine.presentation.screens.zonedetails.ZoneDetailsActivity;

import org.joda.time.LocalDateTime;
import org.parceler.Parcels;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProgramDetailsZonesActivity extends SprinklerActivity implements
        ProgramDetailsZonesContract.Container {

    public static final String EXTRA_PROGRAM_DETAILS_ZONES = "extra_program_details_zones";
    private static final int REQ_CODE_ZONE = 1;

    @Inject
    ProgramDetailsZonesContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context, Program program, int positionInList,
                                        LocalDateTime sprinklerLocalDateTime) {
        Intent intent = new Intent(context, ProgramDetailsZonesActivity.class);
        ProgramDetailsZonesExtra extra = new ProgramDetailsZonesExtra();
        extra.program = program;
        extra.positionInList = positionInList;
        extra.sprinklerLocalDateTime = sprinklerLocalDateTime;
        intent.putExtra(EXTRA_PROGRAM_DETAILS_ZONES, Parcels.wrap(extra));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_program_details_zones);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_ZONE && resultCode == Activity.RESULT_OK) {
            ZoneProperties zoneProperties = Parcels.unwrap(data.getParcelableExtra
                    (ZoneDetailsActivity.EXTRA_RESULT_ZONE));
            presenter.onComingBackFromChangingZoneProperties(zoneProperties);
        }
    }

    @Override
    public Object getModule() {
        return new ProgramDetailsZonesModule(this);
    }

    @Override
    public void onBackPressed() {
        presenter.onClickBack();
    }

    @Override
    public ProgramDetailsZonesExtra getExtra() {
        return getParcelable(EXTRA_PROGRAM_DETAILS_ZONES);
    }

    @Override
    public void closeScreen(Program program) {
        Intent intent = new Intent();
        intent.putExtra(ProgramDetailsActivity.EXTRA_PROGRAM_MODIFIED_ZONES, Parcels.wrap(program));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void updateTitle(ProgramWateringTimes programWateringTimes) {
        String title = getString(R.string.program_details_zone_name_index, String.format
                (Locale.ENGLISH, "%02d", programWateringTimes.id), programWateringTimes.name);
        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void showCustomZoneDurationDialog(ProgramWateringTimes programWateringTimes) {
        DialogFragment dialog = ZoneDurationDialogFragment.newInstance(programWateringTimes.id,
                programWateringTimes.name, (int) programWateringTimes.duration, true);
        showDialogSafely(dialog);
    }

    @Override
    public void goToZone(long id) {
        startActivityForResult(ZoneDetailsActivity.getStartIntent(this, id), REQ_CODE_ZONE);
    }
}
