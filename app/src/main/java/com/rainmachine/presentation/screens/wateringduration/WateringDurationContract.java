package com.rainmachine.presentation.screens.wateringduration;


interface WateringDurationContract {

    interface View extends WateringDurationDialogFragment.Callback {

        void render(WateringDurationViewModel viewModel);
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View> {

        void onClickRetry();

        void onClickZone(ZoneViewModel zone);

        void onClickSaveWateringDuration(ZoneViewModel zone);
    }
}
