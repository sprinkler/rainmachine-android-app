package com.rainmachine.presentation.screens.programdetailsold;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.joda.time.DateTimeConstants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WateringZonesAdapter extends GenericListAdapter<ProgramWateringTimes> implements
        CompoundButton.OnCheckedChangeListener {

    private ProgramDetailsOldPresenter presenter;
    private CalendarFormatter formatter;
    private boolean useMinutesSeconds;

    public WateringZonesAdapter(Context context, ProgramDetailsOldPresenter presenter,
                                CalendarFormatter formatter, List<ProgramWateringTimes> items,
                                boolean
                                        useMinutesSeconds) {
        super(context, items);
        this.presenter = presenter;
        this.formatter = formatter;
        this.useMinutesSeconds = useMinutesSeconds;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View convertView = this.inflater.inflate(useMinutesSeconds ? R.layout.item_watering_zones :
                R.layout.item_watering_zones3, container, false);
        ViewHolder holder = new ViewHolder(convertView);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        ProgramWateringTimes programWateringTimes = getItem(position);
        holder.name.setText(getZoneName(programWateringTimes));
        if (useMinutesSeconds) {
            holder.duration.setText(formatter.hourMinSecColon(programWateringTimes.duration));
            holder.toggleZoneActive.setTag(programWateringTimes);
            holder.toggleZoneActive.setOnCheckedChangeListener(null);
            holder.toggleZoneActive.setChecked(programWateringTimes.active);
            holder.toggleZoneActive.setOnCheckedChangeListener(this);
        } else {
            int minutes = (int) programWateringTimes.duration / DateTimeConstants
                    .SECONDS_PER_MINUTE;
            holder.duration.setText(getContext().getResources().getQuantityString(R.plurals
                    .all_x_minutes, minutes, minutes));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.toggle_zone_active) {
            ProgramWateringTimes item = (ProgramWateringTimes) buttonView.getTag();
            presenter.onCheckedWateringZone(item, isChecked);
        }
    }

    private String getZoneName(ProgramWateringTimes wateringTime) {
        if (!Strings.isBlank(wateringTime.name)) {
            return wateringTime.name;
        }
        return getContext().getString(R.string.all_zone_default_name, wateringTime.id);
    }

    static class ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.duration)
        TextView duration;
        @Nullable
        @BindView(R.id.toggle_zone_active)
        SwitchCompat toggleZoneActive;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
