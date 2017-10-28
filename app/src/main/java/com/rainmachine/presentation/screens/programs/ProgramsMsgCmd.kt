package com.rainmachine.presentation.screens.programs

import com.rainmachine.domain.model.*
import com.rainmachine.presentation.util.Cmd
import com.rainmachine.presentation.util.Msg
import com.rainmachine.presentation.util.None
import org.javatuples.Quartet
import org.javatuples.Quintet
import org.javatuples.Triplet
import org.joda.time.LocalDateTime

// Commands
object GetFirstTimeDataCmd : Cmd()

object GetFirstTimeData3Cmd : Cmd()

object GetLiveDataCmd : Cmd()

object GetLiveData3Cmd : Cmd()

data class DeleteProgramCmd(val program: Program) : Cmd()

data class CopyProgramCmd(val program: Program, val use24HourFormat: Boolean) : Cmd()

data class StartStopProgramCmd(val program: Program) : Cmd()

data class UpdateProgramCmd(val program: Program, val originalProgram: Program,
                            val use24HourFormat: Boolean) : Cmd()

// Messages
abstract class ProgramsMsg : Msg() {
    abstract fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd>
}

object FirstMsg : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        return Pair(oldState.copy(initialize = false, isProgress = true),
                if (oldState.useNewApi) GetFirstTimeDataCmd else GetFirstTimeData3Cmd)
    }
}

object PingNetworkMsg : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        return Pair(oldState.copy(isProgress = true),
                if (oldState.useNewApi) GetLiveDataCmd else GetLiveData3Cmd)
    }
}

data class FirstTimeDataMsg(
        private val quintet: Quintet<List<Program>, List<ZoneProperties>, DevicePreferences, LocalDateTime, HandPreference>
) : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        val programs = quintet.value0
        val zoneProperties = quintet.value1
        val devicePreferences = quintet.value2
        val sprinklerLocalDateTime = quintet.value3
        val handPreference = quintet.value4

        val (enabledPrograms, disabledPrograms) = splitPrograms(programs, zoneProperties)
        val visibleZones = visibledZonesForNewProgram(zoneProperties)

        return Pair(oldState.copy(isProgress = false, isContent = true,
                enabledPrograms = enabledPrograms, disabledPrograms = disabledPrograms,
                visibleZonesForNewProgram = visibleZones,
                sprinklerLocalDateTime = sprinklerLocalDateTime,
                use24HourFormat = devicePreferences.use24HourFormat,
                handPreference = handPreference), None)
    }
}

data class LiveDataMsg(
        private val quartet: Quartet<List<Program>, List<ZoneProperties>, DevicePreferences, LocalDateTime>
) : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        val programs = quartet.value0
        val zoneProperties = quartet.value1
        val devicePreferences = quartet.value2
        val sprinklerLocalDateTime = quartet.value3

        val (enabledPrograms, disabledPrograms) = splitPrograms(programs, zoneProperties)
        val visibleZones = visibledZonesForNewProgram(zoneProperties)

        return Pair(oldState.copy(isProgress = false, isContent = true,
                enabledPrograms = enabledPrograms, disabledPrograms = disabledPrograms,
                visibleZonesForNewProgram = visibleZones,
                sprinklerLocalDateTime = sprinklerLocalDateTime,
                use24HourFormat = devicePreferences.use24HourFormat), None)
    }
}

private fun splitPrograms(programs: List<Program>,
                          zoneProperties: List<ZoneProperties>
): Pair<List<Program>, List<Program>> {
    val enabledPrograms = ArrayList<Program>()
    val disabledPrograms = ArrayList<Program>()
    for (program in programs) {
        if (program.enabled) {
            enabledPrograms.add(program)
        } else {
            disabledPrograms.add(program)
        }

        var isMasterValve: Boolean
        var isEnabled: Boolean
        val it = program.wateringTimes.iterator()
        while (it.hasNext()) {
            val wt = it.next()
            isMasterValve = false
            isEnabled = false
            for (zone in zoneProperties) {
                if (wt.id == zone.id) {
                    wt.referenceTime = zone.referenceTime
                    wt.hasDefaultAdvancedSettings = zone.hasDefaultAdvancedSettings()
                    isMasterValve = zone.masterValve
                    isEnabled = zone.enabled
                    break
                }
            }
            if (isMasterValve || !isEnabled) {
                it.remove()
            }
        }
    }
    return Pair(enabledPrograms, disabledPrograms)
}

private fun visibledZonesForNewProgram(
        zoneProperties: List<ZoneProperties>
): ArrayList<ProgramWateringTimes> {
    val visibleZones = ArrayList<ProgramWateringTimes>()
    for (zone in zoneProperties) {
        if (zone.masterValve || !zone.enabled) {
            continue
        }
        val wt = ProgramWateringTimes()
        wt.id = zone.id
        wt.name = zone.name
        wt.duration = 0
        wt.active = false // new program has all zones inactive
        wt.userPercentage = 1.0f
        wt.referenceTime = zone.referenceTime
        wt.hasDefaultAdvancedSettings = zone.hasDefaultAdvancedSettings()
        visibleZones.add(wt)
    }
    return visibleZones
}

data class FirstTimeData3Msg(
        private val triplet: Triplet<org.javatuples.Pair<List<Program>, Boolean>, List<ZoneProperties>, HandPreference>
) : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        val programsNetwork = triplet.value0.value0
        val use24HourFormat = triplet.value0.value1
        val zoneProperties = triplet.value1
        val handPreference = triplet.value2

        val programs = splitPrograms3(programsNetwork, zoneProperties)
        val visibleZones = visibledZonesForNewProgram3(zoneProperties)

        return Pair(
                oldState.copy(isProgress = false, isContent = true,
                        enabledPrograms = programs, disabledPrograms = emptyList(),
                        visibleZonesForNewProgram = visibleZones,
                        use24HourFormat = use24HourFormat,
                        handPreference = handPreference), None)
    }
}

data class LiveData3Msg(
        private val pair: org.javatuples.Pair<org.javatuples.Pair<List<Program>, Boolean>, List<ZoneProperties>>
) : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        val programsNetwork = pair.value0.value0
        val use24HourFormat = pair.value0.value1
        val zoneProperties = pair.value1

        val programs = splitPrograms3(programsNetwork, zoneProperties)
        val visibleZones = visibledZonesForNewProgram3(zoneProperties)

        return Pair(
                oldState.copy(isProgress = false, isContent = true,
                        enabledPrograms = programs, disabledPrograms = emptyList(),
                        visibleZonesForNewProgram = visibleZones,
                        use24HourFormat = use24HourFormat), None)
    }
}

private fun splitPrograms3(programs: List<Program>,
                           zoneProperties: List<ZoneProperties>
): List<Program> {
    for (program in programs) {
        var isMasterValve: Boolean
        var isEnabled: Boolean
        val it = program.wateringTimes.iterator()
        while (it.hasNext()) {
            val wt = it.next()
            isMasterValve = false
            isEnabled = false
            for (zone in zoneProperties) {
                if (wt.id == zone.id) {
                    isMasterValve = zone.masterValve
                    isEnabled = zone.enabled
                    break
                }
            }
            if (isMasterValve || !isEnabled) {
                it.remove()
            }
        }
    }
    return programs
}

private fun visibledZonesForNewProgram3(
        zoneProperties: List<ZoneProperties>
): ArrayList<ProgramWateringTimes> {
    val visibleZones = ArrayList<ProgramWateringTimes>()
    for (zone in zoneProperties) {
        if (zone.masterValve || !zone.enabled) {
            continue
        }
        val lwt = ProgramWateringTimes()
        lwt.id = zone.id
        lwt.name = zone.name
        lwt.duration = 0
        lwt.active = false // new program has all zones inactive
        lwt.hasDefaultAdvancedSettings = zone.hasDefaultAdvancedSettings()
        visibleZones.add(lwt)
    }
    return visibleZones
}

data class ClickDeleteProgramMsg(private val program: Program) : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        return Pair(oldState.copy(isProgress = true), DeleteProgramCmd(program))
    }
}

data class ClickCopyProgramMsg(private val program: Program) : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        return Pair(oldState.copy(isProgress = true),
                CopyProgramCmd(program, oldState.use24HourFormat))
    }
}

data class ClickStartStopProgramMsg(private val program: Program) : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        return Pair(oldState.copy(isProgress = true), StartStopProgramCmd(program))
    }
}

data class ClickEditMoreMsg(private val program: Program) : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        return Pair(oldState.copy(showMoreDialog = true, dialogMoreProgram = program), None)
    }
}

object ShowingEditMoreMsg : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        return Pair(oldState.copy(showMoreDialog = false, dialogMoreProgram = null), None)
    }
}

data class ClickActivateDeactivateMsg(
        private val program: Program,
        private val use24HourFormat: Boolean
) : ProgramsMsg() {

    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        val updatedProgram = program.cloneIt()
        updatedProgram.enabled = !program.enabled
        return Pair(oldState.copy(isProgress = true),
                UpdateProgramCmd(updatedProgram, program, use24HourFormat))
    }
}

data class HandPreferenceChangeMsg(private val handPreference: HandPreference) : ProgramsMsg() {
    override fun reduce(oldState: ProgramsState): Pair<ProgramsState, Cmd> {
        return Pair(oldState.copy(handPreference = handPreference), None)
    }
}
