package com.rainmachine.presentation.screens.offline;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.NonSprinklerActivity;

import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OfflineActivity extends NonSprinklerActivity implements OfflineContract.View {

    private static final String EXTRA = "extra";

    @Inject
    OfflineContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context, long _deviceDatabaseId, String
            deviceName) {
        Intent intent = new Intent(context, OfflineActivity.class);
        OfflineExtra extra = new OfflineExtra(_deviceDatabaseId, deviceName);
        intent.putExtra(EXTRA, Parcels.wrap(extra));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        buildGraphAndInject();
        presenter.attachView(this);
        presenter.init();
    }

    @Override
    public Object getModule() {
        return new OfflineModule();
    }

    @Override
    public OfflineExtra getExtra() {
        return getParcelable(EXTRA);
    }

    @Override
    public void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void closeScreen() {
        finish();
    }

    @OnClick(R.id.btn_forget)
    void onClickForget() {
        presenter.onClickForget();
    }
}
