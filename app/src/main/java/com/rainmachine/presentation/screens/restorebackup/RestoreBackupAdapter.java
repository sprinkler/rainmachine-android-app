package com.rainmachine.presentation.screens.restorebackup;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.DeviceBackup;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestoreBackupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    private RestoreBackupPresenter presenter;
    private List<RestoreBackupViewModel.BackupDeviceData> items;
    private LayoutInflater inflater;
    private Resources res;

    public RestoreBackupAdapter(Context context, List<RestoreBackupViewModel.BackupDeviceData>
            items,
                                RestoreBackupPresenter presenter) {
        this.items = items;
        this.presenter = presenter;
        inflater = LayoutInflater.from(context);
        res = context.getResources();
    }

    @Override
    public int getItemCount() {
        return items.size() + 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(List<RestoreBackupViewModel.BackupDeviceData> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public List<RestoreBackupViewModel.BackupDeviceData> getItems() {
        return items;
    }

    public RestoreBackupViewModel.BackupDeviceData getItem(int position) {
        // the header is the first position
        return items.get(--position);
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else if (isPositionFooter(position)) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    private boolean isPositionFooter(int position) {
        return position == items.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View convertView = inflater.inflate(R.layout.item_header_restore_backup, parent, false);
            return new HeaderViewHolder(convertView);
        } else if (viewType == TYPE_FOOTER) {
            View convertView = inflater.inflate(R.layout.item_footer_restore_backup, parent, false);
            return new FooterViewHolder(convertView);
        } else if (viewType == TYPE_ITEM) {
            View convertView = inflater.inflate(R.layout.item_restore_backup, parent, false);
            return new ItemViewHolder(convertView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == TYPE_HEADER) {

        } else if (itemViewType == TYPE_FOOTER) {

        } else if (itemViewType == TYPE_ITEM) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            RestoreBackupViewModel.BackupDeviceData item = getItem(position);
            itemViewHolder.tvName.setText(item.name);
            String date = item.lastBackupLocalDateTime.toString("MMM dd, yyyy", Locale.ENGLISH);
            String time = CalendarFormatter.hourMinColon(item.lastBackupLocalDateTime.toLocalTime
                    (), item.use24HourFormat);
            itemViewHolder.tvDescription.setText(res.getString(R.string
                    .restore_backup_description, res.getString(R.string
                    .restore_backup_description_date, date, time)));
            if (item.deviceType == DeviceBackup.DeviceType.SPK1) {
                itemViewHolder.tvDeviceType.setText(R.string.restore_backup_touch_hd_12_old);
            } else if (item.deviceType == DeviceBackup.DeviceType.SPK2) {
                itemViewHolder.tvDeviceType.setText(R.string.restore_backup_mini8);
            } else if (item.deviceType == DeviceBackup.DeviceType.SPK3_12) {
                itemViewHolder.tvDeviceType.setText(R.string.restore_backup_touch_hd_12);
            } else if (item.deviceType == DeviceBackup.DeviceType.SPK3_16) {
                itemViewHolder.tvDeviceType.setText(R.string.restore_backup_touch_hd_16);
            }
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.name)
        TextView tvName;
        @BindView(R.id.description)
        TextView tvDescription;
        @BindView(R.id.device_type)
        TextView tvDeviceType;

        View view;

        public ItemViewHolder(View view) {
            super(view);
            this.view = view;
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            RestoreBackupViewModel.BackupDeviceData item = getItem(getAdapterPosition());
            presenter.onClickBackupDevice(item);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View view) {
            super(view);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View view) {
            super(view);
        }
    }
}
