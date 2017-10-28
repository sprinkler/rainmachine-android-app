package com.rainmachine.presentation.screens.weathersources;

import android.content.Intent;
import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.data.remote.util.RemoteUtils;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.screens.weathersourcedetails.WeatherSourceDetailsActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class WeatherSourcesPresenter extends BasePresenter<WeatherSourcesView> implements
        WeatherSourceDialogFragment.Callback {

    private WeatherSourcesActivity activity;
    private WeatherSourcesMixer mixer;
    private Features features;
    private CompositeDisposable disposables;

    WeatherSourcesPresenter(WeatherSourcesActivity activity, WeatherSourcesMixer mixer,
                            Features features) {
        this.activity = activity;
        this.mixer = mixer;
        this.features = features;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(WeatherSourcesView view) {
        super.attachView(view);

        view.setup(features.hasParserFullFunctionality());
        view.showProgress();
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
    public void onDialogDataSourcePositiveClick(String dataSourceUrl) {
        if (!RemoteUtils.isValidURI(dataSourceUrl)) {
            Toasts.show(R.string.all_error_fill_in);
        } else {
            view.showProgress();
            disposables.add(mixer.addDataSource(dataSourceUrl)
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new AddDataSourceSubscriber()));
        }
    }

    @Override
    public void onDialogDataSourceCancel() {
        // Do nothing
    }

    public void onToggleParser(Parser parser, boolean isChecked) {
        view.showProgress();
        disposables.add(mixer.saveParserEnabled(parser, isChecked)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    public void onClickParser(Parser parser) {
        Intent intent = WeatherSourceDetailsActivity.getStartIntent(activity, parser.uid);
        activity.startActivity(intent);
    }

    public void onClickAddWeatherSource() {
        DialogFragment dialog = WeatherSourceDialogFragment.newInstance();
        activity.showDialogSafely(dialog);
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

    private final class RefreshSubscriber extends DisposableObserver<WeatherSourcesViewModel> {

        @Override
        public void onNext(WeatherSourcesViewModel viewModel) {
            view.updateContent(viewModel);
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
            Toasts.show(R.string.weather_sources_error_msg_save_parser_enabled);
            refresh();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class AddDataSourceSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            Toasts.show(R.string.weather_sources_success_add);
            refresh();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Toasts.show(R.string.weather_sources_error_msg_add_weather_source);
            refresh();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
