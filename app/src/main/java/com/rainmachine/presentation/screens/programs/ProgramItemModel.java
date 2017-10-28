package com.rainmachine.presentation.screens.programs;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.rainmachine.R;
import com.rainmachine.domain.model.HandPreference;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.util.Truss;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.joda.time.LocalTime;

import butterknife.BindView;
import butterknife.ButterKnife;

@EpoxyModelClass
abstract class ProgramItemModel extends EpoxyModelWithHolder<ProgramItemModel.ViewHolder> {

    @EpoxyAttribute
    Program item;
    @EpoxyAttribute
    HandPreference handPreference;
    @EpoxyAttribute
    boolean use24HourFormat;
    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    View.OnClickListener clickListener;

    private Context context;
    private CalendarFormatter formatter;

    ProgramItemModel() {
    }

    ProgramItemModel(Context context, CalendarFormatter formatter) {
        this.context = context;
        this.formatter = formatter;
    }

    @Override
    public void bind(ViewHolder holder) {
        holder.name.setText(item.name);

        CharSequence sTime;
        LocalTime startTime = item.startTime.actualStartTime();
        if (item.startTime.isTimeOfDay()) {
            sTime = context.getResources().getString(R.string.programs_at, CalendarFormatter
                    .hourMinColon(startTime, use24HourFormat));
        } else {
            Truss truss = new Truss()
                    .append(context.getResources().getString(R.string.all_at))
                    .append(" " + item.startTime.offsetMinutes + " minutes " + (item.startTime
                            .isBefore() ? "before" : "after") + " " + (item.startTime
                            .isSunrise() ? "sunrise" : "sunset") + " (")
                    .pushSpan(new StyleSpan(Typeface.ITALIC))
                    .append(CalendarFormatter.hourMinColon(startTime, use24HourFormat))
                    .popSpan()
                    .append(")");
            sTime = truss.build();
        }

        holder.imgLeft.setOnClickListener(clickListener);
        holder.imgRight.setOnClickListener(clickListener);

        renderStartStopButton(handPreference == HandPreference.RIGHT_HAND
                ? holder.imgRight : holder.imgLeft, item);
        hideOtherPlaceholder(handPreference == HandPreference.RIGHT_HAND
                ? holder.imgLeft : holder.imgRight);

        if (item.enabled) {
            int textColor = R.color.text_primary;
            holder.name.setTextColor(ContextCompat.getColor(context, textColor));
            holder.schedule.setTextColor(ContextCompat.getColor(context, textColor));

            if (item.nextRunSprinklerLocalDate != null) {
                String nextRun = formatter.monthDay(item.nextRunSprinklerLocalDate);
                holder.schedule.setText(context.getResources().getString(R.string
                        .programs_next_run, nextRun
                        + " " + sTime));
            } else {
                CharSequence freq = null;
                if (item.isDaily()) {
                    freq = context.getResources().getString(R.string.programs_daily_at, sTime);
                } else if (item.isWeekDays()) {
                    String print = formatter.weekDays(item.frequencyWeekDays());
                    if (print.length() > 0) {
                        freq = context.getResources().getString(R.string.programs_weekdays_at,
                                print, sTime);
                    } else {
                        freq = context.getResources().getString(R.string.programs_at, sTime);
                    }
                } else if (item.isOddDays()) {
                    freq = context.getResources().getString(R.string.programs_odd_days_at, sTime);
                } else if (item.isEvenDays()) {
                    freq = context.getResources().getString(R.string.programs_even_days_at, sTime);
                } else if (item.isEveryNDays()) {
                    String numDays = Integer.toString(item.frequencyNumDays());
                    freq = context.getResources().getString(R.string.programs_every_n_days_at,
                            numDays, sTime);
                }
                holder.schedule.setText(freq);
            }
        } else {
            int textColor = R.color.text_gray;
            holder.name.setTextColor(ContextCompat.getColor(context, textColor));
            holder.schedule.setTextColor(ContextCompat.getColor(context, textColor));
            holder.schedule.setText(R.string.all_inactive);
        }

        holder.viewNameSchedule.setOnClickListener(clickListener);
    }

    private void renderStartStopButton(ImageView img, Program program) {
        if (program.wateringState == Program.WateringState.RUNNING) {
            img.setImageResource(R.drawable.ic_stop_red_bg_transparent);
        } else if (program.wateringState == Program.WateringState.PENDING) {
            img.setImageResource(R.drawable.ic_stop_orange_bg_transparent);
        } else {
            img.setImageResource(R.drawable.ic_start_blue_bg_transparent);
        }
    }

    private void hideOtherPlaceholder(ImageView img) {
        img.setImageDrawable(null);
    }

    @Override
    protected ViewHolder createNewHolder() {
        return new ViewHolder();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_program;
    }

    class ViewHolder extends EpoxyHolder {

        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.schedule)
        TextView schedule;
        @BindView(R.id.img_left)
        ImageView imgLeft;
        @BindView(R.id.img_right)
        ImageView imgRight;
        @BindView(R.id.view_name_schedule)
        ViewGroup viewNameSchedule;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
