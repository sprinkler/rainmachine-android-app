package com.rainmachine.presentation.screens.restorebackup;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.util.adapter.BindableAdapter;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

public class RestoreBackupSpinnerAdapter extends BindableAdapter<RestoreBackupViewModel.Backup> {

    private List<RestoreBackupViewModel.Backup> items;
    private RestoreBackupViewModel.BackupDeviceData backupDeviceData;
    private Resources res;

    public RestoreBackupSpinnerAdapter(Context context, List<RestoreBackupViewModel.Backup> items,
                                       RestoreBackupViewModel.BackupDeviceData backupDeviceData) {
        super(context);
        this.items = items;
        this.backupDeviceData = backupDeviceData;
        res = context.getResources();
    }

    @Override
    public RestoreBackupViewModel.Backup getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public final View newDropDownView(LayoutInflater inflater, int position, ViewGroup container) {
        return inflater.inflate(R.layout.item_simple_spinner_dropdown, container, false);
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        return inflater.inflate(R.layout.item_simple_spinner, container, false);
    }

    @Override
    public void bindView(RestoreBackupViewModel.Backup item, int position, View view) {
        TextView tv = ButterKnife.findById(view, android.R.id.text1);
        String date = item.localDateTime.toString("MMM dd, yyyy", Locale.ENGLISH);
        String time = CalendarFormatter.hourMinColon(item.localDateTime.toLocalTime(),
                backupDeviceData.use24HourFormat);
        tv.setText(backupDeviceData.name + " " + res.getString(R.string
                .restore_backup_description_date, date, time));
    }
}
