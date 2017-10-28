package com.rainmachine.presentation.screens.waternow

import com.rainmachine.data.local.database.model.ZoneSettings
import com.rainmachine.domain.model.Program

data class ZoneViewModel(
        @JvmField val id: Long,
        @JvmField val name: String,
        @JvmField val isEnabled: Boolean,
        @JvmField val isMasterValve: Boolean,
        @JvmField val defaultManualStartDuration: Int,
        @JvmField val nextProgramToRun: Program?, // the next program to run this zone in the future
        @JvmField val zoneSettings: ZoneSettings?,
        @JvmField val state: State = State.IDLE,
        @JvmField val stateDirtyCount: Int = 0,
        @JvmField val stateLastTimeDirty: Long = 0,
        @JvmField val runningCounter: Int = 0,
        @JvmField var totalMachineDuration: Int = 0,
        @JvmField var totalMachineDurationDirtyCount: Int = 0,
        @JvmField val totalMachineDurationlastTimeDirty: Long = 0) {

    enum class State {IDLE, PENDING, RUNNING }
}
