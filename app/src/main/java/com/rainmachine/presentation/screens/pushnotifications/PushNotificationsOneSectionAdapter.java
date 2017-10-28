package com.rainmachine.presentation.screens.pushnotifications;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.PushNotification;
import com.rainmachine.presentation.util.ViewUtils;
import com.rainmachine.presentation.util.adapter.GenericRecyclerAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class PushNotificationsOneSectionAdapter extends
        GenericRecyclerAdapter<PushNotificationViewModel, PushNotificationsOneSectionAdapter
                .ViewHolder> {

    private final Context context;
    private final PushNotificationsContract.Presenter presenter;

    PushNotificationsOneSectionAdapter(Context context, List<PushNotificationViewModel> items,
                                       PushNotificationsContract.Presenter presenter) {
        super(context, items);
        this.presenter = presenter;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View convertView = inflater.inflate(R.layout.item_push_notification, viewGroup, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PushNotificationViewModel item = getItem(position);
        holder.name.setText(pushNotificationName(item.pushNotification.type));
        holder.toggleEnabled.setOnCheckedChangeListener(null);
        holder.toggleEnabled.setChecked(item.pushNotification.enabled);
        holder.toggleEnabled.setOnCheckedChangeListener(holder);
        holder.view.setOnClickListener(holder);

        if (item.showSettingActionable) {
            holder.toggleEnabled.setEnabled(true);
            holder.name.setEnabled(true);
            ViewUtils.updateBackgroundResourceWithRetainedPadding(holder.view, R.drawable
                    .row_background);
        } else {
            holder.toggleEnabled.setEnabled(false);
            holder.name.setEnabled(false);
            ViewUtils.updateBackgroundResourceWithRetainedPadding(holder.view, R.drawable
                    .rain_list_selector_disabled_holo_light);
        }
    }

    private String pushNotificationName(PushNotification.Type type) {
        @StringRes int stringId = View.NO_ID;
        switch (type) {
            case GLOBAL:
                stringId = R.string.all_notifications;
                break;
            case DISCONNECTED:
                stringId = R.string.push_notifications_disconnected;
                break;
            case BACK_ONLINE:
                stringId = R.string.push_notifications_connected;
                break;
            case NEW_SOFTWARE_VERSION:
                stringId = R.string.push_notifications_firmware_update;
                break;
            case ZONE:
                stringId = R.string.push_notifications_zone_start_stop;
                break;
            case PROGRAM:
                stringId = R.string.push_notifications_program_start_stop;
                break;
            case WEATHER:
                stringId = R.string.push_notifications_weather_update;
                break;
            case RAIN_SENSOR:
                stringId = R.string.push_notifications_rain_sensor;
                break;
            case RAIN_DELAY:
                stringId = R.string.push_notifications_snooze;
                break;
            case FREEZE_TEMPERATURE:
                stringId = R.string.push_notifications_freeze_temperature;
                break;
            case SHORT:
                stringId = R.string.push_notifications_short;
                break;
            case REBOOT:
                stringId = R.string.push_notifications_reboot;
                break;
        }
        return stringId != View.NO_ID ? context.getString(stringId) : "";
    }

    class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton
            .OnCheckedChangeListener, View.OnClickListener {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.toggle_enabled)
        SwitchCompat toggleEnabled;

        View view;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }

        @Override
        public void onClick(View v) {
            toggleEnabled.setChecked(!toggleEnabled.isChecked());
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            int adapterPosition = getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                PushNotificationViewModel item = getItem(adapterPosition);
                if (id == R.id.toggle_enabled) {
                    presenter.onTogglePushNotification(item.pushNotification, isChecked);
                }
            }
        }
    }
}
