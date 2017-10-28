package com.rainmachine.presentation.screens.wizardremoteaccess;

import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.rainmachine.presentation.screens.main.MainActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import org.joda.time.DateTime;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class WizardRemoteAccessPresenter extends BasePresenter<WizardRemoteAccessView> implements
        InfoMessageDialogFragment.Callback, ActionMessageDialogFragment.Callback {

    private static final int DIALOG_SKIP_REMOTE_ACCESS = 0;

    private WizardRemoteAccessActivity activity;
    private WizardRemoteAccessMixer mixer;
    private SprinklerPrefRepositoryImpl sprinklerPrefsRepository;

    private final CompositeDisposable disposables;

    WizardRemoteAccessPresenter(WizardRemoteAccessActivity activity,
                                WizardRemoteAccessMixer mixer,
                                SprinklerPrefRepositoryImpl sprinklerPrefsRepository) {
        this.activity = activity;
        this.mixer = mixer;
        this.sprinklerPrefsRepository = sprinklerPrefsRepository;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(WizardRemoteAccessView view) {
        super.attachView(view);
        view.setup();
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
        if (dialogId == DIALOG_SKIP_REMOTE_ACCESS) {
            moveForward();
        }
    }

    @Override
    public void onDialogActionMessageNegativeClick(int dialogId) {
        // do nothing
    }

    @Override
    public void onDialogActionMessageCancel(int dialogId) {
        // do nothing
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

    public void onClickSkip() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance
                (DIALOG_SKIP_REMOTE_ACCESS, null, activity
                                .getString(R.string.wizard_remote_access_skip_message), activity
                                .getString(R.string
                                        .all_skip),
                        activity.getString(R.string.wizard_remote_access_back));
        activity.showDialogSafely(dialog);
    }

    private void moveForward() {
        activity.startActivity(MainActivity.getStartIntent(activity, true));
        activity.finish();
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
        moveForward();
    }

    private final class RefreshSubscriber extends DisposableObserver<WizardRemoteAccessViewModel> {

        @Override
        public void onNext(WizardRemoteAccessViewModel viewModel) {
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
}
