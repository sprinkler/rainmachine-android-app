package com.rainmachine.presentation.screens.mini8settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.rainmachine.R;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class Mini8SettingsProgramAdapter extends GenericListAdapter<TouchProgramViewModel> {

    private TouchProgramViewModel selectedItem;

    Mini8SettingsProgramAdapter(Context ctx, List<TouchProgramViewModel> items,
                                TouchProgramViewModel selectedItem) {
        super(ctx, items);
        this.selectedItem = selectedItem;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View row = inflater.inflate(R.layout.item_mini8_settings_program, container, false);
        ViewHolder holder = new ViewHolder(row);
        row.setTag(holder);
        return row;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        TouchProgramViewModel touchProgram = getItem(position);
        holder.programName.setText(touchProgram.name);

        holder.programName.setOnCheckedChangeListener(null);
        holder.programName.setChecked(selectedItem.id == touchProgram.id);
        holder.programName.setOnCheckedChangeListener(holder);

        holder.programName.setTag(touchProgram);
    }

    private void setSelectedItem(TouchProgramViewModel touchProgram) {
        selectedItem = touchProgram;
        notifyDataSetChanged();
    }

    TouchProgramViewModel getSelectedItem() {
        return selectedItem;
    }

    class ViewHolder implements CompoundButton.OnCheckedChangeListener {
        @BindView(R.id.program_name)
        RadioButton programName;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                selectedItem = (TouchProgramViewModel) buttonView.getTag();
                setSelectedItem(selectedItem);
            }
        }
    }
}
