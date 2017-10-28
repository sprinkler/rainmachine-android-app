package com.rainmachine.presentation.screens.waternow

import com.rainmachine.presentation.screens.programdetailsold.ZoneDurationDialogFragment
import com.rainmachine.presentation.util.ElmPresenter

interface WaterNowContract {

    interface View {
        fun render(state: WaterNowState)
    }

    interface Container {
        fun render(state: WaterNowState)

        fun goToZoneScreen(zoneId: Long)
    }

    interface Presenter : com.rainmachine.presentation.util.Presenter<WaterNowContract.View>,
            ElmPresenter, ZoneDurationDialogFragment.Callback {
        fun start()

        fun stop()

        fun onClickEdit(item: ZoneViewModel)

        fun onClickStart(item: ZoneViewModel)

        fun onClickStop(item: ZoneViewModel)

        fun onClickMinus(item: ZoneViewModel)

        fun onClickPlus(item: ZoneViewModel)

        fun onClickStopAll()

        fun onConfirmStopAll()

        fun onShowingStartZoneDialog()

        fun onShowingStopAllDialog()
    }
}
