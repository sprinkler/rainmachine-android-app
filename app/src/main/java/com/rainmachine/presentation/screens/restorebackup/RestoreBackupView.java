package com.rainmachine.presentation.screens.restorebackup;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RestoreBackupView extends ViewFlipper {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    RestoreBackupPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private RestoreBackupAdapter adapter;

    public RestoreBackupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        if (!isInEditMode()) {
            presenter.attachView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            presenter.init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            presenter.destroy();
        }
    }

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.onRetry();
    }

    void setup() {
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), null, false));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                .VERTICAL, false));
        adapter = new RestoreBackupAdapter(getContext(), new ArrayList<>(), presenter);
        recyclerView.setAdapter(adapter);
    }

    public void render(RestoreBackupViewModel viewModel) {
        adapter.setItems(viewModel.backupDevices);
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }
}
