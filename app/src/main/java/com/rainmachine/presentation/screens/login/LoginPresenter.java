package com.rainmachine.presentation.screens.login;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.domain.model.LoginStatus;
import com.rainmachine.presentation.screens.sprinklerdelegate.SprinklerDelegateActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

class LoginPresenter extends BasePresenter<LoginView> {

    private Device device;
    private LoginActivity activity;
    private LoginMixer mixer;
    private final SprinklerPrefRepositoryImpl sprinklerPrefsRepository;

    private final CompositeDisposable disposables;

    LoginPresenter(Device device, LoginActivity activity, LoginMixer mixer,
                   SprinklerPrefRepositoryImpl sprinklerPrefsRepository) {
        this.device = device;
        this.activity = activity;
        this.mixer = mixer;
        this.sprinklerPrefsRepository = sprinklerPrefsRepository;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(LoginView view) {
        super.attachView(view);

        view.updateContent(sprinklerPrefsRepository.username());
    }

    public void start() {
        view.showKeyboard();
    }

    public void stop() {
        view.cancelShowKeyboard();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    public void onClickLogin(String username, String pass, boolean isRemember) {
        view.showProgress();
        disposables.add(mixer
                .login(username, pass, isRemember)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new LoginSubscriber()));
    }

    private final class LoginSubscriber extends DisposableObserver<LoginStatus> {

        @Override
        public void onNext(LoginStatus loginStatus) {
            Timber.d("login status %s", loginStatus);
            if (loginStatus == LoginStatus.SUCCESS) {
                activity.startActivity(SprinklerDelegateActivity.getStartIntent(activity));
                activity.finish();
            } else {
                view.showContent();
                String message = activity.getString(R.string.login_error_network, device.name);
                if (loginStatus == LoginStatus.AUTHENTICATION_FAILED) {
                    message = activity.getString(R.string.login_error_authentication);
                }
                Toasts.showLong(message);
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
}
