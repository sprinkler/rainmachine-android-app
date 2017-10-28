package com.rainmachine.presentation.screens.dashboardgraphs;

import com.airbnb.epoxy.TypedEpoxyController;
import com.rainmachine.data.local.database.model.DashboardGraphs;

class DashboardGraphsController extends TypedEpoxyController<DashboardGraphsViewModel> {

    private static final int OFFSET_PROGRAM_ID = 100; // not to conflict with other ids

    private DashboardGraphsPresenter presenter;

    DashboardGraphsController(DashboardGraphsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void buildModels(DashboardGraphsViewModel viewModel) {
        for (DashboardGraphs.DashboardGraph dashboardGraph : viewModel.generalGraphs) {
            new DashboardGraphItemModel_()
                    .id(dashboardGraph.graphType.ordinal())
                    .item(dashboardGraph)
                    .changeListener((buttonView, isChecked) -> presenter.onCheckedChangedItem
                            (dashboardGraph, isChecked))
                    .addTo(this);
        }

        if (!viewModel.programGraphs.isEmpty()) {
            new DashboardGraphSectionModel()
                    .id("section")
                    .addTo(this);

            for (DashboardGraphs.DashboardGraph dashboardGraph : viewModel.programGraphs) {
                new DashboardGraphItemModel_()
                        .id(OFFSET_PROGRAM_ID + dashboardGraph.programId)
                        .item(dashboardGraph)
                        .changeListener((buttonView, isChecked) -> presenter.onCheckedChangedItem
                                (dashboardGraph, isChecked))
                        .addTo(this);
            }
        }
    }
}
