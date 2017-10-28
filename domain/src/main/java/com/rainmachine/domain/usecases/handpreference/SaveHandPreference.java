package com.rainmachine.domain.usecases.handpreference;

import com.rainmachine.domain.boundary.data.HandPreferenceRepository;
import com.rainmachine.domain.model.HandPreference;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;

public class SaveHandPreference extends ObservableUseCase<SaveHandPreference.RequestModel,
        SaveHandPreference.ResponseModel> {

    private HandPreferenceRepository handPreferenceRepository;

    public SaveHandPreference(HandPreferenceRepository handPreferenceRepository) {
        this.handPreferenceRepository = handPreferenceRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(final RequestModel requestModel) {
        return handPreferenceRepository
                .saveHandPreference(requestModel.handPreference)
                .andThen(Observable.just(new ResponseModel()));
    }

    public static class RequestModel {
        public HandPreference handPreference;

        public RequestModel(HandPreference handPreference) {
            this.handPreference = handPreference;
        }
    }

    public static class ResponseModel {
    }
}
