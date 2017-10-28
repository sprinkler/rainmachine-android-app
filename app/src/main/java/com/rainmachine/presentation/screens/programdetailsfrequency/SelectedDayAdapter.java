package com.rainmachine.presentation.screens.programdetailsfrequency;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;

import org.joda.time.DateTimeConstants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class SelectedDayAdapter extends GenericListAdapter<ItemSelectedDay> implements
        CompoundButton.OnCheckedChangeListener {

    SelectedDayAdapter(Context ctx, List<ItemSelectedDay> items) {
        super(ctx, items);
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View row = inflater.inflate(R.layout.item_selected_day, container, false);
        ViewHolder holder = new ViewHolder(row);
        row.setTag(holder);
        return row;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        ItemSelectedDay itemDay = getItem(position);
        holder.day.setText(itemDay.name);
        holder.day.setTag(itemDay);
        holder.day.setOnCheckedChangeListener(null);
        holder.day.setChecked(itemDay.isChecked);
        holder.day.setOnCheckedChangeListener(this);
        if (itemDay.isChecked) {
            boolean[] checkedItemPositions = new boolean[DateTimeConstants.DAYS_PER_WEEK];
            for (int i = 0; i < checkedItemPositions.length; i++) {
                checkedItemPositions[i] = items.get(i).isChecked;
            }
            int numDays = Program.futureWeekDayMultiplier(checkedItemPositions, itemDay.index);
            holder.tvCoversNumDays.setText(res.getQuantityString(R.plurals
                    .program_details_covers, numDays, numDays));
        } else {
            holder.tvCoversNumDays.setText("");
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ItemSelectedDay item = (ItemSelectedDay) buttonView.getTag();
        item.isChecked = isChecked;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        @BindView(R.id.day)
        CheckBox day;
        @BindView(R.id.tv_covers_num_days)
        TextView tvCoversNumDays;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
