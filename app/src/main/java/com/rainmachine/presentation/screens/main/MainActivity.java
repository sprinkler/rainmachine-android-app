package com.rainmachine.presentation.screens.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.usecases.watering.GetWateringLive;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.screens.programdetailsold.ZoneDurationDialogFragment;
import com.rainmachine.presentation.screens.programs.ProgramsContract;
import com.rainmachine.presentation.screens.stats.StatsContract;
import com.rainmachine.presentation.screens.stats.StatsView;
import com.rainmachine.presentation.screens.statsdetails.StatsDetailsExtra;
import com.rainmachine.presentation.screens.waternow.WaterNowContract;
import com.rainmachine.presentation.screens.waternow.WaterNowState;
import com.rainmachine.presentation.screens.waternow.ZoneViewModel;
import com.rainmachine.presentation.screens.zonedetails.ZoneDetailsActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.widgets.CustomViewPager;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import timber.log.Timber;

public class MainActivity extends SprinklerActivity implements WaterNowContract.Container {

    public static final String EXTRA_AFTER_FINISHING_SETUP = "after_finishing_setup";

    private static final int FLIPPER_CHILD_TABS = 0;
    private static final int FLIPPER_CHILD_PROGRESS = 1;
    public static final int REQ_CODE_STATS_DETAILS = 1;
    private static final int TAB_POSITION_STATS = 0;
    private static final int TAB_POSITION_WATER_NOW = 1;
    private static final int TAB_POSITION_PROGRAMS = 2;
    private static final int TAB_NUM = 4;

    private static final int FLIPPER_SPECIAL_PROGRESS = 0;
    private static final int FLIPPER_SPECIAL_TIMER = 1;

    public static int latestChartViewType = StatsDetailsExtra.TYPE_WEEK;

    @Inject
    MainPresenter presenter;
    @Inject
    StatsContract.Presenter statsPresenter;
    @Inject
    WaterNowContract.Presenter waterNowPresenter;
    @Inject
    ProgramsContract.Presenter programsPresenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.pager)
    CustomViewPager viewPager;
    @BindView(R.id.flipper_main)
    ViewFlipper flipperMain;
    @BindView(R.id.view_restriction_live)
    ViewGroup viewRestrictionNotification;
    @BindView(R.id.view_watering_live)
    ViewGroup viewWateringNotification;
    @BindView(R.id.tv_watering_info)
    TextView tvWateringInfo;
    @BindView(R.id.tv_watering_info_subtitle)
    TextView tvWateringInfoSubtitle;
    @BindView(R.id.tv_special_timer)
    TextView tvSpecialTimer;
    @BindView(R.id.flipper_special_timer)
    ViewFlipper flipperSpecialTimer;
    @BindView(R.id.tv_restriction_text)
    TextView tvRestrictionText;
    @BindView(R.id.icon_watering)
    ImageView ivIconWatering;
    @BindView(R.id.tv_num_active_restrictions)
    TextView tvNumActiveRestrictions;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.progress_text)
    TextView progressText;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private View customActionBarView;
    @State
    int currentTabPosition = TAB_POSITION_STATS;
    @State
    boolean[] isEditMode = new boolean[TAB_NUM];

    public static Intent getStartIntent(Context context, boolean afterFinishingSetup) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_AFTER_FINISHING_SETUP, afterFinishingSetup);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        buildCustomActionBar();
        updateCustomActionBarTitle();

        presenter.init();

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter();
        viewPager.setPagingEnabled(false);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_action_dashboard));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_action_zones));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_action_programs));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_action_settings));
        tabLayout.addOnTabSelectedListener(tabSelectedListener);
        //noinspection ConstantConditions
        tabLayout.getTabAt(currentTabPosition).select();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem editItem = menu.findItem(R.id.menu_edit);
        if (currentTabPosition == TAB_POSITION_STATS) {
            editItem.setTitle(isEditMode[currentTabPosition] ? R.string.all_done : R.string
                    .stats_edit);
            editItem.setVisible(true);

            if (currentTabPosition == TAB_POSITION_STATS) {
                statsPresenter.onChangeEditMode(isEditMode[currentTabPosition]);
            }
        } else {
            editItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_edit) {
            isEditMode[currentTabPosition] = !isEditMode[currentTabPosition];
            if (currentTabPosition == TAB_POSITION_STATS) {
                statsPresenter.onChangeEditMode(isEditMode[currentTabPosition]);
            }
            supportInvalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
        statsPresenter.start();
        waterNowPresenter.start();
        programsPresenter.start();

        // Coming from wizard, we want to see the programs tab directly
        boolean afterFinishingSetup = getIntent().getBooleanExtra(
                EXTRA_AFTER_FINISHING_SETUP, false);
        if (afterFinishingSetup) {
            //noinspection ConstantConditions
            tabLayout.getTabAt(TAB_POSITION_PROGRAMS).select();
            getIntent().removeExtra(EXTRA_AFTER_FINISHING_SETUP);
            presenter.onComingFromSetup();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stop();
        statsPresenter.stop();
        waterNowPresenter.stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onDestroy() {
        // Because of weird use case where the sprinkler graph is null and we force finish this
        // activity without having injected anything
        if (waterNowPresenter != null && tabLayout != null) {
            waterNowPresenter.destroy();
            tabLayout.removeOnTabSelectedListener(tabSelectedListener);
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_STATS_DETAILS && viewPager.getCurrentItem() == 0) {
            Timber.i("Return from stats details screen");
            View view = viewPager.findViewWithTag(viewPager.getCurrentItem());
            if (view != null && view instanceof StatsView) {
                ((StatsView) view).updateChartViewType(MainActivity.latestChartViewType);
            }
        }
    }

    @Override
    public Object getModule() {
        return new MainModule(this);
    }

    @Override
    public void goToZoneScreen(long zoneId) {
        startActivity(ZoneDetailsActivity.getStartIntent(this, zoneId));
    }

    @OnClick(R.id.view_restriction_live)
    public void onClickRestrictionLive() {
        presenter.onClickRestrictionLive();
    }

    @OnClick(R.id.view_watering_live)
    public void onClickWateringLive(View view) {
        presenter.onClickWateringLive((GetWateringLive.ResponseModel) view.getTag());
    }

    public void showProgramsScreen() {
        //noinspection ConstantConditions
        tabLayout.getTabAt(TAB_POSITION_PROGRAMS).select();
    }

    public void showWaterNowScreen() {
        //noinspection ConstantConditions
        tabLayout.getTabAt(TAB_POSITION_WATER_NOW).select();
    }

    public void render(boolean isUpdateInProgress, boolean isResetInProgress) {
        if (isUpdateInProgress || isResetInProgress) {
            progressText.setText(isUpdateInProgress ? getString(R.string.main_update_progress) :
                    null);
            flipperMain.setDisplayedChild(FLIPPER_CHILD_PROGRESS);
        } else {
            flipperMain.setDisplayedChild(FLIPPER_CHILD_TABS);
        }
    }

    public void showContentRestrictionLive(String text, int numActiveRestrictions) {
        tvRestrictionText.setText(text);
        tvNumActiveRestrictions.setText(String.format(Locale.ENGLISH, "%d", numActiveRestrictions));
        viewRestrictionNotification.setVisibility(View.VISIBLE);
        invalidateCurrentPagerScreen();
    }

    public void hideContentRestrictionLive() {
        viewRestrictionNotification.setVisibility(View.GONE);
        invalidateCurrentPagerScreen();
    }

    public void showContentWateringLive(GetWateringLive.ResponseModel responseModel) {
        String text = null;
        if (responseModel.wateringState == GetWateringLive.ResponseModel.WateringState
                .MASTER_VALVE_START) {
            text = getString(R.string.water_now_master_valve_pre_open);
            tvSpecialTimer.setText(formatter.hourMinSecColon(responseModel.runningCounter));
            flipperSpecialTimer.setDisplayedChild(FLIPPER_SPECIAL_TIMER);
        } else if (responseModel.wateringState == GetWateringLive.ResponseModel.WateringState
                .MASTER_VALVE_STOP) {
            text = getString(R.string.water_now_master_valve_post_open);
            tvSpecialTimer.setText(formatter.hourMinSecColon(responseModel.runningCounter));
            flipperSpecialTimer.setDisplayedChild(FLIPPER_SPECIAL_TIMER);
        } else if (responseModel.wateringState == GetWateringLive.ResponseModel.WateringState
                .DELAY_BETWEEN_ZONES) {
            text = getString(R.string.water_now_zone_delay);
            flipperSpecialTimer.setDisplayedChild(FLIPPER_SPECIAL_PROGRESS);
        } else if (responseModel.wateringState == GetWateringLive.ResponseModel.WateringState
                .SOAKING) {
            text = getString(R.string.water_now_soak_time, responseModel.currentCycle,
                    responseModel.numTotalCycles);
            flipperSpecialTimer.setDisplayedChild(FLIPPER_SPECIAL_PROGRESS);
        } else if (responseModel.wateringState == GetWateringLive.ResponseModel.WateringState
                .ZONE_RUNNING) {
            String zoneName;
            if (!responseModel.zoneName.toLowerCase().startsWith("zone")) {
                zoneName = getString(R.string.main_zone_name, responseModel.zoneName);
            } else {
                zoneName = responseModel.zoneName;
            }
            text = zoneName;
            tvSpecialTimer.setText(formatter.hourMinSecColon(responseModel.runningCounter));
            flipperSpecialTimer.setDisplayedChild(FLIPPER_SPECIAL_TIMER);
        }
        tvWateringInfo.setText(text);

        if (responseModel.isProgramRunning) {
            StringBuilder sb = new StringBuilder();
            if (responseModel.showCycle) {
                sb.append(getString(R.string.main_cycle, responseModel.currentCycle, responseModel
                        .numTotalCycles));
                if (!Strings.isBlank(responseModel.programName)) {
                    sb.append(", ");
                }
            }
            if (!Strings.isBlank(responseModel.programName)) {
                String programName;
                if (!responseModel.programName.toLowerCase().startsWith("program")) {
                    programName = getString(R.string.main_program_name, responseModel.programName);
                } else {
                    programName = responseModel.programName;
                }
                sb.append(programName);
            }
            tvWateringInfoSubtitle.setText(sb.toString());
            tvWateringInfoSubtitle.setVisibility(View.VISIBLE);
        } else {
            tvWateringInfoSubtitle.setVisibility(View.GONE);
        }

        ivIconWatering.setImageResource(responseModel.isManual ? R.drawable.ic_watering_manual : R
                .drawable.ic_watering_scheduled);
        viewWateringNotification.setTag(responseModel);
        viewWateringNotification.setVisibility(View.VISIBLE);
        invalidateCurrentPagerScreen();
    }

    public void hideContentWateringLive() {
        viewWateringNotification.setVisibility(View.GONE);
        invalidateCurrentPagerScreen();
    }

    private void invalidateCurrentPagerScreen() {
        View view = viewPager.findViewWithTag(viewPager.getCurrentItem());
        if (view != null) {
            view.invalidate();
        }
    }

    private void buildCustomActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        customActionBarView = View.inflate(actionBar.getThemedContext(), R.layout
                .include_actionbar_main, null);
        customActionBarView.findViewById(R.id.btn_devices).setOnClickListener(v -> finish());
        // Show the custom action bar view and hide the normal Home icon and title.
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void updateCustomActionBarTitle() {
        ((TextView) customActionBarView.findViewById(R.id.title)).setText(device.name);
    }

    public boolean isWaterNowScreen() {
        return currentTabPosition == TAB_POSITION_WATER_NOW;
    }

    @Override
    public void render(@NonNull WaterNowState viewModel) {
        if (viewModel.showStartZoneDialog) {
            showStartZoneDialog(viewModel.dialogStartZone, viewModel.showMinutesSeconds);
        } else if (viewModel.showStopAllDialog) {
            showStopAllDialog();
        }
    }

    private void showStartZoneDialog(@NonNull ZoneViewModel zoneViewModel, boolean
            showMinutesSeconds) {
        DialogFragment dialog = ZoneDurationDialogFragment.newInstance(zoneViewModel.id,
                zoneViewModel.name, zoneViewModel.defaultManualStartDuration, showMinutesSeconds);
        showDialogSafely(dialog);
        waterNowPresenter.onShowingStartZoneDialog();
    }

    private void showStopAllDialog() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance(
                MainPresenter.DIALOG_ID_ACTION_STOP_ALL,
                getString(R.string.all_are_you_sure),
                getString(R.string.water_now_stop_all_sure),
                getString(R.string.all_yes), getString(R.string.all_no));
        showDialogSafely(dialog);
        waterNowPresenter.onShowingStopAllDialog();
    }

    public void onConfirmStopAll() {
        waterNowPresenter.onConfirmStopAll();
    }

    private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout
            .OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            supportInvalidateOptionsMenu();
            currentTabPosition = tab.getPosition();
            viewPager.setCurrentItem(currentTabPosition);
            presenter.onTabSelected();
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };

    private class ViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        private ViewPagerAdapter() {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int layoutId = View.NO_ID;
            if (position == TAB_POSITION_STATS) {
                layoutId = R.layout.view_stats;
            } else if (position == TAB_POSITION_WATER_NOW) {
                layoutId = R.layout.view_water_now;
            } else if (position == TAB_POSITION_PROGRAMS) {
                layoutId = R.layout.view_programs;
            } else if (position == 3) {
                layoutId = R.layout.view_settings;
            }
            View view = layoutInflater.inflate(layoutId, container, false);
            view.setTag(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.main_title_tab_stats);
                case TAB_POSITION_WATER_NOW:
                    return getString(R.string.main_title_tab_water);
                case TAB_POSITION_PROGRAMS:
                    return getString(R.string.main_programs);
                case 3:
                    return getString(R.string.main_title_tab_settings);
            }
            return null;
        }
    }
}
