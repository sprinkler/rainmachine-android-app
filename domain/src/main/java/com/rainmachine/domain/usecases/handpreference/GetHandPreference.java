package com.rainmachine.domain.usecases.handpreference;

import com.rainmachine.domain.boundary.data.HandPreferenceRepository;
import com.rainmachine.domain.model.HandPreference;
import com.rainmachine.domain.util.usecase.SingleUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Single;

public class GetHandPreference extends SingleUseCase<GetHandPreference.RequestModel,
        GetHandPreference.ResponseModel> {

    private HandPreferenceRepository handPreferenceRepository;

    public GetHandPreference(HandPreferenceRepository handPreferenceRepository) {
        this.handPreferenceRepository = handPreferenceRepository;
    }

    @NotNull
    @Override
    public Single<ResponseModel> execute(RequestModel requestModel) {
        return handPreferenceRepository.getHandPreference()
                .map(handPreference -> new ResponseModel(handPreference));
    }

    public static class RequestModel {
    }

    public static class ResponseModel {
        public HandPreference handPreference;

        public ResponseModel(HandPreference handPreference) {
            this.handPreference = handPreference;
        }
    }
}
