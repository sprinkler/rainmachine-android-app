package com.rainmachine.presentation.screens.zonedetails;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import org.parceler.Parcels;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class ZoneDetailsPresenter extends BasePresenter<ZoneDetailsActivity> implements
        ActionMessageDialogFragment.Callback {

    private static final int DIALOG_ID_ACTION_MESSAGE_DISCARD = 1;

    private ZoneDetailsMixer mixer;
    private ZoneDetailsViewModel viewModel;
    private CompositeDisposable disposables;

    ZoneDetailsPresenter(ZoneDetailsMixer mixer) {
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(ZoneDetailsActivity view) {
        super.attachView(view);
        long zoneId = view.getIntent().getLongExtra(ZoneDetailsActivity.EXTRA_ZONE_ID, -1L);
        view.showProgress();
        view.toggleCustomActionBar(false);
        disposables.add(mixer.refresh(zoneId)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogActionMessagePositiveClick(int dialogId) {
        leaveScreen();
    }

    @Override
    public void onDialogActionMessageNegativeClick(int dialogId) {
        // Do nothing. Stay on this screen
    }

    @Override
    public void onDialogActionMessageCancel(int dialogId) {
        // Do nothing. Stay on this screen
    }

    public void onClickSave() {
        view.showProgress();
        view.toggleCustomActionBar(false);
        boolean uploadZoneImage = !Strings.areEqual(viewModel.zoneSettingsOriginal
                .imageLocalPath, viewModel.zoneSettings.imageLocalPath);
        disposables.add(mixer.saveZoneProperties(viewModel.zoneProperties, viewModel.zoneSettings,
                uploadZoneImage)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveZoneSubscriber()));
    }

    public void onClickLeaveScreen() {
        if (hasUnsavedChanges()) {
            confirmLeaveScreen();
        } else {
            leaveScreen();
        }
    }

    public Object getRetainedState() {
        return viewModel;
    }

    private boolean hasUnsavedChanges() {
        return viewModel != null && !viewModel.zoneProperties.equals(viewModel
                .zonePropertiesOriginal);
    }

    private void confirmLeaveScreen() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance
                (DIALOG_ID_ACTION_MESSAGE_DISCARD,
                        view.getString(R.string.all_unsaved_changes), view.getString(R.string
                                .all_unsaved_changes_zone),
                        view.getString(R.string.all_yes), view.getString(R.string.all_no));
        view.showDialogSafely(dialog);
    }

    private void leaveScreen() {
        // "Discard" OR Back
        view.setResult(Activity.RESULT_CANCELED);
        view.finish();
    }

    public void setRetainedState(Object object) {
        viewModel = (ZoneDetailsViewModel) object;
        view.updateViewModel(viewModel);
    }

    private final class RefreshSubscriber extends DisposableObserver<ZoneDetailsViewModel> {

        @Override
        public void onNext(ZoneDetailsViewModel viewModel) {
            ZoneDetailsPresenter.this.viewModel = viewModel;
            view.updateViewModel(viewModel);
            view.showMainView();
            view.toggleCustomActionBar(true);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
            view.toggleCustomActionBar(false);
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class SaveZoneSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            Intent data = new Intent();
            data.putExtra(ZoneDetailsActivity.EXTRA_RESULT_ZONE, Parcels.wrap(viewModel
                    .zoneProperties));
            view.setResult(ZoneDetailsActivity.RESULT_OK, data);
            view.finish();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Toasts.show(R.string.zone_details_error_saving_zone);
            view.toggleCustomActionBar(true);
            view.showMainView();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
