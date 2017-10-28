package com.rainmachine.presentation.screens.about;

import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.domain.model.DiagnosticsUploadStatus;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.infrastructure.UpdateHandler;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class AboutPresenter extends BasePresenter<AboutContract.View> implements AboutContract.Presenter {

    private AboutActivity activity;
    private AboutMixer mixer;
    private Features features;
    private UpdateHandler updateHandler;

    private final CompositeDisposable disposables;

    AboutPresenter(AboutActivity activity, AboutMixer mixer, Features features, UpdateHandler
            updateHandler) {
        this.activity = activity;
        this.mixer = mixer;
        this.features = features;
        this.updateHandler = updateHandler;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(AboutContract.View view) {
        super.attachView(view);

        view.setup(features.showDiagnostics());
        view.showProgress(null);
    }

    @Override
    public void init() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogActionMessagePositiveClick(int dialogId) {
        view.showProgress(null);
        disposables.add(mixer
                .resetCloudCertificates()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new ResetSubscriber()));
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
    public void onClickUpdate() {
        view.showProgress(activity.getString(R.string.about_update_progress));
        disposables.add(mixer
                .makeUpdate()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    @Override
    public void onClickRetry() {
        refresh();
    }

    @Override
    public void onClickSendDiagnostics() {
        view.showProgress(null);
        disposables.add(mixer
                .sendDiagnostics()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SendSubscriber()));
    }

    @Override
    public void onConsecutiveClicksSupport() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance(0, activity.getString(R
                        .string.all_are_you_sure),
                activity.getString(R.string.about_are_you_sure_reset_cloud_certificates),
                activity
                        .getString(R.string.all_yes),
                activity.getString(R.string.all_no));
        activity.showDialogSafely(dialog);
    }

    private void refresh() {
        if (updateHandler.isUpdateInProgress()) {
            view.showProgress(activity.getString(R.string.about_update_progress));
        } else {
            view.showProgress(null);
            disposables.add(mixer
                    .refresh()
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new RefreshSubscriber()));
        }
    }

    private void onErrorSendingDiagnostics() {
        Toasts.show(R.string.about_error_send_diagnostics);
        view.showContent();
    }

    private final class RefreshSubscriber extends DisposableObserver<AboutViewModel> {

        @Override
        public void onNext(AboutViewModel viewModel) {
            if (features.showFullAboutData()) {
                view.updateContent(viewModel);
            } else {
                view.updateContent3(viewModel);
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class SendSubscriber extends DisposableObserver<DiagnosticsUploadStatus> {

        @Override
        public void onNext(DiagnosticsUploadStatus diagnosticsUploadStatus) {
            if (diagnosticsUploadStatus.isUploadSuccessful()) {
                Toasts.show(R.string.about_success_send_diagnostics);
                view.showContent();
            } else if (diagnosticsUploadStatus.isUploadInProgress()) {
                view.showProgress(null);
            } else {
                onErrorSendingDiagnostics();
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            onErrorSendingDiagnostics();
        }

        @Override
        public void onComplete() {
            // We might reach here if sending diagnostics time outs before finishing upload
            view.showContent();
        }
    }

    private final class ResetSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            Toasts.show(R.string.about_success_reset_cloud_certificates);
            view.showContent();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Toasts.show(R.string.about_error_reset_cloud_certificates);
            view.showContent();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
