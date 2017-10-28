package com.rainmachine.presentation.screens.cloudaccounts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.DrawerActivity;

import butterknife.ButterKnife;

public class CloudAccountsActivity extends DrawerActivity {

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, CloudAccountsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGraphAndInject();
        setContentView(R.layout.activity_cloud_accounts);
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        linkToolbar(toolbar);

        drawerHelper.setupDrawer(toolbar);
    }

    public Object getModule() {
        return new CloudAccountsModule(this);
    }
}
