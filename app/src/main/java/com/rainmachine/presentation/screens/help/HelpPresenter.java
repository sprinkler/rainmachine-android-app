package com.rainmachine.presentation.screens.help;

import com.rainmachine.presentation.util.BasePresenter;

import java.util.ArrayList;
import java.util.List;

import static com.rainmachine.presentation.screens.help.AdapterItemType.CONTACT;
import static com.rainmachine.presentation.screens.help.AdapterItemType.FORUMS;
import static com.rainmachine.presentation.screens.help.AdapterItemType.ONLINE_MANUALS;
import static com.rainmachine.presentation.screens.help.AdapterItemType.SOFTWARE_UPDATES;
import static com.rainmachine.presentation.screens.help.AdapterItemType.USING_THE_APP;


class HelpPresenter extends BasePresenter<HelpContract.View> implements HelpContract.Presenter {

    private HelpContract.View view;

    HelpPresenter(HelpContract.View view) {
        this.view = view;
    }

    @Override
    public void init() {
        List<AdapterItemType> items = new ArrayList<>();
        items.add(ONLINE_MANUALS);
        items.add(USING_THE_APP);
        items.add(SOFTWARE_UPDATES);
        items.add(FORUMS);
        items.add(CONTACT);
        view.setup(items);
    }

    @Override
    public void onClick(AdapterItemType item) {
        view.startWebScreen(item.getUrl());
    }
}
