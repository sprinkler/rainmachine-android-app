package com.rainmachine.presentation.screens.directaccess;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.NonSprinklerActivity;

import butterknife.ButterKnife;

public class DirectAccessActivity extends NonSprinklerActivity {

    public static Intent getStartIntent(Context context) {
        return new Intent(context, DirectAccessActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGraphAndInject();
        setContentView(R.layout.activity_direct_access);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        linkToolbar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public Object getModule() {
        return new DirectAccessModule(this);
    }
}
