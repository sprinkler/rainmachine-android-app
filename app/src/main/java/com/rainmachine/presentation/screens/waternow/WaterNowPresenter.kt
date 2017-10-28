package com.rainmachine.presentation.screens.waternow

import com.pacoworks.rxtuples2.RxTuples
import com.rainmachine.R
import com.rainmachine.data.boundary.SprinklerRepositoryImpl
import com.rainmachine.data.local.database.DatabaseRepositoryImpl
import com.rainmachine.data.local.database.model.Device
import com.rainmachine.data.local.database.model.WateringZone
import com.rainmachine.domain.notifiers.*
import com.rainmachine.domain.usecases.handpreference.GetHandPreference
import com.rainmachine.domain.usecases.wateringduration.SaveWateringDuration
import com.rainmachine.domain.util.DomainUtils
import com.rainmachine.domain.util.Features
import com.rainmachine.domain.util.Irrelevant
import com.rainmachine.domain.util.SchedulerProvider
import com.rainmachine.infrastructure.Sleeper
import com.rainmachine.presentation.util.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.joda.time.DateTimeConstants
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WaterNowPresenter(
        private val container: WaterNowContract.Container,
        private val features: Features,
        private val device: Device,
        private val sprinklerRepository: SprinklerRepositoryImpl,
        private val databaseRepository: DatabaseRepositoryImpl,
        private val getHandPreference: GetHandPreference,
        private val saveWateringDuration: SaveWateringDuration,
        private val programChangeNotifier: ProgramChangeNotifier,
        private val zonePropertiesChangeNotifier: ZonePropertiesChangeNotifier,
        private val manualStopAllWateringNotifier: ManualStopAllWateringNotifier,
        private val handPreferenceNotifier: HandPreferenceNotifier,
        private val schedulerProvider: SchedulerProvider
) : BasePresenter<WaterNowContract.View>(), WaterNowContract.Presenter {

    private val elm: Elm = Elm()
    private val disposables: CompositeDisposable = CompositeDisposable()
    private var disposableIntervalPolling: Disposable? = null
    private var fakeCounter: FakeCounter? = null
    private val SECONDS_WAIT = 4L

    override fun attachView(view: WaterNowContract.View) {
        super.attachView(view)

        disposables.add(elm.init(WaterNowState.initialize(features.isAtLeastSpk2,
                features.showMinutesSeconds()), this))
        elm.render()
        elm.accept(FirstMsg)

        subscribeToNotifiers()
    }

    private fun subscribeToNotifiers() {
        disposables.add(sprinklerRepository.deviceSettingsLive()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ deviceSettings ->
                    elm.accept(ZoneImagesChangeMsg(deviceSettings))
                }))

        disposables.add(programChangeNotifier.observe()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ programChange ->
                    if (programChange is ProgramChange.StartStop) {
                        elm.accept(ProgramStartStopChangeMsg)
                    } else if (programChange is ProgramChange.Properties) {
                        elm.accept(ProgramPropertiesChangeMsg)
                    }
                }))

        disposables.add(zonePropertiesChangeNotifier.observe()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ (zoneId, zoneName, isEnabled, isMasterValve) ->
                    elm.accept(
                            ZonePropertyChangeMsg(zoneId, zoneName, isEnabled, isMasterValve))
                }))

        disposables.add(handPreferenceNotifier.observe()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ handPreference ->
                    elm.accept(HandPreferenceChangeMsg(handPreference))
                }))
    }

    override fun start() {
        disposableIntervalPolling = Observable.interval(0, 15, TimeUnit.SECONDS)
                .observeOn(schedulerProvider.ui())
                .subscribe({ _ -> elm.accept(PingNetworkMsg) })
        disposables.add(disposableIntervalPolling!!)
    }

    override fun stop() {
        if (disposableIntervalPolling != null) {
            disposables.remove(disposableIntervalPolling!!)
            disposableIntervalPolling = null
        }
        stopAnyFakeCounter()
    }

    override fun destroy() {
        disposables.clear()
    }

    override fun onClickEdit(item: ZoneViewModel) {
        container.goToZoneScreen(item.id)
    }

    override fun onClickStart(item: ZoneViewModel) {
        elm.accept(ClickStartZoneMsg(item))
    }

    override fun onClickStop(item: ZoneViewModel) {
        elm.accept(ClickStopZoneMsg(item))
    }

    override fun onClickMinus(item: ZoneViewModel) {
        elm.accept(ClickMinusMsg(item))
    }

    override fun onClickPlus(item: ZoneViewModel) {
        elm.accept(ClickPlusMsg(item))
    }

    override fun onClickStopAll() {
        elm.accept(ClickStopAllMsg)
    }

    override fun onConfirmStopAll() {
        elm.accept(ConfirmStopAllMsg)
    }

    override fun onShowingStopAllDialog() {
        elm.accept(ShowingStopAllDialogMsg)
    }

    override fun onShowingStartZoneDialog() {
        elm.accept(ShowingStartZoneDialogMsg)
    }

    override fun onDialogZoneDurationPositiveClick(zoneId: Long, duration: Int) {
        elm.accept(ConfirmStartZoneMsg(zoneId, duration))
    }

    override fun onDialogZoneDurationCancel(zoneId: Long, duration: Int) {
        // Do nothing
    }

    override fun update(msg: Msg, state: ElmState): Pair<ElmState, Cmd> {
        val oldState = state as WaterNowState
        return when (msg) {
            is FirstMsg -> msg.reduce(oldState)
            is CoreDataMsg -> msg.reduce(oldState)

            is PingNetworkMsg -> msg.reduce(oldState)
            is WateringDataMsg -> msg.reduce(oldState)
            is FakeCounterDataMsg -> msg.reduce(oldState)

            is ClickStartZoneMsg -> msg.reduce(oldState)
            is ShowingStartZoneDialogMsg -> msg.reduce(oldState)
            is ConfirmStartZoneMsg -> msg.reduce(oldState)
            is DecrementStateDirtyMsg -> msg.reduce(oldState)

            is ClickPlusMsg -> msg.reduce(oldState)
            is ClickMinusMsg -> msg.reduce(oldState)
            is DecrementTotalMachineDurationDirtyCountMsg -> msg.reduce(oldState)
            is ClickStopZoneMsg -> msg.reduce(oldState)

            is ClickStopAllMsg -> msg.reduce(oldState)
            is ShowingStopAllDialogMsg -> msg.reduce(oldState)
            is ConfirmStopAllMsg -> msg.reduce(oldState)
            is DecrementMultipleStateDirtyMsg -> msg.reduce(oldState)

            is ZoneImagesChangeMsg -> msg.reduce(oldState)
            is ZonePropertyChangeMsg -> msg.reduce(oldState)
            is ProgramPropertiesChangeMsg -> msg.reduce(oldState)
            is ProgramPropertiesDataMsg -> msg.reduce(oldState)
            is ProgramStartStopChangeMsg -> msg.reduce(oldState)
            is HandPreferenceChangeMsg -> msg.reduce(oldState)

            is ErrorMsg -> {
                Timber.d("Error ${msg.err.message} for ${msg.cmd}")
                return when (msg.cmd) {
                    is ChangeZoneCounterCmd -> {
                        val (newState, _) = DecrementTotalMachineDurationDirtyCountMsg(
                                msg.cmd.id).reduce(oldState)
                        Pair(newState, None)
                    }
                    is StartZoneCmd -> {
                        val (newState, _) = DecrementStateDirtyMsg(msg.cmd.id).reduce(oldState)
                        Pair(newState, None)
                    }
                    is StopZoneCmd -> {
                        val (newState, _) = DecrementStateDirtyMsg(msg.cmd.id).reduce(oldState)
                        Pair(newState, None)
                    }
                    is StopAllCmd -> {
                        val (newState, _) = DecrementMultipleStateDirtyMsg(
                                msg.cmd.dirtyZones).reduce(oldState)
                        Pair(newState, None)
                    }
                    else -> {
                        Pair(oldState, None)
                    }
                }
            }
            else -> Pair(oldState, None)
        }
    }

    override fun call(cmd: Cmd): Single<Msg> {
        return when (cmd) {
            is GetFirstTimeDataCmd -> Single.zip(
                    sprinklerRepository.zonesProperties(),
                    defaultManualStartDurations(),
                    sprinklerRepository.programs(),
                    sprinklerRepository.deviceSettings(),
                    getHandPreference.execute(GetHandPreference.RequestModel()),
                    RxTuples.toQuintet())
                    .map { quintet -> CoreDataMsg(quintet) }
            is GetWateringDataCmd -> sprinklerRepository.zones()
                    .map { zones ->
                        WateringDataMsg(zones, cmd.totalMachineDurationDirtyMap,
                                cmd.zoneStateDirtyMap, cmd.timeCall)
                    }
            is GetProgramPropertiesDataCmd -> sprinklerRepository.programs()
                    .map { programs -> ProgramPropertiesDataMsg(programs) }
            is StartZoneCmd -> Completable
                    .fromAction {
                        if (cmd.startFakeCounter) {
                            startFakeCounter(cmd.id, cmd.duration)
                        }
                    }
                    .andThen(sprinklerRepository.startZone(cmd.id, cmd.duration))
                    .doOnSuccess {
                        saveWateringDuration(cmd.id, cmd.duration)
                    }
                    .map { DecrementStateDirtyMsg(cmd.id) }
            is ChangeZoneCounterCmd -> Completable
                    .fromAction {
                        if (cmd.startFakeCounter) {
                            startFakeCounter(cmd.id, cmd.fakeCounterInitialValue)
                        }
                    }
                    .andThen(sprinklerRepository.startZone(cmd.id, cmd.duration))
                    .map { DecrementTotalMachineDurationDirtyCountMsg(cmd.id) }
            is StopZoneCmd -> Completable
                    .fromAction {
                        if (cmd.stopFakeCounter) {
                            stopFakeCounter(cmd.id)
                        }
                    }
                    .andThen(stopZone(cmd.id))
                    .map { DecrementStateDirtyMsg(cmd.id) }
            is StopAllCmd -> Completable
                    .fromAction { stopAnyFakeCounter() }
                    .andThen(sprinklerRepository.stopWatering())
                    .doOnSuccess {
                        Toasts.show(R.string.water_now_stop_all_success)
                        manualStopAllWateringNotifier.publish(Any())
                    }
                    .map { DecrementMultipleStateDirtyMsg(cmd.dirtyZones) }
            is StartFakeCounterCmd ->
                Single.fromCallable {
                    startFakeCounter(cmd.id, cmd.initialValue)
                    Idle
                }
            is StopFakeCounterCmd ->
                Single.fromCallable {
                    stopFakeCounter(cmd.id)
                    if (cmd.clearStateDirty) {
                        // We sleep a bit before clearing dirty flag to make sure
                        // the zone counter on the device has also finished
                        Sleeper.sleep(SECONDS_WAIT * DateTimeConstants.MILLIS_PER_SECOND)
                        DecrementStateDirtyMsg(cmd.id)
                    } else {
                        Idle
                    }
                }
            is SleepThenMsgCmd -> Observable
                    .interval(cmd.seconds, TimeUnit.SECONDS)
                    .firstOrError()
                    .map { cmd.msg }
            else -> Single.just(Idle)
        }
    }

    override fun render(state: ElmState) {
        view.render(state as WaterNowState)
        container.render(state)
    }

    private fun startFakeCounter(zoneId: Long, initialValue: Int) {
        stopAnyFakeCounter()
        fakeCounter = FakeCounter(zoneId, initialValue)
        fakeCounter?.startCounting()
    }

    private fun stopFakeCounter(zoneId: Long) {
        if (fakeCounter?.zoneId == zoneId) {
            fakeCounter?.stopCounting()
            fakeCounter = null
        }
    }

    private fun stopAnyFakeCounter() {
        fakeCounter?.stopCounting()
        fakeCounter = null
    }

    private fun stopZone(id: Long): Single<Irrelevant> {
        return if (features.isAtLeastSpk2) {
            sprinklerRepository.stopZone(id)
        } else {
            // Not sure but new API on SPK1 does not have stop endpoint so we need to call start with 0 duration
            sprinklerRepository.startZone(id, 0)
        }
    }

    private fun saveWateringDuration(zoneId: Long, duration: Int) {
        if (features.isAtLeastSpk2) {
            saveWateringDuration.execute(
                    SaveWateringDuration.RequestModel(zoneId, duration.toLong()))
                    .onErrorResumeNext(Observable.empty())
                    .subscribeOn(schedulerProvider.io())
                    .subscribe()
        } else {
            val wateringZone = databaseRepository.getWateringZone(device, zoneId)
            if (wateringZone == null) {
                val newWateringZone = WateringZone()
                newWateringZone.zoneId = zoneId
                newWateringZone.deviceId = device.deviceId
                newWateringZone.seconds = duration
                databaseRepository.saveWateringZone(newWateringZone)
            } else {
                databaseRepository.updateWateringZone(wateringZone._id, duration)
            }
        }
    }

    private inner class FakeCounter(val zoneId: Long, val initialCounter: Int) {

        private var disposableFakeCounter: Disposable? = null
        var currentCounter: Int = initialCounter

        fun startCounting() {
            stopCounting()
            disposableFakeCounter = Observable.intervalRange(0L, initialCounter.toLong() + 1, 0, 1,
                    TimeUnit.SECONDS)
                    .map { value -> initialCounter - value }
                    .observeOn(schedulerProvider.ui())
                    .subscribe({ counter ->
                        currentCounter = counter.toInt()
                        elm.accept(FakeCounterDataMsg(zoneId, counter.toInt()))
                    })
            disposables.add(disposableFakeCounter!!)
        }

        fun stopCounting() {
            if (disposableFakeCounter != null) {
                disposables.remove(disposableFakeCounter!!)
                disposableFakeCounter = null
            }
        }
    }

    private fun defaultManualStartDurations(): Single<List<Long>> {
        if (features.isAtLeastSpk2) {
            return sprinklerRepository.provision()
                    .map { provision ->
                        val list = MutableList(16) { DomainUtils.DEFAULT_WATER_ZONE_TIMER.toLong() }
                        if (provision.system.zoneDuration != null) {
                            for (i in 0 until provision.system.zoneDuration.size) {
                                list[i] = provision.system.zoneDuration[i]
                            }
                        }
                        list
                    }
        } else {
            return Single.fromCallable {
                val NUM_MAX_ZONES = 16
                val list = MutableList(
                        NUM_MAX_ZONES) { DomainUtils.DEFAULT_WATER_ZONE_TIMER.toLong() }
                for (i in 0 until NUM_MAX_ZONES) {
                    val wateringZone = databaseRepository.getWateringZone(device, i.toLong())
                    if (wateringZone != null) {
                        list[i] = wateringZone.seconds.toLong()
                    }
                }
                list
            }
        }
    }

    // todo: support for old API 3 ?
}