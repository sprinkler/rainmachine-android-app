package com.rainmachine.presentation.screens.help;


import java.util.List;

interface HelpContract {

    interface View {
        void setup(List<AdapterItemType> items);

        void startWebScreen(String url);
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View> {
        void onClick(AdapterItemType item);
    }
}
