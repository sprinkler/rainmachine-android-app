package com.rainmachine.presentation.screens.wizardpassword;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WizardPasswordActivity extends SprinklerActivity {

    @Inject
    WizardPasswordPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, WizardPasswordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_wizard_password);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.all_rainmachine_authentication);
        getSupportActionBar().setSubtitle(R.string.all_wizard);
    }

    public Object getModule() {
        return new WizardPasswordModule(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }
}
