package com.rainmachine.presentation.screens.weathersourcedetails;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.rainmachine.R;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.Truss;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class NetatmoParamsView extends LinearLayout {

    @Inject
    WeatherSourceDetailsPresenter presenter;

    @BindView(R.id.input_username)
    EditText inputUsername;
    @BindView(R.id.input_password)
    EditText inputPassword;
    @BindView(R.id.test_username_password)
    TextView tvTestCredentials;
    @BindView(R.id.modules)
    TextView tvModules;
    @BindView(R.id.view_modules)
    ViewGroup viewModules;

    private CompositeDisposable disposables;

    public NetatmoParamsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
        disposables = new CompositeDisposable();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        if (!isInEditMode()) {
            presenter.attachNetatmoView(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disposables.clear();
    }

    @OnClick(R.id.card_modules)
    void onClickModules() {
        presenter.onClickModules();
    }

    public void updateContent(Parser parser) {
        final int MIN_LENGTH = 2;
        Observable.combineLatest(
                RxTextView
                        .textChanges(inputUsername)
                        .debounce(1000, TimeUnit.MILLISECONDS)
                        .filter(s -> s.length() >= MIN_LENGTH),
                RxTextView
                        .textChanges(inputPassword)
                        .debounce(1000, TimeUnit.MILLISECONDS)
                        .filter(s -> s.length() >= MIN_LENGTH),
                (username, password) -> new NetatmoCredentialsCheck(username.toString(), password
                        .toString(), false))
                .filter(check -> check.username.length() >= MIN_LENGTH
                        && check.password.length() >= MIN_LENGTH)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(netatmoCredentialsCheck -> showNetatmoCredentialsCheck())
                .switchMap(check -> presenter.netatmoCredentialsChanges(check))
                .doOnError(GenericErrorDealer.INSTANCE)
                .subscribeWith(new CredentialsSubscriber());

        inputUsername.setText(parser.netatmoParams.username);
        inputUsername.setSelection(inputUsername.length());
        inputPassword.setText(parser.netatmoParams.password);
        inputPassword.setSelection(inputPassword.length());
        updateModules(parser.netatmoParams);
    }

    public void updateModules(Parser.NetatmoParams params) {
        if (params.specificModules.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Parser.NetatmoModule module : params.specificModules) {
                sb.append(module.name).append(",");
            }
            tvModules.setText(sb.substring(0, sb.length() - 1));
        } else {
            tvModules.setText(R.string.weather_source_details_default_word);
        }
    }

    private void showNetatmoCredentialsCheck() {
        Truss truss = new Truss();
        truss.append(getResources().getString(R.string
                .weather_source_details_test_netatmo_credentials));
        truss.append(getResources().getString(R.string.weather_source_details_please_wait));
        tvTestCredentials.setText(truss.build());
        // A comment
    }

    private final class CredentialsSubscriber extends DisposableObserver<NetatmoCredentialsCheck> {

        @Override
        public void onNext(NetatmoCredentialsCheck credentialsCheck) {
            Truss truss = new Truss();
            truss.append(getResources().getString(R.string
                    .weather_source_details_test_netatmo_credentials));
            if (credentialsCheck.isValid) {
                truss.pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R
                        .color.text_green)));
                truss.append(getResources().getString(R.string.weather_source_details_passed));
                truss.popSpan();
            } else {
                truss.pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R
                        .color.text_red)));
                truss.append(getResources().getString(R.string
                        .weather_source_details_failed_try_again));
                truss.popSpan();
            }
            tvTestCredentials.setText(truss.build());

            if (credentialsCheck.isValid) {
                viewModules.setVisibility(View.VISIBLE);
            } else {
                viewModules.setVisibility(View.GONE);
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
