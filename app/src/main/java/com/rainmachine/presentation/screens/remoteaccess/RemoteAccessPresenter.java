package com.rainmachine.presentation.screens.remoteaccess;

import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import org.joda.time.DateTime;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class RemoteAccessPresenter extends BasePresenter<RemoteAccessView> implements
        InfoMessageDialogFragment.Callback, ActionMessageDialogFragment.Callback {

    private static final int DIALOG_DISABLE_REMOTE_ACCESS = 1;

    private RemoteAccessActivity activity;
    private RemoteAccessMixer mixer;
    private Features features;
    private SprinklerPrefRepositoryImpl sprinklerPrefsRepository;

    private final CompositeDisposable disposables;

    private RemoteAccessViewModel viewModel;

    public RemoteAccessPresenter(RemoteAccessActivity activity, RemoteAccessMixer mixer,
                                 Features features, SprinklerPrefRepositoryImpl
                                         sprinklerPrefsRepository) {
        this.activity = activity;
        this.mixer = mixer;
        this.features = features;
        this.sprinklerPrefsRepository = sprinklerPrefsRepository;
        disposables = new CompositeDisposable();
    }

    @Override
    public void init() {
    }

    public void start() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogInfoMessageClick(int dialogId) {
        doNextAfterConfirmationDialog();
    }

    @Override
    public void onDialogInfoMessageCancel(int dialogId) {
        doNextAfterConfirmationDialog();
    }

    @Override
    public void onDialogActionMessagePositiveClick(int dialogId) {
        if (dialogId == DIALOG_DISABLE_REMOTE_ACCESS) {
            disableCloud();
        }
    }

    @Override
    public void onDialogActionMessageNegativeClick(int dialogId) {
        if (dialogId == DIALOG_DISABLE_REMOTE_ACCESS) {
            restoreSwitchState();
        }
    }

    @Override
    public void onDialogActionMessageCancel(int dialogId) {
        if (dialogId == DIALOG_DISABLE_REMOTE_ACCESS) {
            restoreSwitchState();
        }
    }

    public void onCheckedEnableCloudEmail() {
        showDialog();
    }

    public void onCheckedDisableCloudEmail() {
        String message = viewModel.isCloudDevice ? activity.getString(R.string
                .remote_access_disable_cloud_message) : activity.getString(R.string
                .remote_access_disable_local_message);
        DialogFragment dialog = ActionMessageDialogFragment.newInstance
                (DIALOG_DISABLE_REMOTE_ACCESS, null, message, activity.getString(R.string
                        .all_yes), activity.getString(R.string.all_no));
        activity.showDialogSafely(dialog);
    }

    public void onSaveCloudEmail(String email) {
        view.showProgress();
        activity.showProgress();
        activity.supportInvalidateOptionsMenu();
        disposables.add(mixer
                .enableCloudEmail(email)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new EnableCloudEmailSubscriber(email)));
    }

    public void onDialogCloudEmailCancel() {
        refresh();
    }

    public void onClickCloudEmail() {
        showDialog();
    }

    public void onSendConfirmationEmail() {
        view.showProgress();
        activity.showProgress();
        activity.supportInvalidateOptionsMenu();
        disposables.add(mixer
                .sendConfirmationEmail(viewModel.currentPendingEmail)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SendConfirmationEmailSubscriber()));
    }

    private void disableCloud() {
        view.showProgress();
        activity.showProgress();
        activity.supportInvalidateOptionsMenu();
        disposables.add(mixer
                .disableCloudEmail()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new DisableCloudEmailSubscriber()));
    }

    private void restoreSwitchState() {
        view.updateSwitch(viewModel);
    }

    public void refresh() {
        view.showProgress();
        activity.showProgress();
        activity.supportInvalidateOptionsMenu();
        disposables.add(mixer
                .refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private void doNextAfterConfirmationDialog() {
        refresh();
    }

    private void showDialog() {
        String email = viewModel.currentEmail;
        if (!Strings.isBlank(viewModel.currentPendingEmail)) {
            email = viewModel.currentPendingEmail;
        }
        DialogFragment dialog = CloudEmailDialogFragment.newInstance(email, viewModel.knownEmails);
        activity.showDialogSafely(dialog);
    }

    public void changePassword(final String oldPass, final String newPass) {
        view.showProgress();
        disposables.add(mixer
                .changePassword(oldPass, newPass)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new ChangePasswordSubscriber()));
    }

    public boolean canBeEmptyPassword() {
        return features.canBeEmptyPassword();
    }

    public void onClickPassword() {
        DialogFragment dialog = ChangePasswordDialogFragment.newInstance();
        activity.showDialogSafely(dialog);
    }

    private final class RefreshSubscriber extends DisposableObserver<RemoteAccessViewModel> {

        @Override
        public void onNext(RemoteAccessViewModel viewModel) {
            RemoteAccessPresenter.this.viewModel = viewModel;
            view.render(viewModel);
            view.showContent();
            activity.showContent();
            activity.supportInvalidateOptionsMenu();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
            activity.showError();
            activity.supportInvalidateOptionsMenu();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class ChangePasswordSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            view.showContent();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showContent();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class EnableCloudEmailSubscriber extends DisposableObserver<Irrelevant> {

        private String email;

        public EnableCloudEmailSubscriber(String email) {
            this.email = email;
        }

        @Override
        public void onNext(Irrelevant irrelevant) {
            DialogFragment dialog = InfoMessageDialogFragment.newInstance(0, activity.getString(R
                            .string.all_info),
                    activity.getString(R.string.all_enable_cloud_info, email),
                    activity.getString(R.string.all_ok));
            activity.showDialogSafely(dialog);
            sprinklerPrefsRepository.saveLastCloudEmailPending(new DateTime().getMillis());
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
            activity.showError();
            activity.supportInvalidateOptionsMenu();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class DisableCloudEmailSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            refresh();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
            activity.showError();
            activity.supportInvalidateOptionsMenu();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class SendConfirmationEmailSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            view.showContent();
            activity.showContent();
            Toasts.show(R.string.all_success_send_confirmation_email);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
            activity.showError();
            activity.supportInvalidateOptionsMenu();
            Toasts.show(R.string.all_failure_send_confirmation_email);
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
