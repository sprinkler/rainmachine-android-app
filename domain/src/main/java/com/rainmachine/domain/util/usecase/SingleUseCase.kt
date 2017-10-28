package com.rainmachine.domain.util.usecase

import io.reactivex.Single

abstract class SingleUseCase<in Q, P> {

    abstract fun execute(requestModel: Q): Single<P>
}
