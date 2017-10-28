package com.rainmachine.presentation.screens.zonedetails;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.rainmachine.R;
import com.rainmachine.data.local.database.model.ZoneSettings;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Toasts;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.joda.time.DateTimeConstants;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;

public class ZoneDetailsMainView extends ScrollView implements CompoundButton
        .OnCheckedChangeListener {

    @Inject
    ZoneDetailsMainPresenter presenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.view_master_valve)
    ViewGroup viewMasterValve;
    @BindView(R.id.view_other)
    ViewGroup viewOther;
    @BindView(R.id.toggle_master_valve)
    SwitchCompat toggleMasterValve;
    @BindView(R.id.toggle_enabled)
    SwitchCompat toggleEnabled;
    @BindView(R.id.input_name)
    EditText inputName;
    @BindView(R.id.view_program_after_before)
    ViewGroup viewBeforeAfter;
    @BindView(R.id.tv_before_program_starts)
    TextView tvBefore;
    @BindView(R.id.tv_after_program_starts)
    TextView tvAfter;
    @BindView(R.id.img_zone)
    ImageView imgZone;
    @BindView(R.id.view_zone_image)
    View viewZoneImage;
    @BindView(R.id.toggle_show_image)
    SwitchCompat toggleShowImage;
    @BindView(R.id.img_delete)
    View imgDelete;
    @BindView(R.id.img_camera)
    View imgCamera;
    @BindView(R.id.view_weather_settings)
    View viewWeatherSettings;

    public ZoneDetailsMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        if (!isInEditMode()) {
            presenter.attachView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            presenter.init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            presenter.destroy();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        int id = compoundButton.getId();
        if (id == R.id.toggle_master_valve) {
            toggleMasterValve(checked);
            presenter.onToggleMasterValve(checked);
        } else if (id == R.id.toggle_enabled) {
            presenter.onToggleEnabled(checked);
        } else if (id == R.id.toggle_show_image) {
            presenter.onToggleShowImage(checked);
        }
    }

    @OnClick(R.id.view_before)
    public void onClickBefore() {
        presenter.onClickBefore();
    }

    @OnClick(R.id.view_after)
    public void onClickAfter() {
        presenter.onClickAfter();
    }

    @OnClick(R.id.view_advanced_zone_settings)
    public void onClickAdvanced() {
        presenter.onClickAdvanced();
    }

    @OnClick(R.id.view_weather_settings)
    public void onClickWeather() {
        presenter.onClickWeather();
    }

    @OnClick(R.id.img_camera)
    public void onClickImageCamera() {
        presenter.onClickImageCamera();
    }

    @OnClick(R.id.img_delete)
    public void onClickDeleteImage() {
        presenter.onClickDeleteImage();
    }

    private void toggleMasterValve(boolean checked) {
        if (checked) {
            toggleMasterValve.setOnCheckedChangeListener(null);
            toggleMasterValve.setChecked(true);
            toggleMasterValve.setOnCheckedChangeListener(this);
            viewOther.setVisibility(View.GONE);
            viewBeforeAfter.setVisibility(View.VISIBLE);
        } else {
            toggleMasterValve.setOnCheckedChangeListener(null);
            toggleMasterValve.setChecked(false);
            toggleMasterValve.setOnCheckedChangeListener(this);
            viewOther.setVisibility(View.VISIBLE);
            viewBeforeAfter.setVisibility(View.GONE);
        }
    }

    private void toggleEnabled(boolean checked) {
        toggleEnabled.setOnCheckedChangeListener(null);
        toggleEnabled.setChecked(checked);
        toggleEnabled.setOnCheckedChangeListener(this);
    }

    private void toggleShowImage(boolean checked) {
        toggleShowImage.setOnCheckedChangeListener(null);
        toggleShowImage.setChecked(checked);
        toggleShowImage.setOnCheckedChangeListener(this);
    }

    public Observable<CharSequence> textChanges() {
        return RxTextView.textChanges(inputName);
    }

    public void updateBefore(int durationSeconds, boolean showMinutesSeconds) {
        if (showMinutesSeconds) {
            tvBefore.setText(formatter.hourMinSecColon(durationSeconds));
        } else {
            int minutes = durationSeconds / DateTimeConstants.SECONDS_PER_MINUTE;
            tvBefore.setText(getResources().getQuantityString(R.plurals.all_x_minutes, minutes,
                    minutes));
        }
    }

    public void updateAfter(int durationSeconds, boolean showMinutesSeconds) {
        if (showMinutesSeconds) {
            tvAfter.setText(formatter.hourMinSecColon(durationSeconds));
        } else {
            int minutes = durationSeconds / DateTimeConstants.SECONDS_PER_MINUTE;
            tvAfter.setText(getResources().getQuantityString(R.plurals.all_x_minutes, minutes,
                    minutes));
        }
    }

    public void updateContent(ZoneDetailsViewModel viewModel, Features features) {
        if (viewModel.zoneProperties.canBeMasterValve()) {
            viewMasterValve.setVisibility(View.VISIBLE);
            toggleMasterValve(viewModel.zoneProperties.masterValve);
        } else {
            viewMasterValve.setVisibility(View.GONE);
            toggleMasterValve(false);
        }

        inputName.setText(viewModel.zoneProperties.name);
        inputName.setSelection(inputName.length());

        toggleEnabled(viewModel.zoneProperties.enabled);

        updateBefore(viewModel.zoneProperties.beforeInSeconds, features.showMinutesSeconds());
        updateAfter(viewModel.zoneProperties.afterInSeconds, features.showMinutesSeconds());

        toggleShowImage(viewModel.zoneSettings.showZoneImage);
        updateZoneImage(viewModel.zoneSettings, viewModel.showEditImageActions);
        if (viewModel.zoneSettings.showZoneImage) {
            showZoneImage();
        } else {
            hideZoneImage();
        }

        toggleMasterValve.setOnCheckedChangeListener(this);
        toggleEnabled.setOnCheckedChangeListener(this);
        toggleShowImage.setOnCheckedChangeListener(this);

        viewWeatherSettings.setVisibility(features.showWeatherOption() ? View.VISIBLE : View.GONE);
    }

    public void showZoneImage() {
        viewZoneImage.setVisibility(View.VISIBLE);
    }

    public void hideZoneImage() {
        viewZoneImage.setVisibility(View.GONE);
    }

    public void updateZoneImage(ZoneSettings zoneSettings, boolean showEditImageActions) {
        File file = null;
        if (!Strings.isBlank(zoneSettings.imageLocalPath)) {
            file = new File(zoneSettings.imageLocalPath);
        }
        if (file != null && file.exists()) {
            imgZone.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(getContext()).load(file).into(imgZone);
            imgDelete.setVisibility(showEditImageActions ? View.VISIBLE : View.GONE);
        } else if (!Strings.isBlank(zoneSettings.imageUrl)) {
            imgZone.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(getContext()).load(zoneSettings.imageUrl).into(imgZone);
            imgDelete.setVisibility(showEditImageActions ? View.VISIBLE : View.GONE);
        } else {
            imgZone.setImageResource(R.drawable.ic_zone_details_no_image);
            imgDelete.setVisibility(View.GONE);
        }
        imgCamera.setVisibility(showEditImageActions ? View.VISIBLE : View.GONE);
    }

    public void showDeleteZoneSuccessMessage() {
        Toasts.show(R.string.zone_details_success_delete_image);
    }
}
