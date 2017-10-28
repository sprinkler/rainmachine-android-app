package com.rainmachine.presentation.screens.waternow;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.rainmachine.R;
import com.rainmachine.domain.model.HandPreference;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.joda.time.DateTimeConstants;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

@EpoxyModelClass
abstract class WaterNowItemModel extends EpoxyModelWithHolder<WaterNowItemModel.ViewHolder> {

    @EpoxyAttribute
    ZoneViewModel item;
    @EpoxyAttribute
    HandPreference handPreference;
    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    View.OnClickListener clickListener;

    private boolean showMinusPlus;
    private CalendarFormatter formatter;
    private Context context;

    WaterNowItemModel() {
    }

    WaterNowItemModel(Context context, boolean showMinusPlus, CalendarFormatter formatter) {
        this.context = context;
        this.showMinusPlus = showMinusPlus;
        this.formatter = formatter;
    }

    @Override
    public void bind(ViewHolder holder) {
        renderStartStopButton(handPreference == HandPreference.RIGHT_HAND
                ? holder.imgRight : holder.imgLeft, handPreference == HandPreference.RIGHT_HAND
                ? holder.viewRight : holder.viewLeft, item);
        hideOtherPlaceholder(handPreference == HandPreference.RIGHT_HAND
                ? holder.imgLeft : holder.imgRight, handPreference == HandPreference.RIGHT_HAND
                ? holder.viewLeft : holder.viewRight);

        if (item.state == ZoneViewModel.State.RUNNING && !item.isMasterValve) {
            holder.tvZoneStatus.setText(R.string.water_now_watering_now);
            int minutes = item.runningCounter / DateTimeConstants.SECONDS_PER_MINUTE;
            int seconds = item.runningCounter % DateTimeConstants.SECONDS_PER_MINUTE;
            String sMinutes = minutes <= 9 ? "0" + minutes : "" + minutes;
            String sSeconds = seconds <= 9 ? "0" + seconds : "" + seconds;
            holder.tvMinutes.setText(sMinutes);
            holder.tvSeconds.setText(sSeconds);
            holder.viewMinus.setOnClickListener(clickListener);
            holder.viewPlus.setOnClickListener(clickListener);
        } else if (item.state == ZoneViewModel.State.PENDING && !item.isMasterValve) {
            holder.tvZoneStatus.setText(R.string.water_now_pending);
            int minutes = item.totalMachineDuration / DateTimeConstants.SECONDS_PER_MINUTE;
            int seconds = item.totalMachineDuration % DateTimeConstants.SECONDS_PER_MINUTE;
            String sMinutes = minutes <= 9 ? "0" + minutes : "" + minutes;
            String sSeconds = seconds <= 9 ? "0" + seconds : "" + seconds;
            holder.tvMinutes.setText(sMinutes);
            holder.tvSeconds.setText(sSeconds);
            holder.viewMinus.setOnClickListener(clickListener);
            holder.viewPlus.setOnClickListener(clickListener);
        } else {
            holder.tvZoneStatus.setText(item.isEnabled ? context.getResources().getString(R.string
                            .water_now_next_watering,
                    nextWatering(item.nextProgramToRun)) : context.getResources().getString(R
                    .string.all_inactive));
        }

        if (getItemViewType() == VIEW_TYPE_PHOTO) {
            File file = null;
            if (!Strings.isBlank(item.zoneSettings.imageLocalPath)) {
                file = new File(item.zoneSettings.imageLocalPath);
            }
            if (file != null && file.exists()) {
                holder.zoneImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context).load(file).into(holder.zoneImage);
            } else if (!Strings.isBlank(item.zoneSettings.imageUrl)) {
                holder.zoneImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context).load(item.zoneSettings.imageUrl).into(holder.zoneImage);
            }

            boolean hide = item.state == ZoneViewModel.State.IDLE;
            if (hide) {
                holder.viewTimer.setBackgroundResource(0);
            } else {
                holder.viewTimer.setBackgroundColor(ContextCompat.getColor(context, R.color
                        .transparent_black_dark));
            }
            holder.viewMinutes.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
            holder.viewColon.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
            holder.viewSeconds.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
            holder.viewMinus.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
            holder.viewPlus.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
            holder.viewMinus.setOnClickListener(clickListener);
            holder.viewPlus.setOnClickListener(clickListener);
        }

        if (getItemViewType() != VIEW_TYPE_NO_PHOTO_IDLE && !showMinusPlus) {
            holder.viewMinus.setVisibility(View.INVISIBLE);
            holder.viewPlus.setVisibility(View.INVISIBLE);
        }

        if (item.isEnabled || item.isMasterValve) {
            int color;
            if (getItemViewType() == VIEW_TYPE_PHOTO) {
                color = ContextCompat.getColor(context, R.color.white);
            } else {
                color = ContextCompat.getColor(context, R.color.text_primary);
            }
            holder.tvZoneName.setTextColor(color);
            holder.tvZoneStatus.setTextColor(color);
        } else {
            int color = ContextCompat.getColor(context, R.color.text_gray);
            holder.tvZoneName.setTextColor(color);
            holder.tvZoneStatus.setTextColor(color);
        }
        holder.viewItem.setOnClickListener(clickListener);

        if (item.isMasterValve) {
            holder.tvZoneName.setText(R.string.water_now_master_valve);
            holder.tvZoneStatus.setText(item.state == ZoneViewModel.State.IDLE ? R.string
                    .water_now_closed : R.string.water_now_open);
        } else {
            holder.tvZoneName.setText(item.id + ". " + item.name);
        }
    }

    private void hideOtherPlaceholder(ImageView img, ViewGroup view) {
        img.setImageDrawable(null);
        view.setOnClickListener(null);
    }

    private void renderStartStopButton(ImageView img, ViewGroup view, ZoneViewModel item) {
        if (item.state == ZoneViewModel.State.RUNNING) {
            img.setImageResource(getItemViewType() == VIEW_TYPE_PHOTO ? R
                    .drawable.ic_stop_red_bg_dark_transparent : R.drawable
                    .ic_stop_red_bg_transparent);
        } else if (item.state == ZoneViewModel.State.PENDING) {
            img.setImageResource(getItemViewType() == VIEW_TYPE_PHOTO ? R
                    .drawable.ic_stop_orange_bg_dark_transparent : R.drawable
                    .ic_stop_orange_bg_transparent);
        } else {
            img.setImageResource(getItemViewType() == VIEW_TYPE_PHOTO ? R
                    .drawable.ic_start_white_bg_dark_transparent : R.drawable
                    .ic_start_blue_bg_transparent);
        }
        view.setOnClickListener(clickListener);

        if (item.isMasterValve) {
            img.setVisibility(View.INVISIBLE);
            view.setOnClickListener(null);
        } else {
            img.setVisibility(View.VISIBLE);
            view.setOnClickListener(clickListener);
        }
    }

    private String nextWatering(Program nextProgramToRun) {
        return (nextProgramToRun == null || nextProgramToRun.nextRunSprinklerLocalDate == null) ?
                context.getResources().getString(R.string.all_never) : formatter.monthDay
                (nextProgramToRun.nextRunSprinklerLocalDate);
    }

    @Override
    protected ViewHolder createNewHolder() {
        return new ViewHolder();
    }

    private static final int VIEW_TYPE_NO_PHOTO_IDLE = 0;
    private static final int VIEW_TYPE_NO_PHOTO_NOT_IDLE = 1;
    private static final int VIEW_TYPE_PHOTO = 2;

    private int getItemViewType() {
        boolean hasPhoto = false;
        if (!Strings.isBlank(item.zoneSettings.imageLocalPath) && new File(item.zoneSettings
                .imageLocalPath).exists()) {
            hasPhoto = true;
        } else if (!Strings.isBlank(item.zoneSettings.imageUrl)) {
            hasPhoto = true;
        }
        if (hasPhoto && item.isEnabled) {
            return VIEW_TYPE_PHOTO;
        } else {
            if ((item.state == ZoneViewModel.State.RUNNING || item.state == ZoneViewModel.State
                    .PENDING) && !item.isMasterValve) {
                return VIEW_TYPE_NO_PHOTO_NOT_IDLE;
            } else {
                return VIEW_TYPE_NO_PHOTO_IDLE;
            }
        }
    }

    @Override
    protected int getDefaultLayout() {
        int viewType = getItemViewType();
        if (viewType == VIEW_TYPE_NO_PHOTO_IDLE) {
            return R.layout.item_water_now_no_photo_idle;
        } else if (viewType == VIEW_TYPE_NO_PHOTO_NOT_IDLE) {
            return R.layout.item_water_now_no_photo_not_idle;
        } else {
            return R.layout.item_water_now_photo;
        }
    }

    class ViewHolder extends EpoxyHolder {

        @BindView(R.id.view_item)
        ViewGroup viewItem;
        @BindView(R.id.zone_name)
        TextView tvZoneName;
        @BindView(R.id.zone_status)
        TextView tvZoneStatus;
        @BindView(R.id.img_left)
        ImageView imgLeft;
        @BindView(R.id.img_right)
        ImageView imgRight;
        @Nullable
        @BindView(R.id.view_left)
        FrameLayout viewLeft;
        @Nullable
        @BindView(R.id.view_right)
        FrameLayout viewRight;
        @Nullable
        @BindView(R.id.zone_image)
        ImageView zoneImage;
        @Nullable
        @BindView(R.id.tv_minutes)
        TextView tvMinutes;
        @Nullable
        @BindView(R.id.tv_seconds)
        TextView tvSeconds;
        @Nullable
        @BindView(R.id.view_minutes)
        ViewGroup viewMinutes;
        @Nullable
        @BindView(R.id.view_seconds)
        ViewGroup viewSeconds;
        @Nullable
        @BindView(R.id.view_colon)
        ViewGroup viewColon;
        @Nullable
        @BindView(R.id.view_minus)
        FrameLayout viewMinus;
        @Nullable
        @BindView(R.id.view_plus)
        FrameLayout viewPlus;
        @Nullable
        @BindView(R.id.view_timer)
        ViewGroup viewTimer;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
