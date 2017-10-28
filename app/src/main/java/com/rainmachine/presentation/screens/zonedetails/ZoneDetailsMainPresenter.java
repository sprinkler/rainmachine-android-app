package com.rainmachine.presentation.screens.zonedetails;

import android.net.Uri;
import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.model.LatLong;
import com.rainmachine.domain.usecases.zoneimage.DeleteZoneImage;
import com.rainmachine.domain.util.Features;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import org.joda.time.DateTimeConstants;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import pl.aprilapps.easyphotopicker.EasyImage;

class ZoneDetailsMainPresenter extends BasePresenter<ZoneDetailsMainView> implements
        MinutesDialogFragment.Callback, MasterValveDurationDialogFragment.Callback {

    private static final int DIALOG_ID_BEFORE = 1;
    private static final int DIALOG_ID_AFTER = 2;

    private ZoneDetailsActivity activity;
    private Features features;
    private DeleteZoneImage deleteZoneImage;
    private SprinklerRepository sprinklerRepository;

    private ZoneDetailsViewModel viewModel;
    private boolean justPickedImage;
    private CompositeDisposable disposables;

    ZoneDetailsMainPresenter(ZoneDetailsActivity activity, Features features,
                             DeleteZoneImage deleteZoneImage,
                             SprinklerRepository sprinklerRepository) {
        this.activity = activity;
        this.features = features;
        this.deleteZoneImage = deleteZoneImage;
        this.sprinklerRepository = sprinklerRepository;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(ZoneDetailsMainView view) {
        super.attachView(view);
        disposables.add(view.textChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<CharSequence>() {

                    @Override
                    public void onNext(CharSequence charSequence) {
                        if (viewModel != null) {
                            viewModel.zoneProperties.name = charSequence.toString();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        // Do nothing
                    }

                    @Override
                    public void onComplete() {
                        // Do nothing
                    }
                }));
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogMinutesPositiveClick(int dialogId, int minutes) {
        int durationSeconds = minutes * DateTimeConstants.SECONDS_PER_MINUTE;
        masterValveDuration(dialogId, durationSeconds);
    }

    @Override
    public void onDialogMasterValveDurationPositiveClick(int dialogId, int duration) {
        masterValveDuration(dialogId, duration);
    }

    private void masterValveDuration(int dialogId, int durationSeconds) {
        switch (dialogId) {
            case DIALOG_ID_BEFORE:
                viewModel.zoneProperties.beforeInSeconds = durationSeconds;
                view.updateBefore(durationSeconds, features.showMinutesSeconds());
                break;
            case DIALOG_ID_AFTER:
                viewModel.zoneProperties.afterInSeconds = durationSeconds;
                view.updateAfter(durationSeconds, features.showMinutesSeconds());
                break;
        }
    }

    public void onClickBefore() {
        if (features.showMinutesSeconds()) {
            DialogFragment dialog = MasterValveDurationDialogFragment
                    .newInstance(DIALOG_ID_BEFORE, viewModel.zoneProperties.beforeInSeconds,
                            activity
                                    .getString(R.string.zone_details_master_valve_before_title));
            activity.showDialogSafely(dialog);
        } else {
            int minutes = viewModel.zoneProperties.beforeInSeconds / DateTimeConstants
                    .SECONDS_PER_MINUTE;
            DialogFragment dialog = MinutesDialogFragment.newInstance(DIALOG_ID_BEFORE, activity
                    .getString(R.string.zone_details_water_zone), activity.getString(R.string
                    .all_ok), minutes);
            activity.showDialogSafely(dialog);
        }
    }

    public void onClickAfter() {
        if (features.showMinutesSeconds()) {
            DialogFragment dialog = MasterValveDurationDialogFragment
                    .newInstance(DIALOG_ID_AFTER, viewModel.zoneProperties.afterInSeconds,
                            activity
                                    .getString
                                            (R.string.zone_details_master_valve_after_title));
            activity.showDialogSafely(dialog);
        } else {
            int minutes = viewModel.zoneProperties.afterInSeconds / DateTimeConstants
                    .SECONDS_PER_MINUTE;
            DialogFragment dialog = MinutesDialogFragment.newInstance(DIALOG_ID_AFTER, activity
                    .getString(R.string.zone_details_water_zone), activity.getString(R.string
                    .all_ok), minutes);
            activity.showDialogSafely(dialog);
        }
    }

    public void onClickAdvanced() {
        activity.showAdvancedView();
    }

    public void onClickWeather() {
        activity.showWeatherView();
    }

    public void onToggleMasterValve(boolean checked) {
        viewModel.zoneProperties.masterValve = checked;
    }

    public void onToggleEnabled(boolean checked) {
        viewModel.zoneProperties.enabled = checked;
    }

    public void onToggleShowImage(boolean checked) {
        if (checked) {
            view.showZoneImage();
        } else {
            view.hideZoneImage();
        }
        viewModel.zoneSettings.showZoneImage = checked;
    }

    public void onClickImageCamera() {
        EasyImage.openChooserWithGallery(activity, activity.getString(R.string
                .zone_details_choose_image), 0);
    }

    public void onComingBackFromPickingImage(File imageFile) {
        activity.showCropScreen(Uri.fromFile(imageFile));
    }

    public void onComingBackFromCrop(Uri uri) {
        // hold the image path to use when no Internet connection
        viewModel.zoneSettings.imageLocalPath = uri.getPath();
        view.updateZoneImage(viewModel.zoneSettings, viewModel.showEditImageActions);
        justPickedImage = true;
    }

    public void updateViewModel(ZoneDetailsViewModel viewModel) {
        String newImageLocalPath = null;
        if (justPickedImage) {
            newImageLocalPath = this.viewModel.zoneSettings.imageLocalPath;
        }
        this.viewModel = viewModel;
        if (justPickedImage) {
            this.viewModel.zoneSettings.imageLocalPath = newImageLocalPath;
        }
        view.updateContent(viewModel, features);
    }

    public void onClickDeleteImage() {
        disposables.add(
                sprinklerRepository
                        .provision()
                        .map(provision -> new LatLong(provision.location.latitude,
                                provision.location.longitude))
                        .flatMapObservable(coordinates ->
                                deleteZoneImage.execute(new DeleteZoneImage.RequestModel(
                                        viewModel.deviceMacAddress, viewModel.zoneProperties.id,
                                        coordinates)))
                        .doOnError(GenericErrorDealer.INSTANCE)
                        .compose(RunOnProperThreads.instance())
                        .subscribeWith(new DisposableObserver<DeleteZoneImage.ResponseModel>() {

                            @Override
                            public void onNext(DeleteZoneImage.ResponseModel responseModel) {
                                viewModel.zoneSettings.imageLocalPath = null;
                                viewModel.zoneSettings.imageUrl = null;
                                view.showDeleteZoneSuccessMessage();
                                view.updateZoneImage(viewModel.zoneSettings, viewModel
                                        .showEditImageActions);
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                // This should never be called
                            }

                            @Override
                            public void onComplete() {
                                // Do nothing
                            }
                        }));
    }
}
