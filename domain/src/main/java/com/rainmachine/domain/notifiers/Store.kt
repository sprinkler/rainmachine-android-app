package com.rainmachine.domain.notifiers

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay

import io.reactivex.Observable

open class Store<T> {

    private val storeRelay: Relay<T>

    constructor() {
        this.storeRelay = BehaviorRelay.create<T>().toSerialized()
    }

    constructor(defaultValue: T) {
        this.storeRelay = BehaviorRelay.createDefault(defaultValue).toSerialized()
    }

    fun observe(): Observable<T> = storeRelay.distinctUntilChanged()

    fun publish(value: T) {
        storeRelay.accept(value)
    }
}
