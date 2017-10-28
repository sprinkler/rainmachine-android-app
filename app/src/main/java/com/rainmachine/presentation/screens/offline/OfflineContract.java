package com.rainmachine.presentation.screens.offline;

interface OfflineContract {

    interface View {

        OfflineExtra getExtra();

        void setTitle(String title);

        void closeScreen();
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View> {

        void onClickForget();
    }
}
