package com.rainmachine.presentation.screens.about;

import android.content.Context;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.model.Diagnostics;
import com.rainmachine.domain.model.DiagnosticsUploadStatus;
import com.rainmachine.domain.model.Update;
import com.rainmachine.domain.model.Versions;
import com.rainmachine.domain.model.WifiSettingsSimple;
import com.rainmachine.domain.usecases.TriggerUpdateCheck;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.UpdateHandler;
import com.rainmachine.presentation.util.CustomDataException;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

class AboutMixer {

    private Context context;
    private Features features;
    private UpdateHandler updateHandler;
    private TriggerUpdateCheck triggerUpdateCheck;
    private SprinklerRepositoryImpl sprinklerRepository;
    private CalendarFormatter formatter;

    AboutMixer(Context context, Features features, UpdateHandler updateHandler,
               TriggerUpdateCheck triggerUpdateCheck,
               SprinklerRepositoryImpl sprinklerRepository, CalendarFormatter formatter) {
        this.context = context;
        this.features = features;
        this.updateHandler = updateHandler;
        this.triggerUpdateCheck = triggerUpdateCheck;
        this.sprinklerRepository = sprinklerRepository;
        this.formatter = formatter;
    }

    public Observable<AboutViewModel> refresh() {
        Observable<AboutViewModel> observable;
        if (features.useNewApi()) {
            observable = triggerUpdateCheck
                    .execute(new TriggerUpdateCheck.RequestModel())
                    .andThen(about())
                    .toObservable();
        } else {
            observable = about3().toObservable();
        }
        return observable;
    }

    private Single<AboutViewModel> about() {
        return Single.zip(
                sprinklerRepository.update(true),
                sprinklerRepository.versions(),
                sprinklerRepository.wifiSettings(),
                sprinklerRepository.diagnostics(),
                (update, versions, wifiSettings, diagnostics) ->
                        buildViewModel(update, wifiSettings, versions, diagnostics));
    }

    private Single<AboutViewModel> about3() {
        return sprinklerRepository
                .update3(true)
                .map(update -> buildViewModel(update));
    }

    private AboutViewModel buildViewModel(Update update, WifiSettingsSimple wifiSettings,
                                          Versions versions, Diagnostics diagnostics) {
        AboutViewModel viewModel = new AboutViewModel();
        viewModel.update = update;
        if (Strings.isBlank(viewModel.update.currentVersion)) {
            viewModel.update.currentVersion = versions.softwareVersion;
        }
        viewModel.wifiSettings = wifiSettings;
        viewModel.versions = versions;
        viewModel.cpuUsage = diagnostics.cpuUsage;
        viewModel.gatewayAddress = diagnostics.gatewayAddress;
        viewModel.memUsage = diagnostics.memUsage;
        if (features.isApiAtLeast41()) {
            viewModel.uptime = formatter.uptime(diagnostics.uptimeSeconds, new DateTime());
            viewModel.showRemoteAccessStatus = true;
            if (diagnostics.isCloudStatusOK()) {
                viewModel.remoteAccessStatus = context.getString(R.string.about_rainmachine_online);
            } else if (diagnostics.isCloudStatusDisabled()) {
                viewModel.remoteAccessStatus = context.getString(R.string
                        .about_remote_access_disabled);
            } else {
                viewModel.remoteAccessStatus = context.getString(R.string.about_rainmachine_offline,
                        diagnostics
                                .cloudStatus);
            }
        } else {
            viewModel.uptime = diagnostics.uptime;
        }
        return viewModel;
    }

    private AboutViewModel buildViewModel(Update update) {
        AboutViewModel viewModel = new AboutViewModel();
        viewModel.update = update;
        return viewModel;
    }

    Observable<AboutViewModel> makeUpdate() {
        return updateHandler
                .triggerUpdate()
                .flatMap(ignored -> refresh())
                .compose(RunToCompletion.instance());
    }

    Observable<DiagnosticsUploadStatus> sendDiagnostics() {
        return sprinklerRepository
                .sendDiagnostics().toObservable()
                .flatMap(irrelevant -> Observable
                        .interval(0, 10, TimeUnit.SECONDS)
                        .take(120)
                        .switchMap(step -> sprinklerRepository.getDiagnosticsUpload()
                                .toObservable())
                        .takeUntil(uploadStatus -> !uploadStatus.isUploadInProgress()))
                .onErrorReturn(throwable -> {
                    throw new CustomDataException(CustomDataException.CustomStatus
                            .SEND_DIAGNOSTICS_ERROR);
                })
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> resetCloudCertificates() {
        return sprinklerRepository
                .resetCloudCertificates()
                .flatMap(irrelevant -> {
                    Timber.d("reboot");
                    // Although the reboot API sometimes gives timeout, the reboot is being
                    // performed
                    return sprinklerRepository
                            .reboot()
                            .onErrorReturn(throwable -> Irrelevant.INSTANCE);
                })
                .delay(10, TimeUnit.SECONDS)
                .flatMap(irrelevant -> Observable
                        .interval(0, 5, TimeUnit.SECONDS)
                        .take(120)
                        .switchMap(step -> sprinklerRepository.testApiFunctional().toObservable())
                        .filter(isSuccess -> isSuccess)
                        .firstOrError()
                        .map(aBoolean -> Irrelevant.INSTANCE))
                .onErrorReturn(throwable -> {
                    throw new CustomDataException(CustomDataException.CustomStatus
                            .RESET_CLOUD_CERTIFICATES_ERROR);
                })
                .toObservable()
                .compose(RunToCompletion.instance());
    }
}
