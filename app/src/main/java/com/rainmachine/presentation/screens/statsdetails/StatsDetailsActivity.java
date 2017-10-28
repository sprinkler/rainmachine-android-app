package com.rainmachine.presentation.screens.statsdetails;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatsDetailsActivity extends SprinklerActivity {

    public static final String EXTRA_STATS_DETAILS = "extra_stats_details";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Activity activity, int type, int chart, int
            programId, String programName) {
        Intent intent = new Intent(activity, StatsDetailsActivity.class);
        StatsDetailsExtra extra = new StatsDetailsExtra();
        extra.type = type;
        extra.chart = chart;
        extra.programId = programId;
        extra.programName = programName;
        intent.putExtra(EXTRA_STATS_DETAILS, Parcels.wrap(extra));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_stats_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.stats_details_subtitle);
    }

    public Object getModule() {
        return new StatsDetailsModule(this);
    }
}
