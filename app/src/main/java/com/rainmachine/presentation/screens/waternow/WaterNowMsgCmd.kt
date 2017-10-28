package com.rainmachine.presentation.screens.waternow

import com.rainmachine.data.local.database.model.DeviceSettings
import com.rainmachine.data.local.database.model.ZoneSettings
import com.rainmachine.domain.model.HandPreference
import com.rainmachine.domain.model.Program
import com.rainmachine.domain.model.Zone
import com.rainmachine.domain.model.ZoneProperties
import com.rainmachine.domain.usecases.handpreference.GetHandPreference
import com.rainmachine.presentation.util.Cmd
import com.rainmachine.presentation.util.Msg
import com.rainmachine.presentation.util.None
import org.javatuples.Quintet
import org.joda.time.DateTimeConstants

// Commands
object GetFirstTimeDataCmd : Cmd()

data class GetWateringDataCmd(val totalMachineDurationDirtyMap: Map<Long, Boolean>,
                              val zoneStateDirtyMap: Map<Long, Boolean>,
                              val timeCall: Long) : Cmd()

object GetProgramPropertiesDataCmd : Cmd()

data class StartZoneCmd(val id: Long, val duration: Int, val startFakeCounter: Boolean) : Cmd()

data class ChangeZoneCounterCmd(val id: Long, val duration: Int, val startFakeCounter: Boolean,
                                val fakeCounterInitialValue: Int = 0) : Cmd()

data class StopZoneCmd(val id: Long, val stopFakeCounter: Boolean) : Cmd()

data class StopAllCmd(val dirtyZones: List<Long>) : Cmd()

data class StartFakeCounterCmd(val id: Long, val initialValue: Int) : Cmd()

data class StopFakeCounterCmd(val id: Long, val clearStateDirty: Boolean) : Cmd()

data class SleepThenMsgCmd(val seconds: Long, val msg: Msg) : Cmd()


// Messages
abstract class WaterNowMsg : Msg() {
    abstract fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd>
}

object FirstMsg : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        return Pair(oldState.copy(initialize = false, isProgress = true), GetFirstTimeDataCmd)
    }
}

data class CoreDataMsg(
        private val quintet: Quintet<List<ZoneProperties>, List<Long>, List<Program>, DeviceSettings, GetHandPreference.ResponseModel>
) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        val enabledZones = ArrayList<ZoneViewModel>()
        val disabledZones = ArrayList<ZoneViewModel>()
        for (zoneProperties in quintet.value0) {
            val nextProgramToRun = nextProgramToRun(quintet.value2, zoneProperties.id)
            val zoneSettings = quintet.value3.zones[zoneProperties.id] ?: ZoneSettings(
                    zoneProperties.id)
            val defaultManualStartDuration = quintet.value1[zoneProperties.id.toInt() - 1].toInt()
            val isEnabled = zoneProperties.enabled && !zoneProperties.masterValve

            val zoneViewModel = ZoneViewModel(zoneProperties.id, zoneProperties.name,
                    isEnabled = isEnabled, isMasterValve = zoneProperties.masterValve,
                    nextProgramToRun = nextProgramToRun, zoneSettings = zoneSettings,
                    defaultManualStartDuration = defaultManualStartDuration)

            if (isEnabled) {
                enabledZones.add(zoneViewModel)
            } else {
                disabledZones.add(zoneViewModel)
            }
        }
        enabledZones.sortBy { it.id }
        disabledZones.sortBy { it.id }

        return Pair(oldState.copy(isProgress = false, isContent = true,
                enabledZones = enabledZones, disabledZones = disabledZones,
                handPreference = quintet.value4.handPreference),
                GetWateringDataCmd(oldState.totalMachineDurationDirtyMap(),
                        oldState.zoneStateDirtyMap(), System.currentTimeMillis()))
    }
}

object PingNetworkMsg : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        return Pair(oldState, GetWateringDataCmd(oldState.totalMachineDurationDirtyMap(),
                oldState.zoneStateDirtyMap(), System.currentTimeMillis()))
    }
}

data class WateringDataMsg(private val zones: List<Zone>,
                           private val totalMachineDurationDirtyMapWhenCalled: Map<Long, Boolean>,
                           private val zoneStateDirtyMapWhenCalled: Map<Long, Boolean>,
                           private val timeCall: Long
) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        if (oldState.initialize) {
            return Pair(oldState, None)
        }

        val enabledZones = oldState.enabledZones.toMutableList()
        val disabledZones = oldState.disabledZones.toMutableList()
        var cmd: Cmd = None
        for (zone in zones) {
            var zoneViewModel = oldState.enabledZones.find { it.id == zone.id }
            if (zoneViewModel == null) {
                zoneViewModel = oldState.disabledZones.find { it.id == zone.id }
            }
            if (zoneViewModel == null) {
                continue
            }

            var state = zoneViewModel.state
            var runningCounter = zoneViewModel.runningCounter
            var totalMachineDuration = zoneViewModel.totalMachineDuration

            if (zoneStateDirtyMapWhenCalled[zoneViewModel.id] != true
                    && timeCall > zoneViewModel.stateLastTimeDirty) {
                state = if (zone.isWatering) {
                    ZoneViewModel.State.RUNNING
                } else if (zone.isPending && zone.counter > 0) {
                    ZoneViewModel.State.PENDING
                } else {
                    ZoneViewModel.State.IDLE
                }
            }
            if (totalMachineDurationDirtyMapWhenCalled[zoneViewModel.id] != true
                    && timeCall > zoneViewModel.totalMachineDurationlastTimeDirty) {
                runningCounter = zone.counter
                totalMachineDuration = if (state == ZoneViewModel.State.PENDING) {
                    zone.counter // On SPK1, machineDuration field is not set so we use the counter
                } else {
                    zone.machineDuration.toInt()
                }
            }

            // Master valve may be running but we do not use fake counter for it
            if (!zone.isMaster) {
                val changedStateToRunning = state == ZoneViewModel.State.RUNNING
                        && zoneViewModel.state != ZoneViewModel.State.RUNNING
                val diffTooGreat = state == ZoneViewModel.State.RUNNING
                        && zoneViewModel.state == ZoneViewModel.State.RUNNING
                        && Math.abs(runningCounter - zoneViewModel.runningCounter) > 4
                if (changedStateToRunning || diffTooGreat) {
                    cmd = StartFakeCounterCmd(zone.id, runningCounter)
                } else if (state == ZoneViewModel.State.RUNNING) {
                    // Keep the old running counter in order not to flick the counter on screen
                    runningCounter = zoneViewModel.runningCounter
                }
            }

            val newZoneViewModel = zoneViewModel.copy(state = state,
                    runningCounter = runningCounter, totalMachineDuration = totalMachineDuration)
            if (zoneViewModel.isEnabled) {
                val index = enabledZones.indexOf(zoneViewModel)
                enabledZones[index] = newZoneViewModel
            } else {
                val index = disabledZones.indexOf(zoneViewModel)
                disabledZones[index] = newZoneViewModel
            }
        }
        return Pair(oldState.copy(enabledZones = enabledZones, disabledZones = disabledZones,
                hasCompleteData = true), cmd)
    }
}

data class ClickStartZoneMsg(private val zone: ZoneViewModel) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        return Pair(oldState.copy(showStartZoneDialog = true, dialogStartZone = zone), None)
    }
}

object ShowingStartZoneDialogMsg : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        return Pair(oldState.copy(showStartZoneDialog = false, dialogStartZone = null), None)
    }
}

data class ConfirmStartZoneMsg(val id: Long, val duration: Int) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        var zoneViewModel = oldState.enabledZones.find { it.id == id }
        if (zoneViewModel == null) {
            zoneViewModel = oldState.disabledZones.find { it.id == id }
        }
        if (zoneViewModel == null) {
            return Pair(oldState, None)
        }

        val isWateringInProgress = (oldState.enabledZones + oldState.disabledZones).any { it.state != ZoneViewModel.State.IDLE }
        val nextState: ZoneViewModel.State = if (isWateringInProgress) {
            ZoneViewModel.State.PENDING
        } else {
            ZoneViewModel.State.RUNNING
        }
        val newZoneViewModel = zoneViewModel.copy(state = nextState,
                runningCounter = duration, defaultManualStartDuration = duration,
                totalMachineDuration = duration,
                totalMachineDurationlastTimeDirty = System.currentTimeMillis(),
                stateDirtyCount = zoneViewModel.stateDirtyCount + 1,
                stateLastTimeDirty = System.currentTimeMillis())
        val cmd: Cmd = StartZoneCmd(id, duration, !isWateringInProgress)

        return if (zoneViewModel.isEnabled) {
            val enabledZones = oldState.enabledZones.toMutableList()
            val index = enabledZones.indexOf(zoneViewModel)
            enabledZones[index] = newZoneViewModel
            Pair(oldState.copy(enabledZones = enabledZones), cmd)
        } else {
            val disabledZones = oldState.disabledZones.toMutableList()
            val index = disabledZones.indexOf(zoneViewModel)
            disabledZones[index] = newZoneViewModel
            Pair(oldState.copy(disabledZones = disabledZones), cmd)
        }
    }
}

object ClickStopAllMsg : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        return Pair(oldState.copy(showStopAllDialog = true), None)
    }
}

object ShowingStopAllDialogMsg : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        return Pair(oldState.copy(showStopAllDialog = false), None)
    }
}

object ConfirmStopAllMsg : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        val enabledZones = oldState.enabledZones.toMutableList()
        val disabledZones = oldState.disabledZones.toMutableList()
        val dirtyZones = ArrayList<Long>()

        oldState.enabledZones.forEachIndexed { index, zoneViewModel ->
            val stateDirtyCount: Int
            val stateLastTimeDirty: Long
            if (zoneViewModel.state != ZoneViewModel.State.IDLE) {
                stateDirtyCount = zoneViewModel.stateDirtyCount + 1
                stateLastTimeDirty = System.currentTimeMillis()
                dirtyZones.add(zoneViewModel.id)
            } else {
                stateDirtyCount = zoneViewModel.stateDirtyCount
                stateLastTimeDirty = zoneViewModel.stateLastTimeDirty
            }
            enabledZones[index] = zoneViewModel.copy(state = ZoneViewModel.State.IDLE,
                    stateDirtyCount = stateDirtyCount, stateLastTimeDirty = stateLastTimeDirty)
        }
        oldState.disabledZones.forEachIndexed { index, zoneViewModel ->
            val stateDirtyCount: Int
            val stateLastTimeDirty: Long
            if (zoneViewModel.state != ZoneViewModel.State.IDLE) {
                stateDirtyCount = zoneViewModel.stateDirtyCount + 1
                stateLastTimeDirty = System.currentTimeMillis()
                dirtyZones.add(zoneViewModel.id)
            } else {
                stateDirtyCount = zoneViewModel.stateDirtyCount
                stateLastTimeDirty = zoneViewModel.stateLastTimeDirty
            }
            disabledZones[index] = zoneViewModel.copy(state = ZoneViewModel.State.IDLE,
                    stateDirtyCount = stateDirtyCount, stateLastTimeDirty = stateLastTimeDirty)
        }
        return Pair(oldState.copy(enabledZones = enabledZones, disabledZones = disabledZones),
                StopAllCmd(dirtyZones))
    }
}

data class ZoneImagesChangeMsg(private val deviceSettings: DeviceSettings) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        val cmd = None
        if (oldState.initialize) {
            return Pair(oldState, cmd)
        }
        val enabledZones = oldState.enabledZones.toMutableList()
        val disabledZones = oldState.disabledZones.toMutableList()

        oldState.enabledZones.forEachIndexed { index, zoneViewModel ->
            enabledZones[index] = zoneViewModel.copy(
                    zoneSettings = deviceSettings.zones[zoneViewModel.id] ?: ZoneSettings(
                            zoneViewModel.id))
        }
        oldState.disabledZones.forEachIndexed { index, zoneViewModel ->
            disabledZones[index] = zoneViewModel.copy(
                    zoneSettings = deviceSettings.zones[zoneViewModel.id] ?: ZoneSettings(
                            zoneViewModel.id))
        }
        return Pair(oldState.copy(enabledZones = enabledZones, disabledZones = disabledZones), cmd)
    }
}

object ProgramStartStopChangeMsg : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        return Pair(oldState, GetWateringDataCmd(oldState.totalMachineDurationDirtyMap(),
                oldState.zoneStateDirtyMap(), System.currentTimeMillis()))
    }
}

object ProgramPropertiesChangeMsg : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        return Pair(oldState, GetProgramPropertiesDataCmd)
    }
}

data class ProgramPropertiesDataMsg(private val programs: List<Program>) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        val cmd = None
        if (oldState.initialize) {
            return Pair(oldState, cmd)
        }
        val enabledZones = oldState.enabledZones.toMutableList()
        val disabledZones = oldState.disabledZones.toMutableList()
        for ((index, zoneViewModel) in oldState.enabledZones.withIndex()) {
            enabledZones[index] = zoneViewModel.copy(
                    nextProgramToRun = nextProgramToRun(programs, zoneViewModel.id))
        }
        for ((index, zoneViewModel) in oldState.disabledZones.withIndex()) {
            disabledZones[index] = zoneViewModel.copy(
                    nextProgramToRun = nextProgramToRun(programs, zoneViewModel.id))
        }
        return Pair(oldState.copy(enabledZones = enabledZones, disabledZones = disabledZones), cmd)
    }
}

data class ZonePropertyChangeMsg(private val zoneId: Long,
                                 private val zoneName: String,
                                 private val isEnabled: Boolean,
                                 private val isMasterValve: Boolean) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        val enabledZones = oldState.enabledZones.toMutableList()
        val disabledZones = oldState.disabledZones.toMutableList()
        for ((index, zoneViewModel) in oldState.enabledZones.withIndex()) {
            if (zoneViewModel.id == zoneId) {
                val belongsToEnabledList = isEnabled && !isMasterValve
                if (belongsToEnabledList) {
                    val newZoneViewModel = zoneViewModel.copy(name = zoneName,
                            isMasterValve = false)
                    enabledZones[index] = newZoneViewModel
                } else {
                    enabledZones.remove(zoneViewModel)
                    disabledZones.add(zoneViewModel.copy(name = zoneName, isEnabled = false,
                            isMasterValve = isMasterValve))
                }
                break
            }
        }
        for ((index, zoneViewModel) in oldState.disabledZones.withIndex()) {
            if (zoneViewModel.id == zoneId) {
                val belongsToDisabledList = !isEnabled || isMasterValve
                if (belongsToDisabledList) {
                    val newZoneViewModel = zoneViewModel.copy(name = zoneName,
                            isMasterValve = isMasterValve)
                    disabledZones[index] = newZoneViewModel
                } else {
                    disabledZones.remove(zoneViewModel)
                    enabledZones.add(zoneViewModel.copy(name = zoneName, isEnabled = true,
                            isMasterValve = isMasterValve))
                }
                break
            }
        }
        enabledZones.sortBy { it.id }
        disabledZones.sortBy { it.id }
        return Pair(oldState.copy(enabledZones = enabledZones, disabledZones = disabledZones), None)
    }
}

data class FakeCounterDataMsg(private val zoneId: Long, private val counter: Int) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        if (oldState.initialize) {
            return Pair(oldState, None)
        }

        var zoneViewModel = oldState.enabledZones.find { it.id == zoneId }
        if (zoneViewModel == null) {
            zoneViewModel = oldState.disabledZones.find { it.id == zoneId }
        }
        if (zoneViewModel == null) {
            return Pair(oldState, None)
        }

        if (zoneViewModel.state == ZoneViewModel.State.RUNNING) {
            val finishedRunning = counter <= 0
            val cmd = if (finishedRunning) StopFakeCounterCmd(zoneId,
                    clearStateDirty = true) else None

            val newZoneViewModel = if (!finishedRunning) {
                zoneViewModel.copy(runningCounter = counter)
            } else {
                zoneViewModel.copy(state = ZoneViewModel.State.IDLE,
                        stateDirtyCount = zoneViewModel.stateDirtyCount + 1,
                        stateLastTimeDirty = System.currentTimeMillis())
            }

            return if (zoneViewModel.isEnabled) {
                val enabledZones = oldState.enabledZones.toMutableList()
                val index = enabledZones.indexOf(zoneViewModel)
                enabledZones[index] = newZoneViewModel
                Pair(oldState.copy(enabledZones = enabledZones), cmd)
            } else {
                val disabledZones = oldState.disabledZones.toMutableList()
                val index = disabledZones.indexOf(zoneViewModel)
                disabledZones[index] = newZoneViewModel
                Pair(oldState.copy(disabledZones = disabledZones), cmd)
            }
        } else {
            return Pair(oldState, StopFakeCounterCmd(zoneId, clearStateDirty = false))
        }
    }
}

data class ClickStopZoneMsg(val zone: ZoneViewModel) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        val cmd = StopZoneCmd(zone.id, zone.state == ZoneViewModel.State.RUNNING)
        var zoneViewModel = oldState.enabledZones.find { it == zone }
        if (zoneViewModel == null) {
            zoneViewModel = oldState.disabledZones.find { it == zone }
        }
        if (zoneViewModel == null) {
            return Pair(oldState, cmd)
        }

        val newZoneViewModel = zoneViewModel.copy(state = ZoneViewModel.State.IDLE,
                stateDirtyCount = zoneViewModel.stateDirtyCount + 1,
                stateLastTimeDirty = System.currentTimeMillis())

        return if (zoneViewModel.isEnabled) {
            val enabledZones = oldState.enabledZones.toMutableList()
            val index = enabledZones.indexOf(zoneViewModel)
            enabledZones[index] = newZoneViewModel
            Pair(oldState.copy(enabledZones = enabledZones), cmd)
        } else {
            val disabledZones = oldState.disabledZones.toMutableList()
            val index = disabledZones.indexOf(zoneViewModel)
            disabledZones[index] = newZoneViewModel
            Pair(oldState.copy(disabledZones = disabledZones), cmd)
        }
    }
}

data class ClickPlusMsg(val zone: ZoneViewModel) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        var zoneViewModel = oldState.enabledZones.find { it == zone }
        if (zoneViewModel == null) {
            zoneViewModel = oldState.disabledZones.find { it == zone }
        }
        if (zoneViewModel == null) {
            return Pair(oldState, None)
        }

        val totalMachineDuration = zone.totalMachineDuration + DateTimeConstants.SECONDS_PER_MINUTE
        val newZoneViewModel: ZoneViewModel
        val cmd: Cmd
        if (zoneViewModel.state == ZoneViewModel.State.RUNNING) {
            val runningCounter = zoneViewModel.runningCounter + DateTimeConstants.SECONDS_PER_MINUTE
            val dirtyCount = zoneViewModel.totalMachineDurationDirtyCount + 1
            newZoneViewModel = zoneViewModel.copy(runningCounter = runningCounter,
                    totalMachineDuration = totalMachineDuration,
                    totalMachineDurationDirtyCount = dirtyCount,
                    totalMachineDurationlastTimeDirty = System.currentTimeMillis())
            cmd = ChangeZoneCounterCmd(zone.id, totalMachineDuration,
                    startFakeCounter = true, fakeCounterInitialValue = runningCounter)
        } else {
            val dirtyCount = zoneViewModel.totalMachineDurationDirtyCount + 1
            newZoneViewModel = zoneViewModel.copy(totalMachineDuration = totalMachineDuration,
                    totalMachineDurationDirtyCount = dirtyCount,
                    totalMachineDurationlastTimeDirty = System.currentTimeMillis())
            cmd = ChangeZoneCounterCmd(zone.id, totalMachineDuration,
                    startFakeCounter = false)
        }

        return if (zoneViewModel.isEnabled) {
            val enabledZones = oldState.enabledZones.toMutableList()
            val index = enabledZones.indexOf(zoneViewModel)
            enabledZones[index] = newZoneViewModel
            Pair(oldState.copy(enabledZones = enabledZones), cmd)
        } else {
            val disabledZones = oldState.disabledZones.toMutableList()
            val index = disabledZones.indexOf(zoneViewModel)
            disabledZones[index] = newZoneViewModel
            Pair(oldState.copy(disabledZones = disabledZones), cmd)
        }
    }
}

data class ClickMinusMsg(val zone: ZoneViewModel) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        var zoneViewModel = oldState.enabledZones.find { it == zone }
        if (zoneViewModel == null) {
            zoneViewModel = oldState.disabledZones.find { it == zone }
        }
        if (zoneViewModel == null) {
            return Pair(oldState, None)
        }

        val totalMachineDuration = zone.totalMachineDuration - DateTimeConstants.SECONDS_PER_MINUTE
        val tentativeRunningCounter: Int
        val cmd: Cmd
        val newZoneViewModel: ZoneViewModel = if (zoneViewModel.state == ZoneViewModel.State.RUNNING) {
            tentativeRunningCounter = zoneViewModel.runningCounter - DateTimeConstants.SECONDS_PER_MINUTE
            if (tentativeRunningCounter > 0) {
                cmd = ChangeZoneCounterCmd(zone.id, totalMachineDuration,
                        startFakeCounter = true,
                        fakeCounterInitialValue = tentativeRunningCounter)
                val dirtyCount = zoneViewModel.totalMachineDurationDirtyCount + 1
                zoneViewModel.copy(runningCounter = tentativeRunningCounter,
                        totalMachineDuration = totalMachineDuration,
                        totalMachineDurationDirtyCount = dirtyCount,
                        totalMachineDurationlastTimeDirty = System.currentTimeMillis())
            } else {
                cmd = StopZoneCmd(zone.id, stopFakeCounter = true)
                zoneViewModel.copy(state = ZoneViewModel.State.IDLE,
                        stateDirtyCount = zoneViewModel.stateDirtyCount + 1,
                        stateLastTimeDirty = System.currentTimeMillis())
            }
        } else {
            if (totalMachineDuration > 0) {
                cmd = ChangeZoneCounterCmd(zone.id, totalMachineDuration,
                        startFakeCounter = false,
                        fakeCounterInitialValue = totalMachineDuration)
                val dirtyCount = zoneViewModel.totalMachineDurationDirtyCount + 1
                zoneViewModel.copy(totalMachineDuration = totalMachineDuration,
                        totalMachineDurationDirtyCount = dirtyCount,
                        totalMachineDurationlastTimeDirty = System.currentTimeMillis())
            } else {
                cmd = StopZoneCmd(zone.id, stopFakeCounter = true)
                zoneViewModel.copy(state = ZoneViewModel.State.IDLE,
                        stateDirtyCount = zoneViewModel.stateDirtyCount + 1,
                        stateLastTimeDirty = System.currentTimeMillis())
            }
        }

        return if (zoneViewModel.isEnabled) {
            val enabledZones = oldState.enabledZones.toMutableList()
            val index = enabledZones.indexOf(zoneViewModel)
            enabledZones[index] = newZoneViewModel
            Pair(oldState.copy(enabledZones = enabledZones), cmd)
        } else {
            val disabledZones = oldState.disabledZones.toMutableList()
            val index = disabledZones.indexOf(zoneViewModel)
            disabledZones[index] = newZoneViewModel
            Pair(oldState.copy(disabledZones = disabledZones), cmd)
        }
    }
}

data class DecrementTotalMachineDurationDirtyCountMsg(private val zoneId: Long) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        val cmd = None
        var zoneViewModel = oldState.enabledZones.find { it.id == zoneId }
        if (zoneViewModel == null) {
            zoneViewModel = oldState.disabledZones.find { it.id == zoneId }
        }
        if (zoneViewModel == null) {
            return Pair(oldState, cmd)
        }

        val dirtyCount = maxOf(zoneViewModel.totalMachineDurationDirtyCount - 1, 0)
        val newZoneViewModel = zoneViewModel.copy(totalMachineDurationDirtyCount = dirtyCount)

        return if (zoneViewModel.isEnabled) {
            val enabledZones = oldState.enabledZones.toMutableList()
            val index = enabledZones.indexOf(zoneViewModel)
            enabledZones[index] = newZoneViewModel
            Pair(oldState.copy(enabledZones = enabledZones), cmd)
        } else {
            val disabledZones = oldState.disabledZones.toMutableList()
            val index = disabledZones.indexOf(zoneViewModel)
            disabledZones[index] = newZoneViewModel
            Pair(oldState.copy(disabledZones = disabledZones), cmd)
        }
    }
}

data class DecrementStateDirtyMsg(private val zoneId: Long) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        val cmd = SleepThenMsgCmd(4, PingNetworkMsg)
        var zoneViewModel = oldState.enabledZones.find { it.id == zoneId }
        if (zoneViewModel == null) {
            zoneViewModel = oldState.disabledZones.find { it.id == zoneId }
        }
        if (zoneViewModel == null) {
            return Pair(oldState, cmd)
        }

        val dirtyCount = maxOf(zoneViewModel.stateDirtyCount - 1, 0)
        val newZoneViewModel = zoneViewModel.copy(stateDirtyCount = dirtyCount)

        return if (zoneViewModel.isEnabled) {
            val enabledZones = oldState.enabledZones.toMutableList()
            val index = enabledZones.indexOf(zoneViewModel)
            enabledZones[index] = newZoneViewModel
            Pair(oldState.copy(enabledZones = enabledZones), cmd)
        } else {
            val disabledZones = oldState.disabledZones.toMutableList()
            val index = disabledZones.indexOf(zoneViewModel)
            disabledZones[index] = newZoneViewModel
            Pair(oldState.copy(disabledZones = disabledZones), cmd)
        }
    }
}

data class DecrementMultipleStateDirtyMsg(private val dirtyZones: List<Long>) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        val enabledZones = oldState.enabledZones.toMutableList()
        val disabledZones = oldState.disabledZones.toMutableList()
        for (zoneId in dirtyZones) {
            var zoneViewModel = oldState.enabledZones.find { it.id == zoneId }
            if (zoneViewModel == null) {
                zoneViewModel = oldState.disabledZones.find { it.id == zoneId }
            }
            if (zoneViewModel == null) {
                continue
            }

            val dirtyCount = maxOf(zoneViewModel.stateDirtyCount - 1, 0)
            val newZoneViewModel = zoneViewModel.copy(stateDirtyCount = dirtyCount)

            if (zoneViewModel.isEnabled) {
                val index = enabledZones.indexOf(zoneViewModel)
                enabledZones[index] = newZoneViewModel
            } else {
                val index = disabledZones.indexOf(zoneViewModel)
                disabledZones[index] = newZoneViewModel
            }
        }
        return Pair(oldState.copy(enabledZones = enabledZones, disabledZones = disabledZones),
                SleepThenMsgCmd(SECONDS_WAIT, PingNetworkMsg))
    }
}

data class HandPreferenceChangeMsg(private val handPreference: HandPreference) : WaterNowMsg() {
    override fun reduce(oldState: WaterNowState): Pair<WaterNowState, Cmd> {
        return Pair(oldState.copy(handPreference = handPreference), None)
    }
}

private fun nextProgramToRun(programs: List<Program>, zoneId: Long): Program? {
    // Compute next watering for the zone
    var nextProgramToRun: Program? = null
    for (program in programs) {
        if (program.enabled) {
            for (wt in program.wateringTimes) {
                if (wt.id != zoneId) {
                    continue
                }
                if (!wt.isDoNotWater) {
                    // This program has this zone scheduled
                    if (nextProgramToRun == null || nextProgramToRun.isScheduledAfter(program)) {
                        nextProgramToRun = program
                    }
                }
            }
        }
    }
    return nextProgramToRun
}

private val SECONDS_WAIT = 4L
