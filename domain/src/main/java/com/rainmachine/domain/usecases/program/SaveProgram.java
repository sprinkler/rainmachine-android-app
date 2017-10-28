package com.rainmachine.domain.usecases.program;

import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.notifiers.ProgramChange;
import com.rainmachine.domain.notifiers.ProgramChangeNotifier;
import com.rainmachine.domain.notifiers.StatsNeedRefreshNotifier;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class SaveProgram extends ObservableUseCase<SaveProgram.RequestModel, SaveProgram
        .ResponseModel> {

    private SprinklerRepository sprinklerRepository;
    private Features features;
    private ProgramChangeNotifier programChangeNotifier;
    private StatsNeedRefreshNotifier statsNeedRefreshNotifier;

    public SaveProgram(SprinklerRepository sprinklerRepository, Features features,
                       ProgramChangeNotifier programChangeNotifier,
                       StatsNeedRefreshNotifier statsNeedRefreshNotifier) {
        this.sprinklerRepository = sprinklerRepository;
        this.features = features;
        this.programChangeNotifier = programChangeNotifier;
        this.statsNeedRefreshNotifier = statsNeedRefreshNotifier;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        Completable stream;
        if (features.useNewApi()) {
            if (requestModel.programToSave.isNew()) {
                stream = sprinklerRepository.createProgram(requestModel.programToSave);
            } else {
                stream = sprinklerRepository.updateProgram(requestModel.programToSave);
            }
        } else {
            stream = sprinklerRepository.createUpdateProgram3(requestModel.programToSave,
                    requestModel.use24HourFormat);
        }
        return stream
                .doOnComplete(() -> notifyListeners(requestModel.programToSave, requestModel
                        .originalProgram))
                .andThen(Observable.just(new ResponseModel()))
                .compose(RunToCompletion.instance());
    }

    private void notifyListeners(Program programToSave, Program originalProgram) {
        programChangeNotifier.publish(ProgramChange.Properties.INSTANCE);
        if ((programToSave.isNew() && programToSave.enabled)
                || (!programToSave.isNew() && programToSave.enabled !=
                originalProgram.enabled)
                || (!programToSave.isNew() && programToSave.enabled &&
                !programToSave.startTime.equals(originalProgram.startTime))
                || (!programToSave.name.equals(originalProgram.name))
                || (programToSave.ignoreWeatherData != originalProgram.ignoreWeatherData)
                || (Program.isFrequencyDifferent(programToSave, originalProgram))) {
            statsNeedRefreshNotifier.publish(new Object());
        }
    }

    public static class RequestModel {
        public Program programToSave;
        public Program originalProgram;
        public boolean use24HourFormat;

        public RequestModel(Program programToSave, Program originalProgram, boolean
                use24HourFormat) {
            this.programToSave = programToSave;
            this.originalProgram = originalProgram;
            this.use24HourFormat = use24HourFormat;
        }
    }

    public static class ResponseModel {
    }
}
