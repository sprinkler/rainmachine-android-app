package com.rainmachine.domain.notifiers

import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay

import io.reactivex.Observable

open class Notifier<T> {

    private val notifierRelay: Relay<T> = PublishRelay.create<T>().toSerialized()

    fun observe(): Observable<T> = notifierRelay

    fun publish(value: T) {
        notifierRelay.accept(value)
    }
}
