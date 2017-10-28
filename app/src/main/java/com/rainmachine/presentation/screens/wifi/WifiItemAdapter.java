package com.rainmachine.presentation.screens.wifi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.util.adapter.GenericListAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class WifiItemAdapter extends GenericListAdapter<WifiItemViewModel> {

    WifiItemAdapter(Context context, List<WifiItemViewModel> items) {
        super(context, items);
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View convertView = this.inflater.inflate(R.layout.item_wifi_scan, container, false);
        ViewHolder holder = new ViewHolder(convertView);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void bindView(Object item, int position, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        WifiItemViewModel wifiItemViewModel = getItem(position);
        holder.name.setText(wifiItemViewModel.sSID);
        holder.tvSignalStrength.setText(getContext().getString(R.string.wifi_dbm,
                wifiItemViewModel.rSSI));
        if (wifiItemViewModel.level == 0) {
            holder.wifiStrength.setImageResource(wifiItemViewModel.isEncrypted ? R.drawable
                    .wifi_locked_strength0 : R.drawable.wifi_strength0);
        } else if (wifiItemViewModel.level == 1) {
            holder.wifiStrength.setImageResource(wifiItemViewModel.isEncrypted ? R.drawable
                    .wifi_locked_strength1 : R.drawable.wifi_strength1);
        } else if (wifiItemViewModel.level == 2) {
            holder.wifiStrength.setImageResource(wifiItemViewModel.isEncrypted ? R.drawable
                    .wifi_locked_strength2 : R.drawable.wifi_strength2);
        } else if (wifiItemViewModel.level == 3) {
            holder.wifiStrength.setImageResource(wifiItemViewModel.isEncrypted ? R.drawable
                    .wifi_locked_strength3 : R.drawable.wifi_strength3);
        }
    }

    static class ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.tv_signal_strength)
        TextView tvSignalStrength;
        @BindView(R.id.wifi_strength)
        ImageView wifiStrength;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
