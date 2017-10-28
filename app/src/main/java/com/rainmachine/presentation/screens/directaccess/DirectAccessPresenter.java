package com.rainmachine.presentation.screens.directaccess;

import android.os.Parcelable;
import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.presentation.dialogs.ActionMessageParcelableDialogFragment;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import org.joda.time.DateTime;
import org.parceler.Parcels;

import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class DirectAccessPresenter extends BasePresenter<DirectAccessView> implements
        ActionMessageParcelableDialogFragment.Callback {

    private DirectAccessActivity activity;
    private DirectAccessMixer mixer;
    private CompositeDisposable disposables;

    DirectAccessPresenter(DirectAccessActivity activity, DirectAccessMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(DirectAccessView view) {
        super.attachView(view);

        view.setup();
    }

    @Override
    public void init() {
        disposables.add(mixer.dataChanges()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new DataChangesSubscriber()));
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogActionMessageParcelablePositiveClick(int dialogId, Parcelable parcelable) {
        Device device = Parcels.unwrap(parcelable);
        mixer.removeDevice(device.deviceId, device._id);
    }

    @Override
    public void onDialogActionMessageParcelableNegativeClick(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogActionMessageParcelableCancel(int dialogId) {
        // Do nothing
    }

    public void onClickEditManualDevice(Device device) {
        DialogFragment dialog = DirectAccessDialogFragment.newInstance(activity.getString(R.string
                .direct_access_device), device);
        activity.showDialogSafely(dialog);
    }

    public void onClickDeleteManualDevice(Device device) {
        DialogFragment dialog = ActionMessageParcelableDialogFragment.newInstance(0, null,
                activity.getString(R.string
                        .direct_access_are_you_sure_delete_manual_device), activity.getString
                        (R.string.all_yes),
                activity.getString(R.string.all_no), Parcels.wrap(device));
        activity.showDialogSafely(dialog);
    }

    public void onClickAddManualDevice() {
        DialogFragment dialog = DirectAccessDialogFragment.newInstance(activity.getString(R.string
                .direct_access_device));
        activity.showDialogSafely(dialog);
    }

    public void saveManualDevice(Long _id, String name, String url) {
        if (_id == null) {
            Device device = new Device();
            // We use as device id the url of the sprinkler
            device.deviceId = url;
            device.name = name;
            device.setUrl(url);
            device.type = Device.SPRINKLER_TYPE_MANUAL;
            device.timestamp = new DateTime().getMillis();
            device.wizardHasRun = true;
            device.cloudEmail = null;
            mixer.saveDevice(device);
        } else {
            mixer.updateDevice(_id, name, url);
        }
    }

    private void refresh() {
        disposables.add(mixer.refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<List<Device>> {

        @Override
        public void onNext(List<Device> data) {
            view.render(data);
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

    private final class DataChangesSubscriber extends DisposableObserver<String> {

        @Override
        public void onNext(String ignored) {
            refresh();
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
}
