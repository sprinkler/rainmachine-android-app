package com.rainmachine.presentation.screens.dashboardgraphs;

import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.rainmachine.R;
import com.rainmachine.data.local.database.model.DashboardGraphs;
import com.rainmachine.infrastructure.util.BaseApplication;

import butterknife.BindView;
import butterknife.ButterKnife;

@EpoxyModelClass
class DashboardGraphItemModel extends EpoxyModelWithHolder<DashboardGraphItemModel.ViewHolder> {

    @EpoxyAttribute
    DashboardGraphs.DashboardGraph item;
    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    CompoundButton.OnCheckedChangeListener changeListener;

    @Override
    public void bind(ViewHolder holder) {
        String name = "";
        if (item.graphType == DashboardGraphs.GraphType.WEATHER) {
            name = BaseApplication.getContext().getResources().getString(R.string.all_weather);
            holder.toggleEnabled.setOnCheckedChangeListener(null);
            holder.toggleEnabled.setChecked(item.isEnabled);
            holder.toggleEnabled.setEnabled(false);
        } else {
            if (item.graphType == DashboardGraphs.GraphType.TEMPERATURE) {
                name = BaseApplication.getContext().getResources().getString(R.string
                        .all_temperature_max_min);
            } else if (item.graphType == DashboardGraphs.GraphType.RAIN_AMOUNT) {
                name = BaseApplication.getContext().getResources().getString(R.string
                        .all_rain_amount);
            } else if (item.graphType == DashboardGraphs.GraphType.DAILY_WATER_NEED) {
                name = BaseApplication.getContext().getResources().getString(R.string
                        .all_daily_water_need);
            } else if (item.graphType == DashboardGraphs.GraphType.PROGRAM) {
                name = item.programName;
            }

            holder.toggleEnabled.setOnCheckedChangeListener(null);
            holder.toggleEnabled.setChecked(item.isEnabled);
            holder.toggleEnabled.setOnCheckedChangeListener(changeListener);
            holder.toggleEnabled.setEnabled(true);
        }
        holder.tvName.setText(name);
    }

    @Override
    protected ViewHolder createNewHolder() {
        return new ViewHolder();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_dashboard;
    }

    class ViewHolder extends EpoxyHolder implements View.OnClickListener {

        @BindView(R.id.name)
        TextView tvName;
        @BindView(R.id.toggle_enabled)
        SwitchCompat toggleEnabled;

        @Override
        protected void bindView(View view) {
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            toggleEnabled.toggle();
        }
    }
}
