package com.rainmachine.presentation.screens.pushnotifications;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.remoteaccess.RemoteAccessActivity;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.rainmachine.infrastructure.util.BaseApplication.getContext;

public class PushNotificationsActivity extends SprinklerActivity implements
        PushNotificationsContract.View {

    private static final int FLIPPER_CHILD_NOTIFICATIONS = 0;
    private static final int FLIPPER_CHILD_REMOTE_ACCESS_NEEDED = 1;
    private static final int FLIPPER_CHILD_PROGRESS = 2;
    private static final int FLIPPER_CHILD_ERROR = 3;

    @Inject
    PushNotificationsContract.Presenter presenter;

    @BindView(R.id.flipper)
    ViewFlipper flipper;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.tv_error)
    TextView tvError;

    private PushNotificationsAdapter adapter;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, PushNotificationsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_push_notifications);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        setup();
        presenter.attachView(this);
        presenter.init();
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.destroy();
        }
        super.onDestroy();
    }

    @Override
    public Object getModule() {
        return new PushNotificationsModule();
    }

    @Override
    public void showContent() {
        flipper.setDisplayedChild(FLIPPER_CHILD_NOTIFICATIONS);
    }

    @Override
    public void showRemoteAccessNeeded() {
        flipper.setDisplayedChild(FLIPPER_CHILD_REMOTE_ACCESS_NEEDED);
    }

    @Override
    public void showProgress() {
        flipper.setDisplayedChild(FLIPPER_CHILD_PROGRESS);
    }

    @Override
    public void showError() {
        flipper.setDisplayedChild(FLIPPER_CHILD_ERROR);
    }

    @Override
    public void updateContent(List<SectionViewModel> pushNotifications) {
        adapter.setItems(pushNotifications);
    }

    @Override
    public void goToRemoteAccessScreen() {
        finish();
        startActivity(RemoteAccessActivity.getStartIntent(this, false));
    }

    @OnClick(R.id.btn_retry)
    void onClickRetry() {
        presenter.onClickRetry();
    }

    @OnClick(R.id.btn_set_up_remote_access)
    void onClickSetUpRemoteAccess() {
        presenter.onClickSetUpRemoteAccess();
    }

    private void setup() {
        recyclerView.addItemDecoration(new DividerItemDecoration(this, null, true));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                .VERTICAL, false));
        adapter = new PushNotificationsAdapter(this, new ArrayList<>(), presenter);
        recyclerView.setAdapter(adapter);

        tvError.setText(R.string.push_notifications_error);
    }
}
