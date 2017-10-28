package com.rainmachine.presentation.screens.programs

import com.pacoworks.rxtuples2.RxTuples
import com.rainmachine.R
import com.rainmachine.data.boundary.SprinklerRepositoryImpl
import com.rainmachine.domain.model.Program
import com.rainmachine.domain.notifiers.*
import com.rainmachine.domain.usecases.handpreference.GetHandPreference
import com.rainmachine.domain.usecases.program.SaveProgram
import com.rainmachine.domain.util.*
import com.rainmachine.presentation.util.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ProgramsPresenter(
        private val features: Features,
        private val sprinklerRepository: SprinklerRepositoryImpl,
        private val getHandPreference: GetHandPreference,
        private val programChangeNotifier: ProgramChangeNotifier,
        private val statsNeedRefreshNotifier: StatsNeedRefreshNotifier,
        private val saveProgram: SaveProgram,
        private val manualStopAllWateringNotifier: ManualStopAllWateringNotifier,
        private val handPreferenceNotifier: HandPreferenceNotifier,
        private val schedulerProvider: SchedulerProvider
) : BasePresenter<ProgramsContract.View>(), ProgramsContract.Presenter {

    private val elm: Elm = Elm()
    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun attachView(view: ProgramsContract.View) {
        super.attachView(view)
        disposables.add(elm.init(ProgramsState.initialize(features.useNewApi()), this))
        elm.render()
        elm.accept(FirstMsg)

        subscribeToNotifiers()
    }

    private fun subscribeToNotifiers() {
        disposables.add(manualStopAllWateringNotifier.observe()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ elm.accept(PingNetworkMsg) }))

        disposables.add(handPreferenceNotifier.observe()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ handPreference ->
                    elm.accept(HandPreferenceChangeMsg(handPreference))
                }))
    }

    override fun start() {
        if (disposables.size() > 0) {
            elm.accept(PingNetworkMsg)
        }
    }

    override fun destroy() {
        disposables.clear()
    }

    override fun onClickRetry() {
        elm.accept(PingNetworkMsg)
    }

    override fun onClickAddProgram() {
        val state = elm.getState() as ProgramsState
        val newProgram = Program.defaultProgram(view.newProgramString,
                state.visibleZonesForNewProgram, state.sprinklerLocalDateTime)
        view.goToEditScreen(newProgram, state.sprinklerLocalDateTime, state.use24HourFormat,
                state.isUnitsMetric, features.showNewProgramDetailsScreen())
    }

    override fun onClickStartStop(program: Program) {
        elm.accept(ClickStartStopProgramMsg(program))
    }

    override fun onClickEditMore(program: Program) {
        elm.accept(ClickEditMoreMsg(program))
    }

    override fun onShowingEditMoreDialog() {
        elm.accept(ShowingEditMoreMsg)
    }

    override fun onClickEdit(extra: MoreProgramActionsExtra) {
        view.goToEditScreen(extra.program, extra.sprinklerLocalDateTime, extra.use24HourFormat,
                extra.isUnitsMetric, features.showNewProgramDetailsScreen())
    }

    override fun onClickClone(extra: MoreProgramActionsExtra) {
        elm.accept(ClickCopyProgramMsg(extra.program))
    }

    override fun onClickActivateDeactivate(extra: MoreProgramActionsExtra) {
        elm.accept(ClickActivateDeactivateMsg(extra.program, extra.use24HourFormat))
    }

    override fun onClickDelete(extra: MoreProgramActionsExtra) {
        elm.accept(ClickDeleteProgramMsg(extra.program))
    }

    override fun update(msg: Msg, state: ElmState): Pair<ElmState, Cmd> {
        val oldState = state as ProgramsState
        return when (msg) {
            is FirstMsg -> msg.reduce(oldState)

            is PingNetworkMsg -> msg.reduce(oldState)
            is FirstTimeDataMsg -> msg.reduce(oldState)
            is FirstTimeData3Msg -> msg.reduce(oldState)
            is LiveDataMsg -> msg.reduce(oldState)
            is LiveData3Msg -> msg.reduce(oldState)

            is ClickEditMoreMsg -> msg.reduce(oldState)
            is ShowingEditMoreMsg -> msg.reduce(oldState)

            is ClickDeleteProgramMsg -> msg.reduce(oldState)
            is ClickCopyProgramMsg -> msg.reduce(oldState)
            is ClickStartStopProgramMsg -> msg.reduce(oldState)
            is ClickActivateDeactivateMsg -> msg.reduce(oldState)

            is HandPreferenceChangeMsg -> msg.reduce(oldState)

            is ErrorMsg -> {
                Timber.d("Error ${msg.err.message} for ${msg.cmd}")
                return when (msg.cmd) {
                    else -> Pair(oldState.copy(isProgress = false, isContent = true), None)
                }
            }
            else -> Pair(oldState, None)
        }
    }

    override fun call(cmd: Cmd): Single<Msg> {
        return when (cmd) {
            is GetFirstTimeDataCmd -> Single.zip(
                    sprinklerRepository.programs(true),
                    sprinklerRepository.zonesProperties(),
                    sprinklerRepository.devicePreferences(),
                    sprinklerRepository.timeDate(),
                    getHandPreference.execute(GetHandPreference.RequestModel())
                            .map { responseModel -> responseModel.handPreference },
                    RxTuples.toQuintet())
                    .map { quintet -> FirstTimeDataMsg(quintet) }
            is GetFirstTimeData3Cmd -> Single.zip(
                    sprinklerRepository.programs3(),
                    sprinklerRepository.zonesProperties3(),
                    getHandPreference.execute(GetHandPreference.RequestModel())
                            .map { responseModel -> responseModel.handPreference },
                    RxTuples.toTriplet())
                    .map { triplet -> FirstTimeData3Msg(triplet) }
            is GetLiveDataCmd -> Single.zip(
                    sprinklerRepository.programs(true),
                    sprinklerRepository.zonesProperties(),
                    sprinklerRepository.devicePreferences(),
                    sprinklerRepository.timeDate(),
                    RxTuples.toQuartet())
                    .map { quartet -> LiveDataMsg(quartet) }
            is GetLiveData3Cmd -> Single.zip(
                    sprinklerRepository.programs3(),
                    sprinklerRepository.zonesProperties3(),
                    RxTuples.toPair())
                    .map { pair -> LiveData3Msg(pair) }
            is DeleteProgramCmd -> deleteProgram(cmd.program)
                    .map { PingNetworkMsg }
            is CopyProgramCmd -> copyProgram(cmd.program, cmd.use24HourFormat)
                    .map { PingNetworkMsg }
            is StartStopProgramCmd -> startStopProgram(cmd.program)
                    .delay((if (features.isAtLeastSpk2) 5 else 10).toLong(), TimeUnit.SECONDS)
                    .doOnNext { programChangeNotifier.publish(ProgramChange.StartStop) }
                    .firstOrError()
                    .map { PingNetworkMsg }
            is UpdateProgramCmd -> saveProgram
                    .execute(SaveProgram.RequestModel(cmd.program, cmd.originalProgram,
                            cmd.use24HourFormat))
                    .firstOrError()
                    .map { PingNetworkMsg }
            else -> Single.just(Idle)
        }
    }

    override fun render(state: ElmState) {
        view.render(state as ProgramsState)
    }

    private fun deleteProgram(program: Program): Single<Irrelevant> {
        val stream = if (features.useNewApi()) {
            sprinklerRepository.deleteProgram(program.id)
        } else {
            sprinklerRepository.deleteProgram3(program.id)
        }
        return stream
                .doOnComplete {
                    Toasts.show(R.string.program_details_success_delete_program)
                    programChangeNotifier.publish(ProgramChange.Properties)
                    statsNeedRefreshNotifier.publish(Any())
                }
                .andThen(Single.just(Irrelevant.INSTANCE))
                .compose(RunToCompletionSingle.instance())
    }

    private fun copyProgram(originalProgram: Program,
                            use24HourFormat: Boolean): Single<Irrelevant> {
        val program = originalProgram.cloneIt()
        program.id = 0
        program.name = view.getCopyProgramString(originalProgram.name)
        val stream = if (features.useNewApi()) {
            sprinklerRepository.createProgram(program)
        } else {
            sprinklerRepository.copyProgram3(program, use24HourFormat)
        }
        return stream
                .doOnComplete {
                    Toasts.show(R.string.program_details_success_copy_program)
                    programChangeNotifier.publish(ProgramChange.Properties)
                    statsNeedRefreshNotifier.publish(Any())
                }
                .andThen(Single.just(Irrelevant.INSTANCE))
                .compose(RunToCompletionSingle.instance())
    }

    private fun startStopProgram(program: Program): Observable<Irrelevant> {
        val runObservable = if (features.useNewApi()) {
            if (program.wateringState == Program.WateringState.IDLE) {
                sprinklerRepository.startProgram(program.id).toObservable()
            } else {
                sprinklerRepository.stopProgram(program.id).toObservable()
            }
        } else {
            sprinklerRepository.runStopProgram3(program.id).toObservable()
        }
        return runObservable.compose(RunToCompletion.instance())
    }
}