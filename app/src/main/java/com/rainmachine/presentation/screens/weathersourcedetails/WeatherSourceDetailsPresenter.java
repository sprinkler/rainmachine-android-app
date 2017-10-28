package com.rainmachine.presentation.screens.weathersourcedetails;

import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.dialogs.MultiChoiceDialogFragment;
import com.rainmachine.presentation.screens.nearbystations.NearbyStationsActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.CustomDataException;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

public class WeatherSourceDetailsPresenter extends BasePresenter<WeatherSourceDetailsView>
        implements MultiChoiceDialogFragment.Callback {

    private WeatherSourceDetailsActivity activity;
    private WeatherSourceDetailsMixer mixer;
    private WUndergroundParamsView wUndergroundParamsView;
    private NetatmoParamsView netatmoParamsView;

    private Parser parser;
    private boolean initialParserEnabled; // was the parser enabled / disabled initially?
    private boolean refreshedNetatmoModules;
    private String returnedStationName;
    private CompositeDisposable disposables;

    public WeatherSourceDetailsPresenter(WeatherSourceDetailsActivity activity,
                                         WeatherSourceDetailsMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void init() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    public void attachWUndergroundView(WUndergroundParamsView wUndergroundParamsView) {
        this.wUndergroundParamsView = wUndergroundParamsView;
    }

    public void attachNetatmoView(NetatmoParamsView netatmoParamsView) {
        this.netatmoParamsView = netatmoParamsView;
    }

    @Override
    public void onDialogMultiChoicePositiveClick(int dialogId, String[] items, boolean[]
            checkedItemPositions) {
        parser.netatmoParams.specificModules.clear();
        boolean atLeastOneModule = false;
        for (int i = 0; i < items.length; i++) {
            if (checkedItemPositions[i]) {
                parser.netatmoParams.specificModules.add(parser.netatmoParams.availableModules
                        .get(i));
                atLeastOneModule = true;
            }
        }
        parser.netatmoParams.useSpecifiedModules = atLeastOneModule;
        netatmoParamsView.updateModules(parser.netatmoParams);
    }

    @Override
    public void onDialogMultiChoiceCancel(int dialogId) {
        // Do nothing
    }

    public void onClickDiscard() {
        activity.finish();
    }

    public void onClickSave() {
        showProgress();
        disposables.add(mixer.saveParser(parser)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    public void onClickRetry() {
        refresh();
    }

    public void onChangedBooleanParam(String param, boolean value) {
        parser.params.put(param, value);
    }

    public void onChangedIntParam(String param, int value) {
        parser.params.put(param, value);
        view.updateContent(parser);
    }

    public void onChangedStringParam(String param, String value) {
        parser.params.put(param, value);
        view.updateContent(parser);
    }

    public void onClick(String param, Object value) {
        if (value instanceof Integer) {
            DialogFragment dialog = WeatherSourceParamsDialogFragment.newInstance(param, value,
                    WeatherSourceParamsDialogFragment.VALUE_TYPE_INT);
            activity.showDialogSafely(dialog);
        } else if (value instanceof String) {
            DialogFragment dialog = WeatherSourceParamsDialogFragment.newInstance(param, value,
                    WeatherSourceParamsDialogFragment.VALUE_TYPE_STRING);
            activity.showDialogSafely(dialog);
        }
    }

    private void refresh() {
        showProgress();
        long parserId = activity.getIntent().getLongExtra(WeatherSourceDetailsActivity
                .EXTRA_PARSER_ID, 0L);
        disposables.add(mixer.refresh(parserId)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    public void onCheckedChangedEnabled(boolean isChecked) {
        parser.enabled = isChecked;
        activity.updateCustomActionBarButtons(parser.enabled);
        view.showHideButtons(parser);
        if (parser.isWUnderground()) {
            view.showHideWUndergroundParams(parser);
        } else if (parser.isNetatmo()) {
            view.showHideNetatmoParams(parser);
        } else {
            view.showHideGenericParams(parser);
        }
    }

    public Observable<WUndergroundDeveloperApiKeyCheck> wUndergroundDeveloperKeyChanges
            (CharSequence s) {
        return mixer
                .checkWUndergroundDeveloperApiKey(s.toString())
                .doOnNext(WUndergroundDeveloperApiKeyCheck -> {
                    Timber.d("%s --> %s", WUndergroundDeveloperApiKeyCheck.key,
                            WUndergroundDeveloperApiKeyCheck.isValid);
                    parser.wUndergroundParams.apiKey = WUndergroundDeveloperApiKeyCheck.key;
                })
                .compose(RunOnProperThreads.instance());
    }

    public Observable<NetatmoCredentialsCheck> netatmoCredentialsChanges(NetatmoCredentialsCheck
                                                                                 check) {
        return mixer
                .checkNetatmoCredentials(check)
                .doOnNext(check1 -> {
                    Timber.d("%s / %s --> %s", check1.username, check1.password, check1.isValid);
                    parser.netatmoParams.username = check1.username;
                    parser.netatmoParams.password = check1.password;
                })
                .compose(RunOnProperThreads.instance());
    }

    public void onClickWeatherStation() {
        activity.startActivityForResult(NearbyStationsActivity.getStartIntent(activity, parser,
                initialParserEnabled), WeatherSourceDetailsActivity.REQ_CODE_NEARBY_STATIONS);
    }

    public void onComingBackFromNearbyStations(String stationName) {
        if (parser != null) {
            setWundergroundStationName(stationName);
            wUndergroundParamsView.updateWeatherStation(stationName);
        } else {
            // We store it here and we set it when refresh has finished
            returnedStationName = stationName;
        }
    }

    public void onClickModules() {
        if (!refreshedNetatmoModules) {
            showProgress();
            disposables.add(mixer.refreshNetatmoModules(parser,
                    initialParserEnabled)
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new NetatmoModulesRefreshSubscriber()));
        } else {
            showNetatmoModulesDialog();
        }
    }

    public void onClickRefreshNow() {
        showProgress();
        disposables.add(mixer.runParser(parser, initialParserEnabled)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    public void onClickDefaults() {
        showProgress();
        disposables.add(mixer.setParserDefaults(parser)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private void setWundergroundStationName(String stationName) {
        parser.wUndergroundParams.customStationName = stationName;
        parser.wUndergroundParams.useCustomStation = true;
    }

    private void showNetatmoModulesDialog() {
        List<String> listModules = new ArrayList<>();
        for (Parser.NetatmoModule module : parser.netatmoParams.availableModules) {
            listModules.add(module.name + "  (" + module.mac + ")");
        }
        String[] items = listModules.toArray(new String[listModules.size()]);
        boolean[] checkedItems = new boolean[items.length];
        if (parser.netatmoParams.specificModules.size() > 0) {
            for (int i = 0; i < items.length; i++) {
                for (Parser.NetatmoModule module : parser.netatmoParams.specificModules) {
                    if (parser.netatmoParams.availableModules.get(i).mac.equalsIgnoreCase
                            (module.mac)) {
                        checkedItems[i] = true;
                        break;
                    }
                }
            }
        }
        DialogFragment dialog = MultiChoiceDialogFragment.newInstance(0, activity.getString(R.string
                        .weather_source_details_select_modules), activity.getString(R.string
                        .all_ok),
                items, checkedItems);
        activity.showDialogSafely(dialog);
    }

    private void showContent() {
        view.showContent();
        activity.showActionBarView();
    }

    private void showProgress() {
        view.showProgress();
        activity.hideActionBarView();
    }

    private void showError() {
        activity.hideActionBarView();
        view.showError();
    }

    private final class RefreshSubscriber extends DisposableObserver<WeatherSourceDetailViewModel> {

        @Override
        public void onNext(WeatherSourceDetailViewModel viewModel) {
            WeatherSourceDetailsPresenter.this.parser = viewModel.parser;
            initialParserEnabled = parser.enabled;
            if (returnedStationName != null) {
                setWundergroundStationName(returnedStationName);
                parser.enabled = true;
                returnedStationName = null;
            }
            view.updateContent(viewModel.parser);
            activity.updateCustomActionBarButtons(viewModel.parser.enabled);
            showContent();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            showError();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class SaveSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            Toasts.show(R.string.weather_source_details_success_save_parser);
            activity.finish();
        }

        @Override
        public void onError(@NonNull Throwable throwable) {
            if (throwable instanceof CustomDataException) {
                showContent();
                activity.showActionBarView();
                CustomDataException exception = (CustomDataException) throwable;
                String messageToUser = null;
                if (exception.customStatus == CustomDataException.CustomStatus
                        .INVALID_NETATMO_CREDENTIALS) {
                    messageToUser = activity.getString(R.string
                            .weather_source_details_invalid_netatmo_credentials);
                } else if (exception.customStatus == CustomDataException.CustomStatus
                        .INVALID_WUNDERGROUND_API_KEY) {
                    messageToUser = activity.getString(R.string
                            .weather_source_details_invalid_wunderground_credentials);
                }
                Toasts.showLong(messageToUser);
            } else {
                Toasts.show(R.string.weather_source_details_error_save_parser);
                refresh();
            }
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class NetatmoModulesRefreshSubscriber extends DisposableObserver<Parser> {

        @Override
        public void onNext(Parser parser) {
            WeatherSourceDetailsPresenter.this.parser = parser;
            view.updateContent(parser);
            showContent();
            showNetatmoModulesDialog();
            refreshedNetatmoModules = true;
        }

        @Override
        public void onError(@NonNull Throwable e) {
            showError();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
