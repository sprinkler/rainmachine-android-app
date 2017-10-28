package com.rainmachine.presentation.screens.programs

import com.rainmachine.domain.model.HandPreference
import com.rainmachine.domain.model.Program
import com.rainmachine.domain.model.ProgramWateringTimes
import com.rainmachine.presentation.util.ElmState
import org.joda.time.LocalDateTime

data class ProgramsState(
        @JvmField val initialize: Boolean = false,
        @JvmField val isProgress: Boolean = false,
        @JvmField val isContent: Boolean = false,
        @JvmField val isError: Boolean = false,
        @JvmField val enabledPrograms: List<Program>,
        @JvmField val disabledPrograms: List<Program>,
        @JvmField val visibleZonesForNewProgram: ArrayList<ProgramWateringTimes>,
        @JvmField val sprinklerLocalDateTime: LocalDateTime? = null,
        @JvmField val use24HourFormat: Boolean = false,
        @JvmField val isUnitsMetric: Boolean = false,
        @JvmField val handPreference: HandPreference? = null,
        @JvmField val useNewApi: Boolean,
        @JvmField val showMoreDialog: Boolean = false,
        @JvmField val dialogMoreProgram: Program? = null) : ElmState() {

    companion object {

        @JvmStatic
        fun initialize(useNewApi: Boolean): ProgramsState =
                ProgramsState(initialize = true, enabledPrograms = emptyList(),
                        disabledPrograms = emptyList(), visibleZonesForNewProgram = ArrayList(),
                        useNewApi = useNewApi)
    }
}
