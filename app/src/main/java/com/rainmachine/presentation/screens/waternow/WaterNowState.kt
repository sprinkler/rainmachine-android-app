package com.rainmachine.presentation.screens.waternow

import com.rainmachine.domain.model.HandPreference
import com.rainmachine.presentation.util.ElmState

data class WaterNowState @JvmOverloads constructor(
        @JvmField val initialize: Boolean = false,
        @JvmField val showMinusPlus: Boolean = false,
        @JvmField val showMinutesSeconds: Boolean = false,
        @JvmField val isProgress: Boolean = false,
        @JvmField val isContent: Boolean = false,
        @JvmField val enabledZones: List<ZoneViewModel>,
        @JvmField val disabledZones: List<ZoneViewModel>,
        @JvmField val handPreference: HandPreference = HandPreference.LEFT_HAND,
        @JvmField val showStartZoneDialog: Boolean = false,
        @JvmField val dialogStartZone: ZoneViewModel? = null,
        @JvmField val showStopAllDialog: Boolean = false,
        @JvmField val hasCompleteData: Boolean = false) : ElmState() {

    companion object {

        @JvmStatic
        fun initialize(showMinusPlus: Boolean, showMinutesSeconds: Boolean): WaterNowState =
                WaterNowState(initialize = true, showMinusPlus = showMinusPlus,
                        showMinutesSeconds = showMinutesSeconds,
                        enabledZones = emptyList(), disabledZones = emptyList())
    }

    fun totalMachineDurationDirtyMap(): Map<Long, Boolean> {
        val map = HashMap<Long, Boolean>()
        (enabledZones + disabledZones).forEach {
            map[it.id] = it.totalMachineDurationDirtyCount > 0
        }
        return map
    }

    fun zoneStateDirtyMap(): Map<Long, Boolean> {
        val map = HashMap<Long, Boolean>()
        (enabledZones + disabledZones).forEach {
            map[it.id] = it.stateDirtyCount > 0
        }
        return map
    }
}