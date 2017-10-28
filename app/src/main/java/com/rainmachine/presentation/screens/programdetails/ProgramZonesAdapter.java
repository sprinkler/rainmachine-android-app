package com.rainmachine.presentation.screens.programdetails;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.presentation.util.adapter.GenericRecyclerAdapter;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.ProgramFormatter;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class ProgramZonesAdapter extends GenericRecyclerAdapter<ProgramWateringTimes,
        ProgramZonesAdapter.ViewHolder> {

    private final Context context;
    private final ProgramDetailsContract.Presenter presenter;
    private final CalendarFormatter calendarFormatter;
    private Program program;
    private final ProgramFormatter programFormatter;

    ProgramZonesAdapter(Context context, ProgramDetailsContract.Presenter presenter,
                        CalendarFormatter calendarFormatter, Program program,
                        ProgramFormatter programFormatter) {
        super(context, program.wateringTimes);
        this.context = context;
        this.presenter = presenter;
        this.calendarFormatter = calendarFormatter;
        this.program = program;
        this.programFormatter = programFormatter;
    }

    @Override
    public ProgramZonesAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View convertView = inflater.inflate(R.layout.item_program_zones, viewGroup, false);
        return new ProgramZonesAdapter.ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ProgramZonesAdapter.ViewHolder holder, int position) {
        ProgramWateringTimes programWateringTimes = getItem(position);
        holder.name.setText(res.getString(R.string.program_details_zone_name_index, String.format
                (Locale.ENGLISH, "%02d", programWateringTimes.id), programWateringTimes.name));

        if (programWateringTimes.isCustom()) {
            holder.duration.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.duration.setText(calendarFormatter.hourMinSecLabel(programWateringTimes
                    .duration));
        } else if (programWateringTimes.isDetermined()) {
            holder.duration.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            List<ProgramWateringTimes.SelectedDayDuration> values = ProgramWateringTimes
                    .suggestedProgramWateringDurations(programWateringTimes, program);
            holder.duration.setText(programFormatter.wateringTimesDuration(program, values));
        } else {
            holder.duration.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
            holder.duration.setText(R.string.program_details_not_set);
        }
    }

    void updateData(Program program) {
        this.program = program;
        setItems(program.wateringTimes);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.duration)
        TextView duration;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                presenter.onClickProgramZone(adapterPosition);
            }
        }
    }
}
