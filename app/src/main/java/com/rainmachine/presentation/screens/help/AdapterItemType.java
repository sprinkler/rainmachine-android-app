package com.rainmachine.presentation.screens.help;

import android.support.annotation.StringRes;

import com.rainmachine.R;

enum AdapterItemType {
    ONLINE_MANUALS(R.string.help_online_manuals, "https://rainmachine.zendesk" +
            ".com/hc/en-us/sections/206078667-Online-Manuals"),
    USING_THE_APP(R.string.help_using_the_app, "https://rainmachine.zendesk" +
            ".com/hc/en-us/sections/206180887-Using-the-App"),
    SOFTWARE_UPDATES(R.string.help_software_updates, "https://rainmachine.zendesk" +
            ".com/hc/en-us/sections/206065688-Software-Updates"),
    FORUMS(R.string.help_forums, "https://rainmachine.zendesk" +
            ".com/hc/communities/public/topics?locale=en-us"),
    CONTACT(R.string.help_contact, "http://rainmachine.com/help.php");

    @StringRes
    private int title;
    private String url;

    AdapterItemType(@StringRes int title, String url) {
        this.title = title;
        this.url = url;
    }

    public int getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
