package com.rainmachine.presentation.screens.statsdetails;

import com.rainmachine.presentation.screens.stats.StatsMixer;
import com.rainmachine.presentation.screens.stats.StatsViewModel;
import com.rainmachine.presentation.util.BasePresenter;

class StatsDetailsPresenter extends BasePresenter<StatsDetailsView> {

    private StatsDetailsActivity activity;
    private StatsMixer mixer;

    private StatsDetailsExtra extra;

    StatsDetailsPresenter(StatsDetailsActivity activity, StatsMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        extra = activity.getParcelable(StatsDetailsActivity.EXTRA_STATS_DETAILS);
    }

    @Override
    public void init() {
        StatsViewModel viewModel = mixer.getLatestData();
        if (viewModel != null
                && ((extra.type == StatsDetailsExtra.TYPE_WEEK && viewModel.weekCategory != null)
                || (extra.type == StatsDetailsExtra.TYPE_MONTH && viewModel.monthCategory != null)
                || (extra.type == StatsDetailsExtra.TYPE_YEAR && viewModel.yearCategory != null))) {
            view.render(viewModel, extra);
        } else {
            activity.finish();
        }
    }
}
