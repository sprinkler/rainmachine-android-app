package com.rainmachine.presentation.screens.nearbystations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.rainmachine.R;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.presentation.activities.SprinklerActivity;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NearbyStationsActivity extends SprinklerActivity {

    public static final String EXTRA = "extra";

    @Inject
    NearbyStationsPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private View customActionBarView;

    public static Intent getStartIntent(Context context, Parser parser, boolean
            initialParserEnabled) {
        Intent intent = new Intent(context, NearbyStationsActivity.class);
        NearbyStationsExtra extra = new NearbyStationsExtra();
        extra.parser = parser;
        extra.initialParserEnabled = initialParserEnabled;
        intent.putExtra(EXTRA, Parcels.wrap(extra));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_nearby_stations);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            addFragment(R.id.map, new NearbyStationsLocationFragment());
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.nearby_stations_subtitle);

        buildCustomActionBarView();
    }

    @Override
    public Object getModule() {
        return new NearbyStationsModule(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    private void buildCustomActionBarView() {
        // Inflate a "Done/Cancel" custom action bar view.
        customActionBarView = View.inflate(getSupportActionBar().getThemedContext(), R.layout
                .include_actionbar_discard_save, null);
        customActionBarView.findViewById(R.id.actionbar_save).setOnClickListener(
                v -> presenter.onClickSave());
        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                v -> presenter.onClickDiscard());

        // Show the custom action bar view and hide the normal Home icon and title.
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void showActionBarView() {
        customActionBarView.setVisibility(View.VISIBLE);
    }

    public void hideActionBarView() {
        customActionBarView.setVisibility(View.INVISIBLE);
    }
}
