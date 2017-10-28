package com.rainmachine.presentation.screens.dashboardgraphs;

import com.rainmachine.data.local.database.model.DashboardGraphs;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class DashboardGraphsPresenter extends BasePresenter<DashboardGraphsView> {

    private DashboardGraphsMixer mixer;

    private DashboardGraphsViewModel viewModel;
    private CompositeDisposable disposables;

    DashboardGraphsPresenter(DashboardGraphsMixer mixer) {
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(DashboardGraphsView view) {
        super.attachView(view);

        view.setup();
        view.showProgress();
    }

    @Override
    public void init() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    public void stop() {
        if (viewModel != null) {
            disposables.add(mixer.saveToDatabase(viewModel.dashboardGraphs)
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new SaveSubscriber()));
        }
    }

    public void onCheckedChangedItem(DashboardGraphs.DashboardGraph item, boolean isEnabled) {
        item.isEnabled = isEnabled;
    }

    public void onClickRetry() {
        refresh();
    }

    private void refresh() {
        view.showProgress();
        disposables.add(mixer.refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<DashboardGraphsViewModel> {

        @Override
        public void onNext(DashboardGraphsViewModel viewModel) {
            DashboardGraphsPresenter.this.viewModel = viewModel;
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

    private final class SaveSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
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
