package com.rainmachine.presentation.screens.dashboardgraphs;

import android.view.View;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.rainmachine.R;

class DashboardGraphSectionModel extends EpoxyModelWithHolder<DashboardGraphSectionModel
        .ViewHolder> {

    @Override
    protected ViewHolder createNewHolder() {
        return new ViewHolder();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_dashboard_section;
    }

    class ViewHolder extends EpoxyHolder {
        @Override
        protected void bindView(View itemView) {
            // Do nothing
        }
    }
}
