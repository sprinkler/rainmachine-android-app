package com.rainmachine.presentation.screens.location;

import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.domain.model.Autocomplete;
import com.rainmachine.domain.model.LocationInfo;
import com.rainmachine.infrastructure.LocationHandler;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

class LocationPresenter implements LocationContract.Presenter {

    private LocationContract.View view;
    private LocationContract.Container container;
    private Bus bus;
    private LocationHandler locationHandler;
    private LocationMixer mixer;

    private final CompositeDisposable disposables;
    private final boolean isWizard;
    private LocationInfo localLocation;

    LocationPresenter(LocationContract.Container container, Bus bus, LocationHandler
            locationHandler, LocationMixer mixer) {
        this.container = container;
        this.bus = bus;
        this.locationHandler = locationHandler;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
        isWizard = container.isWizard();
    }

    @Override
    public void attachView(LocationContract.View view) {
        this.view = view;
    }

    @Override
    public void init() {
    }

    @Override
    public void start() {
        bus.register(this);

        if (!locationHandler.isLocationServicesEnabled()) {
            container.showNoLocationServices();
        } else {
            if (localLocation == null) {
                // We don't have a location yet
                locationHandler.requestLocationUpdates(container);
                container.showProgressGetLocation();
            }
        }
    }

    @Override
    public void stop() {
        bus.unregister(this);
        locationHandler.stopLocationUpdates(container);
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    // This should always be triggered after the map has been loaded and a location is known
    @Subscribe
    public void onMapLocationEvent(LocationHandler.MapLocationEvent event) {
        LocationInfo location = event.data;
        view.render(location);
        disposables.add(mixer
                .refreshReverseGeocode(location.latitude, location.longitude)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new ReverseGeocodeSubscriber()));
    }

    @Subscribe
    public void onNoMapLocationFound(LocationHandler.NoMapLocationFound event) {
        container.showNoLocationFound();
    }

    @Override
    public void onDialogActionMessagePositiveClick(int dialogId) {
        container.goToTimezoneScreen();
    }

    @Override
    public void onDialogActionMessageNegativeClick(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogActionMessageCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogEnterAddressPositiveClick(Autocomplete selectedPlace) {
        disposables.add(mixer
                .refreshPlace(selectedPlace)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshPlaceSubscriber()));
    }

    @Override
    public void onClickShowManualAddressDialog() {
        String address;
        if (localLocation != null) {
            address = localLocation.fullAddress;
        } else {
            address = container.getSprinklerAddress();
        }
        DialogFragment dialog = EnterAddressDialogFragment.newInstance(address);
        container.showDialogSafely(dialog);
    }

    @Override
    public Observable<List<Autocomplete>> textChanges(CharSequence text) {
        return mixer.refreshAutocomplete(text).compose(RunOnProperThreads.instance());
    }

    @Override
    public void onClickSave() {
        container.showProgress();
        disposables.add(mixer
                .saveLocation(localLocation, isWizard)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    @Override
    public void onComingBackFromResolveLocation() {
        locationHandler.requestLocationUpdates(container);
    }

    @Override
    public void onClickSkip() {
        container.showSkipDialog();
    }

    private final class ReverseGeocodeSubscriber extends DisposableObserver<LocationInfo> {

        @Override
        public void onNext(LocationInfo data) {
            Timber.d("received location");
            localLocation = data;
            container.render(localLocation);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            // Do nothing
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class RefreshPlaceSubscriber extends DisposableObserver<LocationInfo> {

        @Override
        public void onNext(LocationInfo location) {
            view.render(location);
            localLocation = location;
            container.render(location);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            // Do nothing
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class SaveSubscriber extends DisposableObserver<BackupViewModel> {

        @Override
        public void onNext(BackupViewModel backupViewModel) {
            Toasts.show(R.string.location_success_set);
            if (isWizard) {
                if (backupViewModel.hasBackups) {
                    container.goToBackupsScreen();
                } else {
                    container.goToTimezoneScreen();
                }
            } else {
                container.closeAndGoBackToLocationScreen(localLocation.fullAddress);
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Toasts.show(R.string.location_error_set);
            container.showMap();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
