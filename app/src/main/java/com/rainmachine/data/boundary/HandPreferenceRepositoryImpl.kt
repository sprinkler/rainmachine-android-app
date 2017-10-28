package com.rainmachine.data.boundary

import com.rainmachine.data.local.database.AppPreferencesDb
import com.rainmachine.domain.boundary.data.HandPreferenceRepository
import com.rainmachine.domain.model.HandPreference
import com.rainmachine.domain.notifiers.HandPreferenceNotifier

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import nl.nl2312.rxcupboard2.RxDatabase

class HandPreferenceRepositoryImpl(
        private val rxDatabase: RxDatabase,
        private val handPreferenceNotifier: HandPreferenceNotifier
) : HandPreferenceRepository {

    private val defaultHandPreference = HandPreference.LEFT_HAND

    override fun getHandPreference(): Single<HandPreference> {
        return rxDatabase.query(AppPreferencesDb::class.java).firstElement()
                .map { appPreferencesDb -> appPreferencesDb.handPreference }
                .toSingle(defaultHandPreference)
    }

    override fun saveHandPreference(handPreference: HandPreference): Completable {
        return rxDatabase.query(AppPreferencesDb::class.java).firstElement()
                .switchIfEmpty(Maybe.just(AppPreferencesDb(defaultHandPreference)))
                .flatMapSingle { appPreferencesDb ->
                    // Workaround: We need to explicitly reference _id here otherwise it will be
                    // seen as non-existent and a new row will be created instead of updated
                    appPreferencesDb.handPreference = if (appPreferencesDb._id != null) handPreference else handPreference
                    rxDatabase.put(appPreferencesDb)
                }
                .doOnSuccess { appPreferencesDb ->
                    handPreferenceNotifier.publish(appPreferencesDb.handPreference)
                }
                .toCompletable()
    }
}
