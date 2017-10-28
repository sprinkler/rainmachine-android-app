package com.rainmachine.presentation.screens.savehourlyrestriction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.rainmachine.R;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.presentation.activities.SprinklerActivity;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SaveHourlyRestrictionActivity extends SprinklerActivity {

    public static final String EXTRA_HOURLY_RESTRICTION = "extra_hourly_restriction";

    @Inject
    SaveHourlyRestrictionPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private View customActionBarView;

    public static Intent getStartIntent(Context context, HourlyRestriction restriction, boolean
            use24HourFormat) {
        Intent intent = new Intent(context, SaveHourlyRestrictionActivity.class);
        SaveHourlyRestrictionExtra extra = new SaveHourlyRestrictionExtra();
        extra.restriction = restriction;
        extra.use24HourFormat = use24HourFormat;
        intent.putExtra(EXTRA_HOURLY_RESTRICTION, Parcels.wrap(extra));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_save_hourly_restriction);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.save_hourly_restriction_subtitle);

        buildCustomActionBar();
    }

    public Object getModule() {
        return new SaveHourlyRestrictionModule(this);
    }

    // Inflate a "Done/Cancel" custom action bar view.
    private void buildCustomActionBar() {
        customActionBarView = View.inflate(getSupportActionBar().getThemedContext(), R.layout
                .include_actionbar_discard_save, null);
        customActionBarView.findViewById(R.id.actionbar_save).setOnClickListener(
                v -> presenter.onClickSave()
        );
        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                v -> presenter.onClickDiscard()
        );

        // Show the custom action bar view and hide the normal Home icon and title.
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE
        );
        getSupportActionBar().setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
    }

    public void toggleCustomActionBar(boolean makeVisible) {
        customActionBarView.setVisibility(makeVisible ? View.VISIBLE : View.INVISIBLE);
    }
}
