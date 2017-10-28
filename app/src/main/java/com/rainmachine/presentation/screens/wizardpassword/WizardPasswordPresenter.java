package com.rainmachine.presentation.screens.wizardpassword;

import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.screens.main.MainActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class WizardPasswordPresenter extends BasePresenter<WizardPasswordView> {

    private WizardPasswordActivity activity;
    private WizardPasswordMixer mixer;

    private final CompositeDisposable disposables;

    WizardPasswordPresenter(WizardPasswordActivity activity, WizardPasswordMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(WizardPasswordView view) {
        super.attachView(view);

        view.updateContent(null);
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

    public void onClickSave(String newPass) {
        view.showProgress();
        disposables.add(mixer
                .savePassword(newPass)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SavePasswordSubscriber()));
    }

    private void refresh() {
        disposables.add(mixer
                .refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<WizardPasswordViewModel> {

        @Override
        public void onNext(WizardPasswordViewModel viewModel) {
            view.updateContent(viewModel.preFillPassword);
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

    private final class SavePasswordSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            activity.startActivity(MainActivity.getStartIntent(activity, false));
            activity.finish();
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
}
