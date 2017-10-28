package com.rainmachine.presentation.screens.about;


import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;

interface AboutContract {

    interface View {
        void setup(boolean showDiagnostics);

        void updateContent(AboutViewModel viewModel);

        void updateContent3(AboutViewModel viewModel);

        void showContent();

        void showProgress(String progressText);

        void showError();
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            ActionMessageDialogFragment.Callback {
        void onClickUpdate();

        void onClickRetry();

        void onClickSendDiagnostics();

        void onConsecutiveClicksSupport();
    }

}
