package com.rainmachine.presentation.screens.physicaltouch;

import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.screens.sprinklerdelegate.SprinklerDelegateActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;


class PhysicalTouchPresenter extends BasePresenter<PhysicalTouchView> {

    private PhysicalTouchActivity activity;
    private Features features;
    private PhysicalTouchMixer mixer;

    private Disposable disposable;

    PhysicalTouchPresenter(PhysicalTouchActivity activity, Features features,
                           PhysicalTouchMixer mixer) {
        this.activity = activity;
        this.features = features;
        this.mixer = mixer;
    }

    @Override
    public void attachView(PhysicalTouchView view) {
        super.attachView(view);

        view.updateViews(features.isSpk2());
    }

    public void start() {
        disposable = mixer
                .tryToConnect()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new ConnectedSubscriber());
    }

    public void stop() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    private final class ConnectedSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            Toasts.show("Touch detected");
            activity.startActivity(SprinklerDelegateActivity.getStartIntent(activity));
            activity.finish();
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
