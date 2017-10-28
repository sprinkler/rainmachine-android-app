package com.rainmachine.presentation.screens.main;

import android.os.Parcelable;
import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.domain.notifiers.DeviceNameStore;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.usecases.watering.GetWateringLive;
import com.rainmachine.domain.util.Features;
import com.rainmachine.infrastructure.UpdateHandler;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.ActionMessageParcelableDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.rainmachine.presentation.screens.currentrestrictions.CurrentRestrictionsActivity;
import com.rainmachine.presentation.screens.raindelay.RainDelayActivity;
import com.rainmachine.presentation.screens.remoteaccess.RemoteAccessActivity;
import com.rainmachine.presentation.screens.web.WebActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import org.joda.time.DateTime;
import org.parceler.Parcels;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

class MainPresenter extends BasePresenter implements ActionMessageDialogFragment.Callback,
        ActionMessageParcelableDialogFragment.Callback, InfoMessageDialogFragment.Callback {

    private static final int DIALOG_ID_ACTION_MESSAGE_CLOUD_EMAIL_PENDING = 3;
    private static final int DIALOG_ID_INFO_MESSAGE_SETUP_DONE = 4;
    private static final int DIALOG_ID_ACTION_CLOUD_SETUP = 5;
    static final int DIALOG_ID_ACTION_STOP_ALL = 6;

    private MainActivity activity;
    private Features features;
    private MainMixer mixer;
    private UpdateHandler updateHandler;
    private GetRestrictionsLive getRestrictionsLive;
    private DeviceNameStore deviceNameStore;
    private GetWateringLive getWateringLive;
    private SprinklerPrefRepositoryImpl sprinklerPrefsRepository;

    private CompositeDisposable disposables;

    private GetWateringLive.ResponseModel getWateringResponseModel;
    private boolean isFirstTimeWateringResponse;

    MainPresenter(MainActivity activity, Features features, MainMixer mixer,
                  UpdateHandler updateHandler, GetRestrictionsLive getRestrictionsLive,
                  DeviceNameStore deviceNameStore, GetWateringLive getWateringLive,
                  SprinklerPrefRepositoryImpl sprinklerPrefsRepository) {
        this.activity = activity;
        this.features = features;
        this.mixer = mixer;
        this.updateHandler = updateHandler;
        this.getRestrictionsLive = getRestrictionsLive;
        this.deviceNameStore = deviceNameStore;
        this.getWateringLive = getWateringLive;
        this.sprinklerPrefsRepository = sprinklerPrefsRepository;
        disposables = new CompositeDisposable();
        isFirstTimeWateringResponse = true;
    }

    @Override
    public void init() {
        mixer.syncZoneImages();
        activity.render(updateHandler.isUpdateInProgress(), false);
    }

    public void start() {
        disposables.add(getRestrictionsLive.execute(new GetRestrictionsLive.RequestModel(false))
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshRestrictionsSubscriber()));
        disposables.add(getWateringLive.execute(new GetWateringLive.RequestModel(false))
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshWateringSubscriber()));
        disposables.add(deviceNameStore
                .observe()
                .doOnError(GenericErrorDealer.INSTANCE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DeviceNameChangedSubscriber()));
        disposables.add(updateHandler
                .updateInProgress()
                .doOnError(GenericErrorDealer.INSTANCE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new UpdateIsInProgressSubscriber()));

        if (features.hasRemoteAccess()) {
            DateTime dtLastUpdate = new DateTime(sprinklerPrefsRepository.lastUpdate());
            boolean checkUpdate = dtLastUpdate.plusDays(1).isBeforeNow();
            DateTime dtLastCloudPending = new DateTime(sprinklerPrefsRepository
                    .lastCloudEmailPending());
            boolean checkCloudEmailPending = dtLastCloudPending.plusDays(1).isBeforeNow();
            if (checkUpdate || checkCloudEmailPending) {
                disposables.add(mixer.refresh(checkUpdate, checkCloudEmailPending)
                        .doOnError(GenericErrorDealer.INSTANCE)
                        .compose(RunOnProperThreads.instance())
                        .subscribeWith(new RefreshSubscriber()));
            }
        } else {
            DateTime dtLastUpdate = new DateTime(sprinklerPrefsRepository.lastUpdate());
            if (dtLastUpdate.plusDays(1).isBeforeNow()) {
                mixer.refresh(true, false)
                        .doOnError(GenericErrorDealer.INSTANCE)
                        .compose(RunOnProperThreads.instance())
                        .subscribeWith(new RefreshSubscriber());
            }
        }
    }

    public void stop() {
        disposables.clear();
    }

    @Override
    public void onDialogActionMessagePositiveClick(int dialogId) {
        if (dialogId == DIALOG_ID_ACTION_CLOUD_SETUP) {
            activity.startActivity(RemoteAccessActivity.getStartIntent(activity, false));
        } else if (dialogId == DIALOG_ID_ACTION_STOP_ALL) {
            activity.onConfirmStopAll();
        }
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
    public void onDialogActionMessageParcelablePositiveClick(int dialogId, Parcelable parcelable) {
        if (dialogId == DIALOG_ID_ACTION_MESSAGE_CLOUD_EMAIL_PENDING) {
            String pendingEmail = Parcels.unwrap(parcelable);
            mixer.resendConfirmationEmail(pendingEmail);
        }
    }

    @Override
    public void onDialogActionMessageParcelableNegativeClick(int dialogId) {
        // Do nothing. It will check again for update in 24 hours
    }

    @Override
    public void onDialogActionMessageParcelableCancel(int dialogId) {
        // Do nothing. It will check again for update in 24 hours
    }

    @Override
    public void onDialogInfoMessageClick(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogInfoMessageCancel(int dialogId) {
        // Do nothing
    }

    public void onClickRestrictionLive() {
        if (features.showActiveRestrictionsScreen()) {
            activity.startActivity(CurrentRestrictionsActivity.getStartIntent(activity));
        } else {
            RainDelayActivity.start(activity);
        }
    }

    public void onClickWateringLive(GetWateringLive.ResponseModel getWateringResponseModel) {
        if (getWateringResponseModel.isProgramRunning) {
            activity.showProgramsScreen();
        } else {
            activity.showWaterNowScreen();
        }
    }

    void onTabSelected() {
        if (getWateringResponseModel == null) {
            return;
        }
        if (getWateringResponseModel.isWateringIdle() || activity.isWaterNowScreen()) {
            activity.hideContentWateringLive();
        } else {
            activity.showContentWateringLive(getWateringResponseModel);
        }
    }

    void onClickUpdate() {
        mixer.makeUpdate();
    }

    void onClickSeeUpdateChanges() {
        final String URL = "https://rainmachine.zendesk" +
                ".com/hc/en-us/articles/230344008-Device-Firmware";
        activity.startActivity(WebActivity.getStartIntent(activity, URL));
    }

    public void onComingFromSetup() {
        DialogFragment dialog = InfoMessageDialogFragment.newInstance
                (DIALOG_ID_INFO_MESSAGE_SETUP_DONE, null, activity.getString(R.string
                        .main_success_setup), activity.getString(R.string
                        .all_ok));
        activity.showDialogSafely(dialog);
    }

    private void showUpdateDialog(String newVersion) {
        DialogFragment dialog = NewUpdateDialogFragment.newInstance(newVersion);
        activity.showDialogSafely(dialog);
    }

    private void showCloudEmailPendingDialog(String cloudPendingEmail) {
        DialogFragment dialog = ActionMessageParcelableDialogFragment.newInstance
                (DIALOG_ID_ACTION_MESSAGE_CLOUD_EMAIL_PENDING, activity.getString(R.string
                                .main_cloud_email_reminder), activity.getString(R.string
                                .main_cloud_email_reminder_info, cloudPendingEmail),
                        activity.getString(R.string.main_resend_confirmation), activity.getString
                                (R.string.all_ok), Parcels.wrap(cloudPendingEmail));
        activity.showDialogSafely(dialog);
    }

    private void showCloudSetupDialog() {
        if (!sprinklerPrefsRepository.shownCloudSetupDialog()) {
            DialogFragment dialog = ActionMessageDialogFragment.newInstance
                    (DIALOG_ID_ACTION_CLOUD_SETUP, activity
                            .getString(R.string.all_remote_access), activity.getString(R.string
                            .main_remote_access_dialog_info), activity.getString(R.string
                            .main_set_up_email), activity.getString(R.string.all_ok));
            activity.showDialogSafely(dialog);
        }
        sprinklerPrefsRepository.saveShownCloudSetupDialog(true);
    }

    private final class RefreshSubscriber extends DisposableObserver<MainViewModel> {

        @Override
        public void onNext(MainViewModel viewModel) {
            if (viewModel.showUpdateDialog) {
                showUpdateDialog(viewModel.newUpdateVersion);
            } else if (viewModel.showCloudEmailPendingDialog) {
                showCloudEmailPendingDialog(viewModel.cloudPendingEmail);
            } else if (viewModel.showCloudSetupDialog) {
                showCloudSetupDialog();
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
    }

    private final class RefreshRestrictionsSubscriber extends DisposableObserver<GetRestrictionsLive
            .ResponseModel> {

        @Override
        public void onNext(GetRestrictionsLive.ResponseModel responseModel) {
            Timber.d("Update restriction notification");
            if (responseModel.numActiveRestrictions > 0) {
                activity.showContentRestrictionLive(activity.getString(R.string
                        .main_rainmachine_paused), responseModel.numActiveRestrictions);
            } else {
                activity.hideContentRestrictionLive();
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            activity.hideContentRestrictionLive();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class RefreshWateringSubscriber extends DisposableObserver<GetWateringLive
            .ResponseModel> {

        @Override
        public void onNext(GetWateringLive.ResponseModel responseModel) {
            Timber.d("Update watering notification");
            getWateringResponseModel = responseModel;
            if (isFirstTimeWateringResponse && !responseModel.isWateringIdle()) {
                activity.showWaterNowScreen();
            }
            if (responseModel.isWateringIdle() || activity.isWaterNowScreen()) {
                activity.hideContentWateringLive();
            } else {
                activity.showContentWateringLive(responseModel);
            }
            isFirstTimeWateringResponse = false;
        }

        @Override
        public void onError(@NonNull Throwable e) {
            activity.hideContentWateringLive();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class DeviceNameChangedSubscriber extends DisposableObserver<Object> {

        @Override
        public void onNext(Object object) {
            Timber.d("changed device name");
            activity.updateCustomActionBarTitle();
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

    private final class UpdateIsInProgressSubscriber extends DisposableObserver<Boolean> {

        @Override
        public void onNext(Boolean isUpdateInProgress) {
            activity.render(isUpdateInProgress, false);
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
