package com.rainmachine.domain.util.usecase

import io.reactivex.Completable

abstract class CompletableUseCase<in Q> {

    abstract fun execute(requestModel: Q): Completable
}
