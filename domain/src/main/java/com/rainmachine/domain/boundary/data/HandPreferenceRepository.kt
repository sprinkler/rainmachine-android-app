package com.rainmachine.domain.boundary.data

import com.rainmachine.domain.model.HandPreference

import io.reactivex.Completable
import io.reactivex.Single

interface HandPreferenceRepository {

    fun getHandPreference(): Single<HandPreference>

    fun saveHandPreference(handPreference: HandPreference): Completable
}
