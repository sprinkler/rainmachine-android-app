package com.rainmachine.presentation.screens.softwareupdate;

import com.rainmachine.R;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class SoftwareUpdatePresenter extends BasePresenter<SoftwareUpdateView> {

    private SoftwareUpdateActivity activity;
    private SoftwareUpdateMixer mixer;

    private final CompositeDisposable disposables;

    private SoftwareUpdateViewModel viewModel;

    SoftwareUpdatePresenter(SoftwareUpdateActivity activity, SoftwareUpdateMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(SoftwareUpdateView view) {
        super.attachView(view);

        view.updateProgress(activity.getString(R.string.software_update_checking_software_update));
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

    public void onRetry() {
        refresh();
    }

    public void onClickUpdate() {
        view.updateProgress(activity.getString(R.string.software_update_in_progress));
        view.showProgress();
        disposables.add(mixer
                .makeUpdate()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private void refresh() {
        view.updateProgress(activity.getString(R.string.software_update_checking_software_update));
        view.showProgress();
        disposables.add(mixer
                .refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<SoftwareUpdateViewModel> {

        @Override
        public void onNext(SoftwareUpdateViewModel viewModel) {
            SoftwareUpdatePresenter.this.viewModel = viewModel;
            view.render(viewModel);
            view.showContent();
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
}
