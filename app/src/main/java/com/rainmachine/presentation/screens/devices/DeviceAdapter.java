package com.rainmachine.presentation.screens.devices;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.remote.util.RemoteUtils;
import com.rainmachine.presentation.util.adapter.GenericRecyclerAdapter;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class DeviceAdapter extends GenericRecyclerAdapter<Device, DeviceAdapter.ViewHolder> {

    private Context context;
    private DevicesContract.Presenter presenter;

    private String currentWifiMac;

    DeviceAdapter(Context ctx, List<Device> items, DevicesContract.Presenter presenter) {
        super(ctx, items);
        this.context = ctx;
        this.presenter = presenter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = inflater.inflate(R.layout.item_device, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Device device = getItem(position);
        holder.name.setText(device.name);
        if (device.isAp() || (device.isUdp() && device.deviceId.equalsIgnoreCase
                (currentWifiMac))) {
            holder.url.setText(device.deviceId != null ? device.deviceId.toUpperCase(Locale
                    .getDefault()) : null);
        } else if (device.isUdp()) {
            holder.url.setText(RemoteUtils.getDomainNameWithoutPort(device.getUrl()));
        } else if (device.isManual()) {
            holder.url.setText(device.getUrl());
        } else {
            holder.url.setText(R.string.devices_remote_access);
        }

        int nameColor = device.isOffline ? R.color.text_gray : R.color.text_primary;
        holder.name.setTextColor(ContextCompat.getColor(context, nameColor));
        int urlColor = device.isOffline ? R.color.text_gray : R.color.text_gray_another;
        holder.url.setTextColor(ContextCompat.getColor(context, urlColor));

        String type = null;
        if (device.isAp() || !device.wizardHasRun) {
            type = context.getString(R.string.devices_setup);
        } else if (device.isOffline) {
            type = context.getString(R.string.devices_offline);
        }
        holder.type.setText(type);
    }

    void setCurrentWifiMac(String currentWifiMac) {
        this.currentWifiMac = currentWifiMac;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.url)
        TextView url;
        @BindView(R.id.type)
        TextView type;

        View view;

        public ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            ButterKnife.bind(this, itemView);
            this.view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Device device = getItem(adapterPosition);
                presenter.onClickDevice(device);
            }
        }
    }
}
